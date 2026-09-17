package red.line.pet.domain.model

enum class ExpenseCategory(val titleFa: String, val emoji: String) {
    FOOD("غذا و خوراک", "🍖"),
    VET("ویزیت دامپزشک", "🩺"),
    MEDICINE("دارو و درمان", "💊"),
    ACCESSORIES("وسایل و لوازم", "🧸"),
    HEALTH("بهداشت و درمان", "💊"),
    ACCESSORY("اسباب‌بازی و وسایل", "🧸"),
    GROOMING("آرایش و شستشو", "✂️"),
    CLINIC("کلینیک دامپزشکی", "🩺"),
    OTHER("سایر موارد", "📦")
}

data class Expense(
    val id: Long = 0,
    val petId: Long,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val title: String,
    val amount: Long = 0L,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val amountToman: Long = amount,
    val jalaliDate: String = "",
    val notes: String = description
)

data class ExpenseSummary(
    val totalAmountToman: Long,
    val categoryAmounts: Map<ExpenseCategory, Long>
)
