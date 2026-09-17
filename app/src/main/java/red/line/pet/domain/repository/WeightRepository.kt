package red.line.pet.domain.repository

import kotlinx.coroutines.flow.Flow
import red.line.pet.domain.model.WeightRecord

interface WeightRepository {
    fun getWeightHistory(petId: Long): Flow<List<WeightRecord>>
    suspend fun getWeightRecordById(id: Long): WeightRecord?
    suspend fun insert(weight: WeightRecord): Long
    suspend fun update(weight: WeightRecord)
    suspend fun delete(weight: WeightRecord)
    suspend fun deleteById(id: Long)
}
