package red.line.pet.domain.repository

import kotlinx.coroutines.flow.Flow
import red.line.pet.domain.model.Memory

interface MemoryRepository {
    fun getMemories(petId: Long): Flow<List<Memory>>
    fun getAllMemories(): Flow<List<Memory>>
    suspend fun getMemoryById(id: Long): Memory?
    suspend fun insert(memory: Memory): Long
    suspend fun update(memory: Memory)
    suspend fun delete(memory: Memory)
    suspend fun deleteById(id: Long)
}
