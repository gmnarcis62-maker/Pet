package red.line.pet.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import red.line.pet.domain.model.Expense
import red.line.pet.domain.model.ExpenseCategory
import red.line.pet.domain.model.ExpenseSummary
import red.line.pet.domain.usecase.expense.AddExpenseUseCase
import red.line.pet.domain.usecase.expense.GetExpenseSummaryUseCase
import red.line.pet.domain.usecase.expense.GetExpensesUseCase

import red.line.pet.domain.model.Pet
import red.line.pet.domain.usecase.pet.GetPetsUseCase

data class ExpensesUiState(
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val expenses: List<Expense> = emptyList(),
    val summary: ExpenseSummary = ExpenseSummary(0L, emptyMap()),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false
)

class ExpensesViewModel(
    private val getPetsUseCase: GetPetsUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getExpenseSummaryUseCase: GetExpenseSummaryUseCase,
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState(isLoading = true))
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    private var expensesJob: kotlinx.coroutines.Job? = null
    private var summaryJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getPetsUseCase().collect { petsList ->
                _uiState.update { state ->
                    val currentSelected = state.selectedPet
                    val newSelected = if (currentSelected != null && petsList.any { it.id == currentSelected.id }) {
                        petsList.first { it.id == currentSelected.id }
                    } else {
                        petsList.firstOrNull()
                    }
                    state.copy(pets = petsList, selectedPet = newSelected)
                }
                observeExpensesForSelectedPet()
            }
        }
    }

    fun selectPet(pet: Pet) {
        _uiState.update { it.copy(selectedPet = pet) }
        observeExpensesForSelectedPet()
    }

    private fun observeExpensesForSelectedPet() {
        expensesJob?.cancel()
        summaryJob?.cancel()

        val selectedPetId = _uiState.value.selectedPet?.id

        expensesJob = viewModelScope.launch {
            getExpensesUseCase(selectedPetId).collect { list ->
                if (list.isEmpty() && _uiState.value.expenses.isEmpty() && selectedPetId != null) {
                    seedSampleExpenses(selectedPetId)
                } else {
                    _uiState.update { it.copy(expenses = list, isLoading = false) }
                }
            }
        }

        summaryJob = viewModelScope.launch {
            getExpenseSummaryUseCase(selectedPetId).collect { summary ->
                _uiState.update { it.copy(summary = summary) }
            }
        }
    }

    private suspend fun seedSampleExpenses(petId: Long) {
        val sample1 = Expense(
            petId = petId,
            title = "خرید غذای خشک رویال کنین ۱۰ کیلویی",
            category = ExpenseCategory.FOOD,
            amountToman = 2850000,
            jalaliDate = "۱۴۰۳/۰۶/۱۰",
            notes = "مخصوص سگ نژاد بزرگ، همراه دو عدد کنسور تشویقی"
        )
        val sample2 = Expense(
            petId = petId,
            title = "باکس حمل استاندارد و ظرف خاک گربه",
            category = ExpenseCategory.ACCESSORY,
            amountToman = 1200000,
            jalaliDate = "۱۴۰۳/۰۶/۱۵",
            notes = "خرید از پت‌شاپ پایتخت"
        )

        addExpenseUseCase(sample1)
        addExpenseUseCase(sample2)
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onAddExpenseClick() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun onDismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun addNewExpense(
        title: String,
        category: ExpenseCategory,
        amountToman: Long,
        jalaliDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val selectedPetId = _uiState.value.selectedPet?.id ?: _uiState.value.pets.firstOrNull()?.id ?: 1L
            val expense = Expense(
                petId = selectedPetId,
                title = title,
                category = category,
                amountToman = amountToman,
                jalaliDate = jalaliDate,
                notes = notes
            )
            addExpenseUseCase(expense)
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    class Factory(
        private val getPetsUseCase: GetPetsUseCase,
        private val getExpensesUseCase: GetExpensesUseCase,
        private val getExpenseSummaryUseCase: GetExpenseSummaryUseCase,
        private val addExpenseUseCase: AddExpenseUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpensesViewModel(getPetsUseCase, getExpensesUseCase, getExpenseSummaryUseCase, addExpenseUseCase) as T
        }
    }
}
