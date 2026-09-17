package red.line.pet.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import red.line.pet.data.local.entity.ExpenseEntity
import red.line.pet.data.local.entity.FoodScheduleEntity
import red.line.pet.data.local.entity.MedicalRecordEntity
import red.line.pet.data.local.entity.MemoryEntity
import red.line.pet.data.local.entity.PetEntity
import red.line.pet.data.local.entity.WeightRecordEntity
import red.line.pet.domain.model.Expense
import red.line.pet.domain.model.ExpenseCategory
import red.line.pet.domain.model.FoodSchedule
import red.line.pet.domain.model.MedicalRecord
import red.line.pet.domain.model.MedicalType
import red.line.pet.domain.model.Memory
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.PetGender
import red.line.pet.domain.model.PetSpecies
import red.line.pet.domain.model.WeightRecord

class MappersTest {

    @Test
    fun `test PetEntity toDomain and toEntity bidirectional mapping`() {
        val pet = Pet(
            id = 1L,
            name = "تامی",
            imagePath = "/images/tommy.jpg",
            species = PetSpecies.CAT,
            breed = "پرشین",
            gender = PetGender.MALE,
            birthDate = "۱۴۰۱/۰۲/۱۵",
            color = "سفید",
            weight = 4.5,
            description = "گربه آرام و مهربان"
        )

        val entity = pet.toEntity()
        assertEquals(pet.id, entity.id)
        assertEquals(pet.name, entity.name)
        assertEquals(pet.species.name, entity.species)
        assertEquals(pet.gender.name, entity.gender)
        assertEquals(pet.weight, entity.weightKg, 0.001)

        val mappedBack = entity.toDomain()
        assertEquals(pet.id, mappedBack.id)
        assertEquals(pet.name, mappedBack.name)
        assertEquals(pet.species, mappedBack.species)
        assertEquals(pet.gender, mappedBack.gender)
        assertEquals(pet.weight, mappedBack.weight, 0.001)
        assertEquals(pet.imagePath, mappedBack.imagePath)
        assertEquals(pet.description, mappedBack.description)
    }

    @Test
    fun `test MedicalRecordEntity toDomain and toEntity bidirectional mapping`() {
        val record = MedicalRecord(
            id = 10L,
            petId = 1L,
            type = MedicalType.VACCINE,
            title = "واکسن سه‌گانه",
            description = "تزریق نوبت اول",
            date = 1700000000000L,
            reminderDate = 1730000000000L
        )

        val entity = record.toEntity()
        assertEquals(record.id, entity.id)
        assertEquals(record.petId, entity.petId)
        assertEquals(record.type.name, entity.type)
        assertEquals(record.title, entity.title)
        assertEquals(record.description, entity.description)
        assertEquals(record.reminderDate, entity.reminderDate)

        val mappedBack = entity.toDomain()
        assertEquals(record.id, mappedBack.id)
        assertEquals(record.petId, mappedBack.petId)
        assertEquals(record.type, mappedBack.type)
        assertEquals(record.title, mappedBack.title)
        assertEquals(record.reminderDate, mappedBack.reminderDate)
    }

    @Test
    fun `test FoodScheduleEntity toDomain and toEntity bidirectional mapping`() {
        val schedule = FoodSchedule(
            id = 5L,
            petId = 1L,
            foodName = "غذای خشک گربه رویال کنین",
            amount = "۷۰ گرم",
            morningTime = "۰۸:۰۰",
            eveningTime = "۲۰:۰۰",
            notes = "همراه با آب تازه"
        )

        val entity = schedule.toEntity()
        assertEquals(schedule.foodName, entity.foodName)
        assertEquals(schedule.morningTime, entity.morningTime)

        val mappedBack = entity.toDomain()
        assertEquals(schedule.id, mappedBack.id)
        assertEquals(schedule.amount, mappedBack.amount)
        assertEquals(schedule.notes, mappedBack.notes)
    }

    @Test
    fun `test WeightRecordEntity toDomain and toEntity bidirectional mapping`() {
        val weight = WeightRecord(
            id = 2L,
            petId = 1L,
            weightKg = 4.85,
            date = 1700000000000L
        )

        val entity = weight.toEntity()
        assertEquals(weight.weightKg, entity.weight, 0.001)

        val mappedBack = entity.toDomain()
        assertEquals(weight.id, mappedBack.id)
        assertEquals(weight.weightKg, mappedBack.weightKg, 0.001)
    }

    @Test
    fun `test ExpenseEntity toDomain and toEntity bidirectional mapping`() {
        val expense = Expense(
            id = 15L,
            petId = 1L,
            category = ExpenseCategory.FOOD,
            title = "کنسرو شسیر",
            amount = 185000L,
            date = 1700000000000L,
            description = "خرید از پت شاپ"
        )

        val entity = expense.toEntity()
        assertEquals(expense.amount, entity.amountToman)
        assertEquals(expense.category.name, entity.category)

        val mappedBack = entity.toDomain()
        assertEquals(expense.id, mappedBack.id)
        assertEquals(expense.amount, mappedBack.amount)
        assertEquals(expense.category, mappedBack.category)
        assertEquals(expense.amountToman, mappedBack.amountToman)
    }

    @Test
    fun `test MemoryEntity toDomain and toEntity bidirectional mapping`() {
        val memory = Memory(
            id = 3L,
            petId = 1L,
            imagePath = "/images/memory1.jpg",
            title = "اولین روز ورود به خانه",
            description = "خیلی کنجکاو بود",
            date = 1700000000000L
        )

        val entity = memory.toEntity()
        assertEquals(memory.title, entity.title)
        assertEquals(memory.imagePath, entity.imagePath)

        val mappedBack = entity.toDomain()
        assertEquals(memory.id, mappedBack.id)
        assertEquals(memory.title, mappedBack.title)
        assertEquals(memory.imagePath, mappedBack.imagePath)
    }
}
