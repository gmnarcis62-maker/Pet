package red.line.pet.core.billing

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.android.vending.billing.IInAppBillingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import red.line.pet.core.util.PersianNumberFormatter

sealed class BillingConnectionState {
    data object Disconnected : BillingConnectionState()
    data object Connecting : BillingConnectionState()
    data object Connected : BillingConnectionState()
    data class Unavailable(val reason: String) : BillingConnectionState()
}

sealed class PurchaseResult {
    data class Success(val purchaseToken: String) : PurchaseResult()
    data object AlreadyOwned : PurchaseResult()
    data object Canceled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

sealed class RestoreResult {
    data class Success(val purchaseToken: String) : RestoreResult()
    data object NoPurchasesFound : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

class MyketBillingManager(
    private val context: Context,
    private val freeVipManager: FreeVipManager
) {
    companion object {
        const val BILLING_RESPONSE_RESULT_OK = 0
        const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
        const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3
        const val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4
        const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
        const val BILLING_RESPONSE_RESULT_ERROR = 6
        const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
        const val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8

        const val RESPONSE_CODE = "RESPONSE_CODE"
        const val RESPONSE_BUY_INTENT = "BUY_INTENT"
        const val RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
        const val RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE"
        const val RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
        const val RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"
        const val RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST"
        const val RESPONSE_ITEM_ID_LIST = "ITEM_ID_LIST"

        const val MYKET_PACKAGE = "ir.mservices.market"
        const val BIND_ACTION = "ir.mservices.market.InAppBillingService.BIND"
    }

    private var billingService: IInAppBillingService? = null
    private val _connectionState = MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)
    val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            billingService = IInAppBillingService.Stub.asInterface(service)
            _connectionState.value = BillingConnectionState.Connected
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            billingService = null
            _connectionState.value = BillingConnectionState.Disconnected
        }
    }

    fun isMyketInstalled(): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo(MYKET_PACKAGE, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun startConnection(onConnected: (() -> Unit)? = null) {
        if (billingService != null) {
            _connectionState.value = BillingConnectionState.Connected
            onConnected?.invoke()
            return
        }

        if (!isMyketInstalled()) {
            _connectionState.value = BillingConnectionState.Unavailable("برنامه مایکت روی دستگاه شما نصب نیست.")
            return
        }

        _connectionState.value = BillingConnectionState.Connecting

        val serviceIntent = Intent(BIND_ACTION).apply {
            `package` = MYKET_PACKAGE
        }

        try {
            val bound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (!bound) {
                _connectionState.value = BillingConnectionState.Unavailable("عدم امکان برقراری ارتباط با سرویس پرداخت مایکت")
            }
        } catch (e: Exception) {
            _connectionState.value = BillingConnectionState.Unavailable(e.localizedMessage ?: "خطا در اتصال به مایکت")
        }
    }

    fun endConnection() {
        try {
            if (billingService != null) {
                context.unbindService(serviceConnection)
                billingService = null
            }
        } catch (_: Exception) {
        } finally {
            _connectionState.value = BillingConnectionState.Disconnected
        }
    }

    suspend fun getProductPrice(
        productId: String = MyketSecurity.PRODUCT_ID_VIP
    ): Result<String> = withContext(Dispatchers.IO) {
        var service = billingService
        if (service == null) {
            if (_connectionState.value is BillingConnectionState.Disconnected) {
                startConnection()
            }
            try {
                withTimeoutOrNull(3000L) {
                    _connectionState.first { it is BillingConnectionState.Connected }
                }
                service = billingService
            } catch (_: Exception) {
            }
        }

        if (service == null) {
            return@withContext Result.failure(IllegalStateException("سرویس پرداخت مایکت متصل نیست."))
        }

        try {
            val skuList = arrayListOf(productId)
            val querySkus = Bundle().apply {
                putStringArrayList(RESPONSE_ITEM_ID_LIST, skuList)
            }
            val skuDetailsBundle = service.getSkuDetails(3, context.packageName, "inapp", querySkus)
            val responseCode = skuDetailsBundle?.getInt(RESPONSE_CODE, -1) ?: -1
            if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                val detailsList = skuDetailsBundle.getStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST)
                if (!detailsList.isNullOrEmpty()) {
                    for (detailJson in detailsList) {
                        val json = JSONObject(detailJson)
                        if (json.optString("productId") == productId) {
                            val rawPrice = json.optString("price", "")
                            if (rawPrice.isNotBlank()) {
                                val formatted = formatPriceString(rawPrice)
                                return@withContext Result.success(formatted)
                            }
                        }
                    }
                }
                Result.failure(IllegalStateException("اطلاعات محصول از مایکت دریافت نشد."))
            } else {
                Result.failure(IllegalStateException("خطا در دریافت قیمت از مایکت: کد $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatPriceString(price: String): String {
        var formatted = price.trim()
        if (formatted.isNotBlank()) {
            if (!formatted.contains("تومان") && !formatted.contains("ریال")) {
                val cleanNumber = formatted.replace(",", "").replace("،", "").toLongOrNull()
                formatted = if (cleanNumber != null) {
                    PersianNumberFormatter.formatToman(cleanNumber)
                } else {
                    "${PersianNumberFormatter.toPersian(formatted)} تومان"
                }
            } else {
                formatted = PersianNumberFormatter.toPersian(formatted)
            }
        }
        return formatted
    }

    suspend fun getBuyIntent(
        productId: String = MyketSecurity.PRODUCT_ID_VIP,
        developerPayload: String = "petora_vip_lifetime"
    ): Result<PendingIntent> = withContext(Dispatchers.IO) {
        val service = billingService
        if (service == null) {
            return@withContext Result.failure(IllegalStateException("سرویس پرداخت مایکت متصل نیست."))
        }

        try {
            val buyIntentBundle = service.getBuyIntent(
                3,
                context.packageName,
                productId,
                "inapp",
                developerPayload
            )

            val responseCode = buyIntentBundle?.getInt(RESPONSE_CODE, -1) ?: -1
            if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                @Suppress("DEPRECATION")
                val pendingIntent = buyIntentBundle.getParcelable<PendingIntent>(RESPONSE_BUY_INTENT)
                if (pendingIntent != null) {
                    Result.success(pendingIntent)
                } else {
                    Result.failure(IllegalStateException("Intent خرید از مایکت دریافت نشد."))
                }
            } else if (responseCode == BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                // Item was already purchased earlier -> restore purchase
                restorePurchasesInternal()
                Result.failure(IllegalStateException("این آیتم قبلاً خریداری شده است و اشتراک شما فعال شد."))
            } else {
                Result.failure(IllegalStateException("خطای مایکت در آغاز خرید: کد $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun handlePurchaseActivityResult(
        resultCode: Int,
        data: Intent?
    ): PurchaseResult = withContext(Dispatchers.IO) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return@withContext PurchaseResult.Canceled
        }

        if (data == null) {
            return@withContext PurchaseResult.Error("اطلاعات خرید بازگردانده نشد.")
        }

        val responseCode = data.getIntExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK)
        if (responseCode == BILLING_RESPONSE_RESULT_USER_CANCELED) {
            return@withContext PurchaseResult.Canceled
        }

        if (responseCode == BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
            restorePurchasesInternal()
            return@withContext PurchaseResult.AlreadyOwned
        }

        if (responseCode != BILLING_RESPONSE_RESULT_OK) {
            return@withContext PurchaseResult.Error("خطای پرداخت مایکت (کد: $responseCode)")
        }

        val purchaseData = data.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA)
        val signature = data.getStringExtra(RESPONSE_INAPP_SIGNATURE)

        if (purchaseData.isNullOrBlank() || signature.isNullOrBlank()) {
            return@withContext PurchaseResult.Error("اطلاعات خرید یا امضای دیجیتال نامعتبر است.")
        }

        // Verify RSA Signature
        val isSignatureValid = MyketSecurity.verifyPurchase(
            base64PublicKey = MyketSecurity.MYKET_PUBLIC_KEY,
            signedData = purchaseData,
            signature = signature
        )

        if (!isSignatureValid) {
            return@withContext PurchaseResult.Error("اعتبارسنجی امضای دیجیتال خرید ناموفق بود.")
        }

        // Parse JSON Purchase
        try {
            val json = JSONObject(purchaseData)
            val productId = json.optString("productId")
            val purchaseState = json.optInt("purchaseState", -1)
            val purchaseToken = json.optString("purchaseToken", "")
            val purchaseTime = json.optLong("purchaseTime", System.currentTimeMillis())

            if (productId != MyketSecurity.PRODUCT_ID_VIP) {
                return@withContext PurchaseResult.Error("شناسه محصول نامعتبر است.")
            }

            if (purchaseState != 0) { // 0 = Purchased
                return@withContext PurchaseResult.Error("وضعیت خرید نامعتبر است ($purchaseState).")
            }

            // Successfully verified -> Persist VIP in DataStore
            freeVipManager.setVipStatus(
                isVip = true,
                token = purchaseToken,
                date = purchaseTime.toString()
            )

            PurchaseResult.Success(purchaseToken)
        } catch (e: Exception) {
            PurchaseResult.Error("خطا در پردازش اطلاعات خرید: ${e.localizedMessage}")
        }
    }

    suspend fun restorePurchases(): RestoreResult = withContext(Dispatchers.IO) {
        restorePurchasesInternal()
    }

    private suspend fun restorePurchasesInternal(): RestoreResult {
        val service = billingService
        if (service == null) {
            return RestoreResult.Error("سرویس پرداخت مایکت متصل نیست.")
        }

        try {
            val ownedItems = service.getPurchases(3, context.packageName, "inapp", null)
            val responseCode = ownedItems?.getInt(RESPONSE_CODE, -1) ?: -1

            if (responseCode != BILLING_RESPONSE_RESULT_OK) {
                return RestoreResult.Error("خطا در دریافت خریدهای قبلی (کد $responseCode)")
            }

            val purchaseDataList = ownedItems?.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST)
            val signatureList = ownedItems?.getStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST)

            if (purchaseDataList.isNullOrEmpty() || signatureList.isNullOrEmpty()) {
                return RestoreResult.NoPurchasesFound
            }

            for (i in 0 until purchaseDataList.size) {
                val purchaseData = purchaseDataList[i]
                val signature = signatureList.getOrNull(i) ?: continue

                val isValid = MyketSecurity.verifyPurchase(
                    base64PublicKey = MyketSecurity.MYKET_PUBLIC_KEY,
                    signedData = purchaseData,
                    signature = signature
                )

                if (isValid) {
                    val json = JSONObject(purchaseData)
                    val productId = json.optString("productId")
                    val purchaseState = json.optInt("purchaseState", -1)
                    val purchaseToken = json.optString("purchaseToken", "")
                    val purchaseTime = json.optLong("purchaseTime", System.currentTimeMillis())

                    if (productId == MyketSecurity.PRODUCT_ID_VIP && purchaseState == 0) {
                        freeVipManager.setVipStatus(
                            isVip = true,
                            token = purchaseToken,
                            date = purchaseTime.toString()
                        )
                        return RestoreResult.Success(purchaseToken)
                    }
                }
            }

            return RestoreResult.NoPurchasesFound
        } catch (e: Exception) {
            return RestoreResult.Error("خطا در برقراری ارتباط با مایکت: ${e.localizedMessage}")
        }
    }
}
