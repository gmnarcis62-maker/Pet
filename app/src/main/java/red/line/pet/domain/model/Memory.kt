package red.line.pet.domain.model

import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.core.util.PersianNumberFormatter

enum class MemorySortType(val titleFa: String) {
    NEWEST("جدیدترین"),
    OLDEST("قدیمی‌ترین")
}

data class Memory(
    val id: Long = 0,
    val petId: Long,
    val imagePath: String,
    val title: String,
    val description: String = "",
    val date: Long = System.currentTimeMillis()
) {
    /**
     * Formats the timestamp to a clean Solar Hijri (Jalali) date string.
     */
    fun getFormattedJalaliDate(): String {
        return JalaliDateHelper.formatTimestampToJalali(date)
    }

    /**
     * Checks if this memory matches the given search query.
     */
    fun matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return title.lowercase().contains(q) || description.lowercase().contains(q)
    }
}
