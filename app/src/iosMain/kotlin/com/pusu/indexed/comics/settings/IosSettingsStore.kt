package com.pusu.indexed.comics.settings

import com.pusu.indexed.core.locale.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.Foundation.NSUserDefaults

class IosSettingsStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : SettingsStore {
    private val state = MutableStateFlow(loadSettings())

    override val settings: Flow<AppSettings> = state.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        defaults.setObject(language.name, KEY_LANGUAGE)
        state.update { it.copy(language = language) }
    }

    override suspend fun setThemePresetId(presetId: String) {
        defaults.setObject(presetId, KEY_THEME)
        state.update { it.copy(themePresetId = presetId) }
    }

    private fun loadSettings(): AppSettings {
        val languageValue = defaults.stringForKey(KEY_LANGUAGE) ?: AppLanguage.Chinese.name
        val themeValue = defaults.stringForKey(KEY_THEME) ?: AppSettings().themePresetId
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
