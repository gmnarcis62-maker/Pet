package red.line.pet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import red.line.pet.data.local.entity.ExpenseEntity
import red.line.pet.data.local.entity.FoodScheduleEntity
import red.line.pet.data.local.entity.HealthRecordEntity
import red.line.pet.data.local.entity.MedicalRecordEntity
import red.line.pet.data.local.entity.MemoryEntity
import red.line.pet.data.local.entity.PetEntity
import red.line.pet.data.local.entity.WeightRecordEntity

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY createdAt DESC")
    fun getAllPets(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1")
    fun getPetById(id: Long): Flow<PetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: PetEntity): Long

    @Update
    suspend fun update(pet: PetEntity)

    @Delete
    suspend fun delete(pet: PetEntity)

    @Query("DELETE FROM pets WHERE id = :id")
    suspend fun deletePetById(id: Long)

    // Backwards compatibility helpers
    suspend fun insertPet(pet: PetEntity): Long = insert(pet)
    suspend fun updatePet(pet: PetEntity) = update(pet)
    suspend fun deletePet(pet: PetEntity) = delete(pet)

    @Query("SELECT * FROM pets ORDER BY id ASC")
    suspend fun getAllPetsSync(): List<PetEntity>

    @Query("DELETE FROM pets")
    suspend fun deleteAllPets()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pets: List<PetEntity>)
}

@Dao
interface MedicalDao {
    @Query("SELECT * FROM medical_records WHERE petId = :petId ORDER BY date DESC")
    fun getMedicalRecordsByPet(petId: Long): Flow<List<MedicalRecordEntity>>

    @Query("SELECT * FROM medical_records ORDER BY date DESC")
    fun getAllMedicalRecords(): Flow<List<MedicalRecordEntity>>

    @Query("SELECT * FROM medical_records WHERE reminderDate IS NOT NULL ORDER BY reminderDate ASC")
    fun getUpcomingReminders(): Flow<List<MedicalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MedicalRecordEntity): Long

    @Delete
    suspend fun delete(record: MedicalRecordEntity)

    @Query("DELETE FROM medical_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM medical_records WHERE id = :id LIMIT 1")
    suspend fun getMedicalRecordById(id: Long): MedicalRecordEntity?

    @Query("DELETE FROM medical_records WHERE petId = :petId")
    suspend fun deleteByPetId(petId: Long)

    @Query("SELECT * FROM medical_records ORDER BY id ASC")
    suspend fun getAllMedicalRecordsSync(): List<MedicalRecordEntity>

    @Query("DELETE FROM medical_records")
    suspend fun deleteAllMedicalRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MedicalRecordEntity>)
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_schedules WHERE petId = :petId ORDER BY mealTime ASC")
    fun getFoodSchedule(petId: Long): Flow<List<FoodScheduleEntity>>

    @Query("SELECT * FROM food_schedules ORDER BY mealTime ASC")
    fun getAllFoodSchedules(): Flow<List<FoodScheduleEntity>>

    @Query("SELECT * FROM food_schedules WHERE id = :id LIMIT 1")
    suspend fun getFoodScheduleById(id: Long): FoodScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: FoodScheduleEntity): Long

    @Update
    suspend fun update(schedule: FoodScheduleEntity)

    @Delete
    suspend fun delete(schedule: FoodScheduleEntity)

    @Query("DELETE FROM food_schedules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE food_schedules SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletedStatus(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM food_schedules WHERE petId = :petId")
    suspend fun deleteByPetId(petId: Long)

    @Query("SELECT * FROM food_schedules ORDER BY id ASC")
    suspend fun getAllFoodSchedulesSync(): List<FoodScheduleEntity>

    @Query("DELETE FROM food_schedules")
    suspend fun deleteAllFoodSchedules()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<FoodScheduleEntity>)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_records WHERE petId = :petId ORDER BY date DESC")
    fun getWeightHistory(petId: Long): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE id = :id LIMIT 1")
    suspend fun getWeightRecordById(id: Long): WeightRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weight: WeightRecordEntity): Long

    @Update
    suspend fun update(weight: WeightRecordEntity)

    @Delete
    suspend fun delete(weight: WeightRecordEntity)

    @Query("DELETE FROM weight_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM weight_records WHERE petId = :petId")
    suspend fun deleteByPetId(petId: Long)

    @Query("SELECT * FROM weight_records ORDER BY id ASC")
    suspend fun getAllWeightRecordsSync(): List<WeightRecordEntity>

    @Query("DELETE FROM weight_records")
    suspend fun deleteAllWeightRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weights: List<WeightRecordEntity>)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY jalaliDate DESC, createdAt DESC")
    fun getExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE petId = :petId ORDER BY jalaliDate DESC, createdAt DESC")
    fun getExpensesForPet(petId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amountToman), 0) FROM expenses")
    fun getTotalExpenses(): Flow<Long?>

    @Query("SELECT COALESCE(SUM(amountToman), 0) FROM expenses WHERE petId = :petId")
    fun getTotalExpensesForPet(petId: Long): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM expenses WHERE petId = :petId")
    suspend fun deleteByPetId(petId: Long)

    // Backwards compatibility
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = getExpenses()
    fun getTotalAmount(): Flow<Long> = getTotalExpenses().map { it ?: 0L }
    suspend fun insertExpense(expense: ExpenseEntity): Long = insert(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = delete(expense)

    @Query("SELECT * FROM expenses ORDER BY id ASC")
    suspend fun getAllExpensesSync(): List<ExpenseEntity>

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE petId = :petId ORDER BY date DESC")
    fun getMemories(petId: Long): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY date DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
    suspend fun getMemoryById(id: Long): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity): Long

    @Update
    suspend fun update(memory: MemoryEntity)

    @Delete
    suspend fun delete(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM memories WHERE petId = :petId")
    suspend fun deleteByPetId(petId: Long)

    @Query("SELECT * FROM memories ORDER BY id ASC")
    suspend fun getAllMemoriesSync(): List<MemoryEntity>

    @Query("DELETE FROM memories")
    suspend fun deleteAllMemories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<MemoryEntity>)
}

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records ORDER BY jalaliDate DESC")
    fun getAllRecords(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE petId = :petId ORDER BY jalaliDate DESC")
    fun getRecordsForPet(petId: Long): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE nextDueJalaliDate IS NOT NULL AND isCompleted = 0 ORDER BY nextDueJalaliDate ASC")
    fun getUpcomingReminders(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE petId = :petId AND nextDueJalaliDate IS NOT NULL AND isCompleted = 0 ORDER BY nextDueJalaliDate ASC")
    fun getUpcomingRemindersForPet(petId: Long): Flow<List<HealthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HealthRecordEntity): Long

    @Update
    suspend fun updateRecord(record: HealthRecordEntity)

    @Delete
    suspend fun deleteRecord(record: HealthRecordEntity)

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM health_records WHERE petId = :petId")
    suspend fun deleteByPetId(petId: Long)

    @Query("SELECT * FROM health_records ORDER BY id ASC")
    suspend fun getAllHealthRecordsSync(): List<HealthRecordEntity>

    @Query("DELETE FROM health_records")
    suspend fun deleteAllHealthRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HealthRecordEntity>)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY remindTimestamp ASC")
    fun getAllReminders(): Flow<List<red.line.pet.data.local.entity.ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE petId = :petId ORDER BY remindTimestamp ASC")
    fun getRemindersForPet(petId: Long): Flow<List<red.line.pet.data.local.entity.ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0 AND remindTimestamp >= :fromTime ORDER BY remindTimestamp ASC")
    fun getUpcomingReminders(fromTime: Long): Flow<List<red.line.pet.data.local.entity.ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE petId = :petId AND isEnabled = 1 AND isCompleted = 0 AND remindTimestamp >= :fromTime ORDER BY remindTimestamp ASC")
    fun getUpcomingRemindersForPet(petId: Long, fromTime: Long): Flow<List<red.line.pet.data.local.entity.ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0")
    suspend fun getAllActiveRemindersSync(): List<red.line.pet.data.local.entity.ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): red.line.pet.data.local.entity.ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: red.line.pet.data.local.entity.ReminderEntity): Long

    @Update
    suspend fun update(reminder: red.line.pet.data.local.entity.ReminderEntity)

    @Delete
    suspend fun delete(reminder: red.line.pet.data.local.entity.ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM reminders WHERE petId = :petId")
    suspend fun deleteByPetId(petId: Long)

    @Query("UPDATE reminders SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("UPDATE reminders SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE reminders SET remindTimestamp = :newTimestamp, jalaliDate = :newJalaliDate, timeString = :newTimeString WHERE id = :id")
    suspend fun reschedule(id: Long, newTimestamp: Long, newJalaliDate: String, newTimeString: String)

    @Query("SELECT * FROM reminders ORDER BY id ASC")
    suspend fun getAllRemindersListSync(): List<red.line.pet.data.local.entity.ReminderEntity>

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<red.line.pet.data.local.entity.ReminderEntity>)
}
