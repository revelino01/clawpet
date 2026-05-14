package com.clawpet.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.clawpet.domain.PetRepository
import com.clawpet.widget.PetWidgetProvider
import com.clawpet.widget.WidgetUpdateScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.appwidget.AppWidgetManager
import android.content.ComponentName

@HiltWorker
class PetDecayWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repo: PetRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Decay pet stats based on time elapsed
        val state = repo.tickDecay()

        // Trigger widget refresh so new mood/state appears
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val ids = appWidgetManager.getAppWidgetIds(
            ComponentName(applicationContext, PetWidgetProvider::class.java)
        )
        if (ids.isNotEmpty()) {
            WidgetUpdateScheduler.start(applicationContext)
        }

        return Result.success()
    }
}