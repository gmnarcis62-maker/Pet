package red.line.pet.domain.usecase.health

import kotlinx.coroutines.flow.Flow
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.DataError
import red.line.pet.domain.model.HealthRecord
import red.line.pet.domain.repository.HealthRecordRepository

class GetHealthRecordsUseCase(
    private val healthRecordRepository: HealthRecordRepository
) {
    operator fun invoke(petId: Long? = null): Flow<List<HealthRecord>> {
        return if (petId != null) {
            healthRecordRepository.getHealthRecordsForPet(petId)
        } else {
            healthRecordRepository.getAllHealthRecords()
        }
    }
}

class GetUpcomingRemindersUseCase(
    private val healthRecordRepository: HealthRecordRepository
) {
    operator fun invoke(petId: Long? = null): Flow<List<HealthRecord>> {
        return healthRecordRepository.getUpcomingReminders(petId)
    }
}

class AddHealthRecordUseCase(
    private val healthRecordRepository: HealthRecordRepository
) {
    suspend operator fun invoke(record: HealthRecord): AppResult<Long, DataError.Local> {
        if (record.title.isBlank()) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        val id = healthRecordRepository.insertRecord(record)
        return AppResult.Success(id)
    }
}
