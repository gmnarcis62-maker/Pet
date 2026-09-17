package red.line.pet.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import red.line.pet.RedLinePetApplication
import red.line.pet.domain.model.ReminderRepeatType
import red.line.pet.domain.model.ReminderType

/**
 * BroadcastReceiver triggered by AlarmManager when a reminder's exact time arrives
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, 0L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "یادآور مراقبت پتورا"
        val desc = intent.getStringExtra(AlarmScheduler.EXTRA_DESC) ?: "زمان مراقبت حیوان خانگی شما فرا رسیده است."
        val typeStr = intent.getStringExtra(AlarmScheduler.EXTRA_TYPE) ?: ReminderType.CUSTOM.name
        val reminderType = ReminderType.fromString(typeStr)

        // 1. Post local notification
        PetoraNotificationManager.showReminderNotification(
            context = context,
            reminderId = reminderId,
            title = title,
            body = desc,
            reminderType = reminderType
        )

        // 2. Handle recurring reminders if applicable
        val app = context.applicationContext as? RedLinePetApplication ?: return
        val appContainer = app.appContainer

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = appContainer.reminderRepository.getReminderById(reminderId) ?: return@launch
                if (reminder.repeatType != ReminderRepeatType.ONCE) {
                    val nextTime = reminder.calculateNextOccurrence()
                    if (nextTime != null) {
                        val nextJalali = red.line.pet.core.util.JalaliDateHelper.formatTimestampToJalali(nextTime)
                        appContainer.reminderRepository.reschedule(
                            id = reminderId,
                            newTimestamp = nextTime,
                            newJalaliDate = nextJalali,
                            newTimeString = reminder.timeString
                        )
                        val updated = reminder.copy(
                            remindTimestamp = nextTime,
                            jalaliDate = nextJalali
                        )
                        AlarmScheduler.schedule(context, updated)
                    }
                } else {
                    // Mark single occurrence as completed
                    appContainer.reminderRepository.setCompleted(reminderId, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
