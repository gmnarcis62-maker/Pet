package red.line.pet.domain.usecase.medical

import kotlinx.coroutines.flow.Flow
import red.line.pet.domain.model.MedicalRecord
import red.line.pet.domain.repository.MedicalRepository

class GetMedicalRecordsUseCase(
    private val repository: MedicalRepository
) {
    operator fun invoke(petId: Long): Flow<List<MedicalRecord>> {
        return repository.getMedicalRecordsByPet(petId)
    }

    operator fun invoke(): Flow<List<MedicalRecord>> {
        return repository.getAllMedicalRecords()
    }
}
