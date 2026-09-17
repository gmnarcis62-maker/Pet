package red.line.pet.domain.model

import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.core.util.PersianNumberFormatter
import java.util.Date

/**
 * Unit for weight measurement.
 */
enum class WeightUnit(val titleFa: String, val shortFa: String) {
    KILOGRAM("کیلوگرم", "ک.گ"),
    GRAM("گرم", "گرم");

    companion object {
        fun fromString(value: String): WeightUnit {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.titleFa == value || it.shortFa == value } ?: KILOGRAM
        }
    }
}

/**
 * Time range filter for growth charts.
 */
enum class WeightTimeRange(val titleFa: String, val days: Int?) {
    LAST_7_DAYS("۷ روز", 7),
    LAST_30_DAYS("۳۰ روز", 30),
    LAST_3_MONTHS("۳ ماه", 90),
    LAST_6_MONTHS("۶ ماه", 180),
    LAST_1_YEAR("۱ سال", 365),
    ALL("همه", null)
}

/**
 * Comprehensive statistics for weight analysis.
 */
data class WeightStatistics(
    val currentWeightKg: Double = 0.0,
    val minWeightKg: Double = 0.0,
    val maxWeightKg: Double = 0.0,
    val totalChangeKg: Double = 0.0,
    val lastChangeKg: Double = 0.0,
    val totalRecordsCount: Int = 0,
    val lastRecordedDateJalali: String = ""
)

/**
 * Domain model representing a single weight log entry for a pet.
 */
data class WeightRecord(
    val id: Long = 0,
    val petId: Long,
    val weightKg: Double, // canonical internal weight in KG
    val unit: WeightUnit = WeightUnit.KILOGRAM,
    val displayWeight: Double = weightKg,
    val jalaliDate: String = "",
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
) {
    // Backwards compatibility property
    val weight: Double get() = weightKg

    /**
     * Formats weight for user display (Persian numbers, appropriate unit, no trailing zeros).
     */
    fun getFormattedWeight(): String {
        return if (unit == WeightUnit.GRAM) {
            val grams = (weightKg * 1000).toInt()
            "${PersianNumberFormatter.toPersian(grams)} ${unit.titleFa}"
        } else {
            val formatted = if (weightKg % 1.0 == 0.0) {
                weightKg.toInt().toString()
            } else {
                String.format("%.2f", weightKg).trimEnd('0').trimEnd('.')
            }
            "${PersianNumberFormatter.toPersian(formatted)} ${unit.titleFa}"
        }
    }

    /**
     * Returns a clean Jalali date representation.
     */
    fun getEffectiveJalaliDate(): String {
        return if (jalaliDate.isNotBlank()) {
            jalaliDate
        } else {
            JalaliDateHelper.fromDate(Date(date)).toStandardString()
        }
    }
}
