package com.partyhub

import android.app.Application
import com.partyhub.database.PartyHubDatabase
import timber.log.Timber

class PartyHubApp : Application() {

    /**
     * Instancia de la base de datos de Room accesible desde toda la app.
     * Se usa lazy para que solo se inicialice cuando se necesite realmente.
     */
    val database: PartyHubDatabase by lazy { PartyHubDatabase.getInstance(this) }

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
