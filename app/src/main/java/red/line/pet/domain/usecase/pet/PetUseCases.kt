package red.line.pet.domain.usecase.pet

import kotlinx.coroutines.flow.Flow
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.DataError
import red.line.pet.domain.model.Pet
import red.line.pet.domain.repository.PetRepository

class GetPetsUseCase(
    private val petRepository: PetRepository
) {
    operator fun invoke(): Flow<List<Pet>> {
        return petRepository.getAllPets()
    }
}

class AddPetUseCase(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(pet: Pet): AppResult<Long, DataError.Local> {
        if (pet.name.isBlank()) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        val id = petRepository.insertPet(pet)
        return AppResult.Success(id)
    }
}

class DeletePetUseCase(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(id: Long): AppResult<Unit, DataError.Local> {
        petRepository.deletePetById(id)
        return AppResult.Success(Unit)
    }
}

class UpdatePetUseCase(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(pet: Pet): AppResult<Unit, DataError.Local> {
        if (pet.name.isBlank()) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        petRepository.updatePet(pet)
        return AppResult.Success(Unit)
    }
}
