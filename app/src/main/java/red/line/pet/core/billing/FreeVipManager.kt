package red.line.pet.core.billing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import red.line.pet.data.local.datastore.UserPreferencesDataStore

/**
 * Features governed by Free vs. VIP tier access control.
 */
enum class VipFeature(val faTitle: String, val freeLimitText: String) {
    PETS("حیوانات خانگی", "حداکثر ۱ حیوان در نسخه رایگان"),
    HEALTH("پرونده سلامت", "حداکثر ۱۰ رکورد سلامت در نسخه رایگان"),
    FOOD("برنامه غذایی", "حداکثر ۵ برنامه غذایی در نسخه رایگان"),
    WEIGHT("ثبت وزن", "حداکثر ۱۰ رکورد وزن در نسخه رایگان"),
    MEMORIES("آلبوم خاطرات", "حداکثر ۵ تصویر در نسخه رایگان"),
    REMINDERS("یادآورها", "حداکثر ۵ یادآور در نسخه رایگان"),
    PDF_EXPORT("خروجی کامل PDF", "فقط مخصوص اعضای VIP"),
    BACKUP_RESTORE("پشتیبان‌گیری و بازیابی", "فقط مخصوص اعضای VIP")
}

/**
 * Central Access Control Manager for Petora Free vs VIP tiers.
 * Reads and writes securely to DataStore.
 */
class FreeVipManager(
    private val dataStore: UserPreferencesDataStore,
    val freeTrialManager: FreeTrialManager? = null
) {
    companion object {
        const val MAX_FREE_PETS = 1
        const val MAX_FREE_HEALTH_RECORDS = 10
        const val MAX_FREE_FOOD_SCHEDULES = 5
        const val MAX_FREE_WEIGHT_RECORDS = 10
        const val MAX_FREE_MEMORIES = 5
        const val MAX_FREE_REMINDERS = 5
    }

    val isVip: Flow<Boolean> = dataStore.isVip
    val purchaseToken: Flow<String?> = dataStore.purchaseToken
    val purchaseDate: Flow<String?> = dataStore.purchaseDate

    suspend fun getIsVip(): Boolean = isVip.first()

    /**
     * True if user has purchased VIP OR is currently within the 30-day free trial period.
     */
    suspend fun isFullyUnlocked(): Boolean {
        if (getIsVip()) return true
        if (freeTrialManager?.isTrialActive() == true) return true
        return false
    }

    suspend fun setVipStatus(isVip: Boolean, token: String? = null, date: String? = null) {
        dataStore.setVipStatus(isVip, token, date)
    }

    suspend fun canAddPet(currentCount: Int): Boolean {
        if (isFullyUnlocked()) return true
        return currentCount < MAX_FREE_PETS
    }

    suspend fun canAddHealthRecord(currentCount: Int): Boolean {
        if (isFullyUnlocked()) return true
        return currentCount < MAX_FREE_HEALTH_RECORDS
    }

    suspend fun canAddFoodSchedule(currentCount: Int): Boolean {
        if (isFullyUnlocked()) return true
        return currentCount < MAX_FREE_FOOD_SCHEDULES
    }

    suspend fun canAddWeightRecord(currentCount: Int): Boolean {
        if (isFullyUnlocked()) return true
        return currentCount < MAX_FREE_WEIGHT_RECORDS
    }

    suspend fun canAddMemory(currentCount: Int): Boolean {
        if (isFullyUnlocked()) return true
        return currentCount < MAX_FREE_MEMORIES
    }

    suspend fun canAddReminder(currentCount: Int): Boolean {
        if (isFullyUnlocked()) return true
        return currentCount < MAX_FREE_REMINDERS
    }

    suspend fun canExportPdf(): Boolean {
        return isFullyUnlocked()
    }

    suspend fun canBackupRestore(): Boolean {
        // Backup / Restore is strictly locked for free users, even within the 30-day trial
        return getIsVip()
    }

    suspend fun checkAccess(feature: VipFeature, currentCount: Int = 0): Boolean {
        return when (feature) {
            VipFeature.PETS -> canAddPet(currentCount)
            VipFeature.HEALTH -> canAddHealthRecord(currentCount)
            VipFeature.FOOD -> canAddFoodSchedule(currentCount)
            VipFeature.WEIGHT -> canAddWeightRecord(currentCount)
            VipFeature.MEMORIES -> canAddMemory(currentCount)
            VipFeature.REMINDERS -> canAddReminder(currentCount)
            VipFeature.PDF_EXPORT -> canExportPdf()
            VipFeature.BACKUP_RESTORE -> canBackupRestore()
        }
    }
}
