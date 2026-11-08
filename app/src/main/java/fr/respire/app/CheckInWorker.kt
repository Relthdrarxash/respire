package fr.respire.app

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Vérification locale toutes les 6 heures : prévient si le service d'accessibilité a été coupé,
 * sinon Respire cesse de marcher sans que personne ne le sache. C'est la seule notification de
 * l'appli, et seulement si la personne a autorisé les notifications. Aucun accès réseau.
 */
class CheckInWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = Prefs(context)
        val now = System.currentTimeMillis()
        val running = isServiceRunning(context)
        if (running) prefs.serviceSeenOn = true

        if (CheckIn.shouldWarnStopped(prefs.targets.isNotEmpty(), prefs.serviceSeenOn, running, now, prefs.lastServiceWarning)) {
            if (notify(context, ID_STOPPED, context.getString(R.string.notif_stopped_title), context.getString(R.string.notif_stopped_text))) {
                prefs.lastServiceWarning = now
            }
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "respire-check-in"
        private const val CHANNEL_ID = "respire"
        private const val ID_STOPPED = 1

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<CheckInWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        /** Le service est réellement lié par le système (et pas seulement coché dans les réglages). */
        fun isServiceRunning(context: Context): Boolean {
            val manager = context.getSystemService(AccessibilityManager::class.java) ?: return false
            val me = ComponentName(context, PauseService::class.java)
            return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any { ComponentName.unflattenFromString(it.id) == me }
        }

        fun canNotify(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        @SuppressLint("MissingPermission") // Vérifiée par canNotify.
        private fun notify(context: Context, id: Int, title: String, text: String): Boolean {
            if (!canNotify(context)) return false
            val manager = context.getSystemService(NotificationManager::class.java) ?: return false
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, context.getString(R.string.notif_channel), NotificationManager.IMPORTANCE_DEFAULT),
            )
            val open = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pending = PendingIntent.getActivity(context, id, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(id, notification)
            return true
        }
    }
}
