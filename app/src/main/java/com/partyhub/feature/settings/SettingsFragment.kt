package com.partyhub.feature.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.ListPreference
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

        // Listener para cambio de idioma
        findPreference<ListPreference>("language")?.setOnPreferenceChangeListener { _, newValue ->
            val languageCode = newValue as String
            applyLanguage(languageCode)
            true
        }
    }

    private fun applyLanguage(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
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
            val defaultAlias = context.getString(R.string.game_player_anonymous)
            return prefs.getString(PREF_PLAYER_ALIAS, defaultAlias) ?: defaultAlias
        }

        fun isDarkModeEnabled(context: android.content.Context): Boolean {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean(PREF_DARK_MODE, false)
        }
    }
}


