package com.pusu.indexed.comics.settings

import com.pusu.indexed.core.locale.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemorySettingsStore(
    initial: AppSettings = AppSettings()
) : SettingsStore {
    private val state = MutableStateFlow(initial)

    override val settings: Flow<AppSettings> = state.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        state.update { it.copy(language = language) }
    }

    override suspend fun setThemePresetId(presetId: String) {
        state.update { it.copy(themePresetId = presetId) }
    }
}
