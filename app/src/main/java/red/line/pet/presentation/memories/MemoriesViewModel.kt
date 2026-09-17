package red.line.pet.presentation.memories

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import red.line.pet.core.billing.VipFeature
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.InternalImageStorageHelper
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.domain.model.Memory
import red.line.pet.domain.model.MemorySortType
import red.line.pet.domain.model.Pet
import red.line.pet.domain.usecase.memory.AddMemoryUseCase
import red.line.pet.domain.usecase.memory.DeleteMemoryUseCase
import red.line.pet.domain.usecase.memory.GetMemoriesUseCase
import red.line.pet.domain.usecase.memory.SearchMemoriesUseCase
import red.line.pet.domain.usecase.memory.UpdateMemoryUseCase
import red.line.pet.domain.usecase.pet.GetPetsUseCase
import red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase

enum class MemoryDialogState {
    NONE,
    ADD,
    EDIT,
    DETAIL,
    DELETE_CONFIRM
}

data class MemoriesUiState(
    val isLoading: Boolean = true,
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val memories: List<Memory> = emptyList(),
    val allMemoriesCountForPet: Int = 0,
    val searchQuery: String = "",
    val sortType: MemorySortType = MemorySortType.NEWEST,
    val selectedMemory: Memory? = null,
    val dialogState: MemoryDialogState = MemoryDialogState.NONE,
    val showVipDialog: Boolean = false,
    val errorMessage: String? = null,
    val isSavingImage: Boolean = false
)

class MemoriesViewModel(
    private val getPetsUseCase: GetPetsUseCase,
    private val getMemoriesUseCase: GetMemoriesUseCase,
    private val searchMemoriesUseCase: SearchMemoriesUseCase,
    private val addMemoryUseCase: AddMemoryUseCase,
    private val updateMemoryUseCase: UpdateMemoryUseCase,
    private val deleteMemoryUseCase: DeleteMemoryUseCase,
    private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoriesUiState())
    val uiState: StateFlow<MemoriesUiState> = _uiState.asStateFlow()

    init {
        loadPets()
    }

    private fun loadPets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPetsUseCase().collectLatest { petsList ->
                val currentSelected = _uiState.value.selectedPet
                val newSelected = petsList.find { it.id == currentSelected?.id } ?: petsList.firstOrNull()
                _uiState.update {
                    it.copy(
                        pets = petsList,
                        selectedPet = newSelected,
                        isLoading = petsList.isEmpty()
                    )
                }
                newSelected?.let { loadMemoriesForPet(it.id) }
            }
        }
    }

    fun selectPet(pet: Pet) {
        if (_uiState.value.selectedPet?.id == pet.id) return
        _uiState.update { it.copy(selectedPet = pet, searchQuery = "") }
        loadMemoriesForPet(pet.id)
    }

    fun setSortType(sortType: MemorySortType) {
        _uiState.update { it.copy(sortType = sortType) }
        _uiState.value.selectedPet?.let { loadMemoriesForPet(it.id) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _uiState.value.selectedPet?.let { loadMemoriesForPet(it.id) }
    }

    private fun loadMemoriesForPet(petId: Long) {
        viewModelScope.launch {
            val query = _uiState.value.searchQuery
            val sort = _uiState.value.sortType
            val flow = if (query.isBlank()) {
                getMemoriesUseCase(petId, sort)
            } else {
                searchMemoriesUseCase(petId, query, sort)
            }
            flow.collectLatest { list ->
                _uiState.update {
                    it.copy(
                        memories = list,
                        allMemoriesCountForPet = if (query.isBlank()) list.size else it.allMemoriesCountForPet,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun openAddMemoryDialog() {
        viewModelScope.launch {
            if (checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.MEMORIES, _uiState.value.memories.size)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(showVipDialog = true) }
                    return@launch
                }
            }
            _uiState.update {
                it.copy(
                    dialogState = MemoryDialogState.ADD,
                    selectedMemory = null,
                    errorMessage = null
                )
            }
        }
    }

    fun openDetailDialog(memory: Memory) {
        _uiState.update {
            it.copy(
                dialogState = MemoryDialogState.DETAIL,
                selectedMemory = memory,
                errorMessage = null
            )
        }
    }

    fun openEditMemoryDialog(memory: Memory) {
        _uiState.update {
            it.copy(
                dialogState = MemoryDialogState.EDIT,
                selectedMemory = memory,
                errorMessage = null
            )
        }
    }

    fun openDeleteConfirmDialog(memory: Memory) {
        _uiState.update {
            it.copy(
                dialogState = MemoryDialogState.DELETE_CONFIRM,
                selectedMemory = memory,
                errorMessage = null
            )
        }
    }

    fun closeDialog() {
        _uiState.update {
            it.copy(
                dialogState = MemoryDialogState.NONE,
                errorMessage = null
            )
        }
    }

    fun onDismissVipDialog() {
        _uiState.update { it.copy(showVipDialog = false) }
    }

    fun saveMemoryWithUri(
        context: Context,
        imageUri: Uri?,
        existingImagePath: String,
        title: String,
        description: String,
        targetPetId: Long,
        memoryIdToUpdate: Long? = null
    ) {
        if (title.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "لطفاً عنوان خاطره را وارد کنید.") }
            return
        }

        viewModelScope.launch {
            if (memoryIdToUpdate == null && checkFeatureAccessUseCase != null) {
                val access = checkFeatureAccessUseCase(VipFeature.MEMORIES, _uiState.value.memories.size)
                if (access is AppResult.Error) {
                    _uiState.update { it.copy(dialogState = MemoryDialogState.NONE, showVipDialog = true) }
                    return@launch
                }
            }
            _uiState.update { it.copy(isSavingImage = true, errorMessage = null) }

            val finalImagePath = if (imageUri != null) {
                InternalImageStorageHelper.saveImageToInternalStorage(context, imageUri)
            } else {
                existingImagePath
            }

            if (finalImagePath.isBlank()) {
                _uiState.update {
                    it.copy(
                        isSavingImage = false,
                        errorMessage = "لطفاً یک تصویر برای خاطره انتخاب کنید."
                    )
                }
                return@launch
            }

            if (memoryIdToUpdate != null && memoryIdToUpdate > 0) {
                // Update
                val memoryToUpdate = Memory(
                    id = memoryIdToUpdate,
                    petId = targetPetId,
                    imagePath = finalImagePath,
                    title = title.trim(),
                    description = description.trim(),
                    date = _uiState.value.selectedMemory?.date ?: System.currentTimeMillis()
                )
                when (val result = updateMemoryUseCase(memoryToUpdate)) {
                    is AppResult.Success -> {
                        _uiState.update {
                            it.copy(
                                dialogState = MemoryDialogState.NONE,
                                selectedMemory = null,
                                isSavingImage = false
                            )
                        }
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSavingImage = false,
                                errorMessage = "خطا در ویرایش خاطره"
                            )
                        }
                    }
                }
            } else {
                // Insert New
                val newMemory = Memory(
                    petId = targetPetId,
                    imagePath = finalImagePath,
                    title = title.trim(),
                    description = description.trim(),
                    date = System.currentTimeMillis()
                )
                when (val result = addMemoryUseCase(newMemory)) {
                    is AppResult.Success -> {
                        _uiState.update {
                            it.copy(
                                dialogState = MemoryDialogState.NONE,
                                selectedMemory = null,
                                isSavingImage = false
                            )
                        }
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSavingImage = false,
                                errorMessage = "خطا در ثبت خاطره"
                            )
                        }
                    }
                }
            }
        }
    }

    fun confirmDeleteSelectedMemory() {
        val memory = _uiState.value.selectedMemory ?: return
        viewModelScope.launch {
            // Delete physical internal file if applicable
            InternalImageStorageHelper.deleteInternalImage(memory.imagePath)

            when (deleteMemoryUseCase(memory.id)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            dialogState = MemoryDialogState.NONE,
                            selectedMemory = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(errorMessage = "خطا در حذف خاطره") }
                }
            }
        }
    }

    class Factory(
        private val getPetsUseCase: GetPetsUseCase,
        private val getMemoriesUseCase: GetMemoriesUseCase,
        private val searchMemoriesUseCase: SearchMemoriesUseCase,
        private val addMemoryUseCase: AddMemoryUseCase,
        private val updateMemoryUseCase: UpdateMemoryUseCase,
        private val deleteMemoryUseCase: DeleteMemoryUseCase,
        private val checkFeatureAccessUseCase: CheckFeatureAccessUseCase? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MemoriesViewModel(
                getPetsUseCase,
                getMemoriesUseCase,
                searchMemoriesUseCase,
                addMemoryUseCase,
                updateMemoryUseCase,
                deleteMemoryUseCase,
                checkFeatureAccessUseCase
            ) as T
        }
    }
}
