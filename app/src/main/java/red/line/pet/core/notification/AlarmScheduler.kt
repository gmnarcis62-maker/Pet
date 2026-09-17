package red.line.pet.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import red.line.pet.domain.model.Reminder

/**
 * Petora Alarm & Offline Reminder Scheduler
 * Schedules alarms even when the app is closed or killed
 */
object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    const val ACTION_TRIGGER_REMINDER = "red.line.pet.ACTION_TRIGGER_REMINDER"
    const val EXTRA_REMINDER_ID = "EXTRA_REMINDER_ID"
    const val EXTRA_TITLE = "EXTRA_TITLE"
    const val EXTRA_DESC = "EXTRA_DESC"
    const val EXTRA_TYPE = "EXTRA_TYPE"
    const val EXTRA_PET_ID = "EXTRA_PET_ID"

    fun schedule(context: Context, reminder: Reminder) {
        if (!reminder.isEnabled || reminder.isCompleted) {
            Log.d(TAG, "Reminder ${reminder.id} disabled/completed, cancelling")
            cancel(context, reminder.id)
            return
        }

        val triggerTime = reminder.remindTimestamp
        val now = System.currentTimeMillis()

        if (triggerTime <= now) {
            Log.w(TAG, "Reminder ${reminder.id} is in the past ($triggerTime <= $now), skipping")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: run {
            Log.e(TAG, "AlarmManager not available")
            return
        }

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d(TAG, "Exact alarm scheduled for reminder ${reminder.id} at $triggerTime")
                } else {
                    Log.w(TAG, "No exact alarm permission — using inexact alarm")
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm scheduled for reminder ${reminder.id} at $triggerTime")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm scheduled for reminder ${reminder.id} at $triggerTime")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException, falling back to inexact", e)
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule inexact alarm too", e2)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for reminder ${reminder.id}", e)
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
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for reminder $reminderId")
        }
    }
}