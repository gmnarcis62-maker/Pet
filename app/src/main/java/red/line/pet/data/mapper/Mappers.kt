package red.line.pet.data.mapper

import red.line.pet.data.local.entity.ExpenseEntity
import red.line.pet.data.local.entity.FoodScheduleEntity
import red.line.pet.data.local.entity.HealthRecordEntity
import red.line.pet.data.local.entity.MedicalRecordEntity
import red.line.pet.data.local.entity.MemoryEntity
import red.line.pet.data.local.entity.PetEntity
import red.line.pet.data.local.entity.WeightRecordEntity
import red.line.pet.domain.model.Expense
import red.line.pet.domain.model.ExpenseCategory
import red.line.pet.domain.model.FoodSchedule
import red.line.pet.domain.model.HealthRecord
import red.line.pet.domain.model.HealthRecordType
import red.line.pet.domain.model.MedicalRecord
import red.line.pet.domain.model.MedicalType
import red.line.pet.domain.model.Memory
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.PetGender
import red.line.pet.domain.model.PetSpecies
import red.line.pet.domain.model.WeightRecord

fun PetEntity.toDomain(): Pet {
    return Pet(
        id = id,
        name = name,
        imagePath = avatarUri,
        species = try { PetSpecies.valueOf(species) } catch (e: Exception) { PetSpecies.OTHER },
        breed = breed,
        gender = try { PetGender.valueOf(gender) } catch (e: Exception) { PetGender.MALE },
        birthDate = birthDateJalali,
        color = "",
        weight = weightKg,
        description = notes,
        createdAt = createdAt,
        birthDateJalali = birthDateJalali,
        weightKg = weightKg,
        microchipId = microchipId,
        avatarUri = avatarUri,
        notes = notes
    )
}

fun Pet.toEntity(): PetEntity {
    return PetEntity(
        id = id,
        name = name,
        species = species.name,
        breed = breed,
        birthDateJalali = birthDateJalali.ifBlank { birthDate },
        gender = gender.name,
        weightKg = if (weightKg > 0.0) weightKg else weight,
        microchipId = microchipId,
        avatarUri = avatarUri.ifBlank { imagePath },
        notes = notes.ifBlank { description },
        createdAt = createdAt
    )
}

fun MedicalRecordEntity.toDomain(): MedicalRecord {
    return MedicalRecord(
        id = id,
        petId = petId,
        type = try { MedicalType.valueOf(type) } catch (e: Exception) { MedicalType.VACCINE },
        title = title,
        description = description,
        date = date,
        reminderDate = reminderDate
    )
}

fun MedicalRecord.toEntity(): MedicalRecordEntity {
    return MedicalRecordEntity(
        id = id,
        petId = petId,
        type = type.name,
        title = title,
        description = description,
        date = date,
        reminderDate = reminderDate
    )
}

fun FoodScheduleEntity.toDomain(): FoodSchedule {
    val daysSet = if (repeatDays.isBlank()) {
        red.line.pet.domain.model.DayOfWeekFa.entries.toSet()
    } else {
        repeatDays.split(",")
            .mapNotNull { red.line.pet.domain.model.DayOfWeekFa.fromCode(it.trim()) }
            .toSet()
            .ifEmpty { red.line.pet.domain.model.DayOfWeekFa.entries.toSet() }
    }

    val finalTime = mealTime.ifBlank { morningTime.ifBlank { eveningTime.ifBlank { "08:00" } } }
    val mType = try {
        red.line.pet.domain.model.MealType.valueOf(mealType)
    } catch (e: Exception) {
        red.line.pet.domain.model.MealType.fromString(mealType)
    }
    val finalTitle = mealTitle.ifBlank { mType.titleFa }

    return FoodSchedule(
        id = id,
        petId = petId,
        mealTitle = finalTitle,
        mealType = mType,
        foodName = foodName,
        amount = amount,
        amountUnit = try {
            red.line.pet.domain.model.AmountUnit.valueOf(amountUnit)
        } catch (e: Exception) {
            red.line.pet.domain.model.AmountUnit.fromString(amountUnit)
        },
        mealTime = finalTime,
        morningTime = morningTime,
        eveningTime = eveningTime,
        repeatDays = daysSet,
        isCompleted = isCompleted,
        isActive = isActive,
        supplementName = supplementName,
        supplementAmount = supplementAmount,
        supplementNotes = supplementNotes,
        notes = notes
    )
}

fun FoodSchedule.toEntity(): FoodScheduleEntity {
    val daysString = repeatDays.joinToString(",") { it.code }
    return FoodScheduleEntity(
        id = id,
        petId = petId,
        mealTitle = mealTitle.ifBlank { mealType.titleFa },
        mealType = mealType.name,
        foodName = foodName,
        amount = amount,
        amountUnit = amountUnit.name,
        mealTime = mealTime,
        morningTime = morningTime.ifBlank { mealTime },
        eveningTime = eveningTime,
        repeatDays = daysString,
        isCompleted = isCompleted,
        isActive = isActive,
        supplementName = supplementName,
        supplementAmount = supplementAmount,
        supplementNotes = supplementNotes,
        notes = notes
    )
}

fun WeightRecordEntity.toDomain(): WeightRecord {
    val weightUnit = try {
        red.line.pet.domain.model.WeightUnit.valueOf(unit)
    } catch (e: Exception) {
        red.line.pet.domain.model.WeightUnit.fromString(unit)
    }
    val displayW = if (weightUnit == red.line.pet.domain.model.WeightUnit.GRAM) weight * 1000 else weight
    return WeightRecord(
        id = id,
        petId = petId,
        weightKg = weight,
        unit = weightUnit,
        displayWeight = displayW,
        jalaliDate = jalaliDate,
        notes = notes,
        date = date
    )
}

fun WeightRecord.toEntity(): WeightRecordEntity {
    return WeightRecordEntity(
        id = id,
        petId = petId,
        weight = weightKg,
        unit = unit.name,
        jalaliDate = jalaliDate,
        notes = notes,
        date = date
    )
}

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        petId = petId,
        title = title,
        category = try { ExpenseCategory.valueOf(category) } catch (e: Exception) { ExpenseCategory.OTHER },
        amount = amountToman,
        date = createdAt,
        description = notes,
        amountToman = amountToman,
        jalaliDate = jalaliDate,
        notes = notes
    )
}

fun Expense.toEntity(): ExpenseEntity {
    val finalAmount = if (amount > 0L) amount else amountToman
    val finalDesc = description.ifBlank { notes }
    return ExpenseEntity(
        id = id,
        petId = petId,
        title = title,
        category = category.name,
        amountToman = finalAmount,
        jalaliDate = jalaliDate,
        notes = finalDesc,
        createdAt = if (date > 0L) date else System.currentTimeMillis()
    )
}

fun MemoryEntity.toDomain(): Memory {
    return Memory(
        id = id,
        petId = petId,
        imagePath = imagePath,
        title = title,
        description = description,
        date = date
    )
}

fun Memory.toEntity(): MemoryEntity {
    return MemoryEntity(
        id = id,
        petId = petId,
        imagePath = imagePath,
        title = title,
        description = description,
        date = date
    )
}

fun HealthRecordEntity.toDomain(): HealthRecord {
    return HealthRecord(
        id = id,
        petId = petId,
        title = title,
        type = try { HealthRecordType.valueOf(type) } catch (e: Exception) { HealthRecordType.CHECKUP },
        jalaliDate = jalaliDate,
        nextDueJalaliDate = nextDueJalaliDate,
        costToman = costToman,
        clinicOrDoctor = clinicOrDoctor,
        notes = notes,
        isCompleted = isCompleted
    )
}

fun HealthRecord.toEntity(): HealthRecordEntity {
    return HealthRecordEntity(
        id = id,
        petId = petId,
        title = title,
        type = type.name,
        jalaliDate = jalaliDate,
        nextDueJalaliDate = nextDueJalaliDate,
        costToman = costToman,
        clinicOrDoctor = clinicOrDoctor,
        notes = notes,
        isCompleted = isCompleted
    )
}

fun red.line.pet.data.local.entity.ReminderEntity.toDomain(): red.line.pet.domain.model.Reminder {
    return red.line.pet.domain.model.Reminder(
        id = id,
        petId = petId,
        title = title,
        description = description,
        reminderType = red.line.pet.domain.model.ReminderType.fromString(reminderType),
        targetId = targetId,
        remindTimestamp = remindTimestamp,
        jalaliDate = jalaliDate,
        timeString = timeString,
        repeatType = red.line.pet.domain.model.ReminderRepeatType.fromString(repeatType),
        isEnabled = isEnabled,
        isCompleted = isCompleted,
        createdAt = createdAt
    )
}

fun red.line.pet.domain.model.Reminder.toEntity(): red.line.pet.data.local.entity.ReminderEntity {
    return red.line.pet.data.local.entity.ReminderEntity(
        id = id,
        petId = petId,
        title = title,
        description = description,
        reminderType = reminderType.name,
        targetId = targetId,
        remindTimestamp = remindTimestamp,
        jalaliDate = jalaliDate,
        timeString = timeString,
        repeatType = repeatType.name,
        isEnabled = isEnabled,
        isCompleted = isCompleted,
        createdAt = createdAt
    )
}

