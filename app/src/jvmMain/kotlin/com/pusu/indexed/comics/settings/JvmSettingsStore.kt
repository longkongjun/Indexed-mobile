package com.pusu.indexed.comics.settings

import com.pusu.indexed.core.locale.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.prefs.Preferences

class JvmSettingsStore : SettingsStore {
    private val prefs = Preferences.userRoot().node("com.pusu.indexed.comics.settings")
    private val state = MutableStateFlow(loadSettings())

    override val settings: Flow<AppSettings> = state.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        prefs.put(KEY_LANGUAGE, language.name)
        state.update { it.copy(language = language) }
    }

    override suspend fun setThemePresetId(presetId: String) {
        prefs.put(KEY_THEME, presetId)
        state.update { it.copy(themePresetId = presetId) }
    }

    private fun loadSettings(): AppSettings {
        val languageValue = prefs.get(KEY_LANGUAGE, AppLanguage.Chinese.name)
        val themeValue = prefs.get(KEY_THEME, AppSettings().themePresetId)
        return AppSettings(
            language = AppLanguage.valueOf(languageValue),
            themePresetId = themeValue
        )
    }

    private companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_THEME = "theme_preset"
    }
}
