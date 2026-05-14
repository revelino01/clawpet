package com.clawpet.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/** Starts/stops widget animation based on screen on/off state. */
class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                WidgetUpdateScheduler.start(context)
            }
            Intent.ACTION_SCREEN_OFF -> {
                WidgetUpdateScheduler.stop(context)
            }
        }
    }

    companion object {
        fun register(context: Context) {
            val receiver = ScreenStateReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            context.registerReceiver(receiver, filter)
        }
    }
}

object WidgetUpdateScheduler {
    private const val INTERVAL_MS = 1000L
    private const val REQUEST_CODE = 42

    fun start(context: Context) {
        scheduleNext(context)
    }

    fun stop(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val pi = pendingIntent(context)
        am.cancel(pi)
    }

    fun scheduleNext(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val pi = pendingIntent(context)
        val trigger = android.os.SystemClock.elapsedRealtime() + INTERVAL_MS
        am.setWindow(android.app.AlarmManager.ELAPSED_REALTIME, trigger, 200L, pi)
    }

    private fun pendingIntent(context: Context): android.app.PendingIntent {
        val intent = Intent(context, PetWidgetProvider::class.java).setAction(PetWidgetProvider.ACTION_UPDATE_FRAME)
        return android.app.PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
    }
}