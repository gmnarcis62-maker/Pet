package red.line.pet.presentation.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import red.line.pet.core.billing.VipFeature
import red.line.pet.core.util.AppResult
import red.line.pet.domain.model.HealthRecord
import red.line.pet.domain.model.HealthRecordType
import red.line.pet.domain.usecase.health.AddHealthRecordUseCase
import red.line.pet.domain.usecase.health.GetHealthRecordsUseCase
import red.line.pet.domain.usecase.health.GetUpcomingRemindersUseCase
import red.line.pet.domain.usecase.pet.GetPetsUseCase
import red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase

import red.line.pet.domain.model.Pet

data class HealthUiState(
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val records: List<HealthRecord> = emptyList(),
    val upcomingReminders: List<HealthRecord> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val showVipDialog: Boolean = false
)

class HealthViewModel(
    private val getHealthRecordsUseCase: GetHealthRecordsUseCase,
    private val getUpcomingRemindersUseCase: GetUpcomingRemindersUseCase,
    private val addHealthRecordUseCase: AddHealthRecordUseCase,
    private val getPetsUseCase: GetPetsUseCase,
    private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState(isLoading = true))
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    private var recordsJob: kotlinx.coroutines.Job? = null
    private var remindersJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getPetsUseCase().collect { petsList ->
                _uiState.update { state ->
                    val currentSelected = state.selectedPet
                    val newSelected = if (currentSelected != null && petsList.any { it.id == currentSelected.id }) {
                        petsList.first { it.id == currentSelected.id }
                    } else {
                        petsList.firstOrNull()
                    }
                    state.copy(pets = petsList, selectedPet = newSelected)
                }
                observeHealthRecordsForSelectedPet()
            }
        }
    }

    fun selectPet(pet: Pet) {
        _uiState.update { it.copy(selectedPet = pet) }
        observeHealthRecordsForSelectedPet()
    }

    private fun observeHealthRecordsForSelectedPet() {
        recordsJob?.cancel()
        remindersJob?.cancel()

        val selectedPetId = _uiState.value.selectedPet?.id

        recordsJob = viewModelScope.launch {
            getHealthRecordsUseCase(selectedPetId).collect { list ->
                if (list.isEmpty() && _uiState.value.records.isEmpty() && selectedPetId != null) {
                    seedSampleHealthRecords(selectedPetId)
                } else {
                    _uiState.update { it.copy(records = list, isLoading = false) }
                }
            }
        }

        remindersJob = viewModelScope.launch {
            getUpcomingRemindersUseCase(selectedPetId).collect { reminders ->
                _uiState.update { it.copy(upcomingReminders = reminders) }
            }
        }
    }

    private suspend fun seedSampleHealthRecords(petId: Long) {
        val sample1 = HealthRecord(
            petId = petId,
            title = "واکسن هاری سالانه",
            type = HealthRecordType.VACCINE,
            jalaliDate = "۱۴۰۳/۰۲/۱۰",
            nextDueJalaliDate = "۱۴۰۴/۰۲/۱۰",
            costToman = 450000,
            clinicOrDoctor = "کلینیک تخصصی دکتر البرزی",
            notes = "نوبت بعدی برای یادآوری واکسن چندگانه تنظیم شد",
            isCompleted = false
        )
        val sample2 = HealthRecord(
            petId = petId,
            title = "چکاپ و داروی ضد انگل",
            type = HealthRecordType.MEDICINE,
            jalaliDate = "۱۴۰۳/۰۵/۰۱",
            nextDueJalaliDate = "۱۴۰۳/۰۸/۰۱",
            costToman = 280000,
            clinicOrDoctor = "بیمارستان دامپزشکی تهران",
            notes = "قرص‌های ضدانگل تجویز شد، مصرف هر سه ماه یکبار",
            isCompleted = false
        )

        addHealthRecordUseCase(sample1)
        addHealthRecordUseCase(sample2)
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onAddRecordClick() {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.HEALTH, _uiState.value.records.size)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(showVipDialog = true) }
                    return@launch
                }
            }
            _uiState.update { it.copy(showAddDialog = true) }
        }
    }

    fun onDismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun onDismissVipDialog() {
        _uiState.update { it.copy(showVipDialog = false) }
    }

    fun addNewRecord(
        title: String,
        type: HealthRecordType,
        jalaliDate: String,
        nextDueDate: String?,
        costToman: Long,
        clinic: String,
        notes: String
    ) {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.HEALTH, _uiState.value.records.size)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(showAddDialog = false, showVipDialog = true) }
                    return@launch
                }
            }
            val selectedPetId = _uiState.value.selectedPet?.id ?: _uiState.value.pets.firstOrNull()?.id ?: 1L
            val record = HealthRecord(
                petId = selectedPetId,
                title = title,
                type = type,
                jalaliDate = jalaliDate,
                nextDueJalaliDate = if (nextDueDate.isNullOrBlank()) null else nextDueDate,
                costToman = costToman,
                clinicOrDoctor = clinic,
                notes = notes,
                isCompleted = nextDueDate.isNullOrBlank()
            )
            addHealthRecordUseCase(record)
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    class Factory(
        private val getHealthRecordsUseCase: GetHealthRecordsUseCase,
        private val getUpcomingRemindersUseCase: GetUpcomingRemindersUseCase,
        private val addHealthRecordUseCase: AddHealthRecordUseCase,
        private val getPetsUseCase: GetPetsUseCase,
        private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HealthViewModel(
                getHealthRecordsUseCase,
                getUpcomingRemindersUseCase,
                addHealthRecordUseCase,
                getPetsUseCase,
                checkFeatureAccessUseCase
            ) as T
        }
    }
}
