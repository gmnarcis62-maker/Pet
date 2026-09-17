package red.line.pet.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import red.line.pet.core.billing.FreeVipManager
import red.line.pet.core.billing.VipFeature
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.domain.repository.AppThemeMode
import red.line.pet.domain.repository.UserPreferencesRepository
import red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val currentJalaliDateStr: String = "",
    val appVersion: String = "1.0.0 (تجاری)",
    val isVip: Boolean = false,
    val showVipDialog: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val freeVipManager: FreeVipManager? = null,
    private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            currentJalaliDateStr = JalaliDateHelper.now().toFullString()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.getThemeMode().collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            freeVipManager?.isVip?.collect { vip ->
                _uiState.update { it.copy(isVip = vip) }
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun onBackupRestoreClick() {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.BACKUP_RESTORE)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(showVipDialog = true) }
                    return@launch
                }
            }
            _uiState.update { it.copy(message = "پشتیبان‌گیری ابری و بازیابی داده‌ها فقط برای اعضای ویژه فعال است.") }
        }
    }

    fun onDismissVipDialog() {
        _uiState.update { it.copy(showVipDialog = false) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    class Factory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val freeVipManager: FreeVipManager? = null,
        private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(userPreferencesRepository, freeVipManager, checkFeatureAccessUseCase) as T
        }
    }
}
