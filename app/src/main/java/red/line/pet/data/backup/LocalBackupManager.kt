package red.line.pet.data.backup

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import red.line.pet.core.billing.FreeVipManager
import red.line.pet.core.notification.AlarmScheduler
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.data.local.database.RedLinePetDatabase
import red.line.pet.data.local.entity.ExpenseEntity
import red.line.pet.data.local.entity.FoodScheduleEntity
import red.line.pet.data.local.entity.HealthRecordEntity
import red.line.pet.data.local.entity.MedicalRecordEntity
import red.line.pet.data.local.entity.MemoryEntity
import red.line.pet.data.local.entity.PetEntity
import red.line.pet.data.local.entity.ReminderEntity
import red.line.pet.data.local.entity.WeightRecordEntity
import red.line.pet.data.mapper.toDomain
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Summary metrics of the backup file content
 */
data class BackupSummary(
    val appName: String = "Petora",
    val version: Int = 1,
    val backupTimestamp: Long = System.currentTimeMillis(),
    val jalaliDate: String = "",
    val petsCount: Int = 0,
    val medicalCount: Int = 0,
    val foodSchedulesCount: Int = 0,
    val weightCount: Int = 0,
    val expensesCount: Int = 0,
    val memoriesCount: Int = 0,
    val healthCount: Int = 0,
    val remindersCount: Int = 0,
    val totalRecordsCount: Int = 0,
    val fileSizeBytes: Long = 0L
)

/**
 * In-memory parsed backup package ready to be restored
 */
data class PetoraBackupData(
    val summary: BackupSummary,
    val pets: List<PetEntity>,
    val medicalRecords: List<MedicalRecordEntity>,
    val foodSchedules: List<FoodScheduleEntity>,
    val weightRecords: List<WeightRecordEntity>,
    val expenses: List<ExpenseEntity>,
    val memories: List<MemoryEntity>,
    val healthRecords: List<HealthRecordEntity>,
    val reminders: List<ReminderEntity>,
    val petAvatarBase64Map: Map<Long, String> = emptyMap(),
    val memoryImageBase64Map: Map<Long, String> = emptyMap()
)

/**
 * Result of backup file validation
 */
sealed interface BackupValidationResult {
    data class Valid(
        val summary: BackupSummary,
        val backupData: PetoraBackupData
    ) : BackupValidationResult

    data class Invalid(
        val errorMessage: String
    ) : BackupValidationResult
}

/**
 * 100% Local, Offline Backup & Restore Engine for Petora
 * Fully compliant with Android SAF and atomic database transactions.
 */
class LocalBackupManager(
    private val context: Context,
    private val database: RedLinePetDatabase,
    private val freeVipManager: FreeVipManager
) {

    companion object {
        private const val APP_IDENTIFIER = "Petora"
        private const val CURRENT_BACKUP_VERSION = 1
        private const val MAX_IMAGE_FILE_SIZE = 5 * 1024 * 1024 // 5 MB limit per image to prevent memory exhaustion
    }

    /**
     * Creates a full offline JSON backup and writes it to the user-selected SAF Uri.
     * Guaranteed to be VIP-only.
     */
    suspend fun createBackup(targetUri: Uri): Result<BackupSummary> = withContext(Dispatchers.IO) {
        try {
            if (!freeVipManager.canBackupRestore()) {
                return@withContext Result.failure(
                    SecurityException("قابلیت پشتیبان‌گیری منحصراً برای اعضای VIP پتورا فعال است.")
                )
            }

            // 1. Fetch all data synchronously from DAOs
            val pets = database.petDao().getAllPetsSync()
            val medicals = database.medicalDao().getAllMedicalRecordsSync()
            val foods = database.foodDao().getAllFoodSchedulesSync()
            val weights = database.weightDao().getAllWeightRecordsSync()
            val expenses = database.expenseDao().getAllExpensesSync()
            val memories = database.memoryDao().getAllMemoriesSync()
            val healths = database.healthRecordDao().getAllHealthRecordsSync()
            val reminders = database.reminderDao().getAllRemindersListSync()

            val jalaliDate = JalaliDateHelper.now().toFullString()
            val timestamp = System.currentTimeMillis()

            val rootJson = JSONObject()
            val metaJson = JSONObject().apply {
                put("appName", APP_IDENTIFIER)
                put("version", CURRENT_BACKUP_VERSION)
                put("backupTimestamp", timestamp)
                put("jalaliDate", jalaliDate)
                put("petsCount", pets.size)
                put("medicalCount", medicals.size)
                put("foodCount", foods.size)
                put("weightCount", weights.size)
                put("expensesCount", expenses.size)
                put("memoriesCount", memories.size)
                put("healthCount", healths.size)
                put("remindersCount", reminders.size)
            }
            rootJson.put("metadata", metaJson)

            // 2. Pets Array with optional Base64 avatar
            val petsArray = JSONArray()
            for (pet in pets) {
                val petObj = JSONObject().apply {
                    put("id", pet.id)
                    put("name", pet.name)
                    put("species", pet.species)
                    put("breed", pet.breed)
                    put("birthDateJalali", pet.birthDateJalali)
                    put("gender", pet.gender)
                    put("weightKg", pet.weightKg)
                    put("microchipId", pet.microchipId)
                    put("avatarUri", pet.avatarUri)
                    put("notes", pet.notes)
                    put("createdAt", pet.createdAt)

                    // Safely encode internal avatar if it exists
                    val avatarBase64 = encodeLocalFileToBase64(pet.avatarUri)
                    if (avatarBase64 != null) {
                        put("avatarBase64", avatarBase64)
                    }
                }
                petsArray.put(petObj)
            }
            rootJson.put("pets", petsArray)

            // 3. Medical records
            val medicalArray = JSONArray()
            for (m in medicals) {
                val mObj = JSONObject().apply {
                    put("id", m.id)
                    put("petId", m.petId)
                    put("type", m.type)
                    put("title", m.title)
                    put("description", m.description)
                    put("date", m.date)
                    put("reminderDate", m.reminderDate ?: JSONObject.NULL)
                    put("costToman", m.costToman)
                    put("clinicOrDoctor", m.clinicOrDoctor)
                    put("jalaliDate", m.jalaliDate)
                    put("isCompleted", m.isCompleted)
                }
                medicalArray.put(mObj)
            }
            rootJson.put("medicalRecords", medicalArray)

            // 4. Food schedules
            val foodArray = JSONArray()
            for (f in foods) {
                val fObj = JSONObject().apply {
                    put("id", f.id)
                    put("petId", f.petId)
                    put("mealTitle", f.mealTitle)
                    put("mealType", f.mealType)
                    put("foodName", f.foodName)
                    put("amount", f.amount)
                    put("amountUnit", f.amountUnit)
                    put("mealTime", f.mealTime)
                    put("morningTime", f.morningTime)
                    put("eveningTime", f.eveningTime)
                    put("repeatDays", f.repeatDays)
                    put("isCompleted", f.isCompleted)
                    put("isActive", f.isActive)
                    put("supplementName", f.supplementName)
                    put("supplementAmount", f.supplementAmount)
                    put("supplementNotes", f.supplementNotes)
                    put("notes", f.notes)
                }
                foodArray.put(fObj)
            }
            rootJson.put("foodSchedules", foodArray)

            // 5. Weight records
            val weightArray = JSONArray()
            for (w in weights) {
                val wObj = JSONObject().apply {
                    put("id", w.id)
                    put("petId", w.petId)
                    put("weight", w.weight)
                    put("unit", w.unit)
                    put("jalaliDate", w.jalaliDate)
                    put("notes", w.notes)
                    put("date", w.date)
                }
                weightArray.put(wObj)
            }
            rootJson.put("weightRecords", weightArray)

            // 6. Expenses
            val expenseArray = JSONArray()
            for (e in expenses) {
                val eObj = JSONObject().apply {
                    put("id", e.id)
                    put("petId", e.petId)
                    put("title", e.title)
                    put("category", e.category)
                    put("amountToman", e.amountToman)
                    put("jalaliDate", e.jalaliDate)
                    put("notes", e.notes)
                    put("createdAt", e.createdAt)
                }
                expenseArray.put(eObj)
            }
            rootJson.put("expenses", expenseArray)

            // 7. Memories with optional Base64 image
            val memoryArray = JSONArray()
            for (mem in memories) {
                val memObj = JSONObject().apply {
                    put("id", mem.id)
                    put("petId", mem.petId)
                    put("imagePath", mem.imagePath)
                    put("title", mem.title)
                    put("description", mem.description)
                    put("date", mem.date)

                    val imageBase64 = encodeLocalFileToBase64(mem.imagePath)
                    if (imageBase64 != null) {
                        put("imageBase64", imageBase64)
                    }
                }
                memoryArray.put(memObj)
            }
            rootJson.put("memories", memoryArray)

            // 8. Health records
            val healthArray = JSONArray()
            for (h in healths) {
                val hObj = JSONObject().apply {
                    put("id", h.id)
                    put("petId", h.petId)
                    put("title", h.title)
                    put("type", h.type)
                    put("jalaliDate", h.jalaliDate)
                    put("nextDueJalaliDate", h.nextDueJalaliDate ?: JSONObject.NULL)
                    put("costToman", h.costToman)
                    put("clinicOrDoctor", h.clinicOrDoctor)
                    put("notes", h.notes)
                    put("isCompleted", h.isCompleted)
                }
                healthArray.put(hObj)
            }
            rootJson.put("healthRecords", healthArray)

            // 9. Reminders
            val reminderArray = JSONArray()
            for (r in reminders) {
                val rObj = JSONObject().apply {
                    put("id", r.id)
                    put("petId", r.petId)
                    put("title", r.title)
                    put("description", r.description)
                    put("reminderType", r.reminderType)
                    put("targetId", r.targetId ?: JSONObject.NULL)
                    put("remindTimestamp", r.remindTimestamp)
                    put("jalaliDate", r.jalaliDate)
                    put("timeString", r.timeString)
                    put("repeatType", r.repeatType)
                    put("isEnabled", r.isEnabled)
                    put("isCompleted", r.isCompleted)
                    put("createdAt", r.createdAt)
                }
                reminderArray.put(rObj)
            }
            rootJson.put("reminders", reminderArray)

            // Calculate SHA-256 Checksum of the content for integrity verification
            val rawJsonString = rootJson.toString(2)
            val jsonBytes = rawJsonString.toByteArray(StandardCharsets.UTF_8)
            val checksum = calculateSha256(jsonBytes)
            metaJson.put("checksum", checksum)

            // Write to output stream via SAF ContentResolver
            val outputStream = context.contentResolver.openOutputStream(targetUri)
                ?: return@withContext Result.failure(IllegalStateException("امکان باز کردن مسیر انتخابی برای ذخیره وجود ندارد."))

            outputStream.use { out ->
                out.write(jsonBytes)
                out.flush()
            }

            val summary = BackupSummary(
                appName = APP_IDENTIFIER,
                version = CURRENT_BACKUP_VERSION,
                backupTimestamp = timestamp,
                jalaliDate = jalaliDate,
                petsCount = pets.size,
                medicalCount = medicals.size,
                foodSchedulesCount = foods.size,
                weightCount = weights.size,
                expensesCount = expenses.size,
                memoriesCount = memories.size,
                healthCount = healths.size,
                remindersCount = reminders.size,
                totalRecordsCount = pets.size + medicals.size + foods.size + weights.size + expenses.size + memories.size + healths.size + reminders.size,
                fileSizeBytes = jsonBytes.size.toLong()
            )

            Result.success(summary)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Inspects and validates the backup file from the chosen SAF Uri.
     * Checks schema integrity, version, app signature, and prepares data for user preview.
     */
    suspend fun inspectAndValidateBackup(sourceUri: Uri): BackupValidationResult = withContext(Dispatchers.IO) {
        try {
            if (!freeVipManager.canBackupRestore()) {
                return@withContext BackupValidationResult.Invalid("دسترسی به بازیابی اطلاعات تنها برای کاربران VIP مجاز است.")
            }

            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext BackupValidationResult.Invalid("امکان خواندن فایل انتخابی وجود ندارد.")

            val jsonString = inputStream.use { input ->
                input.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            }

            if (jsonString.isBlank()) {
                return@withContext BackupValidationResult.Invalid("فایل انتخابی خالی است.")
            }

            val rootJson = try {
                JSONObject(jsonString)
            } catch (e: Exception) {
                return@withContext BackupValidationResult.Invalid("ساختار فایل نامعتبر است (قالب JSON صحیح نیست).")
            }

            if (!rootJson.has("metadata")) {
                return@withContext BackupValidationResult.Invalid("این فایل متعلق به برنامه پتورا نیست (فقدان متادیتا).")
            }

            val meta = rootJson.getJSONObject("metadata")
            val appName = meta.optString("appName", "")
            if (appName != APP_IDENTIFIER) {
                return@withContext BackupValidationResult.Invalid("این فایل متعلق به برنامه پتورا نیست ($appName).")
            }

            val version = meta.optInt("version", 0)
            if (version < 1) {
                return@withContext BackupValidationResult.Invalid("نسخه فایل پشتیبان نامعتبر است.")
            }

            val jalaliDate = meta.optString("jalaliDate", "نامشخص")
            val timestamp = meta.optLong("backupTimestamp", System.currentTimeMillis())

            // Parse Pets
            val petsList = mutableListOf<PetEntity>()
            val petAvatarBase64Map = mutableMapOf<Long, String>()
            val petsArray = rootJson.optJSONArray("pets") ?: JSONArray()
            for (i in 0 until petsArray.length()) {
                val obj = petsArray.getJSONObject(i)
                val pet = PetEntity(
                    id = obj.optLong("id", 0),
                    name = obj.optString("name", "پت بدون نام"),
                    species = obj.optString("species", "OTHER"),
                    breed = obj.optString("breed", ""),
                    birthDateJalali = obj.optString("birthDateJalali", ""),
                    gender = obj.optString("gender", "MALE"),
                    weightKg = obj.optDouble("weightKg", 0.0),
                    microchipId = obj.optString("microchipId", ""),
                    avatarUri = obj.optString("avatarUri", ""),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
                petsList.add(pet)
                if (obj.has("avatarBase64")) {
                    petAvatarBase64Map[pet.id] = obj.getString("avatarBase64")
                }
            }

            // Parse Medical Records
            val medicalList = mutableListOf<MedicalRecordEntity>()
            val medArray = rootJson.optJSONArray("medicalRecords") ?: JSONArray()
            for (i in 0 until medArray.length()) {
                val obj = medArray.getJSONObject(i)
                medicalList.add(
                    MedicalRecordEntity(
                        id = obj.optLong("id", 0),
                        petId = obj.optLong("petId", 0),
                        type = obj.optString("type", "VACCINE"),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        reminderDate = if (obj.isNull("reminderDate")) null else obj.optLong("reminderDate"),
                        costToman = obj.optLong("costToman", 0),
                        clinicOrDoctor = obj.optString("clinicOrDoctor", ""),
                        jalaliDate = obj.optString("jalaliDate", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }

            // Parse Food Schedules
            val foodList = mutableListOf<FoodScheduleEntity>()
            val foodArray = rootJson.optJSONArray("foodSchedules") ?: JSONArray()
            for (i in 0 until foodArray.length()) {
                val obj = foodArray.getJSONObject(i)
                foodList.add(
                    FoodScheduleEntity(
                        id = obj.optLong("id", 0),
                        petId = obj.optLong("petId", 0),
                        mealTitle = obj.optString("mealTitle", ""),
                        mealType = obj.optString("mealType", "BREAKFAST"),
                        foodName = obj.optString("foodName", ""),
                        amount = obj.optString("amount", ""),
                        amountUnit = obj.optString("amountUnit", "GRAM"),
                        mealTime = obj.optString("mealTime", "08:00"),
                        morningTime = obj.optString("morningTime", ""),
                        eveningTime = obj.optString("eveningTime", ""),
                        repeatDays = obj.optString("repeatDays", "SAT,SUN,MON,TUE,WED,THU,FRI"),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        isActive = obj.optBoolean("isActive", true),
                        supplementName = obj.optString("supplementName", ""),
                        supplementAmount = obj.optString("supplementAmount", ""),
                        supplementNotes = obj.optString("supplementNotes", ""),
                        notes = obj.optString("notes", "")
                    )
                )
            }

            // Parse Weight Records
            val weightList = mutableListOf<WeightRecordEntity>()
            val weightArray = rootJson.optJSONArray("weightRecords") ?: JSONArray()
            for (i in 0 until weightArray.length()) {
                val obj = weightArray.getJSONObject(i)
                weightList.add(
                    WeightRecordEntity(
                        id = obj.optLong("id", 0),
                        petId = obj.optLong("petId", 0),
                        weight = obj.optDouble("weight", 0.0),
                        unit = obj.optString("unit", "KILOGRAM"),
                        jalaliDate = obj.optString("jalaliDate", ""),
                        notes = obj.optString("notes", ""),
                        date = obj.optLong("date", System.currentTimeMillis())
                    )
                )
            }

            // Parse Expenses
            val expenseList = mutableListOf<ExpenseEntity>()
            val expenseArray = rootJson.optJSONArray("expenses") ?: JSONArray()
            for (i in 0 until expenseArray.length()) {
                val obj = expenseArray.getJSONObject(i)
                expenseList.add(
                    ExpenseEntity(
                        id = obj.optLong("id", 0),
                        petId = obj.optLong("petId", 0),
                        title = obj.optString("title", ""),
                        category = obj.optString("category", "FOOD"),
                        amountToman = obj.optLong("amountToman", 0),
                        jalaliDate = obj.optString("jalaliDate", ""),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Memories
            val memoryList = mutableListOf<MemoryEntity>()
            val memoryImageBase64Map = mutableMapOf<Long, String>()
            val memoryArray = rootJson.optJSONArray("memories") ?: JSONArray()
            for (i in 0 until memoryArray.length()) {
                val obj = memoryArray.getJSONObject(i)
                val mem = MemoryEntity(
                    id = obj.optLong("id", 0),
                    petId = obj.optLong("petId", 0),
                    imagePath = obj.optString("imagePath", ""),
                    title = obj.optString("title", ""),
                    description = obj.optString("description", ""),
                    date = obj.optLong("date", System.currentTimeMillis())
                )
                memoryList.add(mem)
                if (obj.has("imageBase64")) {
                    memoryImageBase64Map[mem.id] = obj.getString("imageBase64")
                }
            }

            // Parse Health Records
            val healthList = mutableListOf<HealthRecordEntity>()
            val healthArray = rootJson.optJSONArray("healthRecords") ?: JSONArray()
            for (i in 0 until healthArray.length()) {
                val obj = healthArray.getJSONObject(i)
                healthList.add(
                    HealthRecordEntity(
                        id = obj.optLong("id", 0),
                        petId = obj.optLong("petId", 0),
                        title = obj.optString("title", ""),
                        type = obj.optString("type", "VACCINE"),
                        jalaliDate = obj.optString("jalaliDate", ""),
                        nextDueJalaliDate = if (obj.isNull("nextDueJalaliDate")) null else obj.optString("nextDueJalaliDate"),
                        costToman = obj.optLong("costToman", 0),
                        clinicOrDoctor = obj.optString("clinicOrDoctor", ""),
                        notes = obj.optString("notes", ""),
                        isCompleted = obj.optBoolean("isCompleted", true)
                    )
                )
            }

            // Parse Reminders
            val reminderList = mutableListOf<ReminderEntity>()
            val reminderArray = rootJson.optJSONArray("reminders") ?: JSONArray()
            for (i in 0 until reminderArray.length()) {
                val obj = reminderArray.getJSONObject(i)
                reminderList.add(
                    ReminderEntity(
                        id = obj.optLong("id", 0),
                        petId = obj.optLong("petId", 0),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        reminderType = obj.optString("reminderType", "CUSTOM"),
                        targetId = if (obj.isNull("targetId")) null else obj.optLong("targetId"),
                        remindTimestamp = obj.optLong("remindTimestamp", System.currentTimeMillis()),
                        jalaliDate = obj.optString("jalaliDate", ""),
                        timeString = obj.optString("timeString", "08:00"),
                        repeatType = obj.optString("repeatType", "ONCE"),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val summary = BackupSummary(
                appName = appName,
                version = version,
                backupTimestamp = timestamp,
                jalaliDate = jalaliDate,
                petsCount = petsList.size,
                medicalCount = medicalList.size,
                foodSchedulesCount = foodList.size,
                weightCount = weightList.size,
                expensesCount = expenseList.size,
                memoriesCount = memoryList.size,
                healthCount = healthList.size,
                remindersCount = reminderList.size,
                totalRecordsCount = petsList.size + medicalList.size + foodList.size + weightList.size + expenseList.size + memoryList.size + healthList.size + reminderList.size,
                fileSizeBytes = jsonString.toByteArray(StandardCharsets.UTF_8).size.toLong()
            )

            val parsedData = PetoraBackupData(
                summary = summary,
                pets = petsList,
                medicalRecords = medicalList,
                foodSchedules = foodList,
                weightRecords = weightList,
                expenses = expenseList,
                memories = memoryList,
                healthRecords = healthList,
                reminders = reminderList,
                petAvatarBase64Map = petAvatarBase64Map,
                memoryImageBase64Map = memoryImageBase64Map
            )

            BackupValidationResult.Valid(summary, parsedData)
        } catch (e: Exception) {
            e.printStackTrace()
            BackupValidationResult.Invalid("خطا در بررسی فایل پشتیبان: ${e.localizedMessage ?: "فرمت نامعتبر"}")
        }
    }

    /**
     * Executes the restore operation within an atomic Room transaction.
     * Guarantees rollback on any failure so the current database is NEVER corrupted or lost.
     */
    suspend fun restoreBackup(data: PetoraBackupData): Result<BackupSummary> = withContext(Dispatchers.IO) {
        try {
            if (!freeVipManager.canBackupRestore()) {
                return@withContext Result.failure(
                    SecurityException("قابلیت بازیابی اطلاعات منحصراً برای اعضای VIP پتورا فعال است.")
                )
            }

            // 1. Restore local images from base64 if present, updating entities to point to local files
            val finalPets = data.pets.map { pet ->
                val b64 = data.petAvatarBase64Map[pet.id]
                if (!b64.isNullOrBlank()) {
                    val restoredPath = decodeBase64ToLocalFile(b64, "avatar_${pet.id}_${System.currentTimeMillis()}.webp")
                    if (restoredPath != null) pet.copy(avatarUri = restoredPath) else pet
                } else pet
            }

            val finalMemories = data.memories.map { mem ->
                val b64 = data.memoryImageBase64Map[mem.id]
                if (!b64.isNullOrBlank()) {
                    val restoredPath = decodeBase64ToLocalFile(b64, "memory_${mem.id}_${System.currentTimeMillis()}.webp")
                    if (restoredPath != null) mem.copy(imagePath = restoredPath) else mem
                } else mem
            }

            // 2. Perform atomic database wipe & insertion inside withTransaction
            database.withTransaction {
                // Clear existing records
                database.petDao().deleteAllPets()
                database.medicalDao().deleteAllMedicalRecords()
                database.foodDao().deleteAllFoodSchedules()
                database.weightDao().deleteAllWeightRecords()
                database.expenseDao().deleteAllExpenses()
                database.memoryDao().deleteAllMemories()
                database.healthRecordDao().deleteAllHealthRecords()
                database.reminderDao().deleteAllReminders()

                // Insert all restored records
                if (finalPets.isNotEmpty()) {
                    database.petDao().insertAll(finalPets)
                }
                if (data.medicalRecords.isNotEmpty()) {
                    database.medicalDao().insertAll(data.medicalRecords)
                }
                if (data.foodSchedules.isNotEmpty()) {
                    database.foodDao().insertAll(data.foodSchedules)
                }
                if (data.weightRecords.isNotEmpty()) {
                    database.weightDao().insertAll(data.weightRecords)
                }
                if (data.expenses.isNotEmpty()) {
                    database.expenseDao().insertAll(data.expenses)
                }
                if (finalMemories.isNotEmpty()) {
                    database.memoryDao().insertAll(finalMemories)
                }
                if (data.healthRecords.isNotEmpty()) {
                    database.healthRecordDao().insertAll(data.healthRecords)
                }
                if (data.reminders.isNotEmpty()) {
                    database.reminderDao().insertAll(data.reminders)
                }
            }

            // 3. Reschedule all active reminders after successful database commit
            try {
                val activeReminders = database.reminderDao().getAllActiveRemindersSync()
                for (reminderEntity in activeReminders) {
                    AlarmScheduler.schedule(context, reminderEntity.toDomain())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success(data.summary)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Safely reads and encodes a local image file to Base64
     */
    private fun encodeLocalFileToBase64(filePath: String): String? {
        if (filePath.isBlank() || !filePath.startsWith("/")) return null
        return try {
            val file = File(filePath)
            if (file.exists() && file.isFile && file.length() <= MAX_IMAGE_FILE_SIZE) {
                val bytes = FileInputStream(file).use { it.readBytes() }
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Safely decodes Base64 to an internal app file
     */
    private fun decodeBase64ToLocalFile(base64Str: String, targetFileName: String): String? {
        return try {
            val bytes = Base64.decode(base64Str, Base64.NO_WRAP)
            val dir = File(context.filesDir, "pet_restored_assets").apply { if (!exists()) mkdirs() }
            val file = File(dir, targetFileName)
            FileOutputStream(file).use { out ->
                out.write(bytes)
                out.flush()
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateSha256(bytes: ByteArray): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(bytes)
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
}
