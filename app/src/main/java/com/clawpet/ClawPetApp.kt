package com.clawpet

import android.app.Application
import com.clawpet.widget.ScreenStateReceiver
import com.clawpet.widget.WidgetUpdateScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClawPetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Register screen state receiver to pause/resume widget animation
        ScreenStateReceiver.register(this)
        // Start widget animation loop
        WidgetUpdateScheduler.start(this)
    }
}