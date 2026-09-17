package red.line.pet.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import red.line.pet.core.billing.FreeVipManager
import red.line.pet.core.billing.VipFeature
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.DataError
import red.line.pet.data.local.datastore.UserPreferencesDataStore
import red.line.pet.domain.usecase.vip.CheckFeatureAccessUseCase
import red.line.pet.domain.usecase.vip.GetVipStatusUseCase

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VipMonetizationRobolectricTest {

    private lateinit var dataStore: UserPreferencesDataStore
    private lateinit var freeVipManager: FreeVipManager
    private lateinit var checkFeatureAccessUseCase: CheckFeatureAccessUseCase
    private lateinit var getVipStatusUseCase: GetVipStatusUseCase

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataStore = UserPreferencesDataStore(context)
        dataStore.setVipStatus(false)
        freeVipManager = FreeVipManager(dataStore)
        checkFeatureAccessUseCase = CheckFeatureAccessUseCase(freeVipManager)
        getVipStatusUseCase = GetVipStatusUseCase(freeVipManager)
    }

    @Test
    fun `free version limits are strictly enforced`() = runBlocking {
        // Reset to Free
        freeVipManager.setVipStatus(false)
        assertFalse(freeVipManager.getIsVip())

        // Pets limit: Max 1
        assertTrue(freeVipManager.canAddPet(0))
        assertFalse(freeVipManager.canAddPet(1))
        assertTrue(checkFeatureAccessUseCase(VipFeature.PETS, 0) is AppResult.Success)
        val petAccessError = checkFeatureAccessUseCase(VipFeature.PETS, 1)
        assertTrue(petAccessError is AppResult.Error && petAccessError.error == DataError.Local.VIP_REQUIRED)

        // Health limit: Max 10
        assertTrue(freeVipManager.canAddHealthRecord(9))
        assertFalse(freeVipManager.canAddHealthRecord(10))

        // Food limit: Max 5
        assertTrue(freeVipManager.canAddFoodSchedule(4))
        assertFalse(freeVipManager.canAddFoodSchedule(5))

        // Weight limit: Max 10
        assertTrue(freeVipManager.canAddWeightRecord(9))
        assertFalse(freeVipManager.canAddWeightRecord(10))

        // Memories limit: Max 5
        assertTrue(freeVipManager.canAddMemory(4))
        assertFalse(freeVipManager.canAddMemory(5))

        // Reminders limit: Max 5
        assertTrue(freeVipManager.canAddReminder(4))
        assertFalse(freeVipManager.canAddReminder(5))

        // Locked features in Free: PDF export & Backup/Restore
        assertFalse(freeVipManager.canExportPdf())
        assertFalse(freeVipManager.canBackupRestore())
        assertTrue(checkFeatureAccessUseCase(VipFeature.PDF_EXPORT) is AppResult.Error)
        assertTrue(checkFeatureAccessUseCase(VipFeature.BACKUP_RESTORE) is AppResult.Error)
    }

    @Test
    fun `vip upgrade unlocks all features completely`() = runBlocking {
        // Upgrade to VIP
        freeVipManager.setVipStatus(true, "test_purchase_token_123", "1403/06/15")
        assertTrue(freeVipManager.getIsVip())

        // Check VIP status usecase
        assertTrue(getVipStatusUseCase.isVipNow())
        assertEquals("test_purchase_token_123", freeVipManager.purchaseToken.first())

        // Check limits unlocked
        assertTrue(freeVipManager.canAddPet(10))
        assertTrue(freeVipManager.canAddHealthRecord(100))
        assertTrue(freeVipManager.canAddFoodSchedule(50))
        assertTrue(freeVipManager.canAddWeightRecord(100))
        assertTrue(freeVipManager.canAddMemory(100))
        assertTrue(freeVipManager.canAddReminder(100))
        assertTrue(freeVipManager.canExportPdf())
        assertTrue(freeVipManager.canBackupRestore())

        // Check use case returns Success for all
        assertTrue(checkFeatureAccessUseCase(VipFeature.PETS, 10) is AppResult.Success)
        assertTrue(checkFeatureAccessUseCase(VipFeature.HEALTH, 100) is AppResult.Success)
        assertTrue(checkFeatureAccessUseCase(VipFeature.FOOD, 50) is AppResult.Success)
        assertTrue(checkFeatureAccessUseCase(VipFeature.WEIGHT, 100) is AppResult.Success)
        assertTrue(checkFeatureAccessUseCase(VipFeature.MEMORIES, 100) is AppResult.Success)
        assertTrue(checkFeatureAccessUseCase(VipFeature.REMINDERS, 100) is AppResult.Success)
        assertTrue(checkFeatureAccessUseCase(VipFeature.PDF_EXPORT) is AppResult.Success)
        assertTrue(checkFeatureAccessUseCase(VipFeature.BACKUP_RESTORE) is AppResult.Success)
    }

    @Test
    fun `30-day trial system allows full access during trial and calculates days correctly`() = runBlocking {
        dataStore.setVipStatus(false)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val trialManager = red.line.pet.core.billing.FreeTrialManager(context)
        val managerWithTrial = FreeVipManager(dataStore, trialManager)

        // Trial is freshly initialized and active
        assertTrue(trialManager.isTrialActive())
        assertFalse(trialManager.isTrialExpired())
        assertTrue(trialManager.getDaysRemaining() in 1..30)

        // With active trial, user has full unlimited access even without VIP
        assertTrue(managerWithTrial.canAddPet(5))
        assertTrue(managerWithTrial.canAddHealthRecord(50))
        assertTrue(managerWithTrial.canAddFoodSchedule(20))
        assertTrue(managerWithTrial.canAddWeightRecord(50))
        assertTrue(managerWithTrial.canAddMemory(50))
        assertTrue(managerWithTrial.canAddReminder(50))
        assertTrue(managerWithTrial.canExportPdf())
        // Strictly VIP-only even during 30-day free trial as requested by user
        assertFalse(managerWithTrial.canBackupRestore())
    }

    @Test
    fun `vip ui state does not contain hardcoded price number and formats dynamic price correctly`() {
        val defaultUiState = red.line.pet.presentation.vip.VipUiState()
        // Ensure no hardcoded price like 39,000 exists
        assertFalse(defaultUiState.formattedPrice.contains("۳۹"))
        assertFalse(defaultUiState.formattedPrice.contains("39"))
        assertEquals("در حال دریافت قیمت...", defaultUiState.formattedPrice)

        // Verify PersianNumberFormatter formats 199000 to "۱۹۹,۰۰۰ تومان"
        val formatted199k = red.line.pet.core.util.PersianNumberFormatter.formatToman(199000L)
        assertEquals("۱۹۹,۰۰۰ تومان", formatted199k)

        // Verify Persian conversion on string with English numbers
        val converted = red.line.pet.core.util.PersianNumberFormatter.toPersian("199,000 تومان")
        assertEquals("۱۹۹,۰۰۰ تومان", converted)
    }
}
