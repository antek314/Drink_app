package com.example.drink

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class WaterWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {}

    companion object {
        const val ACTION_ADD_WATER = "com.example.drink.ACTION_ADD_WATER"
        const val EXTRA_AMOUNT = "amount"

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            runBlocking {
                val waterIntake = WaterDataStore.loadIntake(context)
                val dailyGoal = Szklanki.getPojemnosc(context).firstOrNull() ?: 2500
                val progress = (waterIntake.toFloat() / dailyGoal).coerceIn(0f, 1f)

                val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                    setTextViewText(R.id.water_text, "$waterIntake ml / $dailyGoal ml")
                    setProgressBar(R.id.progress_bar, 100, (progress * 100).toInt(), false)

                    val increment = Szklanki.getPojemnosc1(context).firstOrNull() ?: 250
                    setTextViewText(R.id.add_button, "+${increment}ml")

                    val intent = Intent(context, WaterWidget::class.java).apply {
                        action = ACTION_ADD_WATER
                        putExtra(EXTRA_AMOUNT, increment)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.add_button, pendingIntent)
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_ADD_WATER -> {
                val amount = intent.getIntExtra(EXTRA_AMOUNT, 250)
                runBlocking {
                    // Zwiększ ilość wody i zapisz
                    val current = WaterDataStore.loadIntake(context)
                    val newAmount = current + amount
                    WaterDataStore.saveIntake(context, newAmount)
                    HistoriaRepository.zapisz(context, amount)

                    // Wymuś aktualizację UI w aplikacji
                    val updateIntent = Intent("WATER_INTAKE_UPDATED").apply {
                        putExtra("new_amount", newAmount)
                        `package` = context.packageName
                    }
                    context.sendBroadcast(updateIntent)

                    // Aktualizuj widget
                    updateAllWidgets(context)
                }
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                // Aktualizacja z aplikacji
                runBlocking {
                    updateAllWidgets(context)
                }
            }
        }
    }

    private fun updateAllWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, WaterWidget::class.java))
        manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_container)
        this.onUpdate(context, manager, ids)
    }
}