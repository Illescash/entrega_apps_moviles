package com.partyhub

import android.app.Application
import timber.log.Timber

class PartyHubApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
