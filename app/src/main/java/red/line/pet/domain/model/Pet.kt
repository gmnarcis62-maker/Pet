package red.line.pet.domain.model

enum class PetSpecies(val titleFa: String, val emoji: String) {
    DOG("سگ", "🐶"),
    CAT("گربه", "🐱"),
    BIRD("پرنده", "🦜"),
    RODENT("جونده", "🐹"),
    RABBIT("خرگوش", "🐰"),
    OTHER("سایر", "🐾")
}

enum class PetGender(val titleFa: String) {
    MALE("نر"),
    FEMALE("ماده")
}

data class Pet(
    val id: Long = 0,
    val name: String,
    val imagePath: String = "",
    val species: PetSpecies,
    val breed: String = "",
    val gender: PetGender = PetGender.MALE,
    val birthDate: String = "",
    val color: String = "",
    val weight: Double = 0.0,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val birthDateJalali: String = birthDate,
    val weightKg: Double = weight,
    val microchipId: String = "",
    val avatarUri: String = imagePath,
    val notes: String = description
)
