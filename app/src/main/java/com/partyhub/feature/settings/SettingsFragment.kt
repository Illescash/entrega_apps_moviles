package com.partyhub.feature.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.partyhub.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Listener para cambios de tema (modo oscuro)
        findPreference<SwitchPreferenceCompat>("dark_mode")?.setOnPreferenceChangeListener { _, newValue ->
            val isDarkMode = newValue as Boolean
            applyNightMode(isDarkMode)
            true
        }
    }

    private fun applyNightMode(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    companion object {
        private const val PREF_PLAYER_ALIAS = "player_alias"
        private const val PREF_DARK_MODE = "dark_mode"

        fun getPlayerAlias(context: android.content.Context): String {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getString(PREF_PLAYER_ALIAS, "Jugador Anónimo") ?: "Jugador Anónimo"
        }

        fun isDarkModeEnabled(context: android.content.Context): Boolean {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean(PREF_DARK_MODE, false)
        }
    }
}


