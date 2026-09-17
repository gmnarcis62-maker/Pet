package red.line.pet.core.ui

import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.PetSpecies

object AssetImageHelper {
    const val ASSET_BASE = "file:///android_asset/images"

    // Animals
    const val ANIMAL_CAT = "$ASSET_BASE/pets/cat.webp"
    const val ANIMAL_DOG = "$ASSET_BASE/pets/dog.webp"
    const val ANIMAL_BIRD = "$ASSET_BASE/pets/bird.webp"
    const val ANIMAL_RABBIT = "$ASSET_BASE/pets/rabbit.jpg"
    const val ANIMAL_RODENT = "$ASSET_BASE/pets/rodent.jpg"
    const val ANIMAL_HORSE = "$ASSET_BASE/pets/horse.jpg"
    const val ANIMAL_OTHER = "$ASSET_BASE/pets/other.webp"

    data class DefaultPetAvatar(
        val id: String,
        val titleFa: String,
        val assetPath: String,
        val species: PetSpecies
    )

    val DEFAULT_AVATARS = listOf(
        DefaultPetAvatar("dog", "سگ", ANIMAL_DOG, PetSpecies.DOG),
        DefaultPetAvatar("cat", "گربه", ANIMAL_CAT, PetSpecies.CAT),
        DefaultPetAvatar("bird", "پرنده", ANIMAL_BIRD, PetSpecies.BIRD),
        DefaultPetAvatar("rabbit", "خرگوش", ANIMAL_RABBIT, PetSpecies.RABBIT),
        DefaultPetAvatar("rodent", "جونده", ANIMAL_RODENT, PetSpecies.RODENT),
        DefaultPetAvatar("horse", "اسب", ANIMAL_HORSE, PetSpecies.OTHER),
        DefaultPetAvatar("other", "سایر", ANIMAL_OTHER, PetSpecies.OTHER)
    )

    // Onboarding
    const val ONBOARDING_WELCOME = "$ASSET_BASE/onboarding/welcome_pet.webp"
    const val ONBOARDING_HEALTH = "$ASSET_BASE/onboarding/health_pet.webp"
    const val ONBOARDING_CARE = "$ASSET_BASE/onboarding/care_pet.webp"

    // Feature Cards
    const val CARD_MEDICAL = "$ASSET_BASE/cards/medical.webp"
    const val CARD_FOOD = "$ASSET_BASE/cards/food.webp"
    const val CARD_WEIGHT = "$ASSET_BASE/cards/weight.webp"
    const val CARD_EXPENSE = "$ASSET_BASE/cards/expense.webp"
    const val CARD_MEMORY = "$ASSET_BASE/cards/memory.webp"

    // Empty States
    const val EMPTY_NO_PET = "$ASSET_BASE/empty_states/no_pet.webp"
    const val EMPTY_NO_DATA = "$ASSET_BASE/empty_states/no_data.webp"

    fun getSpeciesPlaceholder(species: PetSpecies): String {
        return when (species) {
            PetSpecies.CAT -> ANIMAL_CAT
            PetSpecies.DOG -> ANIMAL_DOG
            PetSpecies.BIRD -> ANIMAL_BIRD
            PetSpecies.RABBIT -> ANIMAL_RABBIT
            PetSpecies.RODENT -> ANIMAL_RODENT
            PetSpecies.OTHER -> ANIMAL_OTHER
        }
    }

    fun getPetImage(pet: Pet): String {
        return when {
            pet.avatarUri.isNotBlank() -> pet.avatarUri
            pet.imagePath.isNotBlank() -> pet.imagePath
            else -> getSpeciesPlaceholder(pet.species)
        }
    }
}
