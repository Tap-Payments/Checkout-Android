package com.tap.company.checkout_android

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.KeyEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.junit.rules.TestName
import org.junit.runner.RunWith
import java.io.File

/**
 * Checks, on an emulator against the Tap sandbox, that the configuration values documented in
 * README.md do what the README says on Android. Each test starts the checkout with one value
 * changed (through UITestHooks in the app), then looks at the checkout sheet or at the charge the
 * server returns. Every test saves a screenshot and logs a "RESULT |" line (logcat tag ReadmeConfig).
 * Port of ReadmeConfigurationTests.swift in the iOS repository.
 *
 * Run (the sandbox public key is passed in, never committed):
 *   ./gradlew :app:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.TAP_PUBLIC_KEY=pk_test_...
 */
@RunWith(AndroidJUnit4::class)
class ReadmeConfigurationTests {

    /** Like `continueAfterFailure = true` on iOS: a test reports every failed check, not just the first. */
    @get:Rule val errors = ErrorCollector()
    @get:Rule val testName = TestName()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val packageName = instrumentation.targetContext.packageName
    private var publicKey = ""

    @Before
    fun setUp() {
        val key = InstrumentationRegistry.getArguments().getString("TAP_PUBLIC_KEY").orEmpty()
        assumeTrue("Pass -Pandroid.testInstrumentationRunnerArguments.TAP_PUBLIC_KEY=pk_test_... to run these tests.", key.isNotEmpty())
        publicKey = key
    }

    @After
    fun tearDown() {
        // The orchestrator starts the next test in a new process with cleared app data.
        device.pressHome()
    }

    private fun check(message: String, condition: Boolean) {
        errors.checkSucceeds { assertTrue(message, condition) }
    }

    // SDK and gateway

    @Test fun defaultConfigurationOpensCheckout() {
        open()
        check("card input should be shown", hasCardInput)
        result("defaults", "checkout opens, card input shown")
    }

    @Test fun invalidPublicKeyReportsError() {
        open(key = "pk_test_invalidKey000000000000", expectReady = false)
        val gotError = waitForCallback("onError", 30)
        check("an invalid key should end in onError", gotError)
        check("no onReady expected", !log.contains("onReady"))
        result("gateway.publicKey invalid", if (gotError) "onError" else "no onError")
    }

    @Test fun unknownMerchantId() {
        open(mapOf("gateway" to mapOf("merchantId" to "000000")), expectReady = false)
        result("gateway.merchantId unknown", readyOrError())
    }

    @Test fun hashString() {
        open(mapOf("hashString" to "notARealHash"), expectReady = false)
        result("hashString invalid", readyOrError())
    }

    @Test fun languageArabic() {
        open(mapOf("language" to "ar"))
        check("Arabic text expected", hasArabicText)
        result("language ar", if (hasArabicText) "Arabic text shown" else "no Arabic text")
    }

    @Test fun languageEnglish() {
        open(mapOf("language" to "en"))
        check("no Arabic text expected", !hasArabicText)
        result("language en", if (hasArabicText) "Arabic text shown" else "English only")
    }

    @Test fun themeLight() = checkTheme("light", dark = false)
    @Test fun themeDark() = checkTheme("dark", dark = true)
    @Test fun themeLightMono() = checkTheme("light_mono", dark = false)
    @Test fun themeDarkColored() = checkTheme("dark_colored", dark = true)

    @Test fun paymentTypeCard() {
        open(mapOf("paymentType" to "CARD"))
        check("card input expected", hasCardInput)
        result("paymentType CARD", if (hasCardInput) "card input shown" else "no card input")
    }

    @Test fun paymentTypeWeb() {
        open(mapOf("paymentType" to "WEB"), expectReady = false)
        val outcome = readyOrError()
        check("WEB should hide the card input", !hasCardInput)
        result("paymentType WEB", "$outcome, card input ${if (hasCardInput) "shown" else "hidden"}")
    }

    @Test fun paymentTypeDevice() {
        open(mapOf("paymentType" to "DEVICE"), expectReady = false)
        val outcome = readyOrError()
        check("DEVICE should hide the card input", !hasCardInput)
        result("paymentType DEVICE", "$outcome, card input ${if (hasCardInput) "shown" else "hidden"}")
    }

    @Test fun supportedPaymentMethodsVisaOnly() {
        open(mapOf("supportedPaymentMethods" to listOf("VISA")))
        check("VISA expected", hasBrand("VISA"))
        check("MADA should not be offered", !hasBrand("MADA"))
        check("MASTERCARD should not be offered", !hasBrand("MASTERCARD"))
        result("supportedPaymentMethods [VISA]", "brands: $brandsShown")
    }

    @Test fun supportedSchemesVisaOnly() {
        open(mapOf("supportedSchemes" to listOf("VISA")))
        check("MADA should not be offered", !hasBrand("MADA"))
        result("supportedSchemes [VISA]", "brands: $brandsShown")
    }

    @Test fun supportedPaymentTypesCardOnly() {
        open(mapOf("supportedPaymentTypes" to listOf("CARD")))
        check("card input expected", hasCardInput)
        result("supportedPaymentTypes [CARD]", "card input ${if (hasCardInput) "shown" else "hidden"} (see screenshot for wallets/BNPL)")
    }

    @Test fun supportedRegionsLocal() {
        open(mapOf("supportedRegions" to listOf("LOCAL")), expectReady = false)
        result("supportedRegions [LOCAL]", "${readyOrError()}, brands: $brandsShown")
    }

    @Test fun supportedCountriesSaudi() {
        open(mapOf("supportedCountries" to listOf("SA")), expectReady = false)
        result("supportedCountries [SA]", "${readyOrError()}, brands: $brandsShown")
    }

    @Test fun selectedCurrencyKWD() {
        open(mapOf("selectedCurrency" to "KWD", "order" to mapOf("currency" to "KWD", "items" to listOf(item(0.1, currency = "KWD")))), expectReady = false)
        result("selectedCurrency KWD", readyOrError())
    }

    @Test fun supportedCurrenciesSarOnly() {
        open(mapOf("supportedCurrencies" to listOf("SAR")))
        check("the 'Pay in <other currency>' option should be gone", !has("Pay in"))
        result("supportedCurrencies [SAR]", if (has("Pay in")) "other currency still offered" else "no other currency offered")
    }

    @Test fun applePayAvailableOnClientFalse() {
        open(mapOf("isApplePayAvailableOnClient" to false))
        result("isApplePayAvailableOnClient false", "Apple Pay ${if (has("Apple Pay") || has("apple_pay")) "shown" else "not shown"} (see screenshot)")
    }

    @Test fun openFalse() {
        open(mapOf("open" to false), expectReady = false)
        result("open false", readyOrError())
    }

    // Card options

    @Test fun showBrandsFalse() {
        open(mapOf("cardOptions" to mapOf("showBrands" to false)))
        check("brand list should be hidden", !has("WE ACCEPT"))
        result("cardOptions.showBrands false", if (has("WE ACCEPT")) "brands still shown" else "brands hidden")
    }

    @Test fun showLoadingStateTrue() {
        open(mapOf("cardOptions" to mapOf("showLoadingState" to true)))
        result("cardOptions.showLoadingState true", "opens (see screenshot)")
    }

    @Test fun collectHolderNameFalse() {
        open(mapOf("cardOptions" to mapOf("collectHolderName" to false)))
        enterTestCard(holder = null)
        val nameField = hasHolderNameField
        check("no card holder name field expected", !nameField)
        result("cardOptions.collectHolderName false", if (nameField) "name field still asked" else "no name field")
    }

    @Test fun preLoadCardName() {
        open(mapOf("cardOptions" to mapOf("preLoadCardName" to "John Doe")))
        enterTestCard(holder = null)
        val shown = has("JOHN DOE") || has("John Doe")
        check("the pre-loaded name should be filled in", shown)
        result("cardOptions.preLoadCardName", if (shown) "name pre-filled" else "name not pre-filled")
    }

    @Test fun cardNameNotEditable() {
        open(mapOf("cardOptions" to mapOf("preLoadCardName" to "John Doe", "cardNameEditable" to false)))
        enterTestCard(holder = null)
        typeSlowly("X")
        val changed = has("JOHN DOEX") || has("John DoeX")
        check("the name should not be editable", !changed)
        result("cardOptions.cardNameEditable false", if (changed) "name could be edited" else "name not editable")
    }

    @Test fun saveCardOptionNone() {
        open(mapOf("cardOptions" to mapOf("saveCardOption" to "none")))
        enterTestCard(holder = "Test User")
        check("no save-card option expected", !has("Save card"))
        result("cardOptions.saveCardOption none", if (has("Save card")) "save-card still shown" else "save-card hidden")
    }

    @Test fun saveCardOptionAll() {
        open()
        enterTestCard(holder = "Test User")
        check("save-card option expected", has("Save card"))
        result("cardOptions.saveCardOption all", if (has("Save card")) "save-card shown" else "save-card hidden")
    }

    @Test fun cardFundingSourceCreditRejectsDebitCard() {
        // The sandbox test card 4508 7500 1574 1019 is a DEBIT card.
        open(mapOf("cardOptions" to mapOf("cardFundingSource" to "credit")))
        enterTestCard(holder = "Test User")
        val paid = payAndFinish().contains("\"status\":\"CAPTURED\"")
        check("a debit card should not be charged when only credit is allowed", !paid)
        result("cardOptions.cardFundingSource credit", if (paid) "debit card was charged" else "debit card refused")
    }

    @Test fun forceLtrWithArabic() {
        open(mapOf("language" to "ar", "cardOptions" to mapOf("forceLtr" to true)))
        result("cardOptions.forceLtr true (ar)", "opens (see screenshot)")
    }

    @Test fun cardScannerOn() {
        open(mapOf("cardOptions" to mapOf("alternativeCardInputs" to mapOf("cardScanner" to true))))
        result("cardOptions.alternativeCardInputs.cardScanner true", "opens (see screenshot for the scan button)")
    }

    @Test fun cardScannerOff() {
        open()
        result("cardScanner omitted", "opens (see screenshot, no scan button expected)")
    }

    @Test fun cardNfcOn() {
        open(mapOf("cardOptions" to mapOf("alternativeCardInputs" to mapOf("cardNFC" to true))))
        result("cardOptions.alternativeCardInputs.cardNFC true", "opens (see screenshot for the NFC button)")
    }

    // Payments: customer, order and transaction

    @Test fun chargeIsCapturedWithCustomerAndAirline() {
        open()
        enterTestCard(holder = "Test User")
        val charge = payAndFinish()
        check("charge should be CAPTURED", charge.contains("\"status\":\"CAPTURED\""))
        check("customer.firstName", charge.contains("\"first_name\":\"Ahmed\""))
        check("customer.lastName", charge.contains("\"last_name\":\"Sharkawy\""))
        check("customer.email", charge.contains("\"email\":\"example@gmail.com\""))
        check("customer.phone.countryCode", charge.contains("\"country_code\":\"20\""))
        check("customer.phone.number", charge.contains("\"number\":\"1099137773\""))
        check("airline sent by default", charge.contains("\"airline\":{\"id\""))
        result("transaction.mode charge", summary(charge))
    }

    @Test fun authorizeWithAutoVoid() {
        open(mapOf("transaction" to authorize("VOID")))
        enterTestCard(holder = "Test User")
        val auth = payAndFinish()
        check("authorization expected", auth.contains("\"status\":\"AUTHORIZED\""))
        check("auto.type VOID", auth.contains("\"type\":\"VOID\""))
        result("transaction.mode authorize + auto VOID", summary(auth))
    }

    @Test fun authorizeWithAutoCapture() {
        open(mapOf("transaction" to authorize("CAPTURE")))
        enterTestCard(holder = "Test User")
        val auth = payAndFinish()
        check("authorization expected", auth.contains("\"status\":\"AUTHORIZED\""))
        check("auto.type CAPTURE", auth.contains("\"type\":\"CAPTURE\""))
        result("transaction.mode authorize + auto CAPTURE", summary(auth))
    }

    @Test fun threeDSecureOff() {
        open(mapOf("transaction" to mapOf("charge" to mapOf("threeDSecure" to false))))
        enterTestCard(holder = "Test User")
        val charge = payAndFinish()
        result("transaction.charge.threeDSecure false", "${summary(charge)}, 3DS page ${if (sawThreeDSPage) "shown" else "not shown"}")
    }

    @Test fun orderWithTwoItemsAndDiscount() {
        // 0.1 + 0.2 = 0.3, minus 10 % = 0.27.
        open(mapOf(
            "amount" to 0.27,
            "order" to mapOf("amount" to 0.27, "items" to listOf(item(0.1), item(0.2, name = "Item Title 2")), "discount" to mapOf("type" to "P", "value" to 10)),
        ))
        enterTestCard(holder = "Test User")
        val charge = payAndFinish()
        check("the discounted total should be charged", charge.contains("\"amount\":0.27"))
        result("order: 2 items + 10% discount", summary(charge))
    }

    @Test fun agreementUnscheduled() {
        open(mapOf("transaction" to mapOf("charge" to mapOf("agreement" to mapOf("type" to "UNSCHEDULED"), "subscription" to null))), expectReady = false)
        result("transaction.charge.agreement UNSCHEDULED", readyOrError())
    }

    // Launching and waiting

    /** Launches the app, which starts the checkout with [overrides] applied, and waits for onReady. */
    private fun open(overrides: Map<String, Any?> = emptyMap(), key: String? = null, expectReady: Boolean = true) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName(packageName, "$packageName.MainActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(UITestHooks.UITEST, "1")
            putExtra(UITestHooks.UITEST_AUTOSTART, "1")
            putExtra(UITestHooks.UITEST_PUBLIC_KEY, key ?: publicKey)
            if (overrides.isNotEmpty()) putExtra(UITestHooks.UITEST_CONFIG_OVERRIDES, JSONObject(overrides).toString())
        }
        instrumentation.targetContext.startActivity(intent)
        device.wait(Until.hasObject(By.res(packageName, "callbacks")), 10_000)
        if (!expectReady) return
        check("checkout did not become ready. Log: $log", waitForCallback("onReady", 45))
        settle()
    }

    private val log: String
        get() = runCatching { device.findObject(By.res(packageName, "callbacks"))?.text }.getOrNull().orEmpty()

    private fun waitForCallback(name: String, timeoutSeconds: Int): Boolean {
        val deadline = System.currentTimeMillis() + timeoutSeconds * 1000L
        while (System.currentTimeMillis() < deadline) {
            if (log.contains(name)) return true
            Thread.sleep(500)
        }
        return false
    }

    /** Waits for onReady or onError, whichever comes first. */
    private fun readyOrError(): String {
        val deadline = System.currentTimeMillis() + 45_000
        while (System.currentTimeMillis() < deadline) {
            val text = log
            if (text.contains("onError")) { settle(); return "onError: " + text.substringAfterLast("onError: ").take(160) }
            if (text.contains("onReady")) { settle(); return "opens (onReady)" }
            Thread.sleep(500)
        }
        return "neither onReady nor onError within 45 s"
    }

    /** Gives the web page a moment to finish drawing after onReady. */
    private fun settle(seconds: Double = 3.0) = Thread.sleep((seconds * 1000).toLong())

    // Reading the checkout sheet

    private fun has(text: String): Boolean =
        device.hasObject(By.textContains(text)) || device.hasObject(By.descContains(text))

    private fun hasBrand(brand: String): Boolean = device.hasObject(By.desc(brand))

    private val hasCardInput: Boolean get() = device.hasObject(By.res("card_input_mini"))

    private val hasArabicText: Boolean
        get() = device.hasObject(By.text(ARABIC)) || device.hasObject(By.desc(ARABIC))

    /** The holder name field comes after the card number, expiry and CVV fields. */
    private val hasHolderNameField: Boolean
        get() = device.findObjects(By.clazz("android.widget.EditText")).any { field ->
            field.resourceName?.contains("name", ignoreCase = true) == true || field.text?.contains("name", ignoreCase = true) == true
        }

    private val brandsShown: String
        get() = listOf("VISA", "MASTERCARD", "MADA", "AMERICAN_EXPRESS", "UNIONPAY", "BENEFIT", "OMANNET", "JAYWAN", "MAAL")
            .filter { hasBrand(it) }.ifEmpty { listOf("none") }.joinToString(", ")

    private fun checkTheme(theme: String, dark: Boolean) {
        open(mapOf("themeMode" to theme))
        val brightness = sheetBrightness()
        if (dark) check("$theme should give a dark sheet (brightness $brightness)", brightness < 0.4)
        else check("$theme should give a light sheet (brightness $brightness)", brightness > 0.6)
        result("themeMode $theme", "sheet brightness %.2f (%s)".format(brightness, if (brightness < 0.5) "dark" else "light"))
    }

    /** Average brightness (0 black, 1 white) of the lower part of the screen, where the sheet is. */
    private fun sheetBrightness(): Double {
        val screenshot = instrumentation.uiAutomation.takeScreenshot() ?: return -1.0
        val crop = Bitmap.createBitmap(screenshot, 0, (screenshot.height * 0.7).toInt(), screenshot.width, (screenshot.height * 0.3).toInt())
        val small = Bitmap.createScaledBitmap(crop, 40, 40, true)
        var total = 0.0
        for (x in 0 until 40) for (y in 0 until 40) {
            val pixel = small.getPixel(x, y)
            total += (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)) / 255
        }
        return total / (40 * 40)
    }

    // Paying with the sandbox 3DS test card

    private var sawThreeDSPage = false

    /** Types the sandbox test card 4508 7500 1574 1019, 01/39, CVV 100 (and the holder name if given). */
    private fun enterTestCard(holder: String?) {
        val field = device.wait(Until.findObject(By.res("card_input_mini")), 15_000)
            ?: return errors.addError(AssertionError("card input not found"))
        field.click()
        settle(1.5)
        // The page swaps the short card field for the full form after the first digit; keys typed
        // before the new field has focus are lost, so wait for it (the swap can take several seconds).
        typeSlowly("4")
        waitForFocusedCardField()
        for (part in listOf("508750015741019", "0139", "100")) {
            typeSlowly(part)
            settle(0.8)
        }
        if (holder != null) {
            typeSlowly(holder)
            settle(0.8)
        }
        settle(1.0)
    }

    /** Waits until a focused field holds the first digit, then puts the cursor after it. */
    private fun waitForFocusedCardField() {
        settle(1.0)
        val deadline = System.currentTimeMillis() + 10_000
        while (System.currentTimeMillis() < deadline) {
            val focused = device.findObject(By.focused(true).clazz("android.widget.EditText"))
            if (focused?.text?.contains("4") == true) {
                settle(1.0)
                device.pressKeyCode(KeyEvent.KEYCODE_MOVE_END)
                return
            }
            // The new field may not take focus by itself: tap the one that shows the digit.
            device.findObjects(By.clazz("android.widget.EditText")).firstOrNull { it.text?.contains("4") == true }?.click()
            Thread.sleep(500)
        }
        errors.addError(AssertionError("card number field did not take focus after the first digit"))
    }

    /**
     * The checkout formats the card fields as you type and drops keys sent in one burst,
     * so type one character at a time.
     */
    private fun typeSlowly(text: String) {
        for (character in text) {
            when {
                character.isDigit() -> device.pressKeyCode(KeyEvent.KEYCODE_0 + (character - '0'))
                character == ' ' -> device.pressKeyCode(KeyEvent.KEYCODE_SPACE)
                character.isLetter() -> device.pressKeyCode(
                    KeyEvent.KEYCODE_A + (character.lowercaseChar() - 'a'),
                    if (character.isUpperCase()) KeyEvent.META_SHIFT_ON else 0,
                )
            }
            Thread.sleep(120)
        }
    }

    /**
     * Taps Pay, approves the 3DS emulator if it appears, and returns the onChargeCreated payload
     * (empty if the payment did not go through).
     */
    private fun payAndFinish(): String {
        device.pressBack() // closes the keyboard
        settle(1.5)
        val width = device.displayWidth
        val payButton = device.findObjects(By.clazz("android.widget.Button"))
            .filter { it.visibleBounds.width() > width * 0.8 }
            .maxByOrNull { it.visibleBounds.top }
            ?: run { errors.addError(AssertionError("pay button not found")); return "" }
        payButton.click()

        // The 3DS emulator page ("ACS Emulator for 3DS V2") has a Submit button, exposed as text or description.
        var submit: UiObject2? = null
        val submitDeadline = System.currentTimeMillis() + 45_000
        while (submit == null && System.currentTimeMillis() < submitDeadline && !log.contains("onSuccess") && !log.contains("onError")) {
            submit = device.findObject(By.text("Submit")) ?: device.findObject(By.desc("Submit"))
            if (submit == null) Thread.sleep(500)
        }
        sawThreeDSPage = submit != null
        // A tap that lands while the 3DS page is still loading is ignored, so tap again while it stays up.
        var attempts = 0
        while (submit != null && attempts < 4 && !log.contains("onChargeCreated") && !log.contains("onError")) {
            settle(2.0)
            runCatching { submit.click() }
            attempts++
            settle(8.0)
            submit = device.findObject(By.text("Submit")) ?: device.findObject(By.desc("Submit"))
        }

        val deadline = System.currentTimeMillis() + 45_000
        while (System.currentTimeMillis() < deadline && !log.contains("onSuccess") && !log.contains("onError")) Thread.sleep(500)
        settle(1.0)
        return log.split("onChargeCreated: ").drop(1).firstOrNull()?.substringBefore("\n\n").orEmpty()
    }

    // Helpers

    private fun item(amount: Double, name: String = "Item Title 1", currency: String = "SAR") =
        mapOf("amount" to amount, "currency" to currency, "name" to name, "quantity" to 1, "description" to "item description")

    /** A `transaction` object for authorize mode; the default `charge` object is removed. */
    private fun authorize(type: String) = mapOf(
        "mode" to "authorize",
        "charge" to null,
        "authorize" to mapOf(
            "saveCard" to true,
            "auto" to mapOf("type" to type, "time" to 100),
            "redirect" to mapOf("url" to "https://demo.tap.company/v2/sdk/checkout"),
            "threeDSecure" to true,
        ),
    )

    private fun summary(payload: String): String {
        if (payload.isEmpty()) return "no charge created. Log: " + log.take(200)
        fun field(name: String) = payload.substringAfter("\"$name\":", "?").takeWhile { it != ',' && it != '}' }.replace("\"", "")
        return "${field("object")} ${field("status")} ${field("amount")} ${field("currency")}"
    }

    /** Logs a result line and saves the current screen next to the test results. */
    private fun result(name: String, outcome: String) {
        Log.i("ReadmeConfig", "RESULT | $name | $outcome")
        val dir = File(instrumentation.context.getExternalFilesDir(null), "readme-config-screenshots").apply { mkdirs() }
        device.takeScreenshot(File(dir, "${testName.methodName}.png"))
    }

    private companion object {
        val ARABIC = Regex(".*[\\u0600-\\u06FF].*", RegexOption.DOT_MATCHES_ALL).toPattern()
    }
}
