package red.line.pet.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import red.line.pet.domain.model.Expense
import red.line.pet.domain.model.ExpenseCategory
import red.line.pet.domain.model.ExpenseSummary
import red.line.pet.domain.model.FoodSchedule
import red.line.pet.domain.model.HealthRecord
import red.line.pet.domain.model.HealthRecordType
import red.line.pet.domain.model.Memory
import red.line.pet.domain.model.PassportSection
import red.line.pet.domain.model.PassportSectionsConfig
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.PetGender
import red.line.pet.domain.model.PetSpecies
import red.line.pet.domain.model.Reminder
import red.line.pet.domain.model.ReminderType
import red.line.pet.domain.model.WeightRecord
import red.line.pet.domain.model.WeightUnit
import red.line.pet.domain.repository.ExpenseRepository
import red.line.pet.domain.repository.FoodRepository
import red.line.pet.domain.repository.HealthRecordRepository
import red.line.pet.domain.repository.MemoryRepository
import red.line.pet.domain.repository.PetRepository
import red.line.pet.domain.repository.ReminderRepository
import red.line.pet.domain.repository.WeightRepository
import red.line.pet.domain.usecase.passport.GetPetMedicalReportUseCase

class MedicalPassportReportTest {

    private class FakePetRepository : PetRepository {
        val pets = mutableListOf<Pet>()
        override fun getAllPets(): Flow<List<Pet>> = flowOf(pets)
        override fun getPetById(id: Long): Flow<Pet?> = flowOf(pets.find { it.id == id })
        override suspend fun insertPet(pet: Pet): Long { pets.add(pet); return pet.id }
        override suspend fun updatePet(pet: Pet) {}
        override suspend fun deletePet(pet: Pet) {}
        override suspend fun deletePetById(id: Long) {}
    }

    private class FakeHealthRepository : HealthRecordRepository {
        val records = mutableListOf<HealthRecord>()
        override fun getAllHealthRecords(): Flow<List<HealthRecord>> = flowOf(records)
        override fun getHealthRecordsForPet(petId: Long): Flow<List<HealthRecord>> = flowOf(records.filter { it.petId == petId })
        override fun getUpcomingReminders(petId: Long?): Flow<List<HealthRecord>> = flowOf(emptyList())
        override suspend fun insertRecord(record: HealthRecord): Long { records.add(record); return record.id }
        override suspend fun updateRecord(record: HealthRecord) {}
        override suspend fun deleteRecord(record: HealthRecord) {}
        override suspend fun deleteRecordById(id: Long) {}
    }

    private class FakeWeightRepository : WeightRepository {
        val records = mutableListOf<WeightRecord>()
        override fun getWeightHistory(petId: Long): Flow<List<WeightRecord>> = flowOf(records.filter { it.petId == petId })
        override suspend fun getWeightRecordById(id: Long): WeightRecord? = records.find { it.id == id }
        override suspend fun insert(weight: WeightRecord): Long { records.add(weight); return weight.id }
        override suspend fun update(weight: WeightRecord) {}
        override suspend fun delete(weight: WeightRecord) {}
        override suspend fun deleteById(id: Long) {}
    }

    private class FakeFoodRepository : FoodRepository {
        val schedules = mutableListOf<FoodSchedule>()
        override fun getFoodSchedule(petId: Long): Flow<List<FoodSchedule>> = flowOf(schedules.filter { it.petId == petId })
        override fun getAllFoodSchedules(): Flow<List<FoodSchedule>> = flowOf(schedules)
        override suspend fun getFoodScheduleById(id: Long): FoodSchedule? = schedules.find { it.id == id }
        override suspend fun insert(schedule: FoodSchedule): Long { schedules.add(schedule); return schedule.id }
        override suspend fun update(schedule: FoodSchedule) {}
        override suspend fun delete(schedule: FoodSchedule) {}
        override suspend fun deleteById(id: Long) {}
        override suspend fun setMealCompleted(id: Long, isCompleted: Boolean) {}
    }

    private class FakeReminderRepository : ReminderRepository {
        val reminders = mutableListOf<Reminder>()
        override fun getAllReminders(): Flow<List<Reminder>> = flowOf(reminders)
        override fun getRemindersForPet(petId: Long): Flow<List<Reminder>> = flowOf(reminders.filter { it.petId == petId })
        override fun getUpcomingReminders(petId: Long?, fromTime: Long): Flow<List<Reminder>> = flowOf(emptyList())
        override suspend fun getAllActiveRemindersSync(): List<Reminder> = reminders.filter { it.isEnabled }
        override suspend fun getReminderById(id: Long): Reminder? = reminders.find { it.id == id }
        override suspend fun insertReminder(reminder: Reminder): Long { reminders.add(reminder); return reminder.id }
        override suspend fun updateReminder(reminder: Reminder) {}
        override suspend fun deleteReminder(reminder: Reminder) {}
        override suspend fun deleteReminderById(id: Long) {}
        override suspend fun setCompleted(id: Long, isCompleted: Boolean) {}
        override suspend fun setEnabled(id: Long, isEnabled: Boolean) {}
        override suspend fun reschedule(id: Long, newTimestamp: Long, newJalaliDate: String, newTimeString: String) {}
    }

    private class FakeMemoryRepository : MemoryRepository {
        val memories = mutableListOf<Memory>()
        override fun getMemories(petId: Long): Flow<List<Memory>> = flowOf(memories.filter { it.petId == petId })
        override fun getAllMemories(): Flow<List<Memory>> = flowOf(memories)
        override suspend fun getMemoryById(id: Long): Memory? = memories.find { it.id == id }
        override suspend fun insert(memory: Memory): Long { memories.add(memory); return memory.id }
        override suspend fun update(memory: Memory) {}
        override suspend fun delete(memory: Memory) {}
        override suspend fun deleteById(id: Long) {}
    }

    private class FakeExpenseRepository : ExpenseRepository {
        val expenses = mutableListOf<Expense>()
        override fun getAllExpenses(): Flow<List<Expense>> = flowOf(expenses)
        override fun getExpensesForPet(petId: Long): Flow<List<Expense>> = flowOf(expenses.filter { it.petId == petId })
        override fun getTotalExpenseAmount(petId: Long?): Flow<Long> = flowOf(expenses.filter { petId == null || it.petId == petId }.sumOf { it.amount })
        override fun getExpenseSummary(petId: Long?): Flow<ExpenseSummary> = flowOf(ExpenseSummary(0L, emptyMap()))
        override suspend fun insertExpense(expense: Expense): Long { expenses.add(expense); return expense.id }
        override suspend fun deleteExpense(expense: Expense) {}
        override suspend fun deleteExpenseById(id: Long) {}
    }

    @Test
    fun `test GetPetMedicalReportUseCase gathers all pet medical domain data`() = runBlocking {
        val petRepo = FakePetRepository()
        val healthRepo = FakeHealthRepository()
        val weightRepo = FakeWeightRepository()
        val foodRepo = FakeFoodRepository()
        val reminderRepo = FakeReminderRepository()
        val memoryRepo = FakeMemoryRepository()
        val expenseRepo = FakeExpenseRepository()

        val pet = Pet(
            id = 1L,
            name = "میشا",
            species = PetSpecies.CAT,
            breed = "پرشین",
            gender = PetGender.FEMALE,
            birthDate = "1401/05/10",
            microchipId = "982000123456789"
        )
        petRepo.insertPet(pet)

        healthRepo.insertRecord(
            HealthRecord(
                id = 10L,
                petId = 1L,
                title = "واکسن سه‌گانه",
                type = HealthRecordType.VACCINE,
                jalaliDate = "1402/06/01",
                clinicOrDoctor = "کلینیک مرکزی"
            )
        )

        weightRepo.insert(
            WeightRecord(
                id = 20L,
                petId = 1L,
                weightKg = 4.2,
                unit = WeightUnit.KILOGRAM,
                jalaliDate = "1402/06/01"
            )
        )

        foodRepo.insert(
            FoodSchedule(
                id = 30L,
                petId = 1L,
                foodName = "رویال کنین فیت ۳۲",
                amount = "60",
                isActive = true
            )
        )

        reminderRepo.insertReminder(
            Reminder(
                id = 40L,
                petId = 1L,
                title = "نوبت بعدی واکسن هاری",
                remindTimestamp = System.currentTimeMillis() + 86400000L,
                reminderType = ReminderType.VACCINE
            )
        )

        expenseRepo.insertExpense(
            Expense(
                id = 50L,
                petId = 1L,
                title = "ویزیت و چکاپ کامل",
                amount = 450000L,
                category = ExpenseCategory.HEALTH
            )
        )

        val useCase = GetPetMedicalReportUseCase(
            petRepository = petRepo,
            healthRecordRepository = healthRepo,
            weightRepository = weightRepo,
            foodRepository = foodRepo,
            reminderRepository = reminderRepo,
            memoryRepository = memoryRepo,
            expenseRepository = expenseRepo
        )

        val report = useCase(1L)
        assertNotNull(report)
        assertEquals("میشا", report!!.pet.name)
        assertEquals(1, report.healthRecords.size)
        assertEquals(1, report.weightRecords.size)
        assertEquals(1, report.foodSchedules.size)
        assertEquals(1, report.reminders.size)
        assertEquals(1, report.expenses.size)
        assertEquals(450000L, report.totalExpenseAmount)
        assertEquals(4.2, report.weightStats.currentWeightKg, 0.01)
    }

    @Test
    fun `test PassportSectionsConfig toggle and counts`() {
        val config = PassportSectionsConfig()
        assertEquals(6, config.selectedCount)

        val toggledHealth = config.toggleSection(PassportSection.HEALTH)
        assertEquals(5, toggledHealth.selectedCount)
        assertEquals(false, toggledHealth.includeHealth)
        assertTrue(toggledHealth.includeWeight)

        val toggledBack = toggledHealth.toggleSection(PassportSection.HEALTH)
        assertEquals(6, toggledBack.selectedCount)
        assertTrue(toggledBack.includeHealth)
    }
}
