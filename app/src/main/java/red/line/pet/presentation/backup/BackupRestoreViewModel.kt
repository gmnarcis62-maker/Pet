package red.line.pet.presentation.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import red.line.pet.core.billing.FreeVipManager
import red.line.pet.data.backup.BackupSummary
import red.line.pet.data.backup.BackupValidationResult
import red.line.pet.data.backup.LocalBackupManager
import red.line.pet.data.backup.PetoraBackupData

data class BackupRestoreUiState(
    val isVip: Boolean = false,
    val isLoading: Boolean = false,
    val operationProgressTitle: String = "",
    val backupSuccessSummary: BackupSummary? = null,
    val restoreSuccessSummary: BackupSummary? = null,
    val pendingRestoreData: PetoraBackupData? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class BackupRestoreViewModel(
    private val localBackupManager: LocalBackupManager,
    private val freeVipManager: FreeVipManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            freeVipManager.isVip.collect { vip ->
                _uiState.update { it.copy(isVip = vip) }
            }
        }
    }

    /**
     * Called when the user picks a target file location via SAF CreateDocument
     */
    fun onBackupLocationSelected(targetUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    operationProgressTitle = "در حال ایجاد فایل پشتیبان محلی...",
                    errorMessage = null,
                    backupSuccessSummary = null
                )
            }

            val result = localBackupManager.createBackup(targetUri)
            result.onSuccess { summary ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        backupSuccessSummary = summary,
                        infoMessage = "پشتیبان‌گیری با موفقیت انجام شد و در دستگاه ذخیره گردید."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "خطایی در فرآیند پشتیبان‌گیری رخ داد."
                    )
                }
            }
        }
    }

    /**
     * Called when user picks a backup file via SAF OpenDocument to restore
     */
    fun onRestoreFileSelected(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    operationProgressTitle = "در حال اعتبارسنجی و بررسی صحت فایل پشتیبان...",
                    errorMessage = null,
                    pendingRestoreData = null
                )
            }

            when (val validationResult = localBackupManager.inspectAndValidateBackup(sourceUri)) {
                is BackupValidationResult.Valid -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingRestoreData = validationResult.backupData
                        )
                    }
                }
                is BackupValidationResult.Invalid -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = validationResult.errorMessage
                        )
                    }
                }
            }
        }
    }

    /**
     * User confirmed restore after inspecting backup summary in preview dialog
     */
    fun confirmRestore() {
        val dataToRestore = _uiState.value.pendingRestoreData ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    operationProgressTitle = "در حال بازیابی ایمن اطلاعات در دیتابیس...",
                    errorMessage = null,
                    pendingRestoreData = null
                )
            }

            val result = localBackupManager.restoreBackup(dataToRestore)
            result.onSuccess { summary ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        restoreSuccessSummary = summary,
                        infoMessage = "بازیابی اطلاعات با موفقیت کامل انجام شد."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "بازیابی ناموفق بود و دیتابیس فعلی بدون تغییر حفظ شد: ${error.localizedMessage}"
                    )
                }
            }
        }
    }

    fun dismissRestorePreviewDialog() {
        _uiState.update { it.copy(pendingRestoreData = null) }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                infoMessage = null,
                backupSuccessSummary = null,
                restoreSuccessSummary = null
            )
        }
    }

    class Factory(
        private val localBackupManager: LocalBackupManager,
        private val freeVipManager: FreeVipManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackupRestoreViewModel(localBackupManager, freeVipManager) as T
        }
    }
}
