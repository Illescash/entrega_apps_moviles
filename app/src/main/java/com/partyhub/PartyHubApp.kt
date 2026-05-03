package com.partyhub

import android.app.Application
import timber.log.Timber

class PartyHubApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        // Configurar valores por defecto de preferencias
        androidx.preference.PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

        // Aplicar modo oscuro si está activo
        val isDarkMode = com.partyhub.feature.settings.SettingsFragment.isDarkModeEnabled(this)

        val mode = if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
        } else {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
    }

}
