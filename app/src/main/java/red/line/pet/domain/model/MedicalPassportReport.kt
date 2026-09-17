package red.line.pet.domain.model

enum class PassportSection(val titleFa: String, val descriptionFa: String) {
    HEALTH("پرونده سلامت", "سوابق واکسیناسیون، داروها و چکاپ‌ها"),
    WEIGHT("پایش وزن و رشد", "خلاصه تغییرات وزن و نمودار رشد"),
    FOOD("برنامه تغذیه", "وعده‌ها، ساعات و مکمل‌های فعال"),
    REMINDERS("یادآورها و مراقبت", "نوبت‌ها و هشدارهای پیش‌رو"),
    MEMORIES("آلبوم خاطرات", "تصاویر و یادداشت‌های برگزیده"),
    EXPENSES("خلاصه هزینه‌ها", "مجموع مخارج سلامت و کلینیک")
}

data class PassportSectionsConfig(
    val includeHealth: Boolean = true,
    val includeWeight: Boolean = true,
    val includeFood: Boolean = true,
    val includeReminders: Boolean = true,
    val includeMemories: Boolean = true,
    val includeExpenses: Boolean = true
) {
    fun isSectionIncluded(section: PassportSection): Boolean {
        return when (section) {
            PassportSection.HEALTH -> includeHealth
            PassportSection.WEIGHT -> includeWeight
            PassportSection.FOOD -> includeFood
            PassportSection.REMINDERS -> includeReminders
            PassportSection.MEMORIES -> includeMemories
            PassportSection.EXPENSES -> includeExpenses
        }
    }

    fun toggleSection(section: PassportSection): PassportSectionsConfig {
        return when (section) {
            PassportSection.HEALTH -> copy(includeHealth = !includeHealth)
            PassportSection.WEIGHT -> copy(includeWeight = !includeWeight)
            PassportSection.FOOD -> copy(includeFood = !includeFood)
            PassportSection.REMINDERS -> copy(includeReminders = !includeReminders)
            PassportSection.MEMORIES -> copy(includeMemories = !includeMemories)
            PassportSection.EXPENSES -> copy(includeExpenses = !includeExpenses)
        }
    }

    val selectedCount: Int
        get() = listOf(includeHealth, includeWeight, includeFood, includeReminders, includeMemories, includeExpenses).count { it }
}

data class MedicalPassportReport(
    val pet: Pet,
    val healthRecords: List<HealthRecord> = emptyList(),
    val weightRecords: List<WeightRecord> = emptyList(),
    val weightStats: WeightStatistics = WeightStatistics(),
    val foodSchedules: List<FoodSchedule> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val memories: List<Memory> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val totalExpenseAmount: Long = 0L,
    val healthExpenseAmount: Long = 0L,
    val generatedAtTimestamp: Long = System.currentTimeMillis(),
    val generatedAtJalali: String = "",
    val ageStringFa: String = ""
)
