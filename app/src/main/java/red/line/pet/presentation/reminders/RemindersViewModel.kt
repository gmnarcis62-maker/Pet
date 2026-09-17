package red.line.pet.presentation.reminders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import red.line.pet.core.billing.VipFeature
import red.line.pet.core.notification.AlarmScheduler
import red.line.pet.core.notification.PetoraNotificationManager
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.Reminder
import red.line.pet.domain.model.ReminderRepeatType
import red.line.pet.domain.model.ReminderType
import red.line.pet.domain.usecase.pet.GetPetsUseCase
import red.line.pet.domain.usecase.reminder.AddReminderUseCase
import red.line.pet.domain.usecase.reminder.CompleteReminderUseCase
import red.line.pet.domain.usecase.reminder.DeleteReminderUseCase
import red.line.pet.domain.usecase.reminder.GetRemindersUseCase
import red.line.pet.domain.usecase.reminder.GetUpcomingRemindersUseCase
import red.line.pet.domain.usecase.reminder.SnoozeReminderUseCase
import red.line.pet.domain.usecase.reminder.ToggleReminderUseCase
import red.line.pet.domain.usecase.reminder.UpdateReminderUseCase
import red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase

enum class ReminderFilterTab(val titleFa: String) {
    ALL("همه"),
    UPCOMING("پیش‌رو"),
    HEALTH("سلامت"),
    FOOD("تغذیه"),
    COMPLETED("تکمیل‌شده")
}

data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val upcomingReminders: List<Reminder> = emptyList(),
    val pets: List<Pet> = emptyList(),
    val selectedPetId: Long? = null,
    val selectedTab: ReminderFilterTab = ReminderFilterTab.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddEditSheet: Boolean = false,
    val editingReminder: Reminder? = null,
    val presetPetId: Long? = null,
    val presetType: ReminderType? = null,
    val presetTitle: String = "",
    val showVipDialog: Boolean = false
)

class RemindersViewModel(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val getUpcomingRemindersUseCase: GetUpcomingRemindersUseCase,
    private val addReminderUseCase: AddReminderUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val toggleReminderUseCase: ToggleReminderUseCase,
    private val completeReminderUseCase: CompleteReminderUseCase,
    private val snoozeReminderUseCase: SnoozeReminderUseCase,
    private val getPetsUseCase: GetPetsUseCase,
    private val appContext: Context,
    private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState(isLoading = true))
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    private var remindersJob: kotlinx.coroutines.Job? = null
    private var upcomingJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            getPetsUseCase().collect { petsList ->
                _uiState.update { state ->
                    val newSelectedId = if (state.selectedPetId != null && petsList.any { it.id == state.selectedPetId }) {
                        state.selectedPetId
                    } else {
                        petsList.firstOrNull()?.id
                    }
                    state.copy(pets = petsList, selectedPetId = newSelectedId)
                }
                observeRemindersForSelectedPet()
            }
        }
    }

    fun selectPet(petId: Long?) {
        _uiState.update { it.copy(selectedPetId = petId) }
        observeRemindersForSelectedPet()
    }

    private suspend fun seedDefaultRemindersIfEmpty(firstPetId: Long) {
        val now = System.currentTimeMillis()
        val tomorrow = now + 24 * 60 * 60 * 1000L
        val nextWeek = now + 7 * 24 * 60 * 60 * 1000L

        val r1 = Reminder(
            petId = firstPetId,
            title = "وعده غذای خشک صبحگاهی",
            description = "۵۰ گرم غذای مخصوص به همراه آب تازه",
            reminderType = ReminderType.FOOD,
            remindTimestamp = tomorrow,
            jalaliDate = JalaliDateHelper.formatTimestampToJalali(tomorrow),
            timeString = "08:30",
            repeatType = ReminderRepeatType.DAILY,
            isEnabled = true
        )
        val r2 = Reminder(
            petId = firstPetId,
            title = "قرص ضد انگل و ویتامین",
            description = "مصرف همراه با مقدار کمی تشویقی بعد از غذا",
            reminderType = ReminderType.MEDICINE,
            remindTimestamp = nextWeek,
            jalaliDate = JalaliDateHelper.formatTimestampToJalali(nextWeek),
            timeString = "14:00",
            repeatType = ReminderRepeatType.MONTHLY,
            isEnabled = true
        )

        when (val res1 = addReminderUseCase(r1)) {
            is AppResult.Success -> {
                AlarmScheduler.schedule(appContext, r1.copy(id = res1.data))
            }
            is AppResult.Error -> {
                android.util.Log.e("RemindersVM", "Failed to seed r1: ${res1.message}")
            }
        }
        when (val res2 = addReminderUseCase(r2)) {
            is AppResult.Success -> {
                AlarmScheduler.schedule(appContext, r2.copy(id = res2.data))
            }
            is AppResult.Error -> {
                android.util.Log.e("RemindersVM", "Failed to seed r2: ${res2.message}")
            }
        }
    }

    private fun observeRemindersForSelectedPet() {
        remindersJob?.cancel()
        upcomingJob?.cancel()

        val selectedPetId = _uiState.value.selectedPetId

        remindersJob = viewModelScope.launch {
            getRemindersUseCase(selectedPetId).collect { reminderList ->
                if (reminderList.isEmpty() && _uiState.value.reminders.isEmpty() && selectedPetId != null) {
                    seedDefaultRemindersIfEmpty(selectedPetId)
                } else {
                    _uiState.update { it.copy(reminders = reminderList, isLoading = false) }
                }
            }
        }

        upcomingJob = viewModelScope.launch {
            getUpcomingRemindersUseCase(selectedPetId).collect { upcomingList ->
                _uiState.update { it.copy(upcomingReminders = upcomingList) }
            }
        }
    }

    fun selectTab(tab: ReminderFilterTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openAddDialog(
        presetPetId: Long? = null,
        presetType: ReminderType? = null,
        presetTitle: String = ""
    ) {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.REMINDERS, _uiState.value.reminders.size)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(showVipDialog = true) }
                    return@launch
                }
            }
            _uiState.update {
                it.copy(
                    showAddEditSheet = true,
                    editingReminder = null,
                    presetPetId = presetPetId ?: it.selectedPetId ?: it.pets.firstOrNull()?.id,
                    presetType = presetType ?: ReminderType.CUSTOM,
                    presetTitle = presetTitle
                )
            }
        }
    }

    fun openEditDialog(reminder: Reminder) {
        _uiState.update {
            it.copy(
                showAddEditSheet = true,
                editingReminder = reminder
            )
        }
    }

    fun dismissSheet() {
        _uiState.update {
            it.copy(
                showAddEditSheet = false,
                editingReminder = null,
                errorMessage = null
            )
        }
    }

    fun onDismissVipDialog() {
        _uiState.update { it.copy(showVipDialog = false) }
    }

    fun saveReminder(
        id: Long = 0,
        petId: Long,
        title: String,
        description: String,
        reminderType: ReminderType,
        jalaliDate: String,
        timeString: String,
        repeatType: ReminderRepeatType
    ) {
        viewModelScope.launch {
            if (id == 0L && checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.REMINDERS, _uiState.value.reminders.size)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(showAddEditSheet = false, showVipDialog = true) }
                    return@launch
                }
            }

            val timestamp = JalaliDateHelper.parseJalaliDateTimeToTimestamp(jalaliDate, timeString)
                ?: (System.currentTimeMillis() + 60 * 60 * 1000L)

            val reminder = Reminder(
                id = id,
                petId = petId,
                title = title.trim(),
                description = description.trim(),
                reminderType = reminderType,
                remindTimestamp = timestamp,
                jalaliDate = jalaliDate,
                timeString = timeString,
                repeatType = repeatType,
                isEnabled = true,
                isCompleted = false
            )

            if (id == 0L) {
                when (val result = addReminderUseCase(reminder)) {
                    is AppResult.Success -> {
                        val newReminder = reminder.copy(id = result.data)
                        AlarmScheduler.schedule(appContext, newReminder)
                        dismissSheet()
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(errorMessage = "خطا در ثبت یادآور. لطفاً ورودی‌ها را بررسی کنید.") }
                    }
                }
            } else {
                when (updateReminderUseCase(reminder)) {
                    is AppResult.Success -> {
                        AlarmScheduler.schedule(appContext, reminder)
                        dismissSheet()
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(errorMessage = "خطا در ویرایش یادآور.") }
                    }
                }
            }
        }
    }

    fun toggleReminder(reminderId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            toggleReminderUseCase(reminderId, isEnabled)
            val current = _uiState.value.reminders.find { it.id == reminderId }
            if (current != null) {
                val updated = current.copy(isEnabled = isEnabled)
                if (isEnabled) {
                    AlarmScheduler.schedule(appContext, updated)
                } else {
                    AlarmScheduler.cancel(appContext, reminderId)
                    PetoraNotificationManager.cancelNotification(appContext, reminderId)
                }
            }
        }
    }

    fun completeReminder(reminderId: Long, isCompleted: Boolean = true) {
        viewModelScope.launch {
            completeReminderUseCase(reminderId, isCompleted)
            if (isCompleted) {
                AlarmScheduler.cancel(appContext, reminderId)
                PetoraNotificationManager.cancelNotification(appContext, reminderId)
            } else {
                val current = _uiState.value.reminders.find { it.id == reminderId }
                if (current != null) {
                    AlarmScheduler.schedule(appContext, current.copy(isCompleted = false))
                }
            }
        }
    }

    fun snoozeReminder(reminderId: Long, minutes: Int = 30) {
        viewModelScope.launch {
            snoozeReminderUseCase(reminderId, minutes)
            val current = _uiState.value.reminders.find { it.id == reminderId }
            if (current != null) {
                val newTimestamp = System.currentTimeMillis() + (minutes * 60 * 1000L)
                val updated = current.copy(
                    remindTimestamp = newTimestamp,
                    isCompleted = false,
                    isEnabled = true
                )
                AlarmScheduler.schedule(appContext, updated)
            }
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            deleteReminderUseCase(reminderId)
            AlarmScheduler.cancel(appContext, reminderId)
            PetoraNotificationManager.cancelNotification(appContext, reminderId)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class RemindersViewModelFactory(
    private val appContainer: red.line.pet.core.di.AppContainer,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemindersViewModel::class.java)) {
            return RemindersViewModel(
                getRemindersUseCase = appContainer.getRemindersUseCase,
                getUpcomingRemindersUseCase = appContainer.getUpcomingCareRemindersUseCase,
                addReminderUseCase = appContainer.addReminderUseCase,
                updateReminderUseCase = appContainer.updateReminderUseCase,
                deleteReminderUseCase = appContainer.deleteReminderUseCase,
                toggleReminderUseCase = appContainer.toggleReminderUseCase,
                completeReminderUseCase = appContainer.completeReminderUseCase,
                snoozeReminderUseCase = appContainer.snoozeReminderUseCase,
                getPetsUseCase = appContainer.getPetsUseCase,
                appContext = context.applicationContext,
                checkFeatureAccessUseCase = appContainer.checkFeatureAccessUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}