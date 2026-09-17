package red.line.pet.domain.usecase.memory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.DataError
import red.line.pet.domain.model.Memory
import red.line.pet.domain.model.MemorySortType
import red.line.pet.domain.repository.MemoryRepository

class GetMemoriesUseCase(
    private val memoryRepository: MemoryRepository
) {
    operator fun invoke(petId: Long, sortType: MemorySortType = MemorySortType.NEWEST): Flow<List<Memory>> {
        return memoryRepository.getMemories(petId).map { list ->
            when (sortType) {
                MemorySortType.NEWEST -> list.sortedByDescending { it.date }
                MemorySortType.OLDEST -> list.sortedBy { it.date }
            }
        }
    }
}

class SearchMemoriesUseCase(
    private val memoryRepository: MemoryRepository
) {
    operator fun invoke(
        petId: Long,
        query: String,
        sortType: MemorySortType = MemorySortType.NEWEST
    ): Flow<List<Memory>> {
        return memoryRepository.getMemories(petId).map { list ->
            val filtered = if (query.isBlank()) {
                list
            } else {
                list.filter { it.matchesQuery(query) }
            }
            when (sortType) {
                MemorySortType.NEWEST -> filtered.sortedByDescending { it.date }
                MemorySortType.OLDEST -> filtered.sortedBy { it.date }
            }
        }
    }
}

class AddMemoryUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memory: Memory): AppResult<Long, DataError.Local> {
        if (memory.petId <= 0) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        if (memory.title.trim().isBlank()) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        if (memory.imagePath.trim().isBlank()) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        val id = memoryRepository.insert(memory.copy(title = memory.title.trim(), description = memory.description.trim()))
        return AppResult.Success(id)
    }
}

class UpdateMemoryUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memory: Memory): AppResult<Unit, DataError.Local> {
        if (memory.id <= 0 || memory.title.trim().isBlank() || memory.imagePath.trim().isBlank()) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        memoryRepository.update(memory.copy(title = memory.title.trim(), description = memory.description.trim()))
        return AppResult.Success(Unit)
    }
}

class DeleteMemoryUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(id: Long): AppResult<Unit, DataError.Local> {
        if (id <= 0) {
            return AppResult.Error(DataError.Local.NOT_FOUND)
        }
        memoryRepository.deleteById(id)
        return AppResult.Success(Unit)
    }
}
