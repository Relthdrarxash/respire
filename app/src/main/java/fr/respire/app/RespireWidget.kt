package fr.respire.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Widget discret d'écran d'accueil : affiche les renoncements du jour et de la semaine
 * ainsi que le temps estimé épargné, sans série culpabilisante.
 */
class RespireWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, RespireWidget::class.java))
            if (ids.isNotEmpty()) {
                updateWidgets(context, manager, ids)
            }
        }

        private fun updateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val prefs = Prefs(context)
            val todayGiveUps = prefs.giveUpsToday()
            val weekGiveUps = prefs.giveUpsLastWeek().sum()
            val savedWeekMin = weekGiveUps * prefs.sessionMinutes.coerceAtLeast(1)

            val launchIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.respire_widget).apply {
                    setTextViewText(R.id.widget_title, context.getString(R.string.widget_title))
                    setTextViewText(
                        R.id.widget_primary,
                        context.getString(R.string.widget_sub, todayGiveUps, weekGiveUps, savedWeekMin),
                    )
                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                manager.updateAppWidget(id, views)
            }
        }
    }
}
