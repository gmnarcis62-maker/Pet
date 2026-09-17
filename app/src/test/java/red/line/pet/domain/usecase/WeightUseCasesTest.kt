package red.line.pet.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import red.line.pet.core.util.AppResult
import red.line.pet.domain.model.WeightRecord
import red.line.pet.domain.model.WeightTimeRange
import red.line.pet.domain.model.WeightUnit
import red.line.pet.domain.repository.WeightRepository
import red.line.pet.domain.usecase.weight.AddWeightRecordUseCase
import red.line.pet.domain.usecase.weight.DeleteWeightRecordUseCase
import red.line.pet.domain.usecase.weight.GetFilteredWeightRecordsUseCase
import red.line.pet.domain.usecase.weight.GetWeightRecordsUseCase
import red.line.pet.domain.usecase.weight.GetWeightStatisticsUseCase
import red.line.pet.domain.usecase.weight.UpdateWeightRecordUseCase

class WeightUseCasesTest {

    private class FakeWeightRepository : WeightRepository {
        private val records = mutableListOf<WeightRecord>()

        override fun getWeightHistory(petId: Long): Flow<List<WeightRecord>> {
            return flowOf(records.filter { it.petId == petId })
        }

        override suspend fun getWeightRecordById(id: Long): WeightRecord? {
            return records.find { it.id == id }
        }

        override suspend fun insert(weight: WeightRecord): Long {
            val newId = (records.size + 1).toLong()
            records.add(weight.copy(id = newId))
            return newId
        }

        override suspend fun update(weight: WeightRecord) {
            val index = records.indexOfFirst { it.id == weight.id }
            if (index != -1) records[index] = weight
        }

        override suspend fun delete(weight: WeightRecord) {
            records.removeAll { it.id == weight.id }
        }

        override suspend fun deleteById(id: Long) {
            records.removeAll { it.id == id }
        }
    }

    @Test
    fun `test AddWeightRecordUseCase validation and insertion`() = runBlocking {
        val repo = FakeWeightRepository()
        val addUseCase = AddWeightRecordUseCase(repo)
        val getUseCase = GetWeightRecordsUseCase(repo)

        // Invalid: negative or zero weight
        val invalidRecord = WeightRecord(
            petId = 1L,
            weightKg = 0.0
        )
        val errResult = addUseCase(invalidRecord)
        assertTrue(errResult is AppResult.Error)

        // Invalid pet id
        val invalidPetRecord = WeightRecord(
            petId = 0L,
            weightKg = 4.5
        )
        val errPetResult = addUseCase(invalidPetRecord)
        assertTrue(errPetResult is AppResult.Error)

        // Valid record
        val validRecord = WeightRecord(
            petId = 1L,
            weightKg = 4.8,
            unit = WeightUnit.KILOGRAM,
            jalaliDate = "1403/06/20",
            notes = "چکاپ ماهانه"
        )
        val successResult = addUseCase(validRecord)
        assertTrue(successResult is AppResult.Success)
        assertEquals(1L, (successResult as AppResult.Success).data)

        val list = getUseCase(1L).first()
        assertEquals(1, list.size)
        assertEquals(4.8, list[0].weightKg, 0.001)
        assertEquals("1403/06/20", list[0].jalaliDate)
    }

    @Test
    fun `test WeightStatistics calculation`() = runBlocking {
        val repo = FakeWeightRepository()
        val addUseCase = AddWeightRecordUseCase(repo)
        val getStatsUseCase = GetWeightStatisticsUseCase(repo)

        val now = System.currentTimeMillis()
        val r1 = WeightRecord(petId = 1L, weightKg = 3.5, date = now - 30000)
        val r2 = WeightRecord(petId = 1L, weightKg = 4.0, date = now - 20000)
        val r3 = WeightRecord(petId = 1L, weightKg = 4.5, date = now - 10000)
        val r4 = WeightRecord(petId = 1L, weightKg = 4.2, date = now)

        addUseCase(r1)
        addUseCase(r2)
        addUseCase(r3)
        addUseCase(r4)

        val stats = getStatsUseCase(1L).first()
        assertEquals(4.2, stats.currentWeightKg, 0.001)
        assertEquals(3.5, stats.minWeightKg, 0.001)
        assertEquals(4.5, stats.maxWeightKg, 0.001)
        assertEquals(0.7, stats.totalChangeKg, 0.001) // 4.2 - 3.5 = +0.7
        assertEquals(-0.3, stats.lastChangeKg, 0.001) // 4.2 - 4.5 = -0.3
        assertEquals(4, stats.totalRecordsCount)
    }

    @Test
    fun `test FilteredWeightRecords by time range`() = runBlocking {
        val repo = FakeWeightRepository()
        val addUseCase = AddWeightRecordUseCase(repo)
        val filterUseCase = GetFilteredWeightRecordsUseCase(repo)

        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        // Record from 3 days ago
        addUseCase(WeightRecord(petId = 1L, weightKg = 4.0, date = now - (3 * oneDay)))
        // Record from 20 days ago
        addUseCase(WeightRecord(petId = 1L, weightKg = 3.8, date = now - (20 * oneDay)))
        // Record from 50 days ago
        addUseCase(WeightRecord(petId = 1L, weightKg = 3.5, date = now - (50 * oneDay)))

        // 7 days filter should only return 1 record
        val last7Days = filterUseCase(1L, WeightTimeRange.LAST_7_DAYS).first()
        assertEquals(1, last7Days.size)

        // 30 days filter should return 2 records
        val last30Days = filterUseCase(1L, WeightTimeRange.LAST_30_DAYS).first()
        assertEquals(2, last30Days.size)

        // ALL filter should return all 3 records
        val allRecords = filterUseCase(1L, WeightTimeRange.ALL).first()
        assertEquals(3, allRecords.size)
    }

    @Test
    fun `test Update and Delete WeightRecord`() = runBlocking {
        val repo = FakeWeightRepository()
        val addUseCase = AddWeightRecordUseCase(repo)
        val updateUseCase = UpdateWeightRecordUseCase(repo)
        val deleteUseCase = DeleteWeightRecordUseCase(repo)
        val getUseCase = GetWeightRecordsUseCase(repo)

        val addResult = addUseCase(WeightRecord(petId = 1L, weightKg = 5.0, notes = "ثبت اولیه"))
        val id = (addResult as AppResult.Success).data

        val updatedRecord = WeightRecord(id = id, petId = 1L, weightKg = 5.2, notes = "اصلاح شده")
        val updateResult = updateUseCase(updatedRecord)
        assertTrue(updateResult is AppResult.Success)

        val recordsAfterUpdate = getUseCase(1L).first()
        assertEquals(1, recordsAfterUpdate.size)
        assertEquals(5.2, recordsAfterUpdate[0].weightKg, 0.001)
        assertEquals("اصلاح شده", recordsAfterUpdate[0].notes)

        val deleteResult = deleteUseCase(id)
        assertTrue(deleteResult is AppResult.Success)
        assertTrue(getUseCase(1L).first().isEmpty())
    }
}
