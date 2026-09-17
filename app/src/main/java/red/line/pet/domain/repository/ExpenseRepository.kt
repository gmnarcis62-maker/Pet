package red.line.pet.domain.repository

import kotlinx.coroutines.flow.Flow
import red.line.pet.domain.model.Expense
import red.line.pet.domain.model.ExpenseSummary

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesForPet(petId: Long): Flow<List<Expense>>
    fun getTotalExpenseAmount(petId: Long? = null): Flow<Long>
    fun getExpenseSummary(petId: Long? = null): Flow<ExpenseSummary>
    suspend fun insertExpense(expense: Expense): Long
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(id: Long)
}
