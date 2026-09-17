package red.line.pet.domain.model

enum class MedicalType(val titleFa: String) {
    VACCINE("واکسن"),
    MEDICINE("دارو"),
    ILLNESS("بیماری"),
    VET_VISIT("ویزیت دامپزشک")
}

data class MedicalRecord(
    val id: Long = 0,
    val petId: Long,
    val type: MedicalType,
    val title: String,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val reminderDate: Long? = null
)
