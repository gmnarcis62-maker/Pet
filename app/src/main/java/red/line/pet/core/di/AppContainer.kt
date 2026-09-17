package red.line.pet.core.di

import android.content.Context
import red.line.pet.data.local.database.RedLinePetDatabase
import red.line.pet.data.local.datastore.UserPreferencesDataStore
import red.line.pet.data.repository.ExpenseRepositoryImpl
import red.line.pet.data.repository.FoodRepositoryImpl
import red.line.pet.data.repository.HealthRecordRepositoryImpl
import red.line.pet.data.repository.MedicalRepositoryImpl
import red.line.pet.data.repository.MemoryRepositoryImpl
import red.line.pet.data.repository.PetRepositoryImpl
import red.line.pet.data.repository.UserPreferencesRepositoryImpl
import red.line.pet.data.repository.WeightRepositoryImpl
import red.line.pet.domain.repository.ExpenseRepository
import red.line.pet.domain.repository.FoodRepository
import red.line.pet.domain.repository.HealthRecordRepository
import red.line.pet.domain.repository.MedicalRepository
import red.line.pet.domain.repository.MemoryRepository
import red.line.pet.domain.repository.PetRepository
import red.line.pet.domain.repository.UserPreferencesRepository
import red.line.pet.domain.repository.WeightRepository
import red.line.pet.domain.usecase.expense.AddExpenseUseCase
import red.line.pet.domain.usecase.expense.GetExpenseSummaryUseCase
import red.line.pet.domain.usecase.expense.GetExpensesUseCase
import red.line.pet.domain.usecase.health.AddHealthRecordUseCase
import red.line.pet.domain.usecase.health.GetHealthRecordsUseCase
import red.line.pet.domain.usecase.health.GetUpcomingRemindersUseCase
import red.line.pet.domain.usecase.medical.AddMedicalRecordUseCase
import red.line.pet.domain.usecase.medical.GetMedicalRecordsUseCase
import red.line.pet.domain.usecase.pet.AddPetUseCase
import red.line.pet.domain.usecase.pet.DeletePetUseCase
import red.line.pet.domain.usecase.pet.GetPetsUseCase
import red.line.pet.domain.usecase.pet.UpdatePetUseCase

class AppContainer(private val context: Context) {

    val database: RedLinePetDatabase by lazy {
        RedLinePetDatabase.getInstance(context)
    }

    val userPreferencesDataStore: UserPreferencesDataStore by lazy {
        UserPreferencesDataStore(context)
    }

    val petRepository: PetRepository by lazy {
        PetRepositoryImpl(
            database.petDao(),
            database.medicalDao(),
            database.foodDao(),
            database.weightDao(),
            database.expenseDao(),
            database.memoryDao(),
            database.healthRecordDao(),
            database.reminderDao()
        )
    }

    val medicalRepository: MedicalRepository by lazy {
        MedicalRepositoryImpl(database.medicalDao())
    }

    val foodRepository: FoodRepository by lazy {
        FoodRepositoryImpl(database.foodDao())
    }

    val weightRepository: WeightRepository by lazy {
        WeightRepositoryImpl(database.weightDao())
    }

    val memoryRepository: MemoryRepository by lazy {
        MemoryRepositoryImpl(database.memoryDao())
    }

    val healthRecordRepository: HealthRecordRepository by lazy {
        HealthRecordRepositoryImpl(database.healthRecordDao())
    }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(database.expenseDao())
    }

    val reminderRepository: red.line.pet.domain.repository.ReminderRepository by lazy {
        red.line.pet.data.repository.ReminderRepositoryImpl(database.reminderDao())
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(userPreferencesDataStore)
    }

    // UseCases
    val getPetsUseCase: GetPetsUseCase by lazy {
        GetPetsUseCase(petRepository)
    }

    val addPetUseCase: AddPetUseCase by lazy {
        AddPetUseCase(petRepository)
    }

    val deletePetUseCase: DeletePetUseCase by lazy {
        DeletePetUseCase(petRepository)
    }

    val updatePetUseCase: UpdatePetUseCase by lazy {
        UpdatePetUseCase(petRepository)
    }

    val addMedicalRecordUseCase: AddMedicalRecordUseCase by lazy {
        AddMedicalRecordUseCase(medicalRepository)
    }

    val getMedicalRecordsUseCase: GetMedicalRecordsUseCase by lazy {
        GetMedicalRecordsUseCase(medicalRepository)
    }

    val getHealthRecordsUseCase: GetHealthRecordsUseCase by lazy {
        GetHealthRecordsUseCase(healthRecordRepository)
    }

    val getUpcomingRemindersUseCase: GetUpcomingRemindersUseCase by lazy {
        GetUpcomingRemindersUseCase(healthRecordRepository)
    }

    val addHealthRecordUseCase: AddHealthRecordUseCase by lazy {
        AddHealthRecordUseCase(healthRecordRepository)
    }

    val getExpensesUseCase: GetExpensesUseCase by lazy {
        GetExpensesUseCase(expenseRepository)
    }

    val getExpenseSummaryUseCase: GetExpenseSummaryUseCase by lazy {
        GetExpenseSummaryUseCase(expenseRepository)
    }

    val addExpenseUseCase: AddExpenseUseCase by lazy {
        AddExpenseUseCase(expenseRepository)
    }

    // Food UseCases
    val getFoodSchedulesUseCase: red.line.pet.domain.usecase.food.GetFoodSchedulesUseCase by lazy {
        red.line.pet.domain.usecase.food.GetFoodSchedulesUseCase(foodRepository)
    }

    val getTodayMealsUseCase: red.line.pet.domain.usecase.food.GetTodayMealsUseCase by lazy {
        red.line.pet.domain.usecase.food.GetTodayMealsUseCase(foodRepository)
    }

    val addFoodScheduleUseCase: red.line.pet.domain.usecase.food.AddFoodScheduleUseCase by lazy {
        red.line.pet.domain.usecase.food.AddFoodScheduleUseCase(foodRepository)
    }

    val updateFoodScheduleUseCase: red.line.pet.domain.usecase.food.UpdateFoodScheduleUseCase by lazy {
        red.line.pet.domain.usecase.food.UpdateFoodScheduleUseCase(foodRepository)
    }

    val deleteFoodScheduleUseCase: red.line.pet.domain.usecase.food.DeleteFoodScheduleUseCase by lazy {
        red.line.pet.domain.usecase.food.DeleteFoodScheduleUseCase(foodRepository)
    }

    val toggleMealCompletedUseCase: red.line.pet.domain.usecase.food.ToggleMealCompletedUseCase by lazy {
        red.line.pet.domain.usecase.food.ToggleMealCompletedUseCase(foodRepository)
    }

    val getWeeklyFoodScheduleUseCase: red.line.pet.domain.usecase.food.GetWeeklyFoodScheduleUseCase by lazy {
        red.line.pet.domain.usecase.food.GetWeeklyFoodScheduleUseCase(foodRepository)
    }

    // Weight UseCases
    val getWeightRecordsUseCase: red.line.pet.domain.usecase.weight.GetWeightRecordsUseCase by lazy {
        red.line.pet.domain.usecase.weight.GetWeightRecordsUseCase(weightRepository)
    }

    val getFilteredWeightRecordsUseCase: red.line.pet.domain.usecase.weight.GetFilteredWeightRecordsUseCase by lazy {
        red.line.pet.domain.usecase.weight.GetFilteredWeightRecordsUseCase(weightRepository)
    }

    val getWeightStatisticsUseCase: red.line.pet.domain.usecase.weight.GetWeightStatisticsUseCase by lazy {
        red.line.pet.domain.usecase.weight.GetWeightStatisticsUseCase(weightRepository)
    }

    val addWeightRecordUseCase: red.line.pet.domain.usecase.weight.AddWeightRecordUseCase by lazy {
        red.line.pet.domain.usecase.weight.AddWeightRecordUseCase(weightRepository)
    }

    val updateWeightRecordUseCase: red.line.pet.domain.usecase.weight.UpdateWeightRecordUseCase by lazy {
        red.line.pet.domain.usecase.weight.UpdateWeightRecordUseCase(weightRepository)
    }

    val deleteWeightRecordUseCase: red.line.pet.domain.usecase.weight.DeleteWeightRecordUseCase by lazy {
        red.line.pet.domain.usecase.weight.DeleteWeightRecordUseCase(weightRepository)
    }

    // Memory UseCases
    val getMemoriesUseCase: red.line.pet.domain.usecase.memory.GetMemoriesUseCase by lazy {
        red.line.pet.domain.usecase.memory.GetMemoriesUseCase(memoryRepository)
    }

    val searchMemoriesUseCase: red.line.pet.domain.usecase.memory.SearchMemoriesUseCase by lazy {
        red.line.pet.domain.usecase.memory.SearchMemoriesUseCase(memoryRepository)
    }

    val addMemoryUseCase: red.line.pet.domain.usecase.memory.AddMemoryUseCase by lazy {
        red.line.pet.domain.usecase.memory.AddMemoryUseCase(memoryRepository)
    }

    val updateMemoryUseCase: red.line.pet.domain.usecase.memory.UpdateMemoryUseCase by lazy {
        red.line.pet.domain.usecase.memory.UpdateMemoryUseCase(memoryRepository)
    }

    val deleteMemoryUseCase: red.line.pet.domain.usecase.memory.DeleteMemoryUseCase by lazy {
        red.line.pet.domain.usecase.memory.DeleteMemoryUseCase(memoryRepository)
    }

    // Reminder UseCases
    val getRemindersUseCase: red.line.pet.domain.usecase.reminder.GetRemindersUseCase by lazy {
        red.line.pet.domain.usecase.reminder.GetRemindersUseCase(reminderRepository)
    }

    val getUpcomingCareRemindersUseCase: red.line.pet.domain.usecase.reminder.GetUpcomingRemindersUseCase by lazy {
        red.line.pet.domain.usecase.reminder.GetUpcomingRemindersUseCase(reminderRepository)
    }

    val addReminderUseCase: red.line.pet.domain.usecase.reminder.AddReminderUseCase by lazy {
        red.line.pet.domain.usecase.reminder.AddReminderUseCase(reminderRepository)
    }

    val updateReminderUseCase: red.line.pet.domain.usecase.reminder.UpdateReminderUseCase by lazy {
        red.line.pet.domain.usecase.reminder.UpdateReminderUseCase(reminderRepository)
    }

    val deleteReminderUseCase: red.line.pet.domain.usecase.reminder.DeleteReminderUseCase by lazy {
        red.line.pet.domain.usecase.reminder.DeleteReminderUseCase(reminderRepository)
    }

    val toggleReminderUseCase: red.line.pet.domain.usecase.reminder.ToggleReminderUseCase by lazy {
        red.line.pet.domain.usecase.reminder.ToggleReminderUseCase(reminderRepository)
    }

    val completeReminderUseCase: red.line.pet.domain.usecase.reminder.CompleteReminderUseCase by lazy {
        red.line.pet.domain.usecase.reminder.CompleteReminderUseCase(reminderRepository)
    }

    val snoozeReminderUseCase: red.line.pet.domain.usecase.reminder.SnoozeReminderUseCase by lazy {
        red.line.pet.domain.usecase.reminder.SnoozeReminderUseCase(reminderRepository)
    }

    // Medical Passport (PDF Export) UseCases & Services
    val medicalPassportPdfGenerator: red.line.pet.core.pdf.MedicalPassportPdfGenerator by lazy {
        red.line.pet.core.pdf.MedicalPassportPdfGenerator(context)
    }

    val getPetMedicalReportUseCase: red.line.pet.domain.usecase.passport.GetPetMedicalReportUseCase by lazy {
        red.line.pet.domain.usecase.passport.GetPetMedicalReportUseCase(
            petRepository = petRepository,
            healthRecordRepository = healthRecordRepository,
            weightRepository = weightRepository,
            foodRepository = foodRepository,
            reminderRepository = reminderRepository,
            memoryRepository = memoryRepository,
            expenseRepository = expenseRepository
        )
    }

    val generateMedicalPassportUseCase: red.line.pet.domain.usecase.passport.GenerateMedicalPassportUseCase by lazy {
        red.line.pet.domain.usecase.passport.GenerateMedicalPassportUseCase(medicalPassportPdfGenerator)
    }

    val exportPdfUseCase: red.line.pet.domain.usecase.passport.ExportPdfUseCase by lazy {
        red.line.pet.domain.usecase.passport.ExportPdfUseCase(generateMedicalPassportUseCase)
    }

    val sharePdfUseCase: red.line.pet.domain.usecase.passport.SharePdfUseCase by lazy {
        red.line.pet.domain.usecase.passport.SharePdfUseCase()
    }

    // Free & VIP Monetization Management
    val freeTrialManager: red.line.pet.core.billing.FreeTrialManager by lazy {
        red.line.pet.core.billing.FreeTrialManager(context)
    }

    val freeVipManager: red.line.pet.core.billing.FreeVipManager by lazy {
        red.line.pet.core.billing.FreeVipManager(userPreferencesDataStore, freeTrialManager)
    }

    val myketBillingManager: red.line.pet.core.billing.MyketBillingManager by lazy {
        red.line.pet.core.billing.MyketBillingManager(context, freeVipManager)
    }

    val getVipStatusUseCase: red.line.pet.domain.usecase.vip.GetVipStatusUseCase by lazy {
        red.line.pet.domain.usecase.vip.GetVipStatusUseCase(freeVipManager)
    }

    val checkFeatureAccessUseCase: red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase by lazy {
        red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase(freeVipManager)
    }

    val purchaseVipUseCase: red.line.pet.domain.usecase.vip.PurchaseVipUseCase by lazy {
        red.line.pet.domain.usecase.vip.PurchaseVipUseCase(myketBillingManager)
    }

    val processPurchaseResultUseCase: red.line.pet.domain.usecase.vip.ProcessPurchaseResultUseCase by lazy {
        red.line.pet.domain.usecase.vip.ProcessPurchaseResultUseCase(myketBillingManager)
    }

    val restorePurchasesUseCase: red.line.pet.domain.usecase.vip.RestorePurchasesUseCase by lazy {
        red.line.pet.domain.usecase.vip.RestorePurchasesUseCase(myketBillingManager)
    }

    val localBackupManager: red.line.pet.data.backup.LocalBackupManager by lazy {
        red.line.pet.data.backup.LocalBackupManager(context, database, freeVipManager)
    }
}
