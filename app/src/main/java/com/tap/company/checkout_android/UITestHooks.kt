package com.tap.company.checkout_android

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import org.json.JSONArray
import org.json.JSONObject

/**
 * Lets the instrumented tests (ReadmeConfigurationTests) drive the app: start from default settings,
 * use a public key passed at launch, change any configuration key and start the checkout by itself.
 * Debuggable builds only, and only when a test puts these extras on the launch intent.
 * Same behaviour as UITestHooks.swift in the iOS example apps.
 */
class UITestHooks(context: Context, intent: Intent?) {

    private val extras = if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) intent?.extras else null

    /** True when the app was launched by a UI test. */
    val isActive: Boolean = extras?.getString(UITEST) == "1"

    /** Start the checkout as soon as the main screen appears. */
    val autoStart: Boolean = isActive && extras?.getString(UITEST_AUTOSTART) == "1"

    /** Called once at launch: UI tests always start from the web demo defaults. */
    fun prepare(context: Context) {
        if (isActive) CheckoutSettings.reset(context)
    }

    /**
     * Applies the test's changes on top of the configuration built from the settings.
     *
     * `UITEST_PUBLIC_KEY` replaces `gateway.publicKey`. `UITEST_CONFIG_OVERRIDES` is a JSON object
     * merged into the configuration (nested objects are merged key by key); a `null` value removes
     * that key, so a test can check what happens when it is not sent.
     */
    fun apply(configuration: Map<String, Any>): Map<String, Any> {
        if (!isActive) return configuration
        var result = configuration
        extras?.getString(UITEST_PUBLIC_KEY)?.takeIf { it.isNotEmpty() }?.let { key ->
            result = merge(result, mapOf("gateway" to mapOf("publicKey" to key)))
        }
        extras?.getString(UITEST_CONFIG_OVERRIDES)?.let { json ->
            runCatching { toMap(JSONObject(json)) }.getOrNull()?.let { result = merge(result, it) }
        }
        return result
    }

    companion object {
        const val UITEST = "UITEST"
        const val UITEST_AUTOSTART = "UITEST_AUTOSTART"
        const val UITEST_PUBLIC_KEY = "UITEST_PUBLIC_KEY"
        const val UITEST_CONFIG_OVERRIDES = "UITEST_CONFIG_OVERRIDES"

        /** Merges [overrides] into [base]: nested maps key by key, a null value removes the key. */
        fun merge(base: Map<String, Any>, overrides: Map<String, Any?>): Map<String, Any> {
            val result = LinkedHashMap(base)
            for ((key, value) in overrides) {
                val current = result[key]
                when {
                    value == null -> result.remove(key)
                    value is Map<*, *> && current is Map<*, *> ->
                        @Suppress("UNCHECKED_CAST")
                        result[key] = merge(current as Map<String, Any>, value as Map<String, Any?>)
                    else -> result[key] = value
                }
            }
            return result
        }

        /** JSON to maps and lists, with JSON null as Kotlin null. */
        private fun toMap(json: JSONObject): Map<String, Any?> =
            json.keys().asSequence().associateWith { toValue(json.get(it)) }

        private fun toValue(value: Any?): Any? = when (value) {
            JSONObject.NULL -> null
            is JSONObject -> toMap(value)
            is JSONArray -> (0 until value.length()).map { toValue(value.get(it)) }
            else -> value
        }
    }
}
