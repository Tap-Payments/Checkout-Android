package company.tap.tapcheckout_android

import android.content.Context
import android.content.SharedPreferences
import company.tap.tapnetworkkit_android.BuildConfig


object Pref {
    private val PREF_FILE: String = BuildConfig.LIBRARY_PACKAGE_NAME.replace(".", "_")
    private var sharedPreferences: SharedPreferences? = null
    private fun openPref(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    }

    //For string value
    fun getValue(context: Context, key: String?, defaultValue: String?): String {
        openPref(context)
        val result: String = sharedPreferences?.getString(key, defaultValue) ?:""
        sharedPreferences = null
        return result
    }

    fun setValue(context: Context, key: String?, value: String?) {
        openPref(context)
        val prefsPrivateEditor: SharedPreferences.Editor? = sharedPreferences?.edit()
        prefsPrivateEditor?.putString(key, value)
        prefsPrivateEditor?.apply()
        sharedPreferences = null
    } //You can create method like above for boolean, float, int etc...
}