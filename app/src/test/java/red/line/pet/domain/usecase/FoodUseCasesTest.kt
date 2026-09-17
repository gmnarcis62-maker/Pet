package red.line.pet.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import red.line.pet.core.util.AppResult
import red.line.pet.domain.model.AmountUnit
import red.line.pet.domain.model.DayOfWeekFa
import red.line.pet.domain.model.FoodSchedule
import red.line.pet.domain.model.MealType
import red.line.pet.domain.repository.FoodRepository
import red.line.pet.domain.usecase.food.AddFoodScheduleUseCase
import red.line.pet.domain.usecase.food.DeleteFoodScheduleUseCase
import red.line.pet.domain.usecase.food.GetFoodSchedulesUseCase
import red.line.pet.domain.usecase.food.GetTodayMealsUseCase
import red.line.pet.domain.usecase.food.GetWeeklyFoodScheduleUseCase
import red.line.pet.domain.usecase.food.ToggleMealCompletedUseCase
import red.line.pet.domain.usecase.food.UpdateFoodScheduleUseCase

class FoodUseCasesTest {

    private class FakeFoodRepository : FoodRepository {
        private val schedules = mutableListOf<FoodSchedule>()

        override fun getFoodSchedule(petId: Long): Flow<List<FoodSchedule>> {
            return flowOf(schedules.filter { it.petId == petId })
        }

        override fun getAllFoodSchedules(): Flow<List<FoodSchedule>> {
            return flowOf(schedules)
        }

        override suspend fun getFoodScheduleById(id: Long): FoodSchedule? {
            return schedules.find { it.id == id }
        }

        override suspend fun insert(schedule: FoodSchedule): Long {
            val newId = (schedules.size + 1).toLong()
            schedules.add(schedule.copy(id = newId))
            return newId
        }

        override suspend fun update(schedule: FoodSchedule) {
            val index = schedules.indexOfFirst { it.id == schedule.id }
            if (index != -1) schedules[index] = schedule
        }

        override suspend fun delete(schedule: FoodSchedule) {
            schedules.removeAll { it.id == schedule.id }
        }

        override suspend fun deleteById(id: Long) {
            schedules.removeAll { it.id == id }
        }

        override suspend fun setMealCompleted(id: Long, isCompleted: Boolean) {
            val index = schedules.indexOfFirst { it.id == id }
            if (index != -1) {
                schedules[index] = schedules[index].copy(isCompleted = isCompleted)
            }
        }
    }

    @Test
    fun `test AddFoodScheduleUseCase validation and success`() = runBlocking {
        val repo = FakeFoodRepository()
        val addUseCase = AddFoodScheduleUseCase(repo)
        val getUseCase = GetFoodSchedulesUseCase(repo)

        // Invalid: missing food name and meal title
        val invalidSchedule = FoodSchedule(
            petId = 1L,
            mealTitle = "",
            foodName = "",
            amount = ""
        )
        val errorResult = addUseCase(invalidSchedule)
        assertTrue(errorResult is AppResult.Error)

        // Valid schedule
        val validSchedule = FoodSchedule(
            petId = 1L,
            mealTitle = "صبحانه لذیذ",
            mealType = MealType.BREAKFAST,
            foodName = "رویال کنین رژیمی",
            amount = "120",
            amountUnit = AmountUnit.GRAM,
            mealTime = "08:00",
            repeatDays = setOf(DayOfWeekFa.SATURDAY, DayOfWeekFa.SUNDAY)
        )
        val successResult = addUseCase(validSchedule)
        assertTrue(successResult is AppResult.Success)
        assertEquals(1L, (successResult as AppResult.Success).data)

        val list = getUseCase(1L).first()
        assertEquals(1, list.size)
        assertEquals("رویال کنین رژیمی", list[0].foodName)
        assertEquals("120", list[0].amount)
        assertEquals(2, list[0].repeatDays.size)
    }

    @Test
    fun `test ToggleMealCompletedUseCase`() = runBlocking {
        val repo = FakeFoodRepository()
        val addUseCase = AddFoodScheduleUseCase(repo)
        val getUseCase = GetFoodSchedulesUseCase(repo)
        val toggleUseCase = ToggleMealCompletedUseCase(repo)

        val schedule = FoodSchedule(
            petId = 1L,
            mealTitle = "شام",
            mealType = MealType.DINNER,
            foodName = "غذای مرطوب مرغ و کدو",
            amount = "200",
            amountUnit = AmountUnit.GRAM,
            mealTime = "20:00",
            isCompleted = false
        )
        addUseCase(schedule)

        val before = getUseCase(1L).first()[0]
        assertFalse(before.isCompleted)

        val result = toggleUseCase(before.id, true)
        assertTrue(result is AppResult.Success)

        val after = getUseCase(1L).first()[0]
        assertTrue(after.isCompleted)
    }

    @Test
    fun `test GetTodayMealsUseCase and Day Filtering`() = runBlocking {
        val repo = FakeFoodRepository()
        val addUseCase = AddFoodScheduleUseCase(repo)
        val getTodayMealsUseCase = GetTodayMealsUseCase(repo)
        val getWeeklyUseCase = GetWeeklyFoodScheduleUseCase(repo)

        val saturdayMeal = FoodSchedule(
            petId = 1L,
            mealTitle = "صبحانه شنبه",
            foodName = "غذا ۱",
            amount = "100",
            mealTime = "08:00",
            repeatDays = setOf(DayOfWeekFa.SATURDAY)
        )
        val sundayMeal = FoodSchedule(
            petId = 1L,
            mealTitle = "صبحانه یکشنبه",
            foodName = "غذا ۲",
            amount = "100",
            mealTime = "09:00",
            repeatDays = setOf(DayOfWeekFa.SUNDAY)
        )

        addUseCase(saturdayMeal)
        addUseCase(sundayMeal)

        val saturdayList = getTodayMealsUseCase(1L, DayOfWeekFa.SATURDAY).first()
        assertEquals(1, saturdayList.size)
        assertEquals("صبحانه شنبه", saturdayList[0].mealTitle)

        val filteredSunday = getWeeklyUseCase(1L, DayOfWeekFa.SUNDAY).first()
        assertEquals(1, filteredSunday.size)
        assertEquals("صبحانه یکشنبه", filteredSunday[0].mealTitle)
    }

    @Test
    fun `test DeleteFoodScheduleUseCase`() = runBlocking {
        val repo = FakeFoodRepository()
        val addUseCase = AddFoodScheduleUseCase(repo)
        val getUseCase = GetFoodSchedulesUseCase(repo)
        val deleteUseCase = DeleteFoodScheduleUseCase(repo)

        val schedule = FoodSchedule(
            petId = 1L,
            mealTitle = "میان وعده",
            foodName = "تشویقی",
            amount = "2",
            amountUnit = AmountUnit.PIECE
        )
        val addRes = addUseCase(schedule)
        val id = (addRes as AppResult.Success).data

        assertEquals(1, getUseCase(1L).first().size)

        val delRes = deleteUseCase(id)
        assertTrue(delRes is AppResult.Success)
        assertTrue(getUseCase(1L).first().isEmpty())
    }
}
