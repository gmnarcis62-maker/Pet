package red.line.pet.presentation.food

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
import red.line.pet.domain.model.DayOfWeekFa
import red.line.pet.domain.model.FoodSchedule
import red.line.pet.domain.model.Pet
import red.line.pet.domain.usecase.food.AddFoodScheduleUseCase
import red.line.pet.domain.usecase.food.DeleteFoodScheduleUseCase
import red.line.pet.domain.usecase.food.GetFoodSchedulesUseCase
import red.line.pet.domain.usecase.food.GetTodayMealsUseCase
import red.line.pet.domain.usecase.food.GetWeeklyFoodScheduleUseCase
import red.line.pet.domain.usecase.food.ToggleMealCompletedUseCase
import red.line.pet.domain.usecase.food.UpdateFoodScheduleUseCase
import red.line.pet.domain.usecase.pet.GetPetsUseCase
import red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase

data class FoodUiState(
    val isLoading: Boolean = true,
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val allSchedules: List<FoodSchedule> = emptyList(),
    val todayMeals: List<FoodSchedule> = emptyList(),
    val displayedSchedules: List<FoodSchedule> = emptyList(),
    val selectedDayFilter: DayOfWeekFa? = null,
    val totalTodayMealsCount: Int = 0,
    val completedTodayMealsCount: Int = 0,
    val todayProgress: Float = 0f,
    val isAddEditOpen: Boolean = false,
    val editingSchedule: FoodSchedule? = null,
    val showVipDialog: Boolean = false,
    val userMessage: String? = null
)

class FoodViewModel(
    private val getPetsUseCase: GetPetsUseCase,
    private val getFoodSchedulesUseCase: GetFoodSchedulesUseCase,
    private val getTodayMealsUseCase: GetTodayMealsUseCase,
    private val addFoodScheduleUseCase: AddFoodScheduleUseCase,
    private val updateFoodScheduleUseCase: UpdateFoodScheduleUseCase,
    private val deleteFoodScheduleUseCase: DeleteFoodScheduleUseCase,
    private val toggleMealCompletedUseCase: ToggleMealCompletedUseCase,
    private val getWeeklyFoodScheduleUseCase: GetWeeklyFoodScheduleUseCase,
    private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    init {
        loadPets()
    }

    private fun loadPets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPetsUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, userMessage = "خطا در بارگذاری اطلاعات حیوانات") }
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
                        loadFoodData(selected.id)
                    }
                }
        }
    }

    fun selectPet(pet: Pet) {
        if (_uiState.value.selectedPet?.id == pet.id) return
        _uiState.update { it.copy(selectedPet = pet) }
        loadFoodData(pet.id)
    }

    private fun loadFoodData(petId: Long) {
        viewModelScope.launch {
            getFoodSchedulesUseCase(petId)
                .catch { e ->
                    _uiState.update { it.copy(userMessage = "خطا در دریافت برنامه‌های غذایی") }
                }
                .collectLatest { allSchedules ->
                    val currentDay = JalaliDateHelper.getCurrentDayOfWeekFa()
                    val todayMeals = allSchedules
                        .filter { it.isActive && (it.repeatDays.isEmpty() || it.repeatDays.contains(currentDay)) }
                        .sortedBy { it.mealTime }

                    val totalToday = todayMeals.size
                    val completedToday = todayMeals.count { it.isCompleted }
                    val progress = if (totalToday > 0) completedToday.toFloat() / totalToday.toFloat() else 0f

                    val filter = _uiState.value.selectedDayFilter
                    val displayed = if (filter != null) {
                        allSchedules.filter { it.repeatDays.contains(filter) }.sortedBy { it.mealTime }
                    } else {
                        allSchedules.sortedBy { it.mealTime }
                    }

                    _uiState.update {
                        it.copy(
                            allSchedules = allSchedules,
                            todayMeals = todayMeals,
                            displayedSchedules = displayed,
                            totalTodayMealsCount = totalToday,
                            completedTodayMealsCount = completedToday,
                            todayProgress = progress,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun setDayFilter(day: DayOfWeekFa?) {
        _uiState.update { current ->
            val displayed = if (day != null) {
                current.allSchedules.filter { it.repeatDays.contains(day) }.sortedBy { it.mealTime }
            } else {
                current.allSchedules.sortedBy { it.mealTime }
            }
            current.copy(selectedDayFilter = day, displayedSchedules = displayed)
        }
    }

    fun toggleMealCompleted(schedule: FoodSchedule) {
        viewModelScope.launch {
            val newStatus = !schedule.isCompleted
            when (val result = toggleMealCompletedUseCase(schedule.id, newStatus)) {
                is AppResult.Success -> {
                    // Update was successful, Flow will emit new state
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(userMessage = "خطا در تغییر وضعیت مصرف وعده") }
                }
            }
        }
    }

    fun toggleMealActive(schedule: FoodSchedule) {
        viewModelScope.launch {
            val updated = schedule.copy(isActive = !schedule.isActive)
            when (val result = updateFoodScheduleUseCase(updated)) {
                is AppResult.Success -> {}
                is AppResult.Error -> {
                    _uiState.update { it.copy(userMessage = "خطا در تغییر وضعیت فعال بودن برنامه") }
                }
            }
        }
    }

    fun openAddMealDialog() {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.FOOD, _uiState.value.allSchedules.size)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(showVipDialog = true) }
                    return@launch
                }
            }
            _uiState.update { it.copy(isAddEditOpen = true, editingSchedule = null) }
        }
    }

    fun openEditMealDialog(schedule: FoodSchedule) {
        _uiState.update { it.copy(isAddEditOpen = true, editingSchedule = schedule) }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditOpen = false, editingSchedule = null) }
    }

    fun onDismissVipDialog() {
        _uiState.update { it.copy(showVipDialog = false) }
    }

    fun saveMeal(schedule: FoodSchedule) {
        val petId = _uiState.value.selectedPet?.id ?: return
        val finalSchedule = schedule.copy(petId = petId)

        viewModelScope.launch {
            if (finalSchedule.id > 0) {
                when (val result = updateFoodScheduleUseCase(finalSchedule)) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(isAddEditOpen = false, editingSchedule = null, userMessage = "برنامه غذایی با موفقیت ویرایش شد") }
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(userMessage = "اطلاعات وارد شده نامعتبر است") }
                    }
                }
            } else {
                if (checkFeatureAccessUseCase != null) {
                    val access = checkFeatureAccessUseCase(VipFeature.FOOD, _uiState.value.allSchedules.size)
                    if (access is AppResult.Error) {
                        _uiState.update { it.copy(isAddEditOpen = false, showVipDialog = true) }
                        return@launch
                    }
                }
                when (val result = addFoodScheduleUseCase(finalSchedule)) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(isAddEditOpen = false, editingSchedule = null, userMessage = "وعده غذایی جدید با موفقیت ثبت شد") }
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(userMessage = "اطلاعات وارد شده نامعتبر است") }
                    }
                }
            }
        }
    }

    fun deleteMeal(scheduleId: Long) {
        viewModelScope.launch {
            when (val result = deleteFoodScheduleUseCase(scheduleId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(userMessage = "وعده غذایی حذف شد") }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(userMessage = "خطا در حذف وعده غذایی") }
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    class Factory(
        private val getPetsUseCase: GetPetsUseCase,
        private val getFoodSchedulesUseCase: GetFoodSchedulesUseCase,
        private val getTodayMealsUseCase: GetTodayMealsUseCase,
        private val addFoodScheduleUseCase: AddFoodScheduleUseCase,
        private val updateFoodScheduleUseCase: UpdateFoodScheduleUseCase,
        private val deleteFoodScheduleUseCase: DeleteFoodScheduleUseCase,
        private val toggleMealCompletedUseCase: ToggleMealCompletedUseCase,
        private val getWeeklyFoodScheduleUseCase: GetWeeklyFoodScheduleUseCase,
        private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FoodViewModel(
                getPetsUseCase,
                getFoodSchedulesUseCase,
                getTodayMealsUseCase,
                addFoodScheduleUseCase,
                updateFoodScheduleUseCase,
                deleteFoodScheduleUseCase,
                toggleMealCompletedUseCase,
                getWeeklyFoodScheduleUseCase,
                checkFeatureAccessUseCase
            ) as T
        }
    }
}
