package fr.respire.app

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import fr.respire.app.ui.AppItem
import fr.respire.app.ui.Calme
import fr.respire.app.ui.HomeScreen
import fr.respire.app.ui.HomeUi
import fr.respire.app.ui.PickerScreen
import fr.respire.app.ui.RespireTheme
import fr.respire.app.ui.Rule
import fr.respire.app.ui.TargetStat
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var prefs: Prefs

    private var home by mutableStateOf<HomeUi?>(null)
    private var allApps by mutableStateOf<List<AppItem>>(emptyList())
    private var selected by mutableStateOf<Set<String>>(emptySet())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        selected = prefs.targets
        loadApps()

        setContent {
            RespireTheme {
                var picking by rememberSaveable { mutableStateOf(false) }
                BackHandler(enabled = picking) { picking = false }

                val ui = home
                when {
                    picking -> PickerScreen(
                        apps = allApps,
                        selected = selected,
                        onToggle = ::setTarget,
                        onBack = { picking = false },
                    )
                    ui != null -> HomeScreen(
                        ui = ui,
                        onOpenAccessibility = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        onPickApps = { picking = true },
                        onSaveRule = ::saveRule,
                    )
                    else -> Box(Modifier.fillMaxSize().background(Calme.Ground))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun serviceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val me = ComponentName(this, PauseService::class.java)
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    private fun refresh() {
        val byPkg = allApps.associateBy { it.pkg }
        val targets = selected
            .map { pkg -> byPkg[pkg] ?: AppItem(pkg, pkg, null) }
            .sortedBy { it.label.lowercase() }
            .map { TargetStat(it, prefs.opensToday(it.pkg)) }

        home = HomeUi(
            serviceOn = serviceEnabled(),
            giveUps = prefs.giveUpsToday(),
            pauses = prefs.attemptsTodayAll(),
            week = prefs.giveUpsLastWeek(),
            weekLabels = weekLabels(),
            targets = targets,
            rules = mapOf(
                Rule.DELAY to prefs.baseDelay,
                Rule.STEP to prefs.stepDelay,
                Rule.SESSION to prefs.sessionMinutes,
                Rule.QUOTA to prefs.dailyQuota,
            ),
        )
    }

    private fun weekLabels(): List<String> {
        val letters = arrayOf("D", "L", "M", "M", "J", "V", "S") // index = DAY_OF_WEEK - 1
        val now = System.currentTimeMillis()
        return (6 downTo 0).map { back ->
            val c = Calendar.getInstance().apply { timeInMillis = now - back * Prefs.DAY_MS }
            letters[c.get(Calendar.DAY_OF_WEEK) - 1]
        }
    }

    private fun setTarget(pkg: String, on: Boolean) {
        prefs.targets = if (on) prefs.targets + pkg else prefs.targets - pkg
        selected = prefs.targets
        refresh()
    }

    private fun saveRule(rule: Rule, value: Int) {
        when (rule) {
            Rule.DELAY -> prefs.baseDelay = value
            Rule.STEP -> prefs.stepDelay = value
            Rule.SESSION -> prefs.sessionMinutes = value
            Rule.QUOTA -> prefs.dailyQuota = value
        }
        refresh()
    }

    private fun loadApps() {
        Thread {
            val pm = packageManager
            val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            @Suppress("DEPRECATION")
            val infos = pm.queryIntentActivities(launcher, 0)
            val chosen = prefs.targets
            val size = (40 * resources.displayMetrics.density).toInt().coerceAtLeast(48)
            val result = infos
                .distinctBy { it.activityInfo.packageName }
                .filter { it.activityInfo.packageName != packageName }
                .map {
                    AppItem(
                        pkg = it.activityInfo.packageName,
                        label = it.loadLabel(pm).toString(),
                        icon = it.loadIcon(pm).toBitmap(size, size).asImageBitmap(),
                    )
                }
                .sortedWith(compareBy<AppItem>({ it.pkg !in chosen }, { it.label.lowercase() }))
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                allApps = result
                refresh()
            }
        }.start()
    }
}
