package com.pusu.indexed.comics.settings

import com.pusu.indexed.core.locale.AppLanguage

data class AppSettings(
    val language: AppLanguage = AppLanguage.Chinese,
    val themePresetId: String = "ocean"
)
