package red.line.pet.domain.usecase.food

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.DataError
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.domain.model.DayOfWeekFa
import red.line.pet.domain.model.FoodSchedule
import red.line.pet.domain.repository.FoodRepository

class GetFoodSchedulesUseCase(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(petId: Long): Flow<List<FoodSchedule>> {
        return foodRepository.getFoodSchedule(petId)
    }
}

class GetTodayMealsUseCase(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(
        petId: Long,
        todayDay: DayOfWeekFa = JalaliDateHelper.getCurrentDayOfWeekFa()
    ): Flow<List<FoodSchedule>> {
        return foodRepository.getFoodSchedule(petId).map { schedules ->
            schedules
                .filter { it.isActive && (it.repeatDays.isEmpty() || it.repeatDays.contains(todayDay)) }
                .sortedBy { it.mealTime }
        }
    }
}

class AddFoodScheduleUseCase(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(schedule: FoodSchedule): AppResult<Long, DataError.Local> {
        if (schedule.petId <= 0 || (schedule.foodName.isBlank() && schedule.mealTitle.isBlank()) || schedule.amount.isBlank()) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        val id = foodRepository.insert(schedule)
        return AppResult.Success(id)
    }
}

class UpdateFoodScheduleUseCase(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(schedule: FoodSchedule): AppResult<Unit, DataError.Local> {
        if (schedule.id <= 0 || schedule.petId <= 0 || (schedule.foodName.isBlank() && schedule.mealTitle.isBlank()) || schedule.amount.isBlank()) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        foodRepository.update(schedule)
        return AppResult.Success(Unit)
    }
}

class DeleteFoodScheduleUseCase(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(scheduleId: Long): AppResult<Unit, DataError.Local> {
        if (scheduleId <= 0) {
            return AppResult.Error(DataError.Local.NOT_FOUND)
        }
        foodRepository.deleteById(scheduleId)
        return AppResult.Success(Unit)
    }
}

class ToggleMealCompletedUseCase(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(scheduleId: Long, isCompleted: Boolean): AppResult<Unit, DataError.Local> {
        if (scheduleId <= 0) {
            return AppResult.Error(DataError.Local.NOT_FOUND)
        }
        foodRepository.setMealCompleted(scheduleId, isCompleted)
        return AppResult.Success(Unit)
    }
}

class GetWeeklyFoodScheduleUseCase(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(petId: Long, filterDay: DayOfWeekFa? = null): Flow<List<FoodSchedule>> {
        return foodRepository.getFoodSchedule(petId).map { schedules ->
            if (filterDay != null) {
                schedules.filter { it.repeatDays.contains(filterDay) }.sortedBy { it.mealTime }
            } else {
                schedules.sortedBy { it.mealTime }
            }
        }
    }
}
