package red.line.pet.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import red.line.pet.data.local.dao.ExpenseDao
import red.line.pet.data.local.dao.FoodDao
import red.line.pet.data.local.dao.HealthRecordDao
import red.line.pet.data.local.dao.MedicalDao
import red.line.pet.data.local.dao.MemoryDao
import red.line.pet.data.local.dao.PetDao
import red.line.pet.data.local.dao.WeightDao
import red.line.pet.data.local.datastore.UserPreferencesDataStore
import red.line.pet.data.mapper.toDomain
import red.line.pet.data.mapper.toEntity
import red.line.pet.domain.model.Expense
import red.line.pet.domain.model.ExpenseCategory
import red.line.pet.domain.model.ExpenseSummary
import red.line.pet.domain.model.FoodSchedule
import red.line.pet.domain.model.HealthRecord
import red.line.pet.domain.model.MedicalRecord
import red.line.pet.domain.model.Memory
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.WeightRecord
import red.line.pet.domain.repository.AppThemeMode
import red.line.pet.domain.repository.ExpenseRepository
import red.line.pet.domain.repository.FoodRepository
import red.line.pet.domain.repository.HealthRecordRepository
import red.line.pet.domain.repository.MedicalRepository
import red.line.pet.domain.repository.MemoryRepository
import red.line.pet.domain.repository.PetRepository
import red.line.pet.domain.repository.UserPreferencesRepository
import red.line.pet.domain.repository.WeightRepository

class PetRepositoryImpl(
    private val petDao: PetDao,
    private val medicalDao: MedicalDao,
    private val foodDao: FoodDao,
    private val weightDao: WeightDao,
    private val expenseDao: ExpenseDao,
    private val memoryDao: MemoryDao,
    private val healthRecordDao: HealthRecordDao,
    private val reminderDao: red.line.pet.data.local.dao.ReminderDao
) : PetRepository {
    override fun getAllPets(): Flow<List<Pet>> {
        return petDao.getAllPets().map { list -> list.map { it.toDomain() } }
    }

    override fun getPetById(id: Long): Flow<Pet?> {
        return petDao.getPetById(id).map { it?.toDomain() }
    }

    override suspend fun insertPet(pet: Pet): Long {
        return petDao.insert(pet.toEntity())
    }

    override suspend fun updatePet(pet: Pet) {
        petDao.update(pet.toEntity())
    }

    override suspend fun deletePet(pet: Pet) {
        deletePetById(pet.id)
    }

    override suspend fun deletePetById(id: Long) {
        medicalDao.deleteByPetId(id)
        foodDao.deleteByPetId(id)
        weightDao.deleteByPetId(id)
        expenseDao.deleteByPetId(id)
        memoryDao.deleteByPetId(id)
        healthRecordDao.deleteByPetId(id)
        reminderDao.deleteByPetId(id)
        petDao.deletePetById(id)
    }
}

class MedicalRepositoryImpl(
    private val medicalDao: MedicalDao
) : MedicalRepository {
    override fun getMedicalRecordsByPet(petId: Long): Flow<List<MedicalRecord>> {
        return medicalDao.getMedicalRecordsByPet(petId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllMedicalRecords(): Flow<List<MedicalRecord>> {
        return medicalDao.getAllMedicalRecords().map { list -> list.map { it.toDomain() } }
    }

    override fun getUpcomingReminders(): Flow<List<MedicalRecord>> {
        return medicalDao.getUpcomingReminders().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insert(record: MedicalRecord): Long {
        return medicalDao.insert(record.toEntity())
    }

    override suspend fun delete(record: MedicalRecord) {
        medicalDao.delete(record.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        medicalDao.deleteById(id)
    }
}

class FoodRepositoryImpl(
    private val foodDao: FoodDao
) : FoodRepository {
    override fun getFoodSchedule(petId: Long): Flow<List<FoodSchedule>> {
        return foodDao.getFoodSchedule(petId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllFoodSchedules(): Flow<List<FoodSchedule>> {
        return foodDao.getAllFoodSchedules().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getFoodScheduleById(id: Long): FoodSchedule? {
        return foodDao.getFoodScheduleById(id)?.toDomain()
    }

    override suspend fun insert(schedule: FoodSchedule): Long {
        return foodDao.insert(schedule.toEntity())
    }

    override suspend fun update(schedule: FoodSchedule) {
        foodDao.update(schedule.toEntity())
    }

    override suspend fun delete(schedule: FoodSchedule) {
        foodDao.delete(schedule.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        foodDao.deleteById(id)
    }

    override suspend fun setMealCompleted(id: Long, isCompleted: Boolean) {
        foodDao.updateCompletedStatus(id, isCompleted)
    }
}

class WeightRepositoryImpl(
    private val weightDao: WeightDao
) : WeightRepository {
    override fun getWeightHistory(petId: Long): Flow<List<WeightRecord>> {
        return weightDao.getWeightHistory(petId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getWeightRecordById(id: Long): WeightRecord? {
        return weightDao.getWeightRecordById(id)?.toDomain()
    }

    override suspend fun insert(weight: WeightRecord): Long {
        return weightDao.insert(weight.toEntity())
    }

    override suspend fun update(weight: WeightRecord) {
        weightDao.update(weight.toEntity())
    }

    override suspend fun delete(weight: WeightRecord) {
        weightDao.delete(weight.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        weightDao.deleteById(id)
    }
}

class MemoryRepositoryImpl(
    private val memoryDao: MemoryDao
) : MemoryRepository {
    override fun getMemories(petId: Long): Flow<List<Memory>> {
        return memoryDao.getMemories(petId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllMemories(): Flow<List<Memory>> {
        return memoryDao.getAllMemories().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getMemoryById(id: Long): Memory? {
        return memoryDao.getMemoryById(id)?.toDomain()
    }

    override suspend fun insert(memory: Memory): Long {
        return memoryDao.insert(memory.toEntity())
    }

    override suspend fun update(memory: Memory) {
        memoryDao.update(memory.toEntity())
    }

    override suspend fun delete(memory: Memory) {
        memoryDao.delete(memory.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        memoryDao.deleteById(id)
    }
}

class HealthRecordRepositoryImpl(
    private val healthRecordDao: HealthRecordDao
) : HealthRecordRepository {
    override fun getAllHealthRecords(): Flow<List<HealthRecord>> {
        return healthRecordDao.getAllRecords().map { list -> list.map { it.toDomain() } }
    }

    override fun getHealthRecordsForPet(petId: Long): Flow<List<HealthRecord>> {
        return healthRecordDao.getRecordsForPet(petId).map { list -> list.map { it.toDomain() } }
    }

    override fun getUpcomingReminders(petId: Long?): Flow<List<HealthRecord>> {
        val flow = if (petId != null) {
            healthRecordDao.getUpcomingRemindersForPet(petId)
        } else {
            healthRecordDao.getUpcomingReminders()
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertRecord(record: HealthRecord): Long {
        return healthRecordDao.insertRecord(record.toEntity())
    }

    override suspend fun updateRecord(record: HealthRecord) {
        healthRecordDao.updateRecord(record.toEntity())
    }

    override suspend fun deleteRecord(record: HealthRecord) {
        healthRecordDao.deleteRecord(record.toEntity())
    }

    override suspend fun deleteRecordById(id: Long) {
        healthRecordDao.deleteRecordById(id)
    }
}

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getExpenses().map { list -> list.map { it.toDomain() } }
    }

    override fun getExpensesForPet(petId: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesForPet(petId).map { list -> list.map { it.toDomain() } }
    }

    override fun getTotalExpenseAmount(petId: Long?): Flow<Long> {
        val flow = if (petId != null) {
            expenseDao.getTotalExpensesForPet(petId)
        } else {
            expenseDao.getTotalExpenses()
        }
        return flow.map { it ?: 0L }
    }

    override fun getExpenseSummary(petId: Long?): Flow<ExpenseSummary> {
        val flow = if (petId != null) {
            expenseDao.getExpensesForPet(petId)
        } else {
            expenseDao.getExpenses()
        }
        return flow.map { entities ->
            var total = 0L
            val categoryMap = mutableMapOf<ExpenseCategory, Long>()
            for (entity in entities) {
                total += entity.amountToman
                val cat = try {
                    ExpenseCategory.valueOf(entity.category)
                } catch (e: Exception) {
                    ExpenseCategory.OTHER
                }
                categoryMap[cat] = (categoryMap[cat] ?: 0L) + entity.amountToman
            }
            ExpenseSummary(totalAmountToman = total, categoryAmounts = categoryMap)
        }
    }

    override suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insert(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense.toEntity())
    }

    override suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }
}

class UserPreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {
    override fun getThemeMode(): Flow<AppThemeMode> = dataStore.themeMode

    override suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.setThemeMode(mode)
    }

    override fun getActivePetId(): Flow<Long?> = dataStore.activePetId

    override suspend fun setActivePetId(petId: Long?) {
        dataStore.setActivePetId(petId)
    }
}

class ReminderRepositoryImpl(
    private val reminderDao: red.line.pet.data.local.dao.ReminderDao
) : red.line.pet.domain.repository.ReminderRepository {
    override fun getAllReminders(): Flow<List<red.line.pet.domain.model.Reminder>> {
        return reminderDao.getAllReminders().map { list -> list.map { it.toDomain() } }
    }

    override fun getRemindersForPet(petId: Long): Flow<List<red.line.pet.domain.model.Reminder>> {
        return reminderDao.getRemindersForPet(petId).map { list -> list.map { it.toDomain() } }
    }

    override fun getUpcomingReminders(petId: Long?, fromTime: Long): Flow<List<red.line.pet.domain.model.Reminder>> {
        val flow = if (petId != null) {
            reminderDao.getUpcomingRemindersForPet(petId, fromTime)
        } else {
            reminderDao.getUpcomingReminders(fromTime)
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAllActiveRemindersSync(): List<red.line.pet.domain.model.Reminder> {
        return reminderDao.getAllActiveRemindersSync().map { it.toDomain() }
    }

    override suspend fun getReminderById(id: Long): red.line.pet.domain.model.Reminder? {
        return reminderDao.getReminderById(id)?.toDomain()
    }

    override suspend fun insertReminder(reminder: red.line.pet.domain.model.Reminder): Long {
        return reminderDao.insert(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: red.line.pet.domain.model.Reminder) {
        reminderDao.update(reminder.toEntity())
    }

    override suspend fun deleteReminder(reminder: red.line.pet.domain.model.Reminder) {
        reminderDao.delete(reminder.toEntity())
    }

    override suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteById(id)
    }

    override suspend fun setCompleted(id: Long, isCompleted: Boolean) {
        reminderDao.setCompleted(id, isCompleted)
    }

    override suspend fun setEnabled(id: Long, isEnabled: Boolean) {
        reminderDao.setEnabled(id, isEnabled)
    }

    override suspend fun reschedule(id: Long, newTimestamp: Long, newJalaliDate: String, newTimeString: String) {
        reminderDao.reschedule(id, newTimestamp, newJalaliDate, newTimeString)
    }
}
