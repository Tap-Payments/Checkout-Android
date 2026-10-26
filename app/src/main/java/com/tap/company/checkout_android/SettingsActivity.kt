package com.tap.company.checkout_android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * A form with every input of the Tap web demo, grouped like its steps (SDK / User / Card / Order /
 * Transaction). Every change is saved immediately; "Show configuration JSON" displays exactly what
 * Start Checkout will pass to the SDK. Same rows as SettingsViewController.swift in the iOS example apps.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyInsets(findViewById(R.id.settings_container))
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings_container, SettingsFragment()).commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

/** A value offered by a choice row: `value` goes into the configuration, `title` is shown to the user. */
private data class Option(val value: String, val title: String = value)

private object Options {
    private val currencyCodes = listOf("KWD", "BHD", "SAR", "AED", "OMR", "EGP", "GBP", "USD", "EUR")
    val currencies = currencyCodes.map { Option(it) }
    val supportedCurrencies = (listOf("ALL", "AUTO") + currencyCodes).map { Option(it) }
    val paymentTypes = listOf("ALL", "WEB", "CARD", "DEVICE").map { Option(it) }
    val paymentMethods = listOf("ALL", "AMERICAN_EXPRESS", "APPLE_PAY", "BENEFIT", "BENEFITPAY", "CAREEMPAY", "FAWRY",
        "GOOGLE_PAY", "KNET", "MADA", "MASTERCARD", "MEEZA", "OMANNET", "PAYPAL", "POST_PAY",
        "NAPS", "STC_PAY", "TABBY", "VISA").map { Option(it) }
    val languages = listOf("auto", "en", "ar").map { Option(it) }
    val regions = listOf("LOCAL", "REGIONAL", "GLOBAL").map { Option(it) }
    val supportedPaymentTypes = listOf("CARD", "DEVICE_WALLET", "EXPRESS_CHECKOUT_WALLET", "PASS_THRU_WALLET",
        "STORED_VALUE_WALLET", "CASH_WALLET", "BNPL").map { Option(it) }
    val schemes = listOf("BENEFIT", "VISA", "AMEX", "MASTERCARD", "MADA", "MEEZA", "OMANNET").map { Option(it) }
    /** ISO 3166-1 alpha-2 codes, titled with the country name. */
    val countries = Locale.getISOCountries().sorted().map { Option(it, "$it - ${Locale("", it).displayCountry}") }

    val fundingSources = listOf("all", "credit", "debit").map { Option(it) }
    val saveCardOptions = listOf("all", "merchant", "tap", "none").map { Option(it) }
    val discountTypes = listOf(Option("F", "Fixed amount"), Option("P", "Percentage"))
    val modes = listOf(Option("charge", "Charge"), Option("authorize", "Authorize"))
    val themes = listOf("light", "dark", "light_mono", "dark_colored", "auto").map { Option(it) }
    val checkoutModes = listOf("popup", "page").map { Option(it) }
    val authorizeTypes = listOf("CAPTURE", "VOID").map { Option(it) }
    val agreementTypes = listOf("SCHEDULED", "UNSCHEDULED").map { Option(it) }
    val amountVariabilities = listOf("FIXED", "VARIABLE").map { Option(it) }
    val numbersOfPayments = listOf("Defined", "Undefined").map { Option(it) }
    val airline = listOf(Option("", "Not set (sent empty)"), Option("yes", "Yes"), Option("no", "No"))
}

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var settings: CheckoutSettings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        settings = CheckoutSettings.load(requireContext())
        build()
    }

    /** Rebuilds the form, e.g. when a choice shows or hides other rows, keeping the scroll position. */
    private fun rebuild() {
        val state = listView?.layoutManager?.onSaveInstanceState()
        build()
        state?.let { listView?.layoutManager?.onRestoreInstanceState(it) }
    }

    private fun build() {
        val screen = preferenceManager.createPreferenceScreen(requireContext())
        preferenceScreen = screen
        sdkSection()
        userSection()
        cardSection()
        orderSection()
        transactionSection()
        section("Tools") {
            button("Show configuration JSON") { ConfigurationJsonDialog.show(requireContext(), settings.makeConfiguration()) }
            button("Reset to defaults") {
                AlertDialog.Builder(requireContext())
                    .setTitle("Reset to defaults?")
                    .setMessage("All settings go back to the web demo defaults.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reset") { _, _ ->
                        CheckoutSettings.reset(requireContext())
                        settings = CheckoutSettings.load(requireContext())
                        rebuild()
                    }
                    .show()
            }
        }
    }

    // Sections

    private fun sdkSection() = section(
        "SDK Configuration",
        footer = "Defaults to the key in Keys.kt. Under each public key there can be several merchant ids; use the one that belongs to it.",
    ) {
        text("Public Key", settings.publicKey) { settings.publicKey = it }
        text("Merchant Id", settings.merchantId) { settings.merchantId = it }
        multiChoice("Supported Currencies", Options.supportedCurrencies, settings.supportedCurrencies) { settings.supportedCurrencies = it }
        choice("Payment Type", Options.paymentTypes, settings.paymentType) { settings.paymentType = it }
        multiChoice("Supported Payment Methods", Options.paymentMethods, settings.supportedPaymentMethods) { settings.supportedPaymentMethods = it }
        choice("Selected Currency", Options.currencies, settings.selectedCurrency, rebuilds = true) { settings.selectedCurrency = it }
        choice("Language", Options.languages, settings.language) { settings.language = it }
        multiChoice("Supported Regions", Options.regions, settings.supportedRegions) { settings.supportedRegions = it }
        multiChoice("Supported Countries", Options.countries, settings.supportedCountries) { settings.supportedCountries = it }
        multiChoice("Supported Payment Types", Options.supportedPaymentTypes, settings.supportedPaymentTypes) { settings.supportedPaymentTypes = it }
        multiChoice("Supported Schemes", Options.schemes, settings.supportedSchemes) { settings.supportedSchemes = it }
    }

    private fun userSection() = section("User Configuration") {
        text("Customer ID", settings.customerId) { settings.customerId = it }
        text("First Name", settings.firstName) { settings.firstName = it }
        text("Last Name", settings.lastName) { settings.lastName = it }
        text("Customer E-mail", settings.email, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) { settings.email = it }
        text("Customer Country Code", settings.countryCode, InputType.TYPE_CLASS_NUMBER) { settings.countryCode = it }
        text("Customer phone number", settings.phoneNumber, InputType.TYPE_CLASS_PHONE) { settings.phoneNumber = it }
    }

    private fun cardSection() = section("Card Configuration") {
        toggle("Show Brands", settings.showBrands) { settings.showBrands = it }
        toggle("Show Loading State", settings.showLoadingState) { settings.showLoadingState = it }
        toggle("Collect Card Holder Name", settings.collectHolderName) { settings.collectHolderName = it }
        text("PreLoad CardName", settings.preLoadCardName) { settings.preLoadCardName = it }
        toggle("CardName Editable", settings.cardNameEditable) { settings.cardNameEditable = it }
        choice("Card Funding Source", Options.fundingSources, settings.cardFundingSource) { settings.cardFundingSource = it }
        choice("Save Card Options", Options.saveCardOptions, settings.saveCardOption) { settings.saveCardOption = it }
        toggle("Force LTR", settings.forceLtr) { settings.forceLtr = it }
        toggle("Card Scanner", settings.cardScanner) { settings.cardScanner = it }
        toggle("Card NFC (Android only)", settings.cardNFC) { settings.cardNFC = it }
    }

    private fun orderSection() = section("Order Configuration", footer = "Total = items minus the discount (1 if the result is 0 or less).") {
        text("Order ID", settings.orderId) { settings.orderId = it }
        choice("Order discount type", Options.discountTypes, settings.discountType, rebuilds = true) { settings.discountType = it }
        decimal("Order discount value", settings.discountValue) { settings.discountValue = it }
        settings.items.forEachIndexed { index, item ->
            val number = index + 1
            text("Order $number Name", item.name) { item.name = it }
            text("Order $number Description", item.description) { item.description = it }
            integer("Order $number Quantity", item.quantity) { item.quantity = it }
            decimal("Order $number Amount", item.amount) { item.amount = it }
            if (settings.items.size > 1) {
                button("Remove item $number") {
                    settings.items = settings.items.filterIndexed { i, _ -> i != index }
                    saveAndRebuild()
                }
            }
        }
        button("Add New Item") {
            settings.items = settings.items + CheckoutSettings.OrderItem()
            saveAndRebuild()
        }
        info("Total to pay", "${settings.totalAmount.formatted} ${settings.selectedCurrency}")
    }

    private fun transactionSection() = section("Transaction Configuration") {
        choice("Transaction Mode", Options.modes, settings.mode, rebuilds = true) { settings.mode = it }
        if (settings.mode == "authorize") {
            choice("Authorize Type", Options.authorizeTypes, settings.authorizeType) { settings.authorizeType = it }
        }
        choice("Theme", Options.themes, settings.theme) { settings.theme = it }
        choice("Checkout Mode", Options.checkoutModes, settings.checkoutMode) { settings.checkoutMode = it }
        toggle("3D Secure", settings.threeDSecure) { settings.threeDSecure = it }
        text("Hashstring", settings.hashString) { settings.hashString = it }
        toggle("Agreed payment", settings.agreedPayment, rebuilds = true) { settings.agreedPayment = it }
        if (settings.agreedPayment) {
            choice("Agreement Type", Options.agreementTypes, settings.agreementType, rebuilds = true) { settings.agreementType = it }
            if (settings.agreementType != "UNSCHEDULED") {
                choice("Amount Variability", Options.amountVariabilities, settings.amountVariability) { settings.amountVariability = it }
                choice("Number of payments", Options.numbersOfPayments, settings.numberOfPayments, rebuilds = true) { settings.numberOfPayments = it }
                if (settings.numberOfPayments == "Defined") {
                    integer("Count", settings.paymentCount) { settings.paymentCount = it }
                }
            }
            text("Subscription Name", settings.subscriptionName) { settings.subscriptionName = it }
            text("Subscription Description", settings.subscriptionDescription) { settings.subscriptionDescription = it }
            text("Billing Agreement", settings.billingAgreement) { settings.billingAgreement = it }
        }
        choice("Airline", Options.airline, settings.airline, rebuilds = true) { settings.airline = it }
        if (settings.airline == "yes") {
            text("Booking Reference Number", settings.bookingReference) { settings.bookingReference = it }
        }
    }

    // Row builders

    private var category: PreferenceCategory? = null

    private fun section(title: String, footer: String? = null, rows: () -> Unit) {
        val section = PreferenceCategory(requireContext()).apply { this.title = title; isIconSpaceReserved = false }
        preferenceScreen.addPreference(section)
        category = section
        rows()
        footer?.let { info(null, it) }
    }

    private fun <P : Preference> add(preference: P, title: String?): P {
        preference.title = title
        preference.isPersistent = false
        preference.isIconSpaceReserved = false
        category?.addPreference(preference)
        return preference
    }

    private fun saveAndRebuild() {
        settings.save(requireContext())
        rebuild()
    }

    /** Saves after every change; rows whose value changes the total or shows/hides other rows rebuild the form. */
    private fun changed(rebuilds: Boolean) {
        if (rebuilds) saveAndRebuild() else settings.save(requireContext())
    }

    private fun text(title: String, value: String, inputType: Int = InputType.TYPE_CLASS_TEXT, rebuilds: Boolean = false, set: (String) -> Unit) {
        add(EditTextPreference(requireContext()), title).apply {
            text = value
            dialogTitle = title
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { it.text.orEmpty().ifEmpty { "Not set" } }
            setOnBindEditTextListener { it.inputType = inputType; it.setSelection(it.text.length) }
            setOnPreferenceChangeListener { _, newValue -> set(newValue as String); changed(rebuilds); true }
        }
    }

    private fun decimal(title: String, value: Double, set: (Double) -> Unit) =
        text(title, value.formatted, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL, rebuilds = true) { input ->
            input.toDoubleOrNull()?.let(set)
        }

    private fun integer(title: String, value: Int, set: (Int) -> Unit) =
        text(title, value.toString(), InputType.TYPE_CLASS_NUMBER, rebuilds = true) { input ->
            input.toIntOrNull()?.let(set)
        }

    private fun toggle(title: String, value: Boolean, rebuilds: Boolean = false, set: (Boolean) -> Unit) {
        add(SwitchPreferenceCompat(requireContext()), title).apply {
            isChecked = value
            setOnPreferenceChangeListener { _, newValue -> set(newValue as Boolean); changed(rebuilds); true }
        }
    }

    private fun choice(title: String, options: List<Option>, value: String, rebuilds: Boolean = false, set: (String) -> Unit) {
        add(ListPreference(requireContext()), title).apply {
            entries = options.map { it.title }.toTypedArray()
            entryValues = options.map { it.value }.toTypedArray()
            this.value = value
            dialogTitle = title
            summary = options.firstOrNull { it.value == value }?.title ?: value
            setOnPreferenceChangeListener { preference, newValue ->
                set(newValue as String)
                preference.summary = options.firstOrNull { it.value == newValue }?.title ?: newValue
                changed(rebuilds)
                true
            }
        }
    }

    private fun multiChoice(title: String, options: List<Option>, value: List<String>, set: (List<String>) -> Unit) {
        fun describe(selection: List<String>) = if (selection.isEmpty()) "None" else selection.joinToString(", ")
        add(MultiSelectListPreference(requireContext()), title).apply {
            entries = options.map { it.title }.toTypedArray()
            entryValues = options.map { it.value }.toTypedArray()
            values = value.toSet()
            dialogTitle = title
            summary = describe(value)
            setOnPreferenceChangeListener { preference, newValue ->
                // Keep the order of the options list rather than the set's order.
                val selection = (newValue as Set<*>).let { chosen -> options.map { it.value }.filter { it in chosen } }
                set(selection)
                preference.summary = describe(selection)
                changed(false)
                true
            }
        }
    }

    private fun info(title: String?, value: String) {
        add(Preference(requireContext()), title).apply {
            summary = value
            isSelectable = false
        }
    }

    private fun button(title: String, action: () -> Unit) {
        add(Preference(requireContext()), title).apply {
            setOnPreferenceClickListener { action(); true }
        }
    }
}

/** Shows the configuration map as pretty JSON (sorted keys), with a Copy button. */
internal object ConfigurationJsonDialog {

    fun show(context: Context, configuration: Map<String, Any>) {
        val json = prettyJson(configuration)
        val text = TextView(context).apply {
            this.text = json
            typeface = Typeface.MONOSPACE
            textSize = 12f
            setTextIsSelectable(true)
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }
        AlertDialog.Builder(context)
            .setTitle("Configuration JSON")
            .setView(ScrollView(context).apply { addView(text) })
            .setNegativeButton("Close", null)
            .setPositiveButton("Copy") { _, _ ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Configuration JSON", json))
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    fun prettyJson(configuration: Map<String, Any>): String =
        (toJson(configuration) as JSONObject).toString(2).replace("\\/", "/")

    private fun toJson(value: Any?): Any? = when (value) {
        is Map<*, *> -> JSONObject().apply { value.entries.sortedBy { it.key.toString() }.forEach { put(it.key.toString(), toJson(it.value)) } }
        is List<*> -> JSONArray().apply { value.forEach { put(toJson(it)) } }
        else -> value
    }
}

/** targetSdk 35+ draws edge-to-edge, so keep the content below the action bar and above the nav bar. */
internal fun AppCompatActivity.applyInsets(content: android.view.View) {
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
        val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        view.post {
            val actionBarBottom = window.decorView
                .findViewById<android.view.View>(androidx.appcompat.R.id.action_bar_container)?.bottom ?: 0
            view.setPadding(0, actionBarBottom, 0, navBar.bottom)
        }
        insets
    }
}
