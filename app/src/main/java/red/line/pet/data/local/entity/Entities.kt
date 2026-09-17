package red.line.pet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val species: String,
    val breed: String = "",
    val birthDateJalali: String = "",
    val gender: String = "MALE",
    val weightKg: Double = 0.0,
    val microchipId: String = "",
    val avatarUri: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medical_records")
data class MedicalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val type: String,
    val title: String,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val reminderDate: Long? = null,
    val costToman: Long = 0,
    val clinicOrDoctor: String = "",
    val jalaliDate: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "food_schedules")
data class FoodScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val mealTitle: String = "",
    val mealType: String = "BREAKFAST",
    val foodName: String = "",
    val amount: String = "",
    val amountUnit: String = "GRAM",
    val mealTime: String = "08:00",
    val morningTime: String = "",
    val eveningTime: String = "",
    val repeatDays: String = "SAT,SUN,MON,TUE,WED,THU,FRI",
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val supplementName: String = "",
    val supplementAmount: String = "",
    val supplementNotes: String = "",
    val notes: String = ""
)

@Entity(tableName = "weight_records")
data class WeightRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val weight: Double, // canonical in KG
    val unit: String = "KILOGRAM",
    val jalaliDate: String = "",
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val title: String,
    val category: String,
    val amountToman: Long = 0L,
    val jalaliDate: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val imagePath: String,
    val title: String,
    val description: String = "",
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "health_records")
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val title: String,
    val type: String,
    val jalaliDate: String,
    val nextDueJalaliDate: String? = null,
    val costToman: Long = 0,
    val clinicOrDoctor: String = "",
    val notes: String = "",
    val isCompleted: Boolean = true
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val title: String,
    val description: String = "",
    val reminderType: String = "CUSTOM",
    val targetId: Long? = null,
    val remindTimestamp: Long,
    val jalaliDate: String = "",
    val timeString: String = "08:00",
    val repeatType: String = "ONCE",
    val isEnabled: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

