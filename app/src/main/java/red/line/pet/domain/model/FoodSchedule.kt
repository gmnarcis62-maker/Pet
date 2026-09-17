package red.line.pet.domain.model

enum class MealType(val titleFa: String, val defaultHour: String) {
    BREAKFAST("صبحانه", "08:00"),
    SNACK_MORNING("میان‌وعده صبح", "10:30"),
    LUNCH("ناهار", "13:30"),
    SNACK_AFTERNOON("عصرانه", "17:00"),
    DINNER("شام", "20:30"),
    SNACK_NIGHT("میان‌وعده شب", "22:30"),
    OTHER("سایر", "12:00");

    companion object {
        fun fromString(value: String): MealType {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.titleFa == value } ?: OTHER
        }
    }
}

enum class AmountUnit(val titleFa: String) {
    GRAM("گرم"),
    MILLILITER("میلی‌لیتر"),
    PIECE("عدد"),
    CUP("پیمانه"),
    OTHER("سایر");

    companion object {
        fun fromString(value: String): AmountUnit {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.titleFa == value } ?: GRAM
        }
    }
}

enum class DayOfWeekFa(val code: String, val titleFa: String, val shortTitleFa: String) {
    SATURDAY("SAT", "شنبه", "ش"),
    SUNDAY("SUN", "یکشنبه", "ی"),
    MONDAY("MON", "دوشنبه", "د"),
    TUESDAY("TUE", "سه‌شنبه", "س"),
    WEDNESDAY("WED", "چهارشنبه", "چ"),
    THURSDAY("THU", "پنجشنبه", "پ"),
    FRIDAY("FRI", "جمعه", "ج");

    companion object {
        val ALL_CODES = entries.map { it.code }.toSet()
        fun fromCode(code: String): DayOfWeekFa? = entries.find { it.code.equals(code, ignoreCase = true) }
    }
}

data class FoodSchedule(
    val id: Long = 0,
    val petId: Long,
    val mealTitle: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val foodName: String = "",
    val amount: String = "",
    val amountUnit: AmountUnit = AmountUnit.GRAM,
    val mealTime: String = "08:00",
    val morningTime: String = "",
    val eveningTime: String = "",
    val repeatDays: Set<DayOfWeekFa> = DayOfWeekFa.entries.toSet(),
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val supplementName: String = "",
    val supplementAmount: String = "",
    val supplementNotes: String = "",
    val notes: String = ""
)
