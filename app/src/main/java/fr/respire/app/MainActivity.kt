package fr.respire.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {

    private data class AppEntry(val pkg: String, val label: String, val icon: Drawable)

    private lateinit var prefs: Prefs
    private val apps = mutableListOf<AppEntry>()
    private val adapter = AppAdapter()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var tvStatus: TextView
    private lateinit var btnAccess: Button
    private lateinit var tvStats: TextView

    // Valeurs en cours d'édition (appliquées au clic sur « Enregistrer »).
    private var editBase = 0
    private var editStep = 0
    private var editSession = 0
    private var editQuota = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        val list = ListView(this).apply {
            fitsSystemWindows = true
            clipToPadding = false
        }
        val header = layoutInflater.inflate(R.layout.main_header, list, false)
        list.addHeaderView(header, null, false)
        list.adapter = adapter
        list.setOnItemClickListener { _, _, position, _ ->
            apps.getOrNull(position - list.headerViewsCount)?.let { toggle(it) }
        }
        setContentView(list)

        tvStatus = header.findViewById(R.id.tvStatus)
        btnAccess = header.findViewById(R.id.btnAccess)
        tvStats = header.findViewById(R.id.tvStats)
        btnAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        editBase = prefs.baseDelay
        editStep = prefs.stepDelay
        editSession = prefs.sessionMinutes
        editQuota = prefs.dailyQuota

        bindSeek(header, R.id.sbBase, R.id.tvBase, 3, 60, editBase) {
            editBase = it; getString(R.string.set_base, it)
        }
        bindSeek(header, R.id.sbStep, R.id.tvStep, 0, 15, editStep) {
            editStep = it; getString(R.string.set_step, it)
        }
        bindSeek(header, R.id.sbSession, R.id.tvSession, 1, 30, editSession) {
            editSession = it; getString(R.string.set_session, it)
        }
        bindSeek(header, R.id.sbQuota, R.id.tvQuota, 0, 20, editQuota) {
            editQuota = it
            if (it == 0) getString(R.string.set_quota_unlimited) else getString(R.string.set_quota, it)
        }
        header.findViewById<Button>(R.id.btnSave).setOnClickListener { saveSettings() }

        loadApps()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    // ---- État ----------------------------------------------------------------

    private fun serviceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val me = ComponentName(this, PauseService::class.java)
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    private fun refreshStatus() {
        val on = serviceEnabled()
        tvStatus.text = getString(if (on) R.string.status_on else R.string.status_off)
        btnAccess.visibility = if (on) View.GONE else View.VISIBLE
        tvStats.text = getString(
            R.string.stats_today, prefs.attemptsTodayAll(), prefs.giveUpsToday()
        )
    }

    // ---- Réglages ------------------------------------------------------------

    private fun bindSeek(
        root: View, barId: Int, labelId: Int, min: Int, max: Int, initial: Int,
        onChange: (Int) -> String,
    ) {
        val bar = root.findViewById<SeekBar>(barId)
        val label = root.findViewById<TextView>(labelId)
        bar.max = max
        bar.min = min
        bar.progress = initial
        label.text = onChange(initial)
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, value: Int, fromUser: Boolean) {
                label.text = onChange(value)
            }
            override fun onStartTrackingTouch(s: SeekBar) = Unit
            override fun onStopTrackingTouch(s: SeekBar) = Unit
        })
    }

    private fun saveSettings() {
        val oldQuota = prefs.dailyQuota
        val quotaLooser = (oldQuota > 0 && (editQuota == 0 || editQuota > oldQuota))
        val looser = editBase < prefs.baseDelay ||
            editStep < prefs.stepDelay ||
            editSession > prefs.sessionMinutes ||
            quotaLooser

        val applyNow = {
            prefs.baseDelay = editBase
            prefs.stepDelay = editStep
            prefs.sessionMinutes = editSession
            prefs.dailyQuota = editQuota
            toast(getString(R.string.saved))
        }
        if (looser) {
            confirmAfterDelay(getString(R.string.loosen_settings), applyNow)
        } else {
            applyNow()
        }
    }

    /**
     * Assouplir une règle coûte une attente : c'est dans ces moments-là
     * qu'on rechute (les utilisateurs « mettent en pause » l'outil puis replongent).
     */
    private fun confirmAfterDelay(message: String, onConfirm: () -> Unit) {
        var left = LOOSEN_DELAY_S
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.loosen_title)
            .setMessage(getString(R.string.loosen_wait, message, left))
            .setPositiveButton(R.string.loosen_confirm) { _, _ -> onConfirm() }
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            val ok = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ok.isEnabled = false
            val countdown = object : Runnable {
                override fun run() {
                    if (!dialog.isShowing) return
                    left--
                    if (left <= 0) {
                        dialog.setMessage(message)
                        ok.isEnabled = true
                    } else {
                        dialog.setMessage(getString(R.string.loosen_wait, message, left))
                        handler.postDelayed(this, 1000L)
                    }
                }
            }
            handler.postDelayed(countdown, 1000L)
        }
        dialog.show()
    }

    private fun toast(text: String) =
        android.widget.Toast.makeText(this, text, android.widget.Toast.LENGTH_SHORT).show()

    // ---- Liste des applis ----------------------------------------------------

    private fun toggle(entry: AppEntry) {
        val current = prefs.targets
        if (entry.pkg in current) {
            confirmAfterDelay(getString(R.string.loosen_app, entry.label)) {
                prefs.targets = prefs.targets - entry.pkg
                adapter.notifyDataSetChanged()
            }
        } else {
            prefs.targets = current + entry.pkg
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadApps() {
        Thread {
            val pm = packageManager
            val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            @Suppress("DEPRECATION")
            val infos = pm.queryIntentActivities(launcher, 0)
            val selected = prefs.targets
            val result = infos
                .distinctBy { it.activityInfo.packageName }
                .filter { it.activityInfo.packageName != packageName }
                .map {
                    AppEntry(
                        it.activityInfo.packageName,
                        it.loadLabel(pm).toString(),
                        it.loadIcon(pm),
                    )
                }
                .sortedWith(compareBy<AppEntry>({ it.pkg !in selected }, { it.label.lowercase() }))
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                apps.clear()
                apps.addAll(result)
                adapter.notifyDataSetChanged()
            }
        }.start()
    }

    private inner class AppAdapter : BaseAdapter() {
        override fun getCount() = apps.size
        override fun getItem(position: Int): Any = apps[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_app, parent, false)
            val entry = apps[position]
            view.findViewById<ImageView>(R.id.icon).setImageDrawable(entry.icon)
            view.findViewById<TextView>(R.id.label).text = entry.label
            view.findViewById<CheckBox>(R.id.check).isChecked = entry.pkg in prefs.targets
            return view
        }
    }

    companion object {
        private const val LOOSEN_DELAY_S = 30
    }
}
