package red.line.pet.presentation.vip

import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import red.line.pet.core.billing.BillingConnectionState
import red.line.pet.core.billing.MyketBillingManager
import red.line.pet.core.billing.PurchaseResult
import red.line.pet.core.billing.RestoreResult
import red.line.pet.domain.usecase.vip.GetVipStatusUseCase
import red.line.pet.domain.usecase.vip.ProcessPurchaseResultUseCase
import red.line.pet.domain.usecase.vip.PurchaseVipUseCase
import red.line.pet.domain.usecase.vip.RestorePurchasesUseCase

enum class PurchaseFlowState {
    FREE,
    PURCHASING,
    SUCCESS,
    FAILED,
    ALREADY_PURCHASED
}

data class VipUiState(
    val isVip: Boolean = false,
    val flowState: PurchaseFlowState = PurchaseFlowState.FREE,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val pendingIntent: PendingIntent? = null,
    val isConnecting: Boolean = false,
    val formattedPrice: String = "در حال دریافت قیمت..."
)

class VipViewModel(
    private val getVipStatusUseCase: GetVipStatusUseCase,
    private val purchaseVipUseCase: PurchaseVipUseCase,
    private val processPurchaseResultUseCase: ProcessPurchaseResultUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase,
    private val billingManager: MyketBillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VipUiState())
    val uiState: StateFlow<VipUiState> = _uiState.asStateFlow()

    init {
        observeVipStatus()
        observeBillingConnectionAndFetchPrice()
        billingManager.startConnection()
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }

    private fun observeBillingConnectionAndFetchPrice() {
        viewModelScope.launch {
            billingManager.connectionState.collect { state ->
                if (state is BillingConnectionState.Connected) {
                    fetchVipPrice()
                }
            }
        }
    }

    fun fetchVipPrice() {
        viewModelScope.launch {
            val result = billingManager.getProductPrice()
            result.onSuccess { price ->
                if (price.isNotBlank()) {
                    _uiState.update { it.copy(formattedPrice = price) }
                }
            }
        }
    }

    private fun observeVipStatus() {
        viewModelScope.launch {
            getVipStatusUseCase().collect { isVip ->
                _uiState.update { current ->
                    current.copy(
                        isVip = isVip,
                        flowState = if (isVip) PurchaseFlowState.ALREADY_PURCHASED else current.flowState
                    )
                }
            }
        }
    }

    fun initiatePurchase(onReadyToLaunch: (PendingIntent) -> Unit) {
        if (_uiState.value.isVip) {
            _uiState.update { it.copy(flowState = PurchaseFlowState.ALREADY_PURCHASED) }
            return
        }

        _uiState.update { it.copy(flowState = PurchaseFlowState.PURCHASING, errorMessage = null) }

        viewModelScope.launch {
            if (!billingManager.isMyketInstalled()) {
                _uiState.update {
                    it.copy(
                        flowState = PurchaseFlowState.FAILED,
                        errorMessage = "برنامه مایکت روی دستگاه شما یافت نشد. لطفاً ابتدا برنامه مایکت را نصب کنید."
                    )
                }
                return@launch
            }

            val result = purchaseVipUseCase()
            result.onSuccess { pendingIntent ->
                _uiState.update { it.copy(pendingIntent = pendingIntent) }
                onReadyToLaunch(pendingIntent)
            }.onFailure { exception ->
                val msg = exception.localizedMessage ?: "خطا در اتصال به درگاه مایکت"
                if (msg.contains("قبلاً خریداری شده")) {
                    _uiState.update {
                        it.copy(
                            flowState = PurchaseFlowState.ALREADY_PURCHASED,
                            isVip = true,
                            successMessage = "اشتراک VIP شما قبلاً ثبت شده و فعال گردید."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            flowState = PurchaseFlowState.FAILED,
                            errorMessage = msg
                        )
                    }
                }
            }
        }
    }

    fun handlePurchaseActivityResult(resultCode: Int, data: Intent?) {
        viewModelScope.launch {
            _uiState.update { it.copy(flowState = PurchaseFlowState.PURCHASING) }

            val result = processPurchaseResultUseCase(resultCode, data)
            when (result) {
                is PurchaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isVip = true,
                            flowState = PurchaseFlowState.SUCCESS,
                            successMessage = "خرید VIP مادام‌العمر با موفقیت انجام شد! تمامی امکانات آنلاک شدند."
                        )
                    }
                }
                is PurchaseResult.AlreadyOwned -> {
                    _uiState.update {
                        it.copy(
                            isVip = true,
                            flowState = PurchaseFlowState.ALREADY_PURCHASED,
                            successMessage = "نسخه VIP قبلاً برای این حساب خریداری شده است."
                        )
                    }
                }
                is PurchaseResult.Canceled -> {
                    _uiState.update {
                        it.copy(
                            flowState = if (it.isVip) PurchaseFlowState.ALREADY_PURCHASED else PurchaseFlowState.FREE,
                            errorMessage = "فرآیند پرداخت لغو شد."
                        )
                    }
                }
                is PurchaseResult.Error -> {
                    _uiState.update {
                        it.copy(
                            flowState = PurchaseFlowState.FAILED,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun restorePurchases() {
        _uiState.update { it.copy(flowState = PurchaseFlowState.PURCHASING, errorMessage = null) }

        viewModelScope.launch {
            if (!billingManager.isMyketInstalled()) {
                _uiState.update {
                    it.copy(
                        flowState = PurchaseFlowState.FAILED,
                        errorMessage = "برنامه مایکت روی دستگاه شما نصب نیست."
                    )
                }
                return@launch
            }

            val result = restorePurchasesUseCase()
            when (result) {
                is RestoreResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isVip = true,
                            flowState = PurchaseFlowState.SUCCESS,
                            successMessage = "خرید قبلی شما از مایکت بازیابی و نسخه VIP فعال شد."
                        )
                    }
                }
                is RestoreResult.NoPurchasesFound -> {
                    _uiState.update {
                        it.copy(
                            flowState = if (it.isVip) PurchaseFlowState.ALREADY_PURCHASED else PurchaseFlowState.FREE,
                            errorMessage = "هیچ خرید قبلی برای نسخه VIP در حساب مایکت شما یافت نشد."
                        )
                    }
                }
                is RestoreResult.Error -> {
                    _uiState.update {
                        it.copy(
                            flowState = PurchaseFlowState.FAILED,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null,
                flowState = if (it.isVip) PurchaseFlowState.ALREADY_PURCHASED else PurchaseFlowState.FREE
            )
        }
    }

    class Factory(
        private val getVipStatusUseCase: GetVipStatusUseCase,
        private val purchaseVipUseCase: PurchaseVipUseCase,
        private val processPurchaseResultUseCase: ProcessPurchaseResultUseCase,
        private val restorePurchasesUseCase: RestorePurchasesUseCase,
        private val billingManager: MyketBillingManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VipViewModel(
                getVipStatusUseCase,
                purchaseVipUseCase,
                processPurchaseResultUseCase,
                restorePurchasesUseCase,
                billingManager
            ) as T
        }
    }
}
