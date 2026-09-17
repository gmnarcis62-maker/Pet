package red.line.pet.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import red.line.pet.domain.repository.AppThemeMode

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "redline_pet_prefs")

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACTIVE_PET_ID = longPreferencesKey("active_pet_id")
        val IS_VIP = booleanPreferencesKey("is_vip")
        val PURCHASE_TOKEN = stringPreferencesKey("purchase_token")
        val PURCHASE_DATE = stringPreferencesKey("purchase_date")
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        try {
            AppThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    val activePetId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACTIVE_PET_ID]
    }

    val isVip: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_VIP] ?: false
    }

    val purchaseToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PURCHASE_TOKEN]
    }

    val purchaseDate: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PURCHASE_DATE]
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setActivePetId(petId: Long?) {
        context.dataStore.edit { preferences ->
            if (petId != null) {
                preferences[PreferencesKeys.ACTIVE_PET_ID] = petId
            } else {
                preferences.remove(PreferencesKeys.ACTIVE_PET_ID)
            }
        }
    }

    suspend fun setVipStatus(isVip: Boolean, purchaseToken: String? = null, purchaseDate: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_VIP] = isVip
            if (purchaseToken != null) {
                preferences[PreferencesKeys.PURCHASE_TOKEN] = purchaseToken
            } else if (!isVip) {
                preferences.remove(PreferencesKeys.PURCHASE_TOKEN)
            }
            if (purchaseDate != null) {
                preferences[PreferencesKeys.PURCHASE_DATE] = purchaseDate
            } else if (!isVip) {
                preferences.remove(PreferencesKeys.PURCHASE_DATE)
            }
        }
    }
}
