package red.line.pet.presentation.passport

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import red.line.pet.core.billing.VipFeature
import red.line.pet.core.di.AppContainer
import red.line.pet.core.util.AppResult
import red.line.pet.domain.model.MedicalPassportReport
import red.line.pet.domain.model.PassportSection
import red.line.pet.domain.model.PassportSectionsConfig
import red.line.pet.domain.model.Pet
import red.line.pet.domain.usecase.passport.ExportPdfUseCase
import red.line.pet.domain.usecase.passport.GetPetMedicalReportUseCase
import red.line.pet.domain.usecase.passport.SharePdfUseCase
import red.line.pet.domain.usecase.pet.GetPetsUseCase
import red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase
import java.io.File

data class MedicalPassportUiState(
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val reportData: MedicalPassportReport? = null,
    val selectedSections: PassportSectionsConfig = PassportSectionsConfig(),
    val isGenerating: Boolean = false,
    val isLoadingData: Boolean = false,
    val generatedFile: File? = null,
    val showVipDialog: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class MedicalPassportViewModel(
    private val getPetsUseCase: GetPetsUseCase,
    private val getPetMedicalReportUseCase: GetPetMedicalReportUseCase,
    private val exportPdfUseCase: ExportPdfUseCase,
    private val sharePdfUseCase: SharePdfUseCase,
    private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicalPassportUiState())
    val uiState: StateFlow<MedicalPassportUiState> = _uiState.asStateFlow()

    init {
        loadPets()
    }

    fun loadPets() {
        viewModelScope.launch {
            getPetsUseCase().collect { pets ->
                _uiState.update { current ->
                    val selected = current.selectedPet?.let { prev -> pets.find { it.id == prev.id } }
                        ?: pets.firstOrNull()
                    current.copy(pets = pets, selectedPet = selected)
                }
                _uiState.value.selectedPet?.let { pet ->
                    loadReportData(pet.id)
                }
            }
        }
    }

    fun selectPet(pet: Pet) {
        _uiState.update { it.copy(selectedPet = pet, generatedFile = null, error = null, successMessage = null) }
        loadReportData(pet.id)
    }

    private fun loadReportData(petId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingData = true) }
            val report = withContext(Dispatchers.IO) {
                getPetMedicalReportUseCase(petId)
            }
            _uiState.update {
                it.copy(
                    reportData = report,
                    isLoadingData = false
                )
            }
        }
    }

    fun toggleSection(section: PassportSection) {
        _uiState.update {
            it.copy(selectedSections = it.selectedSections.toggleSection(section))
        }
    }

    fun onDismissVipDialog() {
        _uiState.update { it.copy(showVipDialog = false) }
    }

    fun generatePdf(context: Context) {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.PDF_EXPORT)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(showVipDialog = true) }
                    return@launch
                }
            }

            val report = _uiState.value.reportData ?: run {
                _uiState.update { it.copy(error = "اطلاعات حیوان یافت نشد.") }
                return@launch
            }

            if (_uiState.value.selectedSections.selectedCount == 0) {
                _uiState.update { it.copy(error = "حداقل یک بخش را برای خروجی گزارش انتخاب کنید.") }
                return@launch
            }

            _uiState.update { it.copy(isGenerating = true, error = null, successMessage = null) }
            try {
                val file = withContext(Dispatchers.IO) {
                    exportPdfUseCase(
                        context = context.applicationContext,
                        report = report,
                        config = _uiState.value.selectedSections
                    )
                }
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generatedFile = file,
                        successMessage = "شناسنامه سلامت با موفقیت ساخته شد."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = "خطا در تولید فایل PDF: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun viewGeneratedPdf(context: Context) {
        val file = _uiState.value.generatedFile ?: return
        sharePdfUseCase.view(context, file)
    }

    fun shareGeneratedPdf(context: Context) {
        val file = _uiState.value.generatedFile ?: return
        val petName = _uiState.value.selectedPet?.name ?: "حیوان خانگی"
        sharePdfUseCase(context, file, petName)
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

class MedicalPassportViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicalPassportViewModel::class.java)) {
            return MedicalPassportViewModel(
                getPetsUseCase = appContainer.getPetsUseCase,
                getPetMedicalReportUseCase = appContainer.getPetMedicalReportUseCase,
                exportPdfUseCase = appContainer.exportPdfUseCase,
                sharePdfUseCase = appContainer.sharePdfUseCase,
                checkFeatureAccessUseCase = appContainer.checkFeatureAccessUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
