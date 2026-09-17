package red.line.pet.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import red.line.pet.data.local.dao.ExpenseDao
import red.line.pet.data.local.dao.FoodDao
import red.line.pet.data.local.dao.HealthRecordDao
import red.line.pet.data.local.dao.MedicalDao
import red.line.pet.data.local.dao.MemoryDao
import red.line.pet.data.local.dao.PetDao
import red.line.pet.data.local.dao.ReminderDao
import red.line.pet.data.local.dao.WeightDao
import red.line.pet.data.local.entity.ExpenseEntity
import red.line.pet.data.local.entity.FoodScheduleEntity
import red.line.pet.data.local.entity.HealthRecordEntity
import red.line.pet.data.local.entity.MedicalRecordEntity
import red.line.pet.data.local.entity.MemoryEntity
import red.line.pet.data.local.entity.PetEntity
import red.line.pet.data.local.entity.ReminderEntity
import red.line.pet.data.local.entity.WeightRecordEntity

@Database(
    entities = [
        PetEntity::class,
        MedicalRecordEntity::class,
        FoodScheduleEntity::class,
        WeightRecordEntity::class,
        ExpenseEntity::class,
        MemoryEntity::class,
        HealthRecordEntity::class,
        ReminderEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class RedLinePetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun medicalDao(): MedicalDao
    abstract fun foodDao(): FoodDao
    abstract fun weightDao(): WeightDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun memoryDao(): MemoryDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        private const val DATABASE_NAME = "redline_pet.db"

        @Volatile
        private var INSTANCE: RedLinePetDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `medical_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `petId` INTEGER NOT NULL,
                        `type` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `date` INTEGER NOT NULL,
                        `reminderDate` INTEGER,
                        `costToman` INTEGER NOT NULL,
                        `clinicOrDoctor` TEXT NOT NULL,
                        `jalaliDate` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `food_schedules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `petId` INTEGER NOT NULL,
                        `foodName` TEXT NOT NULL,
                        `amount` TEXT NOT NULL,
                        `morningTime` TEXT NOT NULL,
                        `eveningTime` TEXT NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `weight_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `petId` INTEGER NOT NULL,
                        `weight` REAL NOT NULL,
                        `date` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `memories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `petId` INTEGER NOT NULL,
                        `imagePath` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `date` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `mealTitle` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `mealType` TEXT NOT NULL DEFAULT 'BREAKFAST'")
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `amountUnit` TEXT NOT NULL DEFAULT 'GRAM'")
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `mealTime` TEXT NOT NULL DEFAULT '08:00'")
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `repeatDays` TEXT NOT NULL DEFAULT 'SAT,SUN,MON,TUE,WED,THU,FRI'")
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `isCompleted` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `isActive` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `supplementName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `supplementAmount` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `food_schedules` ADD COLUMN `supplementNotes` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `weight_records` ADD COLUMN `unit` TEXT NOT NULL DEFAULT 'KILOGRAM'")
                db.execSQL("ALTER TABLE `weight_records` ADD COLUMN `jalaliDate` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `weight_records` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `petId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `reminderType` TEXT NOT NULL DEFAULT 'CUSTOM',
                        `targetId` INTEGER,
                        `remindTimestamp` INTEGER NOT NULL,
                        `jalaliDate` TEXT NOT NULL DEFAULT '',
                        `timeString` TEXT NOT NULL DEFAULT '08:00',
                        `repeatType` TEXT NOT NULL DEFAULT 'ONCE',
                        `isEnabled` INTEGER NOT NULL DEFAULT 1,
                        `isCompleted` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): RedLinePetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RedLinePetDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
