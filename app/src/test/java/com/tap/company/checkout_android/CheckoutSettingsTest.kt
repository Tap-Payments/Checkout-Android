package com.tap.company.checkout_android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks, on the JVM, that the configuration the example app sends matches the defaults documented
 * in README.md (which are the Tap web demo's) and the mapping rules of the configuration reference.
 */
class CheckoutSettingsTest {

    private fun config(settings: CheckoutSettings = CheckoutSettings()) = settings.makeConfiguration()

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any>.map(key: String) = get(key) as Map<String, Any>

    @Test
    fun defaultsMatchTheReadme() {
        val config = config(CheckoutSettings(publicKey = "pk_test_x"))
        assertEquals(true, config["open"])
        assertEquals("", config["hashString"])
        assertEquals("popup", config["checkoutMode"])
        assertEquals("en", config["language"])
        assertEquals("light", config["themeMode"])
        assertEquals("ALL", config["paymentType"])
        assertEquals("ALL", config["supportedPaymentMethods"])
        assertEquals("SAR", config["selectedCurrency"])
        assertEquals("ALL", config["supportedCurrencies"])
        assertEquals(true, config["isApplePayAvailableOnClient"])
        assertEquals(0.1, config["amount"])
        assertEquals(mapOf("publicKey" to "pk_test_x"), config["gateway"])
        for (key in listOf("supportedRegions", "supportedCountries", "supportedPaymentTypes", "supportedSchemes")) {
            assertFalse("$key should be omitted", config.containsKey(key))
        }
    }

    @Test
    fun customerDefaults() {
        assertEquals(
            mapOf(
                "firstName" to "Ahmed", "lastName" to "Sharkawy", "email" to "example@gmail.com",
                "phone" to mapOf("countryCode" to "20", "number" to "1099137773"),
            ),
            config().map("customer"),
        )
        assertEquals("cus_1", config(CheckoutSettings(customerId = "cus_1")).map("customer")["id"])
    }

    @Test
    fun cardOptionDefaults() {
        assertEquals(
            mapOf(
                "showBrands" to true, "showLoadingState" to false, "collectHolderName" to true, "preLoadCardName" to "",
                "cardNameEditable" to true, "cardFundingSource" to "all", "saveCardOption" to "all", "forceLtr" to false,
            ),
            config().map("cardOptions"),
        )
        val withInputs = config(CheckoutSettings(cardScanner = true, cardNFC = true)).map("cardOptions")
        assertEquals(mapOf("cardScanner" to true, "cardNFC" to true), withInputs["alternativeCardInputs"])
    }

    @Test
    fun orderDefaults() {
        val order = config().map("order")
        assertEquals("", order["id"])
        assertEquals("SAR", order["currency"])
        assertEquals(0.1, order["amount"])
        assertEquals(
            listOf(mapOf("amount" to 0.1, "currency" to "SAR", "name" to "Item Title 1", "quantity" to 1, "description" to "item description 1")),
            order["items"],
        )
        assertFalse("no discount by default", order.containsKey("discount"))
    }

    @Test
    fun chargeTransactionDefaults() {
        val transaction = config().map("transaction")
        assertEquals("charge", transaction["mode"])
        assertEquals(
            mapOf(
                "saveCard" to true,
                "auto" to mapOf("type" to "VOID", "time" to 100),
                "redirect" to mapOf("url" to "https://demo.tap.company/v2/sdk/checkout"),
                "threeDSecure" to true,
                "subscription" to mapOf("type" to "SCHEDULED", "amount_variability" to "FIXED", "txn_count" to 0),
                "airline" to mapOf("reference" to mapOf("booking" to "")),
            ),
            transaction["charge"],
        )
    }

    @Test
    fun authorizeUsesAnObjectNamedAfterTheMode() {
        val transaction = config(CheckoutSettings(mode = "authorize", authorizeType = "CAPTURE")).map("transaction")
        assertEquals("authorize", transaction["mode"])
        assertFalse(transaction.containsKey("charge"))
        assertEquals(mapOf("type" to "CAPTURE", "time" to 100), transaction.map("authorize")["auto"])
    }

    @Test
    fun discountAndTotals() {
        val items = listOf(CheckoutSettings.OrderItem(amount = 0.1), CheckoutSettings.OrderItem(name = "Item Title 2", amount = 0.2))
        val percent = CheckoutSettings(items = items, discountType = "P", discountValue = 10.0)
        assertEquals(0.27, config(percent)["amount"])
        assertEquals(mapOf("type" to "P", "value" to 10.0), config(percent).map("order")["discount"])

        val tooMuch = CheckoutSettings(discountType = "F", discountValue = 5.0)
        assertEquals("a total of 0 or less becomes 1, like the web demo", 1.0, config(tooMuch)["amount"])

        val quantity = CheckoutSettings(items = listOf(CheckoutSettings.OrderItem(quantity = 3, amount = 0.1)))
        assertEquals(0.3, config(quantity)["amount"])
    }

    @Test
    fun autoAndEmptyValuesAreNotSent() {
        val config = config(CheckoutSettings(language = "auto", theme = "auto"))
        assertNull(config["language"])
        assertNull(config["themeMode"])
        assertFalse(config.map("gateway").containsKey("merchantId"))
    }

    @Test
    fun listsAndCollapsing() {
        assertEquals("AUTO", config(CheckoutSettings(supportedCurrencies = listOf("AUTO", "SAR")))["supportedCurrencies"])
        assertEquals("ALL", config(CheckoutSettings(supportedCurrencies = listOf("SAR", "ALL")))["supportedCurrencies"])
        assertEquals(listOf("SAR", "KWD"), config(CheckoutSettings(supportedCurrencies = listOf("SAR", "KWD")))["supportedCurrencies"])
        assertEquals(listOf("VISA"), config(CheckoutSettings(supportedPaymentMethods = listOf("VISA")))["supportedPaymentMethods"])
        assertEquals(listOf("SA"), config(CheckoutSettings(supportedCountries = listOf("SA")))["supportedCountries"])
    }

    @Test
    fun agreementAndSubscription() {
        val unscheduled = config(CheckoutSettings(agreedPayment = true, agreementType = "UNSCHEDULED")).map("transaction").map("charge")
        assertEquals(mapOf("type" to "UNSCHEDULED"), unscheduled["agreement"])
        assertFalse("no subscription when UNSCHEDULED", unscheduled.containsKey("subscription"))
        assertTrue(unscheduled.containsKey("applePayRecurringPaymentRequest"))

        val defined = config(CheckoutSettings(agreedPayment = true, numberOfPayments = "Defined", paymentCount = 4)).map("transaction").map("charge")
        assertEquals(mapOf("type" to "SCHEDULED", "amount_variability" to "FIXED"), defined["agreement"])
        assertEquals(4, defined.map("subscription")["txn_count"])
    }

    @Test
    fun airlineIsSentUnlessNo() {
        assertFalse(config(CheckoutSettings(airline = "no")).map("transaction").map("charge").containsKey("airline"))
        val yes = config(CheckoutSettings(airline = "yes", bookingReference = "ABC123")).map("transaction").map("charge")
        assertEquals(mapOf("reference" to mapOf("booking" to "ABC123")), yes["airline"])
    }

    @Test
    fun placeholderKeyIsNotAKey() {
        assertFalse(CheckoutSettings(publicKey = CheckoutSettings.PLACEHOLDER_PUBLIC_KEY).hasPublicKey)
        assertFalse(CheckoutSettings(publicKey = "  ").hasPublicKey)
        assertTrue(CheckoutSettings(publicKey = "pk_test_x").hasPublicKey)
    }

    @Test
    fun testOverridesMergeAndRemove() {
        val merged = UITestHooks.merge(
            config(),
            mapOf("language" to "ar", "cardOptions" to mapOf("showBrands" to false), "hashString" to null),
        )
        assertEquals("ar", merged["language"])
        assertEquals(false, merged.map("cardOptions")["showBrands"])
        assertEquals("other card options are kept", "all", merged.map("cardOptions")["saveCardOption"])
        assertFalse(merged.containsKey("hashString"))
    }
}
