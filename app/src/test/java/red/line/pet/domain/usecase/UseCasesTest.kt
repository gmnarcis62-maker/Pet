package red.line.pet.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import red.line.pet.domain.model.Expense
import red.line.pet.domain.model.ExpenseCategory
import red.line.pet.domain.model.ExpenseSummary
import red.line.pet.domain.model.MedicalRecord
import red.line.pet.domain.model.MedicalType
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.PetGender
import red.line.pet.domain.model.PetSpecies
import red.line.pet.domain.repository.ExpenseRepository
import red.line.pet.domain.repository.MedicalRepository
import red.line.pet.domain.repository.PetRepository
import red.line.pet.domain.usecase.expense.AddExpenseUseCase
import red.line.pet.domain.usecase.expense.GetExpensesUseCase
import red.line.pet.domain.usecase.medical.AddMedicalRecordUseCase
import red.line.pet.domain.usecase.medical.GetMedicalRecordsUseCase
import red.line.pet.domain.usecase.pet.AddPetUseCase
import red.line.pet.domain.usecase.pet.DeletePetUseCase
import red.line.pet.domain.usecase.pet.GetPetsUseCase
import red.line.pet.domain.usecase.pet.UpdatePetUseCase

class UseCasesTest {

    private class FakePetRepository : PetRepository {
        private val pets = mutableListOf<Pet>()

        override fun getAllPets(): Flow<List<Pet>> = flowOf(pets)

        override fun getPetById(id: Long): Flow<Pet?> = flowOf(pets.find { it.id == id })

        override suspend fun insertPet(pet: Pet): Long {
            pets.add(pet.copy(id = (pets.size + 1).toLong()))
            return (pets.size).toLong()
        }

        override suspend fun updatePet(pet: Pet) {
            val index = pets.indexOfFirst { it.id == pet.id }
            if (index != -1) pets[index] = pet
        }

        override suspend fun deletePet(pet: Pet) {
            pets.removeAll { it.id == pet.id }
        }

        override suspend fun deletePetById(id: Long) {
            pets.removeAll { it.id == id }
        }
    }

    private class FakeMedicalRepository : MedicalRepository {
        private val records = mutableListOf<MedicalRecord>()

        override fun getMedicalRecordsByPet(petId: Long): Flow<List<MedicalRecord>> {
            return flowOf(records.filter { it.petId == petId })
        }

        override fun getAllMedicalRecords(): Flow<List<MedicalRecord>> {
            return flowOf(records)
        }

        override fun getUpcomingReminders(): Flow<List<MedicalRecord>> {
            return flowOf(records.filter { it.reminderDate != null })
        }

        override suspend fun insert(record: MedicalRecord): Long {
            records.add(record.copy(id = (records.size + 1).toLong()))
            return records.size.toLong()
        }

        override suspend fun delete(record: MedicalRecord) {
            records.removeAll { it.id == record.id }
        }

        override suspend fun deleteById(id: Long) {
            records.removeAll { it.id == id }
        }
    }

    private class FakeExpenseRepository : ExpenseRepository {
        private val expenses = mutableListOf<Expense>()

        override fun getAllExpenses(): Flow<List<Expense>> = flowOf(expenses)

        override fun getExpensesForPet(petId: Long): Flow<List<Expense>> {
            return flowOf(expenses.filter { it.petId == petId })
        }

        override fun getTotalExpenseAmount(petId: Long?): Flow<Long> {
            return flowOf(expenses.filter { petId == null || it.petId == petId }.sumOf { it.amount })
        }

        override fun getExpenseSummary(petId: Long?): Flow<ExpenseSummary> {
            val filtered = expenses.filter { petId == null || it.petId == petId }
            val total = filtered.sumOf { it.amount }
            val map = filtered.groupBy { it.category }.mapValues { it.value.sumOf { exp -> exp.amount } }
            return flowOf(ExpenseSummary(total, map))
        }

        override suspend fun insertExpense(expense: Expense): Long {
            expenses.add(expense.copy(id = (expenses.size + 1).toLong()))
            return expenses.size.toLong()
        }

        override suspend fun deleteExpense(expense: Expense) {
            expenses.removeAll { it.id == expense.id }
        }

        override suspend fun deleteExpenseById(id: Long) {
            expenses.removeAll { it.id == id }
        }
    }

    @Test
    fun `test AddPetUseCase and GetPetsUseCase`() = runBlocking {
        val repo = FakePetRepository()
        val addPetUseCase = AddPetUseCase(repo)
        val getPetsUseCase = GetPetsUseCase(repo)

        val pet = Pet(
            name = "میشو",
            species = PetSpecies.CAT,
            gender = PetGender.FEMALE,
            breed = "بریتیش",
            color = "طوسی",
            weight = 3.2
        )

        val result = addPetUseCase(pet)
        assertTrue(result is red.line.pet.core.util.AppResult.Success)
        assertEquals(1L, (result as red.line.pet.core.util.AppResult.Success).data)

        val list = getPetsUseCase().first()
        assertEquals(1, list.size)
        assertEquals("میشو", list[0].name)
    }

    @Test
    fun `test DeletePetUseCase`() = runBlocking {
        val repo = FakePetRepository()
        val addPetUseCase = AddPetUseCase(repo)
        val getPetsUseCase = GetPetsUseCase(repo)
        val deletePetUseCase = DeletePetUseCase(repo)

        val pet = Pet(name = "لئو", species = PetSpecies.DOG, gender = PetGender.MALE)
        addPetUseCase(pet)

        val petsBefore = getPetsUseCase().first()
        assertEquals(1, petsBefore.size)

        val deleteResult = deletePetUseCase(petsBefore[0].id)
        assertTrue(deleteResult is red.line.pet.core.util.AppResult.Success)

        val petsAfter = getPetsUseCase().first()
        assertTrue(petsAfter.isEmpty())
    }

    @Test
    fun `test UpdatePetUseCase`() = runBlocking {
        val repo = FakePetRepository()
        val addPetUseCase = AddPetUseCase(repo)
        val getPetsUseCase = GetPetsUseCase(repo)
        val updatePetUseCase = UpdatePetUseCase(repo)

        val pet = Pet(name = "تدی", species = PetSpecies.DOG, gender = PetGender.MALE)
        addPetUseCase(pet)

        val savedPet = getPetsUseCase().first().first()
        assertEquals("", savedPet.avatarUri)

        val updatedPet = savedPet.copy(avatarUri = "file:///data/user/0/red.line.pet/files/pet_images/pet_123.jpg")
        val updateResult = updatePetUseCase(updatedPet)
        assertTrue(updateResult is red.line.pet.core.util.AppResult.Success)

        val retrievedPet = getPetsUseCase().first().first()
        assertEquals("file:///data/user/0/red.line.pet/files/pet_images/pet_123.jpg", retrievedPet.avatarUri)
    }

    @Test
    fun `test AddMedicalRecordUseCase and GetMedicalRecordsUseCase`() = runBlocking {
        val repo = FakeMedicalRepository()
        val addMedicalRecordUseCase = AddMedicalRecordUseCase(repo)
        val getMedicalRecordsUseCase = GetMedicalRecordsUseCase(repo)

        val record = MedicalRecord(
            petId = 1L,
            type = MedicalType.VACCINE,
            title = "واکسن سالانه",
            date = 1700000000000L
        )

        val id = addMedicalRecordUseCase(record)
        assertEquals(1L, id)

        val records = getMedicalRecordsUseCase(1L).first()
        assertEquals(1, records.size)
        assertEquals("واکسن سالانه", records[0].title)
    }

    @Test
    fun `test AddExpenseUseCase and GetExpensesUseCase`() = runBlocking {
        val repo = FakeExpenseRepository()
        val addExpenseUseCase = AddExpenseUseCase(repo)
        val getExpensesUseCase = GetExpensesUseCase(repo)

        val expense = Expense(
            petId = 1L,
            category = ExpenseCategory.FOOD,
            title = "غذای خشک",
            amount = 500000L
        )

        val result = addExpenseUseCase(expense)
        assertTrue(result is red.line.pet.core.util.AppResult.Success)
        assertEquals(1L, (result as red.line.pet.core.util.AppResult.Success).data)

        val expenses = getExpensesUseCase().first()
        assertEquals(1, expenses.size)
        assertEquals("غذای خشک", expenses[0].title)
        assertEquals(500000L, expenses[0].amount)
    }
}
