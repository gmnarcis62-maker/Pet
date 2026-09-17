package red.line.pet.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import red.line.pet.domain.model.Reminder
import red.line.pet.domain.model.ReminderType

/**
 * Petora Local Offline Notification Engine
 * Manages Persian Notification Channels and High-Priority Alerts
 */
object PetoraNotificationManager {

    const val CHANNEL_ID_REMINDERS = "petora_care_reminders_channel"
    const val CHANNEL_NAME_FA = "یادآوری مراقبت پتورا"
    const val CHANNEL_DESC_FA = "اعلان‌های هوشمند و یادآورهای پزشکی، دارویی و تغذیه حیوان خانگی"

    /**
     * Initializes the notification channel on Android 8.0+
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                CHANNEL_NAME_FA,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_FA
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Posts a local notification for a reminder
     */
    fun showReminderNotification(
        context: Context,
        reminderId: Long,
        title: String,
        body: String,
        reminderType: ReminderType,
        petName: String = ""
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_REMINDER_ID", reminderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val headerText = if (petName.isNotBlank()) "پتورا • $petName" else "پتورا • ${reminderType.categoryFa}"

        val iconRes = android.R.drawable.ic_popup_reminder

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(headerText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(reminderId.toInt(), notification)
        } catch (e: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS permission might be missing
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Cancels an existing notification
     */
    fun cancelNotification(context: Context, reminderId: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(reminderId.toInt())
    }
}
