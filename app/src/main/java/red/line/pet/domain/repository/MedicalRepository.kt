package red.line.pet.domain.repository

import kotlinx.coroutines.flow.Flow
import red.line.pet.domain.model.MedicalRecord

interface MedicalRepository {
    fun getMedicalRecordsByPet(petId: Long): Flow<List<MedicalRecord>>
    fun getAllMedicalRecords(): Flow<List<MedicalRecord>>
    fun getUpcomingReminders(): Flow<List<MedicalRecord>>
    suspend fun insert(record: MedicalRecord): Long
    suspend fun delete(record: MedicalRecord)
    suspend fun deleteById(id: Long)
}
