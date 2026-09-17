package red.line.pet.domain.repository

import kotlinx.coroutines.flow.Flow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

interface UserPreferencesRepository {
    fun getThemeMode(): Flow<AppThemeMode>
    suspend fun setThemeMode(mode: AppThemeMode)
    fun getActivePetId(): Flow<Long?>
    suspend fun setActivePetId(petId: Long?)
}
