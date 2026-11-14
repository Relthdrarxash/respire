package fr.respire.shared

import platform.Foundation.NSDateComponents
import platform.Foundation.NSUserDefaults
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

/**
 * Rappel quotidien local de cohérence cardiaque sur iOS via UNUserNotificationCenter (aucun accès réseau).
 */
object IosBreathingReminder {
    private const val PREF_ENABLED = "respire_daily_reminder_enabled"
    private const val IDENTIFIER = "fr.respire.daily_breathing"

    fun isEnabled(): Boolean = NSUserDefaults.standardUserDefaults.boolForKey(PREF_ENABLED)

    fun setEnabled(enabled: Boolean, onResult: (Boolean) -> Unit) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        if (!enabled) {
            center.removePendingNotificationRequestsWithIdentifiers(listOf(IDENTIFIER))
            NSUserDefaults.standardUserDefaults.setBool(false, forKey = PREF_ENABLED)
            onResult(false)
            return
        }

        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
        ) { granted, _ ->
            if (granted) {
                scheduleDaily(center)
                NSUserDefaults.standardUserDefaults.setBool(true, forKey = PREF_ENABLED)
                onResult(true)
            } else {
                NSUserDefaults.standardUserDefaults.setBool(false, forKey = PREF_ENABLED)
                onResult(false)
            }
        }
    }

    private fun scheduleDaily(center: UNUserNotificationCenter) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(IDENTIFIER))
        val content = UNMutableNotificationContent().apply {
            setTitle("Pause Respire")
            setBody("Trois minutes de cohérence cardiaque (4 s / 6 s) pour relâcher la pression.")
            setSound(UNNotificationSound.defaultSound())
        }
        val components = NSDateComponents().apply {
            hour = 18
            minute = 0
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components,
            repeats = true,
        )
        val request = UNNotificationRequest.requestWithIdentifier(IDENTIFIER, content, trigger)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }
}
