package red.line.pet.presentation.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import red.line.pet.core.billing.VipFeature
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.WeightRecord
import red.line.pet.domain.model.WeightStatistics
import red.line.pet.domain.model.WeightTimeRange
import red.line.pet.domain.model.WeightUnit
import red.line.pet.domain.usecase.pet.GetPetsUseCase
import red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase
import red.line.pet.domain.usecase.weight.AddWeightRecordUseCase
import red.line.pet.domain.usecase.weight.DeleteWeightRecordUseCase
import red.line.pet.domain.usecase.weight.GetFilteredWeightRecordsUseCase
import red.line.pet.domain.usecase.weight.GetWeightRecordsUseCase
import red.line.pet.domain.usecase.weight.GetWeightStatisticsUseCase
import red.line.pet.domain.usecase.weight.UpdateWeightRecordUseCase

data class WeightUiState(
    val isLoading: Boolean = true,
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val allRecords: List<WeightRecord> = emptyList(),
    val filteredRecords: List<WeightRecord> = emptyList(),
    val selectedRange: WeightTimeRange = WeightTimeRange.ALL,
    val statistics: WeightStatistics = WeightStatistics(),
    val selectedRecordPoint: WeightRecord? = null,
    val isAddEditOpen: Boolean = false,
    val editingRecord: WeightRecord? = null,
    val deletingRecord: WeightRecord? = null,
    val showVipDialog: Boolean = false,
    val userMessage: String? = null
)

class WeightViewModel(
    private val getPetsUseCase: GetPetsUseCase,
    private val getWeightRecordsUseCase: GetWeightRecordsUseCase,
    private val getFilteredWeightRecordsUseCase: GetFilteredWeightRecordsUseCase,
    private val getWeightStatisticsUseCase: GetWeightStatisticsUseCase,
    private val addWeightRecordUseCase: AddWeightRecordUseCase,
    private val updateWeightRecordUseCase: UpdateWeightRecordUseCase,
    private val deleteWeightRecordUseCase: DeleteWeightRecordUseCase,
    private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    init {
        loadPets()
    }

    private fun loadPets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPetsUseCase()
                .catch {
                    _uiState.update { it.copy(isLoading = false, userMessage = "خطا در بارگذاری لیست حیوانات") }
                }
                .collectLatest { petsList ->
                    val currentSelected = _uiState.value.selectedPet
                    val selected = if (currentSelected != null && petsList.any { it.id == currentSelected.id }) {
                        petsList.first { it.id == currentSelected.id }
                    } else {
                        petsList.firstOrNull()
                    }
                    _uiState.update { it.copy(pets = petsList, selectedPet = selected, isLoading = false) }
                    if (selected != null) {
                        loadWeightData(selected.id)
                    }
                }
        }
    }

    fun selectPet(pet: Pet) {
        if (_uiState.value.selectedPet?.id == pet.id) return
        _uiState.update { it.copy(selectedPet = pet, selectedRecordPoint = null) }
        loadWeightData(pet.id)
    }

    private fun loadWeightData(petId: Long) {
        viewModelScope.launch {
            getWeightRecordsUseCase(petId)
                .catch {
                    _uiState.update { it.copy(userMessage = "خطا در دریافت پرونده وزن") }
                }
                .collectLatest { allRecords ->
                    val range = _uiState.value.selectedRange
                    val filtered = filterRecords(allRecords, range)
                    val stats = getWeightStatisticsUseCase.calculateStatistics(allRecords)

                    _uiState.update {
                        it.copy(
                            allRecords = allRecords,
                            filteredRecords = filtered,
                            statistics = stats,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun setTimeRange(range: WeightTimeRange) {
        _uiState.update { current ->
            val filtered = filterRecords(current.allRecords, range)
            current.copy(selectedRange = range, filteredRecords = filtered, selectedRecordPoint = null)
        }
    }

    private fun filterRecords(records: List<WeightRecord>, range: WeightTimeRange): List<WeightRecord> {
        val sorted = records.sortedByDescending { it.date }
        return if (range.days == null) {
            sorted
        } else {
            val cutoff = System.currentTimeMillis() - (range.days.toLong() * 24 * 60 * 60 * 1000L)
            sorted.filter { it.date >= cutoff }
        }
    }

    fun selectChartPoint(record: WeightRecord?) {
        _uiState.update { it.copy(selectedRecordPoint = record) }
    }

    fun openAddWeightDialog() {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.WEIGHT, _uiState.value.allRecords.size)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(showVipDialog = true) }
                    return@launch
                }
            }
            _uiState.update { it.copy(isAddEditOpen = true, editingRecord = null) }
        }
    }

    fun openEditWeightDialog(record: WeightRecord) {
        _uiState.update { it.copy(isAddEditOpen = true, editingRecord = record) }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditOpen = false, editingRecord = null) }
    }

    fun onDismissVipDialog() {
        _uiState.update { it.copy(showVipDialog = false) }
    }

    fun promptDeleteRecord(record: WeightRecord) {
        _uiState.update { it.copy(deletingRecord = record) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(deletingRecord = null) }
    }

    fun confirmDeleteRecord() {
        val record = _uiState.value.deletingRecord ?: return
        viewModelScope.launch {
            when (val result = deleteWeightRecordUseCase(record.id)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(deletingRecord = null, selectedRecordPoint = null, userMessage = "رکورد وزن حذف شد") }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(deletingRecord = null, userMessage = "خطا در حذف رکورد وزن") }
                }
            }
        }
    }

    fun saveWeightRecord(
        weightValue: Double,
        unit: WeightUnit,
        jalaliDateStr: String,
        notes: String
    ) {
        val petId = _uiState.value.selectedPet?.id ?: return
        val canonicalKg = if (unit == WeightUnit.GRAM) weightValue / 1000.0 else weightValue

        // Parse date timestamp
        val parsedJalali = JalaliDateHelper.parseJalali(jalaliDateStr) ?: JalaliDateHelper.now()
        val standardDateStr = parsedJalali.toStandardString(usePersianDigits = false)
        
        val editing = _uiState.value.editingRecord

        viewModelScope.launch {
            if (editing != null) {
                val updated = editing.copy(
                    weightKg = canonicalKg,
                    unit = unit,
                    displayWeight = weightValue,
                    jalaliDate = standardDateStr,
                    notes = notes.trim()
                )
                when (val result = updateWeightRecordUseCase(updated)) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(isAddEditOpen = false, editingRecord = null, userMessage = "تغییرات وزن با موفقیت ثبت شد") }
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(userMessage = "مقدار وزن یا اطلاعات وارد شده معتبر نیست") }
                    }
                }
            } else {
                if (checkFeatureAccessUseCase != null) {
                    val access = checkFeatureAccessUseCase(VipFeature.WEIGHT, _uiState.value.allRecords.size)
                    if (access is AppResult.Error) {
                        _uiState.update { it.copy(isAddEditOpen = false, showVipDialog = true) }
                        return@launch
                    }
                }
                val newRecord = WeightRecord(
                    id = 0,
                    petId = petId,
                    weightKg = canonicalKg,
                    unit = unit,
                    displayWeight = weightValue,
                    jalaliDate = standardDateStr,
                    notes = notes.trim(),
                    date = System.currentTimeMillis()
                )
                when (val result = addWeightRecordUseCase(newRecord)) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(isAddEditOpen = false, editingRecord = null, userMessage = "رکورد جدید وزن با موفقیت ثبت شد") }
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(userMessage = "مقدار وزن یا اطلاعات وارد شده معتبر نیست") }
                    }
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    class Factory(
        private val getPetsUseCase: GetPetsUseCase,
        private val getWeightRecordsUseCase: GetWeightRecordsUseCase,
        private val getFilteredWeightRecordsUseCase: GetFilteredWeightRecordsUseCase,
        private val getWeightStatisticsUseCase: GetWeightStatisticsUseCase,
        private val addWeightRecordUseCase: AddWeightRecordUseCase,
        private val updateWeightRecordUseCase: UpdateWeightRecordUseCase,
        private val deleteWeightRecordUseCase: DeleteWeightRecordUseCase,
        private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeightViewModel(
                getPetsUseCase,
                getWeightRecordsUseCase,
                getFilteredWeightRecordsUseCase,
                getWeightStatisticsUseCase,
                addWeightRecordUseCase,
                updateWeightRecordUseCase,
                deleteWeightRecordUseCase,
                checkFeatureAccessUseCase
            ) as T
        }
    }
}
