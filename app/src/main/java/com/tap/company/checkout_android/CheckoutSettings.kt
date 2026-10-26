package com.tap.company.checkout_android

import android.content.Context
import com.google.gson.Gson
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Every input of the Tap web demo (https://demo.tap.company/v2/sdk/checkout), stored in
 * SharedPreferences (the web demo keeps the same state in localStorage), plus the mapping that turns
 * them into the configuration map passed to `CheckoutConfiguration.configureWithTapCheckoutDictionary`.
 * The mapping mirrors what the web demo sends to the web SDK, so both behave the same.
 * Same fields and defaults as CheckoutSettings.swift in the iOS example apps.
 */
data class CheckoutSettings(

    // SDK configuration

    var publicKey: String = Keys.PUBLIC_KEY,
    var merchantId: String = "",
    /** Any of ALL, AUTO, KWD, BHD, SAR, AED, OMR, EGP, GBP, USD, EUR. */
    var supportedCurrencies: List<String> = listOf("ALL"),
    /** ALL, WEB, CARD or DEVICE. */
    var paymentType: String = "ALL",
    var supportedPaymentMethods: List<String> = listOf("ALL"),
    var selectedCurrency: String = "SAR",
    /** auto, en, ar or dynamic (device language). "auto" lets the checkout decide, so the key is not sent. */
    var language: String = "en",
    var supportedRegions: List<String> = emptyList(),
    var supportedCountries: List<String> = emptyList(),
    var supportedPaymentTypes: List<String> = emptyList(),
    var supportedSchemes: List<String> = emptyList(),
    var isApplePayAvailableOnClient: Boolean = true,

    // User configuration

    var customerId: String = "",
    var firstName: String = "Ahmed",
    var lastName: String = "Sharkawy",
    var email: String = "example@gmail.com",
    var countryCode: String = "20",
    var phoneNumber: String = "1099137773",

    // Card configuration

    var showBrands: Boolean = true,
    var showLoadingState: Boolean = false,
    var collectHolderName: Boolean = true,
    var preLoadCardName: String = "",
    var cardNameEditable: Boolean = true,
    /** all, credit or debit. */
    var cardFundingSource: String = "all",
    /** all, merchant, tap or none. */
    var saveCardOption: String = "all",
    var forceLtr: Boolean = false,
    /** Not in the web demo: shows the native card scanner button (needs the CAMERA permission). */
    var cardScanner: Boolean = false,
    /** Android only, not in the web demo: shows the NFC card reader button. */
    var cardNFC: Boolean = false,

    // Order configuration

    var orderId: String = "",
    /** "F" = fixed amount, "P" = percentage of the items total. */
    var discountType: String = "F",
    var discountValue: Double = 0.0,
    var items: List<OrderItem> = listOf(OrderItem()),

    // Transaction configuration

    /** "charge" or "authorize". */
    var mode: String = "charge",
    /** light, dark, light_mono, dark_colored, dynamic (device mode) or auto ("auto" = key not sent). */
    var theme: String = "light",
    /** popup or page. */
    var checkoutMode: String = "popup",
    var threeDSecure: Boolean = true,
    var saveCard: Boolean = true,
    /** `transaction.<mode>.redirect.url`; the web demo returns to its own page. */
    var redirectUrl: String = REDIRECT_URL,
    var hashString: String = "",
    /** CAPTURE or VOID: what happens automatically to an authorization. Only editable in authorize mode. */
    var authorizeType: String = "VOID",
    /** Hours before [authorizeType] is applied. */
    var authorizeTime: Int = 100,
    var agreedPayment: Boolean = false,
    /** SCHEDULED or UNSCHEDULED. */
    var agreementType: String = "SCHEDULED",
    /** FIXED or VARIABLE. */
    var amountVariability: String = "FIXED",
    /** "Defined" (use [paymentCount]) or "Undefined" (txn_count 0). */
    var numberOfPayments: String = "Undefined",
    var paymentCount: Int = 1,
    var subscriptionName: String = "Recurring",
    var subscriptionDescription: String = "A description of the recurring payment to display to the user in the payment sheet.",
    var billingAgreement: String = "A localized billing agreement displayed to the user in the payment sheet prior to the payment authorization.",
    /**
     * "", "yes" or "no". The web demo starts with the airline choice unset and then still sends
     * `airline` with an empty booking reference; only an explicit "no" removes it.
     */
    var airline: String = "",
    var bookingReference: String = "",
) {

    data class OrderItem(
        var name: String = "Item Title 1",
        var description: String = "item description 1",
        var quantity: Int = 1,
        var amount: Double = 0.1,
    )

    // Derived values

    val hasPublicKey: Boolean
        get() = publicKey.trim().let { it.isNotEmpty() && it != PLACEHOLDER_PUBLIC_KEY }

    /** Sum of `amount * quantity` over all items. */
    val itemsTotal: Double
        get() = items.sumOf { it.amount * it.quantity }

    /** Amount charged: items total minus the discount, or 1 when that is zero or negative (as the web demo does). */
    val totalAmount: Double
        get() {
            val discount = if (discountType == "P") itemsTotal * (discountValue / 100) else discountValue
            val total = itemsTotal - discount
            return if (total <= 0) 1.0 else total
        }

    // Configuration map

    /**
     * Builds the map for `CheckoutConfiguration.configureWithTapCheckoutDictionary`, following the web
     * demo's mapping. Nested objects are maps and arrays are lists; the SDK turns them into JSON.
     */
    fun makeConfiguration(now: Date = Date()): LinkedHashMap<String, Any> {
        val config = linkedMapOf<String, Any>(
            "open" to true,
            "hashString" to hashString,
            "checkoutMode" to checkoutMode,
            "paymentType" to paymentType,
            "supportedPaymentMethods" to (if ("ALL" in supportedPaymentMethods) "ALL" else supportedPaymentMethods),
            "selectedCurrency" to selectedCurrency,
            "supportedCurrencies" to collapsedCurrencies,
            "gateway" to gateway,
            "customer" to customer,
            "transaction" to linkedMapOf("mode" to mode, mode to transaction(now)),
            "amount" to jsonNumber(totalAmount),
            "order" to order,
            "cardOptions" to cardOptions,
            "isApplePayAvailableOnClient" to isApplePayAvailableOnClient,
        )

        // "auto" means "let the checkout decide": the key is not sent at all.
        if (language != "auto") config["language"] = language
        if (theme != "auto") config["themeMode"] = theme

        // Empty filters are not sent.
        if (supportedRegions.isNotEmpty()) config["supportedRegions"] = supportedRegions
        if (supportedCountries.isNotEmpty()) config["supportedCountries"] = supportedCountries
        if (supportedPaymentTypes.isNotEmpty()) config["supportedPaymentTypes"] = supportedPaymentTypes
        if (supportedSchemes.isNotEmpty()) config["supportedSchemes"] = supportedSchemes
        return config
    }

    /** ALL wins over AUTO, which wins over an explicit list. */
    private val collapsedCurrencies: Any
        get() = when {
            "ALL" in supportedCurrencies -> "ALL"
            "AUTO" in supportedCurrencies -> "AUTO"
            else -> supportedCurrencies
        }

    private val gateway: Map<String, Any>
        get() = linkedMapOf<String, Any>("publicKey" to publicKey).apply {
            if (merchantId.isNotEmpty()) put("merchantId", merchantId)
        }

    private val customer: Map<String, Any>
        get() = linkedMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phone" to linkedMapOf("countryCode" to countryCode, "number" to phoneNumber),
        ).apply {
            if (customerId.isNotEmpty()) put("id", customerId)
        }

    private val cardOptions: Map<String, Any>
        get() = linkedMapOf<String, Any>(
            "showBrands" to showBrands,
            "showLoadingState" to showLoadingState,
            "collectHolderName" to collectHolderName,
            "preLoadCardName" to preLoadCardName,
            "cardNameEditable" to cardNameEditable,
            "cardFundingSource" to cardFundingSource,
            "saveCardOption" to saveCardOption,
            "forceLtr" to forceLtr,
        ).apply {
            // Sent only when switched on, like the iOS example.
            val inputs = linkedMapOf<String, Any>()
            if (cardScanner) inputs["cardScanner"] = true
            if (cardNFC) inputs["cardNFC"] = true
            if (inputs.isNotEmpty()) put("alternativeCardInputs", inputs)
        }

    private val order: Map<String, Any>
        get() = linkedMapOf<String, Any>(
            "id" to orderId,
            "currency" to selectedCurrency,
            "amount" to jsonNumber(totalAmount),
            "items" to items.map { item ->
                linkedMapOf<String, Any>(
                    "amount" to jsonNumber(item.amount),
                    "currency" to selectedCurrency,
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "description" to item.description,
                )
            },
        ).apply {
            if (discountValue > 0) put("discount", linkedMapOf("type" to discountType, "value" to jsonNumber(discountValue)))
        }

    /**
     * The `transaction.<mode>` object. Charge and authorize send the very same keys; only the
     * name of this object (and `transaction.mode`) differs.
     */
    private fun transaction(now: Date): Map<String, Any> {
        val transaction = linkedMapOf<String, Any>(
            "saveCard" to saveCard,
            // Sent in both modes. `type` and `time` only matter (and are only editable) for authorizations.
            "auto" to linkedMapOf("type" to authorizeType, "time" to authorizeTime),
            "redirect" to linkedMapOf("url" to redirectUrl),
            "threeDSecure" to threeDSecure,
        )

        if (agreedPayment) {
            transaction["agreement"] = if (agreementType == "UNSCHEDULED") {
                linkedMapOf("type" to "UNSCHEDULED")
            } else {
                linkedMapOf("type" to agreementType, "amount_variability" to amountVariability)
            }
            transaction["applePayRecurringPaymentRequest"] = applePayRecurringPaymentRequest(now)
        }

        // Like the web demo, `subscription` does not depend on "Agreed payment": it is sent
        // whenever the agreement type is not UNSCHEDULED (which is the default).
        if (agreementType != "UNSCHEDULED") {
            transaction["subscription"] = linkedMapOf<String, Any>(
                "type" to agreementType,
                "amount_variability" to amountVariability,
                "txn_count" to (if (numberOfPayments == "Defined") paymentCount else 0),
            )
        }

        // Also like the web demo: sent unless the airline choice is explicitly "no".
        if (airline != "no") {
            transaction["airline"] = linkedMapOf("reference" to linkedMapOf("booking" to bookingReference))
        }
        return transaction
    }

    private fun applePayRecurringPaymentRequest(now: Date): Map<String, Any> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        return linkedMapOf(
            "paymentDescription" to subscriptionDescription,
            "regularBilling" to linkedMapOf(
                "label" to subscriptionName,
                "paymentTiming" to "recurring",
                "recurringPaymentStartDate" to formatter.format(now),
            ),
            "billingAgreement" to billingAgreement,
            // The web demo uses the page origin for both URLs.
            "managementURL" to "https://demo.tap.company",
            "tokenNotificationURL" to "https://demo.tap.company",
        )
    }

    // Persistence

    fun save(context: Context) {
        preferences(context).edit().putString(STORAGE_KEY, Gson().toJson(this)).apply()
    }

    companion object {
        const val PLACEHOLDER_PUBLIC_KEY = "YOUR_TAP_PUBLIC_KEY"

        /**
         * The page the web demo returns to after a redirect. The native SDK ignores it, but the
         * checkout config still expects `transaction.<mode>.redirect.url`.
         */
        const val REDIRECT_URL = "https://demo.tap.company/v2/sdk/checkout"

        private const val STORAGE_KEY = "CheckoutSettings.v1"

        /** Loads the saved settings, or the web demo defaults when nothing valid is saved. */
        fun load(context: Context): CheckoutSettings {
            val json = preferences(context).getString(STORAGE_KEY, null) ?: return CheckoutSettings()
            // Every field has a default, so Gson uses the no-argument constructor and keeps the
            // defaults for anything missing from the saved JSON.
            val settings = runCatching { Gson().fromJson(json, CheckoutSettings::class.java) }.getOrNull()
                ?: return CheckoutSettings()
            // A key saved while Keys.kt still held the placeholder follows Keys.kt once it is filled in.
            if (settings.publicKey == PLACEHOLDER_PUBLIC_KEY) settings.publicKey = Keys.PUBLIC_KEY
            return settings
        }

        fun reset(context: Context) {
            preferences(context).edit().remove(STORAGE_KEY).apply()
        }

        private fun preferences(context: Context) =
            context.getSharedPreferences("checkout_settings", Context.MODE_PRIVATE)

        /**
         * 0.1 + 0.2 is 0.30000000000000004 as a Double. Rounding to 3 decimals (the most any currency
         * uses) sends 0.3, like the web demo.
         */
        fun jsonNumber(value: Double): Double =
            BigDecimal(value).setScale(3, RoundingMode.HALF_UP).toDouble()
    }
}
