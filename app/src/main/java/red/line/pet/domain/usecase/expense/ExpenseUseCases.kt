package red.line.pet.domain.usecase.expense

import kotlinx.coroutines.flow.Flow
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.DataError
import red.line.pet.domain.model.Expense
import red.line.pet.domain.model.ExpenseSummary
import red.line.pet.domain.repository.ExpenseRepository

class GetExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(petId: Long? = null): Flow<List<Expense>> {
        return if (petId != null) {
            expenseRepository.getExpensesForPet(petId)
        } else {
            expenseRepository.getAllExpenses()
        }
    }
}

class GetExpenseSummaryUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(petId: Long? = null): Flow<ExpenseSummary> {
        return expenseRepository.getExpenseSummary(petId)
    }
}

class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): AppResult<Long, DataError.Local> {
        if (expense.title.isBlank() || expense.amountToman <= 0) {
            return AppResult.Error(DataError.Local.VALIDATION_ERROR)
        }
        val id = expenseRepository.insertExpense(expense)
        return AppResult.Success(id)
    }
}
