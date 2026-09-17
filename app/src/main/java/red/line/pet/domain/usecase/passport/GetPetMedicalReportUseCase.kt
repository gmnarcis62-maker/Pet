package red.line.pet.domain.usecase.passport

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.domain.model.ExpenseCategory
import red.line.pet.domain.model.MedicalPassportReport
import red.line.pet.domain.model.WeightStatistics
import red.line.pet.domain.repository.ExpenseRepository
import red.line.pet.domain.repository.FoodRepository
import red.line.pet.domain.repository.HealthRecordRepository
import red.line.pet.domain.repository.MemoryRepository
import red.line.pet.domain.repository.PetRepository
import red.line.pet.domain.repository.ReminderRepository
import red.line.pet.domain.repository.WeightRepository

class GetPetMedicalReportUseCase(
    private val petRepository: PetRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val weightRepository: WeightRepository,
    private val foodRepository: FoodRepository,
    private val reminderRepository: ReminderRepository,
    private val memoryRepository: MemoryRepository,
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(petId: Long): MedicalPassportReport? {
        val pet = petRepository.getPetById(petId).firstOrNull() ?: return null

        val healthRecords = healthRecordRepository.getHealthRecordsForPet(petId).firstOrNull() ?: emptyList()
        val weightRecords = (weightRepository.getWeightHistory(petId).firstOrNull() ?: emptyList())
            .sortedBy { it.date }

        val currentWeight = weightRecords.lastOrNull()?.weightKg ?: pet.weightKg
        val minWeight = weightRecords.minOfOrNull { it.weightKg } ?: currentWeight
        val maxWeight = weightRecords.maxOfOrNull { it.weightKg } ?: currentWeight
        val totalChange = if (weightRecords.size >= 2) {
            weightRecords.last().weightKg - weightRecords.first().weightKg
        } else {
            0.0
        }
        val lastChange = if (weightRecords.size >= 2) {
            weightRecords.last().weightKg - weightRecords[weightRecords.size - 2].weightKg
        } else {
            0.0
        }

        val weightStats = WeightStatistics(
            currentWeightKg = currentWeight,
            minWeightKg = minWeight,
            maxWeightKg = maxWeight,
            totalChangeKg = totalChange,
            lastChangeKg = lastChange,
            totalRecordsCount = weightRecords.size,
            lastRecordedDateJalali = weightRecords.lastOrNull()?.getEffectiveJalaliDate() ?: ""
        )

        val foodSchedules = (foodRepository.getFoodSchedule(petId).firstOrNull() ?: emptyList())
            .filter { it.isActive }

        val reminders = (reminderRepository.getRemindersForPet(petId).firstOrNull() ?: emptyList())

        val memories = (memoryRepository.getMemories(petId).firstOrNull() ?: emptyList())
            .sortedByDescending { it.date }
            .take(6)

        val expenses = (expenseRepository.getExpensesForPet(petId).firstOrNull() ?: emptyList())
            .sortedByDescending { it.date }

        val totalExpense = expenses.sumOf { it.amountToman }
        val healthExpense = expenses.filter {
            it.category == ExpenseCategory.VET ||
            it.category == ExpenseCategory.MEDICINE ||
            it.category == ExpenseCategory.CLINIC ||
            it.category == ExpenseCategory.HEALTH
        }.sumOf { it.amountToman }

        val birthDateStr = pet.birthDateJalali.ifBlank { pet.birthDate }
        val ageFa = JalaliDateHelper.calculateAgeFa(birthDateStr)
        val generatedJalali = JalaliDateHelper.now().toFullString()

        return MedicalPassportReport(
            pet = pet,
            healthRecords = healthRecords,
            weightRecords = weightRecords,
            weightStats = weightStats,
            foodSchedules = foodSchedules,
            reminders = reminders,
            memories = memories,
            expenses = expenses,
            totalExpenseAmount = totalExpense,
            healthExpenseAmount = healthExpense,
            generatedAtTimestamp = System.currentTimeMillis(),
            generatedAtJalali = generatedJalali,
            ageStringFa = ageFa
        )
    }
}
