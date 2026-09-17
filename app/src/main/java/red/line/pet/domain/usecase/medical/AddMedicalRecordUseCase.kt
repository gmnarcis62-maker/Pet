package red.line.pet.domain.usecase.medical

import red.line.pet.domain.model.MedicalRecord
import red.line.pet.domain.repository.MedicalRepository

class AddMedicalRecordUseCase(
    private val repository: MedicalRepository
) {
    suspend operator fun invoke(record: MedicalRecord): Long {
        require(record.title.isNotBlank()) { "عنوان پرونده پزشکی نباید خالی باشد" }
        return repository.insert(record)
    }
}
