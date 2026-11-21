package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore(name = "karizma_preferences")

class LanguagePreferences(private val context: Context) {
    private val keyLanguage = stringPreferencesKey("app_language")

    val languageFlow: Flow<AppLanguage> = context.languageDataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[keyLanguage])
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.languageDataStore.edit { prefs ->
            prefs[keyLanguage] = language.code
        }
    }
}
