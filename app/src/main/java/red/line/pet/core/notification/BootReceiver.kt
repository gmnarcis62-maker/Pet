package red.line.pet.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import red.line.pet.RedLinePetApplication

/**
 * Restores and reschedules all active alarms after device reboot
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val app = context.applicationContext as? RedLinePetApplication ?: return
            val appContainer = app.appContainer

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeReminders = appContainer.reminderRepository.getAllActiveRemindersSync()
                    val now = System.currentTimeMillis()
                    for (reminder in activeReminders) {
                        if (reminder.remindTimestamp > now) {
                            AlarmScheduler.schedule(context, reminder)
                        } else if (reminder.repeatType != red.line.pet.domain.model.ReminderRepeatType.ONCE) {
                            // Calculate next recurring occurrence
                            val nextTime = reminder.calculateNextOccurrence()
                            if (nextTime != null) {
                                val nextJalali = red.line.pet.core.util.JalaliDateHelper.formatTimestampToJalali(nextTime)
                                appContainer.reminderRepository.reschedule(
                                    id = reminder.id,
                                    newTimestamp = nextTime,
                                    newJalaliDate = nextJalali,
                                    newTimeString = reminder.timeString
                                )
                                AlarmScheduler.schedule(context, reminder.copy(remindTimestamp = nextTime, jalaliDate = nextJalali))
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
