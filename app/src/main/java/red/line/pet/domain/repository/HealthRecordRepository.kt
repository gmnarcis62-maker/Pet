package red.line.pet.domain.repository

import kotlinx.coroutines.flow.Flow
import red.line.pet.domain.model.HealthRecord

interface HealthRecordRepository {
    fun getAllHealthRecords(): Flow<List<HealthRecord>>
    fun getHealthRecordsForPet(petId: Long): Flow<List<HealthRecord>>
    fun getUpcomingReminders(petId: Long? = null): Flow<List<HealthRecord>>
    suspend fun insertRecord(record: HealthRecord): Long
    suspend fun updateRecord(record: HealthRecord)
    suspend fun deleteRecord(record: HealthRecord)
    suspend fun deleteRecordById(id: Long)
}
