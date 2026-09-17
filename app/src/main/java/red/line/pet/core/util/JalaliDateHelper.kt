package red.line.pet.core.util

import java.util.Calendar
import java.util.Date

/**
 * Data class representing a Jalali (Solar Hijri / شمسی) date.
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<JalaliDate> {

    override fun compareTo(other: JalaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    /**
     * Standard formatted representation: "۱۴۰۳/۰۶/۲۱"
     */
    fun toStandardString(usePersianDigits: Boolean = true): String {
        val raw = String.format("%04d/%02d/%02d", year, month, day)
        return if (usePersianDigits) PersianNumberFormatter.toPersian(raw) else raw
    }

    /**
     * Verbose formatted representation: "۲۱ شهریور ۱۴۰۳"
     */
    fun toFullString(): String {
        val monthName = JalaliDateHelper.getMonthName(month)
        val dayStr = PersianNumberFormatter.toPersian(day.toString())
        val yearStr = PersianNumberFormatter.toPersian(year.toString())
        return "$dayStr $monthName $yearStr"
    }
}

/**
 * Comprehensive helper for Solar Hijri (Jalali / تقویم خورشیدی ایران) calculations.
 */
object JalaliDateHelper {

    val MONTH_NAMES_FA = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    val WEEKDAY_NAMES_FA = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    fun getMonthName(month: Int): String {
        return if (month in 1..12) MONTH_NAMES_FA[month - 1] else ""
    }

    /**
     * Gets the current date in Jalali calendar.
     */
    fun now(): JalaliDate {
        val cal = Calendar.getInstance()
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Converts Date to JalaliDate.
     */
    fun fromDate(date: Date): JalaliDate {
        val cal = Calendar.getInstance().apply { time = date }
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Check if a Jalali year is a leap year (سال کبیسه).
     */
    fun isJalaliLeapYear(year: Int): Boolean {
        val r = year % 33
        return r == 1 || r == 5 || r == 9 || r == 13 || r == 17 || r == 22 || r == 26 || r == 30
    }

    /**
     * Days in a given Jalali month.
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isJalaliLeapYear(year)) 30 else 29
            else -> 0
        }
    }

    /**
     * Precise conversion algorithm from Gregorian to Jalali (Solar Hijri).
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400

        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        val jd = jDayNo + 1
        return JalaliDate(jy, jm + 1, jd)
    }

    /**
     * Converts a Jalali (Solar Hijri) date to Gregorian Calendar date.
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Calendar {
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val jy2 = jy - 979
        val jm2 = jm - 1
        val jd2 = jd - 1

        var jDayNo = 365 * jy2 + (jy2 / 33) * 8 + ((jy2 % 33 + 3) / 4)
        for (i in 0 until jm2) {
            jDayNo += jDaysInMonth[i]
        }
        jDayNo += jd2

        var gDayNo = jDayNo + 79

        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524

            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        var gm = 0
        val febDays = if (leap) 29 else 28
        val daysInMonths = intArrayOf(31, febDays, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        while (gm < 12 && gDayNo >= daysInMonths[gm]) {
            gDayNo -= daysInMonths[gm]
            gm++
        }
        val gd = gDayNo + 1

        return Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, gm)
            set(Calendar.DAY_OF_MONTH, gd)
        }
    }

    /**
     * Parses a Jalali date ("1403/06/21" or "۱۴۰۳/۰۶/۲۱") and time string ("08:30" or "۰۸:۳۰") to epoch milliseconds.
     */
    fun parseJalaliDateTimeToTimestamp(dateStr: String, timeStr: String = "08:00"): Long? {
        val jDate = parseJalali(dateStr) ?: return null
        val cleanTime = timeStr
            .replace('۰', '0').replace('۱', '1').replace('۲', '2')
            .replace('۳', '3').replace('۴', '4').replace('۵', '5')
            .replace('۶', '6').replace('۷', '7').replace('۸', '8')
            .replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2')
            .replace('٣', '3').replace('٤', '4').replace('٥', '5')
            .replace('٦', '6').replace('٧', '7').replace('٨', '8')
            .replace('٩', '9')
            .trim()
        val timeParts = cleanTime.split(':')
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = jalaliToGregorian(jDate.year, jDate.month, jDate.day).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Converts a millisecond timestamp to formatted Jalali string "۱۴۰۳/۰۶/۲۱".
     */
    fun formatTimestampToJalali(timestamp: Long, usePersianDigits: Boolean = true): String {
        val jDate = fromDate(Date(timestamp))
        return jDate.toStandardString(usePersianDigits)
    }

    /**
     * Converts a Jalali date string (like "1403/06/21" or "۱۴۰۳/۰۶/۲۱") to a JalaliDate.
     */
    fun parseJalali(dateStr: String): JalaliDate? {
        if (dateStr.isBlank()) return null
        val cleaned = dateStr
            .replace('۰', '0').replace('۱', '1').replace('۲', '2')
            .replace('۳', '3').replace('۴', '4').replace('۵', '5')
            .replace('۶', '6').replace('۷', '7').replace('۸', '8')
            .replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2')
            .replace('٣', '3').replace('٤', '4').replace('٥', '5')
            .replace('٦', '6').replace('٧', '7').replace('٨', '8')
            .replace('٩', '9')
            .trim()
        val parts = cleaned.split('/', '-', '.', ' ').filter { it.isNotBlank() }
        if (parts.size != 3) return null

        var y = parts[0].toIntOrNull() ?: return null
        var m = parts[1].toIntOrNull() ?: return null
        var d = parts[2].toIntOrNull() ?: return null

        // If formatted as DD/MM/YYYY
        if (y < 100 && d > 1000) {
            val temp = y
            y = d
            d = temp
        }

        if (m !in 1..12) return null
        if (d !in 1..31) return null

        return JalaliDate(y, m, d)
    }

    /**
     * Calculates the pet age in human-friendly Persian text from Jalali birthdate.
     */
    fun calculateAgeFa(birthDateStr: String): String {
        val birth = parseJalali(birthDateStr) ?: return "نامشخص"
        val today = now()
        var years = today.year - birth.year
        var months = today.month - birth.month
        var days = today.day - birth.day
        if (days < 0) {
            months--
        }
        if (months < 0) {
            years--
            months += 12
        }
        return when {
            years > 0 && months > 0 -> "${PersianNumberFormatter.toPersian(years)} سال و ${PersianNumberFormatter.toPersian(months)} ماه"
            years > 0 -> "${PersianNumberFormatter.toPersian(years)} سال"
            months > 0 -> "${PersianNumberFormatter.toPersian(months)} ماه"
            days > 0 -> "${PersianNumberFormatter.toPersian(days)} روز"
            years == 0 && months == 0 -> "کمتر از ۱ ماه"
            else -> "نامشخص"
        }
    }

    /**
     * Gets current Persian Day of Week enum.
     */
    fun getCurrentDayOfWeekFa(calendar: Calendar = Calendar.getInstance()): red.line.pet.domain.model.DayOfWeekFa {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> red.line.pet.domain.model.DayOfWeekFa.SATURDAY
            Calendar.SUNDAY -> red.line.pet.domain.model.DayOfWeekFa.SUNDAY
            Calendar.MONDAY -> red.line.pet.domain.model.DayOfWeekFa.MONDAY
            Calendar.TUESDAY -> red.line.pet.domain.model.DayOfWeekFa.TUESDAY
            Calendar.WEDNESDAY -> red.line.pet.domain.model.DayOfWeekFa.WEDNESDAY
            Calendar.THURSDAY -> red.line.pet.domain.model.DayOfWeekFa.THURSDAY
            Calendar.FRIDAY -> red.line.pet.domain.model.DayOfWeekFa.FRIDAY
            else -> red.line.pet.domain.model.DayOfWeekFa.SATURDAY
        }
    }
}
