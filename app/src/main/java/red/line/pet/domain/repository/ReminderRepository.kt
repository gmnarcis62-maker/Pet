package red.line.pet.domain.repository

import kotlinx.coroutines.flow.Flow
import red.line.pet.domain.model.Reminder

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    fun getRemindersForPet(petId: Long): Flow<List<Reminder>>
    fun getUpcomingReminders(petId: Long? = null, fromTime: Long = System.currentTimeMillis()): Flow<List<Reminder>>
    suspend fun getAllActiveRemindersSync(): List<Reminder>
    suspend fun getReminderById(id: Long): Reminder?
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun deleteReminderById(id: Long)
    suspend fun setCompleted(id: Long, isCompleted: Boolean)
    suspend fun setEnabled(id: Long, isEnabled: Boolean)
    suspend fun reschedule(id: Long, newTimestamp: Long, newJalaliDate: String, newTimeString: String)
}
