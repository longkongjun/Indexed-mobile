package com.pusu.indexed.comics.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pusu.indexed.core.locale.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AndroidSettingsStore(
    context: Context
) : SettingsStore {
    private val store = context.dataStore

    override val settings: Flow<AppSettings> = store.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val languageValue = prefs[KEY_LANGUAGE] ?: AppLanguage.Chinese.name
            val themeValue = prefs[KEY_THEME] ?: AppSettings().themePresetId
            AppSettings(
                language = AppLanguage.valueOf(languageValue),
                themePresetId = themeValue
            )
        }

    override suspend fun setLanguage(language: AppLanguage) {
        store.edit { it[KEY_LANGUAGE] = language.name }
    }

    override suspend fun setThemePresetId(presetId: String) {
        store.edit { it[KEY_THEME] = presetId }
    }

    private companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_THEME = stringPreferencesKey("theme_preset")
    }
}
