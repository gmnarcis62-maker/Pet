package red.line.pet.domain.usecase.vip

import android.app.PendingIntent
import android.content.Intent
import kotlinx.coroutines.flow.Flow
import red.line.pet.core.billing.FreeVipManager
import red.line.pet.core.billing.MyketBillingManager
import red.line.pet.core.billing.PurchaseResult
import red.line.pet.core.billing.RestoreResult
import red.line.pet.core.billing.VipFeature
import red.line.pet.core.util.AppResult
import red.line.pet.core.util.DataError

class GetVipStatusUseCase(
    private val freeVipManager: FreeVipManager
) {
    operator fun invoke(): Flow<Boolean> = freeVipManager.isVip
    suspend fun isVipNow(): Boolean = freeVipManager.getIsVip()
}

class CheckFeatureAccessUseCase(
    private val freeVipManager: FreeVipManager
) {
    suspend operator fun invoke(feature: VipFeature, currentCount: Int = 0): AppResult<Unit, DataError.Local> {
        val allowed = freeVipManager.checkAccess(feature, currentCount)
        return if (allowed) {
            AppResult.Success(Unit)
        } else {
            AppResult.Error(DataError.Local.VIP_REQUIRED)
        }
    }
}

class PurchaseVipUseCase(
    private val billingManager: MyketBillingManager
) {
    suspend operator fun invoke(): Result<PendingIntent> {
        return billingManager.getBuyIntent()
    }
}

class ProcessPurchaseResultUseCase(
    private val billingManager: MyketBillingManager
) {
    suspend operator fun invoke(resultCode: Int, data: Intent?): PurchaseResult {
        return billingManager.handlePurchaseActivityResult(resultCode, data)
    }
}

class RestorePurchasesUseCase(
    private val billingManager: MyketBillingManager
) {
    suspend operator fun invoke(): RestoreResult {
        return billingManager.restorePurchases()
    }
}
