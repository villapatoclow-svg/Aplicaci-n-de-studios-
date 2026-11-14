package com.example.core

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.data.model.PlayProductOfferUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayBillingManager(
    context: Context,
    private val scope: CoroutineScope,
    private val onPurchasesVerified: (productIds: List<String>) -> Unit,
    private val onBillingStatusMessage: (String) -> Unit
) : PurchasesUpdatedListener {

    private val _offers = MutableStateFlow(defaultOffers())
    val offers = _offers.asStateFlow()

    private val _isPlayStoreReady = MutableStateFlow(false)
    val isPlayStoreReady = _isPlayStoreReady.asStateFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun startConnection() {
        try {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _isPlayStoreReady.value = true
                        queryCatalog()
                        restorePurchases(silent = true)
                    } else {
                        _isPlayStoreReady.value = false
                        Log.w(TAG, "Play Billing setup code=${billingResult.responseCode}: ${billingResult.debugMessage}")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _isPlayStoreReady.value = false
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Unable to initialize Play BillingClient: ${e.message}")
            _isPlayStoreReady.value = false
        }
    }

    fun queryCatalog() {
        val inAppProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_EXTRA_25)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_LIFETIME_250)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val subsProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_MONTHLY_AGENT)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(inAppProducts).build()
        ) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                detailsResult.productDetailsList.forEach { pd ->
                    productDetailsMap[pd.productId] = pd
                }
                refreshOfferUiState()
            }
        }

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(subsProducts).build()
        ) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                detailsResult.productDetailsList.forEach { pd ->
                    productDetailsMap[pd.productId] = pd
                }
                refreshOfferUiState()
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity?, productId: String) {
        if (activity == null) {
            onBillingStatusMessage("No se encontró la ventana activa para abrir Google Play.")
            return
        }
        val details = productDetailsMap[productId]
        if (details == null || !_isPlayStoreReady.value) {
            onBillingStatusMessage(
                "Google Play Billing está listo en código, pero el producto '$productId' requiere que subas el AAB a Google Play Console (Prueba Interna o Producción) y actives el SKU en Monetización."
            )
            return
        }

        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (details.productType == BillingClient.ProductType.SUBS) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken.isNullOrBlank()) {
                onBillingStatusMessage("No hay oferta activa en Play Console para la suscripción $productId.")
                return
            }
            productParamsBuilder.setOfferToken(offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            onBillingStatusMessage("No se pudo abrir la hoja de pago de Google Play: ${result.debugMessage}")
        }
    }

    fun restorePurchases(silent: Boolean = false) {
        if (!_isPlayStoreReady.value) {
            if (!silent) {
                onBillingStatusMessage("Google Play Store no está disponible en este dispositivo/emulador para restaurar compras.")
            }
            return
        }

        val verified = mutableListOf<String>()
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { res, purchases ->
            if (res.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchasesList(purchases, verified)
            }
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
            ) { subRes, subPurchases ->
                if (subRes.responseCode == BillingClient.BillingResponseCode.OK) {
                    handlePurchasesList(subPurchases, verified)
                }
                if (verified.isNotEmpty()) {
                    onPurchasesVerified(verified.distinct())
                    if (!silent) onBillingStatusMessage("Compras de Google Play restauradas (${verified.size}).")
                } else if (!silent) {
                    onBillingStatusMessage("No se encontraron compras previas en esta cuenta de Google Play.")
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchasedIds = mutableListOf<String>()
                if (!purchases.isNullOrEmpty()) {
                    handlePurchasesList(purchases, purchasedIds)
                }
                if (purchasedIds.isNotEmpty()) {
                    onPurchasesVerified(purchasedIds.distinct())
                    onBillingStatusMessage("¡Compra confirmada por Google Play! Beneficios activados.")
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                onBillingStatusMessage("Compra cancelada por el usuario.")
            }
            else -> {
                onBillingStatusMessage("Estado de Google Play (${billingResult.responseCode}): ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchasesList(purchases: List<Purchase>, outProductIds: MutableList<String>) {
        purchases.forEach { purchase ->
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    outProductIds.addAll(purchase.products)
                    if (!purchase.isAcknowledged) {
                        val ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        scope.launch {
                            billingClient.acknowledgePurchase(ackParams) { ackRes ->
                                if (ackRes.responseCode != BillingClient.BillingResponseCode.OK) {
                                    Log.w(TAG, "Acknowledge failed: ${ackRes.debugMessage}")
                                }
                            }
                        }
                    }
                }
                Purchase.PurchaseState.PENDING -> {
                    onBillingStatusMessage("Tu pago está pendiente de aprobación en Google Play.")
                }
                else -> Unit
            }
        }
    }

    private fun refreshOfferUiState() {
        _offers.value = defaultOffers().map { base ->
            val pd = productDetailsMap[base.productId]
            val price = if (pd != null) {
                if (pd.productType == BillingClient.ProductType.SUBS) {
                    pd.subscriptionOfferDetails?.firstOrNull()
                        ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                } else {
                    pd.oneTimePurchaseOfferDetails?.formattedPrice
                }
            } else null
            base.copy(
                livePlayPrice = price,
                isAvailableInPlayStore = pd != null
            )
        }
    }

    fun endConnection() {
        try {
            billingClient.endConnection()
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val TAG = "PlayBillingManager"
        const val SKU_EXTRA_25 = "mentepro_extra_25"
        const val SKU_LIFETIME_250 = "mentepro_lifetime_pass"
        const val SKU_MONTHLY_AGENT = "mentepro_monthly_agent"

        fun defaultOffers(): List<PlayProductOfferUi> = listOf(
            PlayProductOfferUi(
                productId = SKU_EXTRA_25,
                title = "Acceso Extra Protón 2",
                description = "40 tickets IA/día, Mascota Autosauro VIP y prioridad en conectores externos.",
                fallbackPriceLabel = "$25 MXN · Pago Único Extra",
                livePlayPrice = null,
                isSubscription = false,
                isAvailableInPlayStore = false
            ),
            PlayProductOfferUi(
                productId = SKU_LIFETIME_250,
                title = "Pase Único Vitalicio",
                description = "Tickets de IA ilimitados de por vida, Workshop, Diapositivas y Duelos IA.",
                fallbackPriceLabel = "$250 MXN · Pago Único",
                livePlayPrice = null,
                isSubscription = false,
                isAvailableInPlayStore = false
            ),
            PlayProductOfferUi(
                productId = SKU_MONTHLY_AGENT,
                title = "Agente Premium Automatizado",
                description = "Suscripción mensual: piloto automático total de planes, tareas y profesores.",
                fallbackPriceLabel = "$149 MXN / mes",
                livePlayPrice = null,
                isSubscription = true,
                isAvailableInPlayStore = false
            )
        )
    }
}
