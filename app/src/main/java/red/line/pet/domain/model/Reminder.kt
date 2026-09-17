package red.line.pet.domain.model

import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.core.util.PersianNumberFormatter
import java.util.Calendar

enum class ReminderType(val titleFa: String, val categoryFa: String) {
    VACCINE("واکسیناسیون", "پزشکی"),
    MEDICINE("مصرف دارو", "پزشکی"),
    CHECKUP("چکاپ دوره‌ای", "پزشکی"),
    FOOD("وعده غذایی", "تغذیه"),
    SUPPLEMENT("مکمل و ویتامین", "تغذیه"),
    WEIGHT("ثبت وزن", "پایش"),
    GROOMING("آرایش و بهداشت", "مراقبت"),
    CUSTOM("یادداشت و مراقبت ویژه", "سایر");

    companion object {
        fun fromString(value: String): ReminderType {
            return try {
                valueOf(value)
            } catch (e: Exception) {
                CUSTOM
            }
        }
    }
}

enum class ReminderRepeatType(val titleFa: String) {
    ONCE("یک‌بار"),
    DAILY("روزانه"),
    WEEKLY("هفتگی"),
    MONTHLY("ماهانه"),
    YEARLY("سالانه");

    companion object {
        fun fromString(value: String): ReminderRepeatType {
            return try {
                valueOf(value)
            } catch (e: Exception) {
                ONCE
            }
        }
    }
}

data class Reminder(
    val id: Long = 0,
    val petId: Long,
    val title: String,
    val description: String = "",
    val reminderType: ReminderType = ReminderType.CUSTOM,
    val targetId: Long? = null,
    val remindTimestamp: Long,
    val jalaliDate: String = "",
    val timeString: String = "08:00",
    val repeatType: ReminderRepeatType = ReminderRepeatType.ONCE,
    val isEnabled: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Display formatted Jalali Date & Time in Persian digits
     */
    fun getFormattedDateTimeFa(): String {
        val jDate = if (jalaliDate.isNotBlank()) {
            PersianNumberFormatter.toPersian(jalaliDate)
        } else {
            JalaliDateHelper.formatTimestampToJalali(remindTimestamp)
        }
        val pTime = PersianNumberFormatter.toPersian(timeString)
        return "$jDate - ساعت $pTime"
    }

    /**
     * Checks if this reminder is due in the near future (e.g., within 48 hours)
     */
    fun isUpcoming(now: Long = System.currentTimeMillis()): Boolean {
        val fortyEightHours = 48 * 60 * 60 * 1000L
        return isEnabled && !isCompleted && remindTimestamp in now..(now + fortyEightHours)
    }

    /**
     * Checks if this reminder has passed its target time
     */
    fun isOverdue(now: Long = System.currentTimeMillis()): Boolean {
        return isEnabled && !isCompleted && remindTimestamp < now
    }

    /**
     * Calculates the next timestamp if this reminder repeats
     */
    fun calculateNextOccurrence(): Long? {
        if (repeatType == ReminderRepeatType.ONCE) return null

        val cal = Calendar.getInstance().apply {
            timeInMillis = remindTimestamp
        }
        val now = System.currentTimeMillis()

        while (cal.timeInMillis <= now) {
            when (repeatType) {
                ReminderRepeatType.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                ReminderRepeatType.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                ReminderRepeatType.MONTHLY -> cal.add(Calendar.MONTH, 1)
                ReminderRepeatType.YEARLY -> cal.add(Calendar.YEAR, 1)
                ReminderRepeatType.ONCE -> return null
            }
        }
        return cal.timeInMillis
    }
}
