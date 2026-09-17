package red.line.pet.domain.usecase.weight

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.DataError
import red.line.pet.domain.model.WeightRecord
import red.line.pet.domain.model.WeightStatistics
import red.line.pet.domain.model.WeightTimeRange
import red.line.pet.domain.repository.WeightRepository

class GetWeightRecordsUseCase(
    private val weightRepository: WeightRepository
) {
    operator fun invoke(petId: Long): Flow<List<WeightRecord>> {
        return weightRepository.getWeightHistory(petId).map { records ->
            records.sortedByDescending { it.date }
        }
    }
}

class GetFilteredWeightRecordsUseCase(
    private val weightRepository: WeightRepository
) {
    operator fun invoke(petId: Long, timeRange: WeightTimeRange): Flow<List<WeightRecord>> {
        return weightRepository.getWeightHistory(petId).map { records ->
            val sorted = records.sortedByDescending { it.date }
            if (timeRange.days == null) {
                sorted
            } else {
                val cutoff = System.currentTimeMillis() - (timeRange.days.toLong() * 24 * 60 * 60 * 1000L)
                sorted.filter { it.date >= cutoff }
            }
        }
    }
}

class GetWeightStatisticsUseCase(
    private val weightRepository: WeightRepository
) {
    operator fun invoke(petId: Long): Flow<WeightStatistics> {
        return weightRepository.getWeightHistory(petId).map { records ->
            calculateStatistics(records)
        }
    }

    fun calculateStatistics(records: List<WeightRecord>): WeightStatistics {
        if (records.isEmpty()) {
            return WeightStatistics()
        }

        val chronological = records.sortedBy { it.date }
        val latest = chronological.last()
        val first = chronological.first()

        val weights = chronological.map { it.weightKg }
        val minWeight = weights.minOrNull() ?: 0.0
        val maxWeight = weights.maxOrNull() ?: 0.0
        val currentWeight = latest.weightKg

        val totalChange = currentWeight - first.weightKg
        val lastChange = if (chronological.size > 1) {
            currentWeight - chronological[chronological.size - 2].weightKg
        } else {
            0.0
        }

        return WeightStatistics(
            currentWeightKg = currentWeight,
            minWeightKg = minWeight,
            maxWeightKg = maxWeight,
            totalChangeKg = totalChange,
            lastChangeKg = lastChange,
            totalRecordsCount = records.size,
            lastRecordedDateJalali = latest.getEffectiveJalaliDate()
        )
    }
}

class AddWeightRecordUseCase(
    private val weightRepository: WeightRepository
) {
    suspend operator fun invoke(record: WeightRecord): AppResult<Long, DataError.Local> {
        if (record.petId <= 0) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        if (record.weightKg <= 0.0 || record.weightKg > 500.0) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        val id = weightRepository.insert(record)
        return AppResult.Success(id)
    }
}

class UpdateWeightRecordUseCase(
    private val weightRepository: WeightRepository
) {
    suspend operator fun invoke(record: WeightRecord): AppResult<Unit, DataError.Local> {
        if (record.id <= 0 || record.weightKg <= 0.0 || record.weightKg > 500.0) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        weightRepository.update(record)
        return AppResult.Success(Unit)
    }
}

class DeleteWeightRecordUseCase(
    private val weightRepository: WeightRepository
) {
    suspend operator fun invoke(id: Long): AppResult<Unit, DataError.Local> {
        if (id <= 0) {
            return AppResult.Error(DataError.Local.NOT_FOUND)
        }
        weightRepository.deleteById(id)
        return AppResult.Success(Unit)
    }
}
