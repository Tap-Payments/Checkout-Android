package com.tap.company.checkout_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import company.tap.tapcheckout_android.CheckoutConfiguration
import company.tap.tapcheckout_android.TapCheckout
import company.tap.tapcheckout_android.TapCheckoutStatusDelegate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Checkout-Android integration: build the configuration from the saved settings, start the checkout
 * and log every callback. The Settings screen edits the same inputs as the Tap web demo.
 */
class MainActivity : AppCompatActivity(), TapCheckoutStatusDelegate {

    private val callbackMessages = mutableListOf<String>()

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

        findViewById<Button>(R.id.start_checkout).setOnClickListener { startCheckout() }

        // targetSdk 35+ draws edge-to-edge, so keep the content below the action bar and above the nav bar
        val content = findViewById<View>(R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.post {
                val actionBarBottom = window.decorView
                    .findViewById<View>(androidx.appcompat.R.id.action_bar_container)?.bottom ?: 0
                view.setPadding(view.paddingLeft, actionBarBottom + view.paddingLeft, view.paddingRight, navBar.bottom + view.paddingLeft)
            }
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        settings = CheckoutSettings.load(this)
        val mode = if (settings.mode == "authorize") "Authorize (${settings.authorizeType})" else "Charge"
        findViewById<TextView>(R.id.summary).text = "$mode · ${settings.totalAmount.formatted} ${settings.selectedCurrency}"

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
        startActivity(Intent(this, SettingsActivity::class.java))
        return true
    }

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

        // Load the checkout invisibly; it's shown on onReady so a page that never loads can't cover this screen
        checkoutView.visibility = View.INVISIBLE
        CheckoutConfiguration.configureWithTapCheckoutDictionary(this, publicKey, checkoutView, configuration, this)
    }

    private fun addCallbackMessage(message: String) = runOnUiThread {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        callbackMessages.add(0, "[$timestamp] $message")
        Log.d("TapCheckoutCallback", message)
        findViewById<TextView>(R.id.callbacks).text = callbackMessages.joinToString("\n\n")
        findViewById<ScrollView>(R.id.callbacks_scroll).smoothScrollTo(0, 0)
    }

    private fun hideCheckout() = runOnUiThread { checkoutView.visibility = View.GONE }

    // TapCheckoutStatusDelegate

    override fun onCheckoutReady() {
        addCallbackMessage("onReady: Checkout is ready")
        runOnUiThread { checkoutView.visibility = View.VISIBLE }
    }

    override fun onCheckoutChargeCreated(data: String) {
        addCallbackMessage("onChargeCreated: $data")
    }

    override fun onCheckoutSuccess(data: String) {
        addCallbackMessage("onSuccess: $data")
        hideCheckout()
    }

    override fun onCheckoutError(error: String) {
        addCallbackMessage("onError: $error")
        hideCheckout()
    }

    override fun onCheckoutcancel() {
        addCallbackMessage("onClose: Checkout was closed")
        hideCheckout()
    }

    private companion object {
        const val MENU_SETTINGS = 1
    }
}

/** 0.1 -> "0.1", 10.0 -> "10" (no trailing ".0"). */
internal val Double.formatted: String
    get() = toString().removeSuffix(".0")
