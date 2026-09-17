package red.line.pet.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import red.line.pet.domain.model.Reminder

/**
 * Petora Alarm & Offline Reminder Scheduler
 * Schedules alarms even when the app is closed or killed
 */
object AlarmScheduler {

    const val ACTION_TRIGGER_REMINDER = "red.line.pet.ACTION_TRIGGER_REMINDER"
    const val EXTRA_REMINDER_ID = "EXTRA_REMINDER_ID"
    const val EXTRA_TITLE = "EXTRA_TITLE"
    const val EXTRA_DESC = "EXTRA_DESC"
    const val EXTRA_TYPE = "EXTRA_TYPE"
    const val EXTRA_PET_ID = "EXTRA_PET_ID"

    fun schedule(context: Context, reminder: Reminder) {
        if (!reminder.isEnabled || reminder.isCompleted) {
            cancel(context, reminder.id)
            return
        }

        val triggerTime = reminder.remindTimestamp
        val now = System.currentTimeMillis()

        if (triggerTime <= now) {
            // Overdue reminder, don't schedule in the past
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_TITLE, reminder.title)
            putExtra(EXTRA_DESC, reminder.description)
            putExtra(EXTRA_TYPE, reminder.reminderType.name)
            putExtra(EXTRA_PET_ID, reminder.petId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // In case exact alarm permission isn't granted on Android 12+, fallback to inexact
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
