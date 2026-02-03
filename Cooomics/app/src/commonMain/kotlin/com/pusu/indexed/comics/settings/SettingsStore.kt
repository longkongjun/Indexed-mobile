package com.pusu.indexed.comics.settings

import com.pusu.indexed.core.locale.AppLanguage
import kotlinx.coroutines.flow.Flow

interface SettingsStore {
    val settings: Flow<AppSettings>

    suspend fun setLanguage(language: AppLanguage)

    suspend fun setThemePresetId(presetId: String)
}
