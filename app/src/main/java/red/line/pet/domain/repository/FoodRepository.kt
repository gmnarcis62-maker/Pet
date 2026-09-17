package red.line.pet.domain.repository

import kotlinx.coroutines.flow.Flow
import red.line.pet.domain.model.FoodSchedule

interface FoodRepository {
    fun getFoodSchedule(petId: Long): Flow<List<FoodSchedule>>
    fun getAllFoodSchedules(): Flow<List<FoodSchedule>>
    suspend fun getFoodScheduleById(id: Long): FoodSchedule?
    suspend fun insert(schedule: FoodSchedule): Long
    suspend fun update(schedule: FoodSchedule)
    suspend fun delete(schedule: FoodSchedule)
    suspend fun deleteById(id: Long)
    suspend fun setMealCompleted(id: Long, isCompleted: Boolean)
}
