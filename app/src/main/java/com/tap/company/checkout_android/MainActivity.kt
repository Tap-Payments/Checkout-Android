package com.tap.company.checkout_android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import company.tap.tapcheckout_android.CheckoutConfiguration
import company.tap.tapcheckout_android.TapCheckout
import company.tap.tapcheckout_android.TapCheckoutStatusDelegate
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Checkout-Android integration: build the configuration from the saved settings, start the checkout
 * and log every callback. The Settings screen edits the same inputs as the Tap web demo.
 *
 * The screen shows what the next checkout will do (mode, amount, key), the main settings, and every
 * callback as a card that expands to its full payload.
 */
class MainActivity : AppCompatActivity(), TapCheckoutStatusDelegate {

    private data class Callback(val name: String, val time: String, val payload: String?)

    private val callbacks = mutableListOf<Callback>()

    /** Edited in the Settings screen and persisted in SharedPreferences; reloaded whenever this screen appears. */
    private lateinit var settings: CheckoutSettings

    private lateinit var testHooks: UITestHooks
    private var didAutoStart = false

    private val checkoutView get() = findViewById<TapCheckout>(R.id.checkout_pay)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Tap Checkout"

        testHooks = UITestHooks(this, intent)
        if (savedInstanceState == null) testHooks.prepare(this)
        // Keep the callbacks when the screen is recreated (rotation, dark mode switch).
        savedInstanceState?.getStringArrayList(STATE_CALLBACKS)?.chunked(3)?.forEach { (name, time, payload) ->
            callbacks.add(Callback(name, time, payload.ifEmpty { null }))
        }

        findViewById<Button>(R.id.start_checkout).setOnClickListener { startCheckout() }
        findViewById<View>(R.id.edit_settings).setOnClickListener { openSettings() }
        findViewById<View>(R.id.view_json).setOnClickListener {
            ConfigurationJsonDialog.show(this, testHooks.apply(settings.makeConfiguration()))
        }
        findViewById<View>(R.id.clear_callbacks).setOnClickListener {
            callbacks.clear()
            renderCallbacks()
        }

        // UI tests read the plain-text log, so show it near the top instead of the cards.
        if (testHooks.isActive) {
            listOf(R.id.hero, R.id.config_card, R.id.callbacks_empty).forEach { findViewById<View>(it).visibility = View.GONE }
            findViewById<View>(R.id.callbacks).visibility = View.VISIBLE
        }

        // targetSdk 35+ draws edge-to-edge, so keep the content below the action bar and above the nav bar
        val content = findViewById<View>(R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.post {
                val actionBarBottom = window.decorView
                    .findViewById<View>(androidx.appcompat.R.id.action_bar_container)?.bottom ?: 0
                view.setPadding(0, actionBarBottom, 0, navBar.bottom)
            }
            insets
        }
        renderCallbacks()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(STATE_CALLBACKS, ArrayList(callbacks.flatMap { listOf(it.name, it.time, it.payload.orEmpty()) }))
    }

    override fun onResume() {
        super.onResume()
        settings = CheckoutSettings.load(this)
        renderSummary()
        renderConfiguration()

        if (testHooks.autoStart && !didAutoStart) {
            didAutoStart = true
            startCheckout()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, "Settings").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != MENU_SETTINGS) return super.onOptionsItemSelected(item)
        openSettings()
        return true
    }

    private fun openSettings() = startActivity(Intent(this, SettingsActivity::class.java))

    private fun startCheckout() {
        if (!settings.hasPublicKey && !testHooks.isActive) {
            AlertDialog.Builder(this)
                .setTitle("Public key missing")
                .setMessage("Set your Tap sandbox public key in Keys.kt (Keys.PUBLIC_KEY), or enter it in Settings > SDK Configuration > Public Key.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        // The SDK takes a plain map; makeConfiguration() builds it like the Tap web demo does.
        val configuration = HashMap(testHooks.apply(settings.makeConfiguration()))
        val publicKey = (configuration["gateway"] as? Map<*, *>)?.get("publicKey") as? String

        // The checkout view shows the SDK's loader right away; the button shows a spinner until the
        // checkout is ready, and a page that never loads is taken off the screen after a minute.
        setLoading(true)
        checkoutView.visibility = View.VISIBLE
        checkoutView.removeCallbacks(loadTimeout)
        checkoutView.postDelayed(loadTimeout, LOAD_TIMEOUT_MS)
        CheckoutConfiguration.configureWithTapCheckoutDictionary(this, publicKey, checkoutView, configuration, this)
    }

    private val loadTimeout: Runnable = Runnable {
        setLoading(false)
        checkoutView.visibility = View.GONE
        Toast.makeText(this, "The checkout did not load. Check your connection and try again.", Toast.LENGTH_LONG).show()
    }

    private fun setLoading(loading: Boolean): Unit = runOnUiThread {
        if (!loading) checkoutView.removeCallbacks(loadTimeout)
        findViewById<Button>(R.id.start_checkout).apply {
            isEnabled = !loading
            alpha = if (loading) 0.7f else 1f
            text = if (loading) "Opening checkout…" else "Start Checkout"
        }
        findViewById<View>(R.id.start_progress).visibility = if (loading) View.VISIBLE else View.GONE
    }

    // Summary and configuration

    private fun renderSummary() {
        val mode = if (settings.mode == "authorize") "AUTHORIZE · ${settings.authorizeType}" else "CHARGE"
        findViewById<TextView>(R.id.hero_mode).text = mode
        findViewById<TextView>(R.id.summary).text = "${settings.totalAmount.formatted} ${settings.selectedCurrency}"

        val itemCount = settings.items.size
        val customer = "${settings.firstName} ${settings.lastName}".trim()
        findViewById<TextView>(R.id.hero_detail).text =
            "$itemCount item${if (itemCount == 1) "" else "s"} · $customer"

        val key = settings.publicKey.trim()
        val (label, color) = when {
            !settings.hasPublicKey -> "No public key" to R.color.chip_missing
            key.startsWith("pk_live_") -> "Live key" to R.color.chip_live
            else -> "Sandbox key" to R.color.chip_sandbox
        }
        findViewById<TextView>(R.id.key_chip).apply {
            text = label
            backgroundTintList = ColorStateList.valueOf(color(color))
        }
    }

    private fun renderConfiguration() {
        fun list(values: List<String>, empty: String = "All") = if (values.isEmpty()) empty else values.joinToString(", ")
        val rows = listOf(
            "Payment type" to settings.paymentType,
            "Payment methods" to list(settings.supportedPaymentMethods),
            "Currencies" to list(settings.supportedCurrencies),
            "Language" to settings.language,
            "Theme" to settings.theme,
            "Checkout mode" to settings.checkoutMode,
            "3D Secure" to if (settings.threeDSecure) "On" else "Off",
            "Save card" to settings.saveCardOption,
            "Card funding" to settings.cardFundingSource,
            "Customer" to "${settings.email} · +${settings.countryCode} ${settings.phoneNumber}",
        )
        val container = findViewById<LinearLayout>(R.id.config_rows)
        container.removeAllViews()
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) container.addView(divider())
            val row = LayoutInflater.from(this).inflate(R.layout.item_config_row, container, false)
            row.findViewById<TextView>(R.id.config_label).text = label
            row.findViewById<TextView>(R.id.config_value).text = value
            container.addView(row)
        }
        container.addView(divider())
    }

    private fun divider() = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        setBackgroundColor(color(R.color.divider))
    }

    // Callbacks

    private fun addCallback(name: String, payload: String?) = runOnUiThread {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        callbacks.add(0, Callback(name, time, payload))
        Log.d("TapCheckoutCallback", logLine(name, payload))
        renderCallbacks()
        findViewById<View>(R.id.main_scroll).let { scroll ->
            scroll.post { scroll.scrollTo(0, findViewById<View>(R.id.callbacks_header).top) }
        }
    }

    private fun logLine(name: String, payload: String?) = when (name) {
        "onReady" -> "onReady: Checkout is ready"
        "onClose" -> "onClose: Checkout was closed"
        else -> "$name: $payload"
    }

    private fun renderCallbacks() {
        findViewById<TextView>(R.id.callbacks).text = callbacks.joinToString("\n\n") { "[${it.time}] ${logLine(it.name, it.payload)}" }
            .ifEmpty { "Callbacks will appear here..." }
        findViewById<TextView>(R.id.callbacks_header).text = if (callbacks.isEmpty()) "Callbacks" else "Callbacks (${callbacks.size})"
        findViewById<View>(R.id.clear_callbacks).visibility = if (callbacks.isEmpty()) View.GONE else View.VISIBLE
        if (!testHooks.isActive) {
            findViewById<View>(R.id.callbacks_empty).visibility = if (callbacks.isEmpty()) View.VISIBLE else View.GONE
        }

        val list = findViewById<LinearLayout>(R.id.callbacks_list)
        list.removeAllViews()
        if (testHooks.isActive) return
        for (callback in callbacks) {
            val card = LayoutInflater.from(this).inflate(R.layout.item_callback, list, false)
            card.findViewById<TextView>(R.id.callback_name).apply {
                text = callback.name
                backgroundTintList = ColorStateList.valueOf(color(eventColor(callback.name)))
            }
            card.findViewById<TextView>(R.id.callback_time).text = callback.time
            card.findViewById<TextView>(R.id.callback_summary).text = summary(callback)
            val payload = card.findViewById<TextView>(R.id.callback_payload)
            if (callback.payload != null) {
                payload.text = prettyJson(callback.payload)
                card.setOnClickListener {
                    payload.visibility = if (payload.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
                card.setOnLongClickListener {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(callback.name, callback.payload))
                    Toast.makeText(this, "Payload copied", Toast.LENGTH_SHORT).show()
                    true
                }
            }
            list.addView(card)
        }
    }

    @ColorRes
    private fun eventColor(name: String) = when (name) {
        "onReady" -> R.color.event_ready
        "onChargeCreated" -> R.color.event_charge
        "onSuccess" -> R.color.event_success
        "onError" -> R.color.event_error
        else -> R.color.event_close
    }

    /** One line that says what happened, read from the payload where there is one. */
    private fun summary(callback: Callback): String {
        val json = callback.payload?.let { runCatching { JSONObject(it) }.getOrNull() }
        return when (callback.name) {
            "onReady" -> "Checkout is ready"
            "onClose" -> "Checkout was closed"
            "onChargeCreated" -> json?.let {
                listOf(it.optString("id"), it.optString("status"), "${it.opt("amount") ?: ""} ${it.optString("currency")}".trim())
                    .filter(String::isNotBlank).joinToString(" · ")
            }
            "onSuccess" -> json?.optString("chargeId")?.takeIf { it.isNotBlank() }?.let { "Completed · $it" }
            "onError" -> json?.optString("message")?.takeIf { it.isNotBlank() }?.let { message ->
                json.optString("code").takeIf { it.isNotBlank() }?.let { "$message ($it)" } ?: message
            }
            else -> null
        } ?: callback.payload.orEmpty().take(120)
    }

    private fun prettyJson(payload: String): String = runCatching {
        val trimmed = payload.trim()
        (if (trimmed.startsWith("[")) JSONArray(trimmed).toString(2) else JSONObject(trimmed).toString(2)).replace("\\/", "/")
    }.getOrDefault(payload)

    private fun color(@ColorRes id: Int) = ContextCompat.getColor(this, id)

    private fun hideCheckout() = runOnUiThread {
        setLoading(false)
        checkoutView.visibility = View.GONE
    }

    // TapCheckoutStatusDelegate

    override fun onCheckoutReady() {
        addCallback("onReady", null)
        setLoading(false)
    }

    override fun onCheckoutChargeCreated(data: String) {
        addCallback("onChargeCreated", data)
    }

    override fun onCheckoutSuccess(data: String) {
        addCallback("onSuccess", data)
        hideCheckout()
    }

    override fun onCheckoutError(error: String) {
        addCallback("onError", error)
        hideCheckout()
    }

    override fun onCheckoutcancel() {
        addCallback("onClose", null)
        hideCheckout()
    }

    private companion object {
        const val MENU_SETTINGS = 1
        const val STATE_CALLBACKS = "callbacks"
        const val LOAD_TIMEOUT_MS = 60_000L
    }
}

/** 0.1 -> "0.1", 10.0 -> "10" (no trailing ".0"). */
internal val Double.formatted: String
    get() = toString().removeSuffix(".0")
