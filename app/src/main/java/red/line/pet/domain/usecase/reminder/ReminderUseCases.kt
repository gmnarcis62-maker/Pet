package red.line.pet.domain.usecase.reminder

import kotlinx.coroutines.flow.Flow
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.DataError
import red.line.pet.domain.model.Reminder
import red.line.pet.domain.repository.ReminderRepository

class GetRemindersUseCase(
    private val reminderRepository: ReminderRepository
) {
    operator fun invoke(petId: Long? = null): Flow<List<Reminder>> {
        return if (petId != null && petId > 0) {
            reminderRepository.getRemindersForPet(petId)
        } else {
            reminderRepository.getAllReminders()
        }
    }
}

class GetUpcomingRemindersUseCase(
    private val reminderRepository: ReminderRepository
) {
    operator fun invoke(petId: Long? = null, fromTime: Long = System.currentTimeMillis()): Flow<List<Reminder>> {
        return reminderRepository.getUpcomingReminders(petId, fromTime)
    }
}

class AddReminderUseCase(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder): AppResult<Long, DataError.Local> {
        if (reminder.title.isBlank() || reminder.petId <= 0 || reminder.remindTimestamp <= 0) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }

        return try {
            val id = reminderRepository.insertReminder(reminder)
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error(DataError.Local.DATABASE_ERROR)
        }
    }
}

class UpdateReminderUseCase(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder): AppResult<Unit, DataError.Local> {
        if (reminder.title.isBlank() || reminder.id <= 0) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        return try {
            reminderRepository.updateReminder(reminder)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(DataError.Local.DATABASE_ERROR)
        }
    }
}

class DeleteReminderUseCase(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: Long): AppResult<Unit, DataError.Local> {
        return try {
            reminderRepository.deleteReminderById(reminderId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(DataError.Local.DATABASE_ERROR)
        }
    }
}

class ToggleReminderUseCase(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: Long, isEnabled: Boolean): AppResult<Unit, DataError.Local> {
        return try {
            reminderRepository.setEnabled(reminderId, isEnabled)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(DataError.Local.DATABASE_ERROR)
        }
    }
}

class CompleteReminderUseCase(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: Long, isCompleted: Boolean = true): AppResult<Unit, DataError.Local> {
        return try {
            reminderRepository.setCompleted(reminderId, isCompleted)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(DataError.Local.DATABASE_ERROR)
        }
    }
}

class SnoozeReminderUseCase(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: Long, minutes: Int = 30): AppResult<Unit, DataError.Local> {
        return try {
            val reminder = reminderRepository.getReminderById(reminderId)
                ?: return AppResult.Error(DataError.Local.NOT_FOUND)

            val newTimestamp = System.currentTimeMillis() + (minutes * 60 * 1000L)
            val newJalali = red.line.pet.core.util.JalaliDateHelper.formatTimestampToJalali(newTimestamp)
            
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = newTimestamp }
            val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = cal.get(java.util.Calendar.MINUTE)
            val newTimeString = String.format("%02d:%02d", hour, minute)

            reminderRepository.reschedule(reminderId, newTimestamp, newJalali, newTimeString)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(DataError.Local.DATABASE_ERROR)
        }
    }
}
