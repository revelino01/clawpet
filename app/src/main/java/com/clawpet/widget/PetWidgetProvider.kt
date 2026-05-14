package com.clawpet.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.clawpet.R
import com.clawpet.domain.PetAction
import com.clawpet.domain.PetRepository
import dagger.hilt.android.EntryPointAccessors

class PetWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE_FRAME = "com.clawpet.widget.UPDATE_FRAME"
        const val ACTION_FEED = "com.clawpet.ACTION_FEED"
        const val ACTION_PET = "com.clawpet.ACTION_PET"
        const val ACTION_PLAY = "com.clawpet.ACTION_PLAY"
        private const val INTERVAL_MS = 1000L // 1 FPS
        private const val REQUEST_CODE = 42
    }

    // Per-widget animators (keyed by widgetId)
    private val animators = HashMap<Int, PetAnimator>()
    private val renderer = PetRenderer()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_FRAME -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, PetWidgetProvider::class.java)
                )
                for (id in ids) {
                    updateFrame(context, appWidgetManager, id)
                }
                scheduleNextFrame(context)
            }
            ACTION_FEED, ACTION_PET, ACTION_PLAY -> {
                val repo = getRepo(context) ?: return
                val action = when (intent.action) {
                    ACTION_FEED -> PetAction.FEED
                    ACTION_PET -> PetAction.PET
                    ACTION_PLAY -> PetAction.PLAY
                    else -> return
                }
                repo.performAction(action)
                // Force immediate frame update
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, PetWidgetProvider::class.java)
                )
                for (id in ids) {
                    updateFrame(context, appWidgetManager, id)
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            // Initialize animator for each widget
            if (!animators.containsKey(id)) {
                val opts = appWidgetManager.getAppWidgetOptions(id)
                val w = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 300).dpToPx(context)
                val h = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 300).dpToPx(context)
                animators[id] = PetAnimator(w.toFloat(), h.toFloat())
            }
            updateFrame(context, appWidgetManager, id)
        }
        scheduleNextFrame(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextFrame(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelSchedule(context)
        animators.clear()
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) {
            animators.remove(id)
        }
    }

    private fun updateFrame(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val repo = getRepo(context)
        val petState = repo?.getPet() ?: com.clawpet.domain.PetState()

        // Get or create animator
        val opts = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val w = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 300).dpToPx(context)
        val h = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 300).dpToPx(context)

        val animator = animators.getOrPut(appWidgetId) {
            PetAnimator(w.toFloat(), h.toFloat())
        }

        // Advance animation
        animator.advance(petState.mood.emoji)

        // Render pet to bitmap
        val bitmap = renderer.render(animator.state, w, h)

        // Build RemoteViews
        val views = RemoteViews(context.packageName, R.layout.clawpet_widget)

        // Set pet image
        views.setImageViewBitmap(R.id.pet_image, bitmap)

        // Set stats
        views.setTextViewText(R.id.stat_hunger, "🍖${petState.hunger}%")
        views.setTextViewText(R.id.stat_happy, "😊${petState.happiness}%")
        views.setTextViewText(R.id.stat_energy, "⚡${petState.energy}%")
        views.setTextViewText(R.id.mood_text, "${petState.mood.emoji} Lv.${petState.level}")

        // Action button intents
        views.setOnClickPendingIntent(R.id.btn_feed, actionPendingIntent(context, ACTION_FEED))
        views.setOnClickPendingIntent(R.id.btn_pet, actionPendingIntent(context, ACTION_PET))
        views.setOnClickPendingIntent(R.id.btn_play, actionPendingIntent(context, ACTION_PLAY))

        // Tap pet image to open app
        val openAppIntent = Intent(context, com.clawpet.ui.MainActivity::class.java)
        val openAppPi = PendingIntent.getActivity(context, 100, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.pet_image, openAppPi)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun scheduleNextFrame(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PetWidgetProvider::class.java).setAction(ACTION_UPDATE_FRAME)
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val trigger = SystemClock.elapsedRealtime() + INTERVAL_MS
        am.setWindow(AlarmManager.ELAPSED_REALTIME, trigger, 200L, pi)
    }

    private fun cancelSchedule(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PetWidgetProvider::class.java).setAction(ACTION_UPDATE_FRAME)
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    private fun actionPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, PetWidgetProvider::class.java).setAction(action)
        val requestCode = when (action) {
            ACTION_FEED -> 1
            ACTION_PET -> 2
            ACTION_PLAY -> 3
            else -> 0
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getRepo(context: Context): PetRepository? {
        val app = context.applicationContext as? com.clawpet.ClawPetApp ?: return null
        val entryPoint = EntryPointAccessors.fromApplication(app, WidgetRepoEntryPoint::class.java)
        return entryPoint.petRepository()
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface WidgetRepoEntryPoint {
    fun petRepository(): PetRepository
}