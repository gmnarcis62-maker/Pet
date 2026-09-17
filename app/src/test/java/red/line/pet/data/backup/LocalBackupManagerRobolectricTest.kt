package red.line.pet.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import red.line.pet.core.billing.FreeTrialManager
import red.line.pet.core.billing.FreeVipManager
import red.line.pet.data.local.database.RedLinePetDatabase
import red.line.pet.data.local.datastore.UserPreferencesDataStore
import red.line.pet.data.local.entity.ExpenseEntity
import red.line.pet.data.local.entity.FoodScheduleEntity
import red.line.pet.data.local.entity.HealthRecordEntity
import red.line.pet.data.local.entity.MedicalRecordEntity
import red.line.pet.data.local.entity.MemoryEntity
import red.line.pet.data.local.entity.PetEntity
import red.line.pet.data.local.entity.ReminderEntity
import red.line.pet.data.local.entity.WeightRecordEntity
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalBackupManagerRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: RedLinePetDatabase
    private lateinit var dataStore: UserPreferencesDataStore
    private lateinit var freeTrialManager: FreeTrialManager
    private lateinit var freeVipManager: FreeVipManager
    private lateinit var backupManager: LocalBackupManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, RedLinePetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataStore = UserPreferencesDataStore(context)
        freeTrialManager = FreeTrialManager(context)
        freeVipManager = FreeVipManager(dataStore, freeTrialManager)
        backupManager = LocalBackupManager(context, database, freeVipManager)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `non-vip users are strictly forbidden from backup and restore`() = runBlocking {
        // Ensure user is NOT VIP
        freeVipManager.setVipStatus(false)
        assertFalse(freeVipManager.getIsVip())
        assertFalse(freeVipManager.canBackupRestore())

        val tempFile = File(context.cacheDir, "test_backup.json").apply { createNewFile() }
        val uri = Uri.fromFile(tempFile)

        // Attempt backup -> fails
        val backupResult = backupManager.createBackup(uri)
        assertTrue(backupResult.isFailure)
        assertTrue(backupResult.exceptionOrNull() is SecurityException)

        // Attempt inspection -> invalid due to VIP requirement
        val inspectResult = backupManager.inspectAndValidateBackup(uri)
        assertTrue(inspectResult is BackupValidationResult.Invalid)

        // Attempt restore -> fails
        val emptyData = PetoraBackupData(
            summary = BackupSummary(),
            pets = emptyList(),
            medicalRecords = emptyList(),
            foodSchedules = emptyList(),
            weightRecords = emptyList(),
            expenses = emptyList(),
            memories = emptyList(),
            healthRecords = emptyList(),
            reminders = emptyList()
        )
        val restoreResult = backupManager.restoreBackup(emptyData)
        assertTrue(restoreResult.isFailure)
        assertTrue(restoreResult.exceptionOrNull() is SecurityException)
    }

    @Test
    fun `vip users can create backup, validate it, and restore atomically`() = runBlocking {
        // Grant VIP
        freeVipManager.setVipStatus(true)
        assertTrue(freeVipManager.getIsVip())
        assertTrue(freeVipManager.canBackupRestore())

        // 1. Seed initial data
        val pet1 = PetEntity(
            id = 1,
            name = "میشا",
            species = "CAT",
            breed = "پرشین",
            birthDateJalali = "1401/02/10",
            gender = "FEMALE",
            weightKg = 4.2,
            microchipId = "982000123456789",
            avatarUri = "",
            notes = "گربه آرام",
            createdAt = System.currentTimeMillis()
        )
        database.petDao().insertPet(pet1)

        val food1 = FoodScheduleEntity(
            id = 1,
            petId = 1,
            mealTitle = "صبحانه رویال کنین",
            mealType = "BREAKFAST",
            foodName = "Royal Canin Persian",
            amount = "50",
            amountUnit = "GRAM",
            mealTime = "08:30",
            morningTime = "08:30",
            eveningTime = "",
            repeatDays = "SAT,SUN,MON,TUE,WED,THU,FRI",
            isCompleted = false,
            isActive = true,
            supplementName = "مولتی ویتامین",
            supplementAmount = "1 قرص",
            supplementNotes = "",
            notes = ""
        )
        database.foodDao().insert(food1)

        val expense1 = ExpenseEntity(
            id = 1,
            petId = 1,
            title = "ویزیت دکتر",
            category = "VET",
            amountToman = 450000,
            jalaliDate = "1403/06/10",
            notes = "چکاپ دوره‌ای",
            createdAt = System.currentTimeMillis()
        )
        database.expenseDao().insertExpense(expense1)

        val reminder1 = ReminderEntity(
            id = 1,
            petId = 1,
            title = "قرص ضد انگل",
            description = "یک عدد همراه غذا",
            reminderType = "MEDICATION",
            targetId = null,
            remindTimestamp = System.currentTimeMillis() + 86400000,
            jalaliDate = "1403/06/20",
            timeString = "10:00",
            repeatType = "MONTHLY",
            isEnabled = true,
            isCompleted = false,
            createdAt = System.currentTimeMillis()
        )
        database.reminderDao().insert(reminder1)

        // 2. Perform Backup
        val backupFile = File(context.cacheDir, "petora_test_backup.json")
        val backupUri = Uri.fromFile(backupFile)

        val backupResult = backupManager.createBackup(backupUri)
        assertTrue(backupResult.isSuccess)
        val backupSummary = backupResult.getOrNull()
        assertNotNull(backupSummary)
        assertEquals(1, backupSummary!!.petsCount)
        assertEquals(1, backupSummary.foodSchedulesCount)
        assertEquals(1, backupSummary.expensesCount)
        assertEquals(1, backupSummary.remindersCount)

        // 3. Inspect and Validate Backup File
        val inspectResult = backupManager.inspectAndValidateBackup(backupUri)
        assertTrue(inspectResult is BackupValidationResult.Valid)
        val validResult = inspectResult as BackupValidationResult.Valid
        assertEquals("Petora", validResult.summary.appName)
        assertEquals(1, validResult.backupData.pets.size)
        assertEquals("میشا", validResult.backupData.pets[0].name)
        assertEquals(1, validResult.backupData.foodSchedules.size)
        assertEquals("صبحانه رویال کنین", validResult.backupData.foodSchedules[0].mealTitle)
        assertEquals(1, validResult.backupData.expenses.size)
        assertEquals(450000L, validResult.backupData.expenses[0].amountToman)
        assertEquals(1, validResult.backupData.reminders.size)
        assertEquals("قرص ضد انگل", validResult.backupData.reminders[0].title)

        // 4. Modify current database to simulate loss / other state
        database.petDao().deleteAllPets()
        database.foodDao().deleteAllFoodSchedules()
        database.expenseDao().deleteAllExpenses()
        database.reminderDao().deleteAllReminders()
        assertEquals(0, database.petDao().getAllPetsSync().size)

        // 5. Restore from the validated backup
        val restoreResult = backupManager.restoreBackup(validResult.backupData)
        assertTrue(restoreResult.isSuccess)

        // 6. Verify full recovery
        val restoredPets = database.petDao().getAllPetsSync()
        assertEquals(1, restoredPets.size)
        assertEquals("میشا", restoredPets[0].name)
        assertEquals("پرشین", restoredPets[0].breed)

        val restoredFoods = database.foodDao().getAllFoodSchedulesSync()
        assertEquals(1, restoredFoods.size)
        assertEquals("Royal Canin Persian", restoredFoods[0].foodName)

        val restoredExpenses = database.expenseDao().getAllExpensesSync()
        assertEquals(1, restoredExpenses.size)
        assertEquals(450000L, restoredExpenses[0].amountToman)

        val restoredReminders = database.reminderDao().getAllRemindersListSync()
        assertEquals(1, restoredReminders.size)
        assertEquals("قرص ضد انگل", restoredReminders[0].title)
    }

    @Test
    fun `inspecting corrupted or invalid file returns clear error`() = runBlocking {
        freeVipManager.setVipStatus(true)

        // Empty file
        val emptyFile = File(context.cacheDir, "empty.json").apply { writeText("") }
        val emptyResult = backupManager.inspectAndValidateBackup(Uri.fromFile(emptyFile))
        assertTrue(emptyResult is BackupValidationResult.Invalid)

        // Non-JSON file
        val badJsonFile = File(context.cacheDir, "bad.json").apply { writeText("Hello World Not JSON") }
        val badResult = backupManager.inspectAndValidateBackup(Uri.fromFile(badJsonFile))
        assertTrue(badResult is BackupValidationResult.Invalid)

        // JSON from another app
        val otherAppJsonFile = File(context.cacheDir, "other.json").apply {
            writeText("""{"metadata":{"appName":"OtherApp","version":1}}""")
        }
        val otherResult = backupManager.inspectAndValidateBackup(Uri.fromFile(otherAppJsonFile))
        assertTrue(otherResult is BackupValidationResult.Invalid)
    }
}
