/**
 *  Created by AhlaamK on 10/26/23, 2:14 PM
 *  Copyright (c) 2023 .
 *  All rights reserved Tap Payments.
 **
 */

package com.tap.company.checkout_android

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import com.chillibits.simplesettings.core.SimpleSettings
import com.chillibits.simplesettings.core.SimpleSettingsConfig
import com.chillibits.simplesettings.tool.getPrefBooleanValue
import com.chillibits.simplesettings.tool.getPrefStringSetValue
import com.chillibits.simplesettings.tool.getPrefStringValue
import com.chillibits.simplesettings.tool.getPrefs
import com.tap.commondatamodels.TapTheme

class SettingsActivity : AppCompatActivity(),SimpleSettingsConfig.PreferenceCallback  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val configuration = SimpleSettingsConfig.Builder()
            .setActivityTitle("Configuration")
            .setPreferenceCallback(this)
            .displayHomeAsUpEnabled(false)
            .build()


        SimpleSettings(this, configuration).show(R.xml.preferences)

    }


    override fun onPreferenceClick(context: Context, key: String): Preference.OnPreferenceClickListener? {
        return when(key) {
            "dialog_preference" -> Preference.OnPreferenceClickListener {
                navigateToMainActivity()
                true
            }
            else -> super.onPreferenceClick(context, key)
        }
    }
    fun navigateToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        /**
         * operator
         */
       // intent.putExtra("publicKey", getPrefStringValue("publicKey","pk_test_YhUjg9PNT8oDlKJ1aE2fMRz7"))
        intent.putExtra("publicKey", getPrefStringValue("publicKey","pk_test_ohzQrUWRnTkCLD1cqMeudyjX"))
        intent.putExtra("hashStringKey", getPrefStringValue("hashStringKey","pk_test_YhUjg9PNT8oDlKJ1aE2fMRz7"))
        intent.putExtra("merchantId", getPrefStringValue("merchantId",""))

        /**
         * order
         */
        intent.putExtra("orderIdKey", getPrefStringValue("orderIdKey",""))
        intent.putExtra("orderDescKey", getPrefStringValue("orderDescKey","test"))
        intent.putExtra("amountKey", getPrefStringValue("amountKey","1"))
        intent.putExtra("orderCurrencyKey", getPrefStringValue("orderCurrencyKey","KWD"))
        intent.putExtra("orderRefrenceKey", getPrefStringValue("orderRefrenceKey","test"))
        intent.putExtra("selectedCurrencyKey", getPrefStringValue("selectedCurrencyKey","test"))
        intent.putExtra("customerIdKey", getPrefStringValue("customerIdKey",""))


        /**
         * interface
         */

        intent.putExtra("selectedcardedgeKey",if (getPrefStringValue("selectedcardedgeKey","") == "1")  "flat" else  getPrefStringValue("selectedcardedgeKey","flat"))
        intent.putExtra("selectedCardDirection", if (getPrefStringValue("selectedcardirectKey","") == "0") "ltr" else getPrefStringValue("selectedcardirectKey","dynamic"))
        intent.putExtra("selectedcolorstyleKey", getPrefStringValue("selectedcolorstyleKey","colored"))
        intent.putExtra("selectedthemeKey", if (getPrefStringValue("selectedthemeKey","") == "1") TapTheme.light.name else  getPrefStringValue("selectedthemeKey","light"))
        intent.putExtra("selectedlangKey", if (getPrefStringValue("selectedlangKey","") == "1") "en" else getPrefStringValue("selectedlangKey", default = "en"))
        intent.putExtra("loaderKey", getPrefBooleanValue("loaderKey",true))


        /**
         * posturl
         */


        intent.putExtra("posturlKey", getPrefStringValue("posturlKey",""))
        intent.putExtra("redirectUrlKey", getPrefStringValue("redirectUrlKey",""))


        /**
         * scope && transaction
         */

        intent.putExtra("scopeKey", getPrefStringValue("scopeKey","charge"))
        intent.putExtra("transactionRefrenceKey", getPrefStringValue("transactionRefrenceKey",""))
        intent.putExtra("transactionAuthroizeTypeKey", getPrefStringValue("transactionAuthroizeTypeKey",""))
        intent.putExtra("transactionAuthroizeTimeKey", getPrefStringValue("transactionAuthroizeTimeKey",""))
        intent.putExtra("buttonKey", getPrefStringValue("buttonKey","KNET"))

        intent.putExtra("transactionSourceId", getPrefStringValue("transactionSourceId",""))
        intent.putExtra("transactionAuthenticationId", getPrefStringValue("transactionAuthenticationId",""))

        val defaultHash = hashSetOf("VISA","AMEX","MASTERCARD","BENEFIT_CARD")


        /**
         * acceptance
         */
       // intent.putExtra("supportedFundSourceKey", getPrefStringSetAsArray(getPrefs(),"supportedFundSourceKey"))
        intent.putExtra("supportedFundSourceKey", getPrefStringValue("supportedFundSourceKey","all"))
        intent.putExtra("supportedPaymentAuthenticationsKey", getPrefStringSetValue("supportedPaymentAuthenticationsKey", emptySet()).toHashSet())
        //  intent.putExtra("supportedSchemesKey", getPrefStringSetValue("supportedSchemesKey", defaultHash).toHashSet())
      //  intent.putExtra("supportedSchemesKey", getPrefStringSetAsArray(getPrefs(),"supportedSchemesKey"))
        intent.putExtra("supportedPaymentMethodKey", getPrefStringSetValue("supportedPaymentMethodKey", emptySet()).toHashSet())
        /**
         * Fields Visibility
         ***/
        intent.putExtra("cardHolder",  getPrefBooleanValue("displayHoldernameKey",true))
        intent.putExtra("cvv",getPrefBooleanValue("displayCVVKey",true))

        /**
         * Features
         ***/
        intent.putExtra("selectedCardBrand", getPrefBooleanValue("displayPymtBrndKey",true))
        intent.putExtra("saveCard", getPrefBooleanValue("displaySaveCardKey",true))
        intent.putExtra("autoSaveCard", getPrefBooleanValue("displayAutosaveCardKey",true))

        finish()
        startActivity(intent)

    }
    /*fun getPrefStringSetAsArray(sharedPreferences: SharedPreferences, key: String): Array<String>? {

        val sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        var stringSet: Set<String>? = null
        if(key.contains("supportedSchemesKey")) {
            val defaultHash = hashSetOf("VISA", "AMEX", "MASTERCARD", "BENEFIT_CARD")
// Save a HashSet to SharedPreferences
            val hashSet = getPrefs().getStringSet("supportedSchemesKey", defaultHash)
            editor.putStringSet("myKey", hashSet)
            editor.apply()
            // Retrieve the HashSet (stored as a Set<String>) from SharedPreferences
            stringSet = hashSet
        }else if (
            key.contains("supportedFundSourceKey")

        ) {

            val defaultHash = hashSetOf("DEBIT", "CREDIT")
// Save a HashSet to SharedPreferences
            val hashSet = getPrefs().getStringSet("supportedSchemesKey", defaultHash)
            editor.putStringSet("myKey", hashSet)
            editor.apply()
            // Retrieve the HashSet (stored as a Set<String>) from SharedPreferences
            stringSet = hashSet
        }
        // Convert the Set<String> (HashSet) to Array<String>
        return stringSet?.toTypedArray()
    }*/
}