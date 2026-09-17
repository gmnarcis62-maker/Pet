package red.line.pet.domain.repository

import kotlinx.coroutines.flow.Flow
import red.line.pet.domain.model.Pet

interface PetRepository {
    fun getAllPets(): Flow<List<Pet>>
    fun getPetById(id: Long): Flow<Pet?>
    suspend fun insertPet(pet: Pet): Long
    suspend fun updatePet(pet: Pet)
    suspend fun deletePet(pet: Pet)
    suspend fun deletePetById(id: Long)
}
