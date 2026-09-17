package red.line.pet.presentation.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import red.line.pet.core.billing.VipFeature
import red.line.pet.core.util.AppResult
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.PetGender
import red.line.pet.domain.model.PetSpecies
import red.line.pet.domain.usecase.pet.AddPetUseCase
import red.line.pet.domain.usecase.pet.DeletePetUseCase
import red.line.pet.domain.usecase.pet.GetPetsUseCase
import red.line.pet.domain.usecase.pet.UpdatePetUseCase
import red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase

data class PetsUiState(
    val pets: List<Pet> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val showVipDialog: Boolean = false,
    val errorMessage: String? = null
)

class PetsViewModel(
    private val getPetsUseCase: GetPetsUseCase,
    private val addPetUseCase: AddPetUseCase,
    private val deletePetUseCase: DeletePetUseCase,
    private val updatePetUseCase: UpdatePetUseCase? = null,
    private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState(isLoading = true))
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    init {
        loadPets()
    }

    private fun loadPets() {
        viewModelScope.launch {
            getPetsUseCase().collect { petsList ->
                if (petsList.isEmpty()) {
                    seedInitialData()
                } else {
                    _uiState.update { it.copy(pets = petsList, isLoading = false) }
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        val sample1 = Pet(
            name = "لئو",
            species = PetSpecies.DOG,
            breed = "ژرمن شپرد اصیل",
            birthDateJalali = "۱۴۰۱/۰۴/۱۵",
            gender = PetGender.MALE,
            weightKg = 28.5,
            notes = "بسیار باهوش، عاشق پیاده‌روی عصرگاهی و تمرین اطاعت"
        )
        val sample2 = Pet(
            name = "میشا",
            species = PetSpecies.CAT,
            breed = "پرشین سوپرفلت",
            birthDateJalali = "۱۴۰۲/۰۸/۲۰",
            gender = PetGender.FEMALE,
            weightKg = 4.2,
            notes = "واکسینه کامل، حساس به غذای خشک غیراستاندارد"
        )
        addPetUseCase(sample1)
        addPetUseCase(sample2)
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onAddPetClick() {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val accessCheck = checkFeatureAccessUseCase(VipFeature.PETS, _uiState.value.pets.size)
                if (accessCheck is AppResult.Error) {
                    _uiState.update { it.copy(showVipDialog = true) }
                    return@launch
                }
            }
            _uiState.update { it.copy(showAddDialog = true) }
        }
    }

    fun onDismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun onDismissVipDialog() {
        _uiState.update { it.copy(showVipDialog = false) }
    }

    fun addNewPet(
        name: String,
        species: PetSpecies,
        breed: String,
        birthDateJalali: String,
        gender: PetGender,
        weightKg: Double,
        notes: String,
        avatarUri: String = ""
    ) {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val accessCheck = checkFeatureAccessUseCase(VipFeature.PETS, _uiState.value.pets.size)
                if (accessCheck is AppResult.Error) {
                    _uiState.update { it.copy(showAddDialog = false, showVipDialog = true) }
                    return@launch
                }
            }
            val pet = Pet(
                name = name,
                species = species,
                breed = breed,
                birthDateJalali = birthDateJalali,
                gender = gender,
                weightKg = weightKg,
                notes = notes,
                avatarUri = avatarUri,
                imagePath = avatarUri
            )
            addPetUseCase(pet)
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    fun updatePetAvatar(petId: Long, newAvatarUri: String) {
        viewModelScope.launch {
            val currentPet = _uiState.value.pets.find { it.id == petId } ?: return@launch
            val updatedPet = currentPet.copy(
                avatarUri = newAvatarUri,
                imagePath = newAvatarUri
            )
            updatePetUseCase?.invoke(updatedPet)
        }
    }

    fun updatePet(pet: Pet) {
        viewModelScope.launch {
            updatePetUseCase?.invoke(pet)
        }
    }

    fun deletePet(petId: Long) {
        viewModelScope.launch {
            deletePetUseCase(petId)
        }
    }

    class Factory(
        private val getPetsUseCase: GetPetsUseCase,
        private val addPetUseCase: AddPetUseCase,
        private val deletePetUseCase: DeletePetUseCase,
        private val updatePetUseCase: UpdatePetUseCase? = null,
        private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PetsViewModel(
                getPetsUseCase,
                addPetUseCase,
                deletePetUseCase,
                updatePetUseCase,
                checkFeatureAccessUseCase
            ) as T
        }
    }
}
