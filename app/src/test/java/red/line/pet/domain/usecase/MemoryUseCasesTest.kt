package red.line.pet.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import red.line.pet.core.util.AppResult
import red.line.pet.domain.model.Memory
import red.line.pet.domain.model.MemorySortType
import red.line.pet.domain.repository.MemoryRepository
import red.line.pet.domain.usecase.memory.AddMemoryUseCase
import red.line.pet.domain.usecase.memory.DeleteMemoryUseCase
import red.line.pet.domain.usecase.memory.GetMemoriesUseCase
import red.line.pet.domain.usecase.memory.SearchMemoriesUseCase
import red.line.pet.domain.usecase.memory.UpdateMemoryUseCase

class MemoryUseCasesTest {

    private class FakeMemoryRepository : MemoryRepository {
        private val memories = mutableListOf<Memory>()

        override fun getMemories(petId: Long): Flow<List<Memory>> {
            return flowOf(memories.filter { it.petId == petId })
        }

        override fun getAllMemories(): Flow<List<Memory>> {
            return flowOf(memories)
        }

        override suspend fun getMemoryById(id: Long): Memory? {
            return memories.find { it.id == id }
        }

        override suspend fun insert(memory: Memory): Long {
            val newId = (memories.size + 1).toLong()
            memories.add(memory.copy(id = newId))
            return newId
        }

        override suspend fun update(memory: Memory) {
            val index = memories.indexOfFirst { it.id == memory.id }
            if (index != -1) memories[index] = memory
        }

        override suspend fun delete(memory: Memory) {
            memories.removeAll { it.id == memory.id }
        }

        override suspend fun deleteById(id: Long) {
            memories.removeAll { it.id == id }
        }
    }

    @Test
    fun `test AddMemoryUseCase validation and insertion`() = runBlocking {
        val repo = FakeMemoryRepository()
        val addUseCase = AddMemoryUseCase(repo)
        val getUseCase = GetMemoriesUseCase(repo)

        // Invalid: missing title
        val invalidMemory = Memory(
            petId = 1L,
            imagePath = "/data/user/0/test.webp",
            title = ""
        )
        val errRes = addUseCase(invalidMemory)
        assertTrue(errRes is AppResult.Error)

        // Invalid: missing imagePath
        val invalidImage = Memory(
            petId = 1L,
            imagePath = "   ",
            title = "اولین روز در خانه"
        )
        val errImgRes = addUseCase(invalidImage)
        assertTrue(errImgRes is AppResult.Error)

        // Valid memory
        val valid = Memory(
            petId = 1L,
            imagePath = "/data/user/0/test.webp",
            title = "اولین روز در خانه",
            description = "خیلی روز شاد و به یاد ماندنی بود"
        )
        val successRes = addUseCase(valid)
        assertTrue(successRes is AppResult.Success)
        assertEquals(1L, (successRes as AppResult.Success).data)

        val list = getUseCase(1L).first()
        assertEquals(1, list.size)
        assertEquals("اولین روز در خانه", list[0].title)
    }

    @Test
    fun `test Search and Sorting Memories`() = runBlocking {
        val repo = FakeMemoryRepository()
        val addUseCase = AddMemoryUseCase(repo)
        val searchUseCase = SearchMemoriesUseCase(repo)
        val getUseCase = GetMemoriesUseCase(repo)

        val now = System.currentTimeMillis()
        addUseCase(
            Memory(
                petId = 1L,
                imagePath = "/path1.webp",
                title = "بازی در پارک ملت",
                description = "دویدن روی چمن‌ها",
                date = now - 50000
            )
        )
        addUseCase(
            Memory(
                petId = 1L,
                imagePath = "/path2.webp",
                title = "تولد یک سالگی ببری",
                description = "کیک مخصوص سگ و جشن کوچک",
                date = now
            )
        )

        // Test Newest First Sort (Default)
        val newestList = getUseCase(1L, MemorySortType.NEWEST).first()
        assertEquals(2, newestList.size)
        assertEquals("تولد یک سالگی ببری", newestList[0].title)

        // Test Oldest First Sort
        val oldestList = getUseCase(1L, MemorySortType.OLDEST).first()
        assertEquals("بازی در پارک ملت", oldestList[0].title)

        // Test Search by title
        val searchPark = searchUseCase(1L, "پارک").first()
        assertEquals(1, searchPark.size)
        assertEquals("بازی در پارک ملت", searchPark[0].title)

        // Test Search by description
        val searchCake = searchUseCase(1L, "کیک").first()
        assertEquals(1, searchCake.size)
        assertEquals("تولد یک سالگی ببری", searchCake[0].title)

        // Test Search empty result
        val searchNotFound = searchUseCase(1L, "شنا").first()
        assertTrue(searchNotFound.isEmpty())
    }

    @Test
    fun `test Update and Delete Memory`() = runBlocking {
        val repo = FakeMemoryRepository()
        val addUseCase = AddMemoryUseCase(repo)
        val updateUseCase = UpdateMemoryUseCase(repo)
        val deleteUseCase = DeleteMemoryUseCase(repo)
        val getUseCase = GetMemoriesUseCase(repo)

        val addRes = addUseCase(
            Memory(
                petId = 1L,
                imagePath = "/old.webp",
                title = "عنوان قدیمی",
                description = "توضیح"
            )
        )
        val id = (addRes as AppResult.Success).data

        // Update
        val updated = Memory(
            id = id,
            petId = 1L,
            imagePath = "/new.webp",
            title = "عنوان جدید اصلاح شده",
            description = "توضیح کامل‌تر"
        )
        val updateRes = updateUseCase(updated)
        assertTrue(updateRes is AppResult.Success)

        val afterUpdate = getUseCase(1L).first()
        assertEquals("عنوان جدید اصلاح شده", afterUpdate[0].title)
        assertEquals("/new.webp", afterUpdate[0].imagePath)

        // Delete
        val delRes = deleteUseCase(id)
        assertTrue(delRes is AppResult.Success)
        assertTrue(getUseCase(1L).first().isEmpty())
    }
}
