package com.tap.company.checkout_android

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chillibits.simplesettings.tool.getPrefStringValue
import com.chillibits.simplesettings.tool.getPrefs
import com.tap.company.tapcheckout_android.CheckoutConfiguration
import com.tap.company.tapcheckout_android.TapCheckoutStatusDelegate
import com.tap.company.tapcheckout_android.TapCheckoutView
import okhttp3.Call
import android.text.method.ScrollingMovementMethod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() ,TapCheckoutStatusDelegate{
    lateinit var payButton: TapCheckoutView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /***
         * If pre creating intent ****/
        //  callIntentAPI()

        /*** If PASSING INTENT OBJECT **** /
         *
         */
        configureSdk(null)

    }


    fun configureSdk(intentId: String?) {

        //  val publicKey = "pk_test_J2OSkKAFxu4jghc9zeRfQ0da"

        val publicKey = getPrefStringValue("publicKey", "pk_test_ohzQrUWRnTkCLD1cqMeudyjX")

        /**
         * intent
         */
        val intentObj = HashMap<String, Any>()
        if (intentId != null) {
            intentObj.put("intent", intentId)
        }

        /**
         * configureWithPayButtonDictionary and calling the PayButton SDK
         */
        val scope = intent.getStringExtra("scopeKey")
        val charge = intent.getStringExtra("scopeKey")
        val configuration = LinkedHashMap<String, Any>()
        val jsonObject = JSONObject()
        jsonObject.put("scope", scope)
        jsonObject.put("purpose", charge)
        jsonObject.put("statement_descriptor", "statement_descriptor")
        jsonObject.put("description", "sd")
        jsonObject.put("reference", "uuid_testabcdfgkgdgd121992")
        jsonObject.put("customer_initiated", true)
        jsonObject.put("hash_string", "")
        jsonObject.put("idempotent", "")


        val merchant = JSONObject()
        val terminal = JSONObject()
        terminal.put("id", "")
        val terminaldevice = JSONObject()
        terminaldevice.put("id", "")
        terminal.put("terminal_device", terminaldevice)

        val medata = JSONObject()
        medata.put("uud", "qq")
        medata.put("uud2", "222")

        val operator = JSONObject()
        operator.put("id", "")
        val device = JSONObject()
        device.put("id", "")
        operator.put("device", device)
        merchant.put("id", intent.getStringExtra("merchantId"))
        merchant.put("terminal", terminal)
        merchant.put("operator", operator)
        val paymentprovider = JSONObject()
        val technology = JSONObject()
        technology.put("id", "")

        val institution = JSONObject()
        institution.put("id", "")

        paymentprovider.put("technology", technology)
        paymentprovider.put("institution", institution)

        val ddevelopmenthouse = JSONObject()
        ddevelopmenthouse.put("id", "")

        val platform = JSONObject()
        platform.put("id", "")

        merchant.put("payment_provider", paymentprovider)
        merchant.put("institution", institution)
        merchant.put("development_house", ddevelopmenthouse)
        merchant.put("platform", platform)

        jsonObject.put("merchant", merchant)

        val authenticate = JSONObject()
        authenticate.put("id", "")
        authenticate.put("required", true)

        jsonObject.put("authenticate", authenticate)
        val transaction = JSONObject()
        val cardholderlogin = JSONObject()
        cardholderlogin.put("type", "GUEST")
        cardholderlogin.put("timestamp", "123213213")

        val payment_agreement = JSONObject()

        // val contract = JSONObject()
        // contract.put("id","")
        // payment_agreement.put("id","")
        // payment_agreement.put("contract",contract)

        // transaction.put("card_holder_login",cardholderlogin)
        // transaction.put("metadata",medata)
        // transaction.put("reference","TEST")
        //  transaction.put("payment_agreement",payment_agreement)

        //  jsonObject.put("transaction", transaction)

        val invoice = JSONObject()
        invoice.put("id", "")
        jsonObject.put("invoice", invoice)


        val descriptiom = JSONObject()
        descriptiom.put("text", "name ")
        descriptiom.put("lang", "en ")

        val reference = JSONObject()
        reference.put("sku", "stock keeping unit ")
        reference.put("gtin", "global trade item number ")
        reference.put("code", "00dfd ")
        reference.put("financial_code", "0022343")


        val descArray = JSONArray()
        descArray.put(descriptiom)

        val product = JSONObject()
        product.put("id", "")
        product.put("amount", intent.getStringExtra("amountKey")?.toDouble())
        product.put("name", descArray)
        product.put("description", descArray)
        product.put("metadata", medata)
        product.put("category", "PHYSICAL_GOODS")
        product.put("reference", reference)
        val itemsList = JSONObject()
        itemsList.put("id", " ")
        itemsList.put("quantity", 1)
        itemsList.put("pickup", false)
        itemsList.put("product", product)

        val itemsArry = JSONArray()
        itemsArry.put(itemsList)
        val items = JSONObject()
        items.put("count", 1)
        items.put("list", itemsArry)


        val order = JSONObject()
        order.put("amount", (intent.getStringExtra("amountKey")?.toDouble()))
        // order.put("amount",3)
        order.put("currency", intent.getStringExtra("orderCurrencyKey"))
        order.put("description", descArray)
        order.put("reference", intent.getStringExtra("orderRefrenceKey"))
        order.put("items", items)

        val discount = JSONObject()
        discount.put("type", "F")
        discount.put("value", 1)

        val tax = JSONObject()
        tax.put("name", "VAT")
        tax.put("description", "test")
        tax.put("type", "F")
        tax.put("value", 1)

        val taxarry = JSONArray()
        taxarry.put(tax)


        val adddress = JSONObject()
        adddress.put("type", "home ")
        adddress.put("line1", "sdfghjk ")
        adddress.put("line2", "oiuytr ")
        adddress.put("line3", "line3 ")
        adddress.put("line4", "line4 ")
        adddress.put("apartment", " ")
        adddress.put("building", " ")
        adddress.put("street", " ")
        adddress.put("avenue", " ")
        adddress.put("block", " ")
        adddress.put("area", " ")
        adddress.put("city", "salmyia")
        adddress.put("state", "kuwait")
        adddress.put("country", "KW")
        adddress.put("zip_code", "30003")
        adddress.put("postal_code", " ")

        val provider = JSONObject()
        provider.put("id", "")


        val shippigobject = JSONObject()
        shippigobject.put("amount", 1)
        shippigobject.put("description", descArray)
        shippigobject.put("recipient_name", descArray)
        shippigobject.put("address", adddress)
        shippigobject.put("provider", provider)
        shippigobject.put("metadata", medata)

        // order.put("tax",taxarry)
        // order.put("discount",discount)
        //  order.put("shipping",shippigobject)
        order.put("metadata", medata)


        jsonObject.put("order", order)

        val name = JSONObject()
        name.put("first", "OSAMA ")
        name.put("last", "Ahmed ")
        name.put("middle", " ")
        name.put("title", "MR ")

        val nameList = JSONArray()
        nameList.put(name)

        val nameCard = JSONObject()
        nameCard.put("content", "OSAMA AHMED ")
        nameCard.put("editable", true)

        val phone = JSONObject()
        phone.put("country_code", "965")
        phone.put("number", "55683784")

        val contact = JSONObject()
        contact.put("email", "tap.test@company")
        contact.put("phone", phone)

        val customer = JSONObject()
        customer.put("id", intent.getStringExtra("customerIdKey"))
        customer.put("name", nameList)
        customer.put("name_on_card", nameCard)
        customer.put("contact", contact)




        customer.put("address", adddress)

        jsonObject.put("customer", customer)
        val receipt = JSONObject()
        receipt.put("email", false)
        receipt.put("sms", false)

        jsonObject.put("receipt", receipt)


        val configOb = JSONObject()
        configOb.put("initiator", "MERCHANT")
        configOb.put("type", "BUTTON")
        val features = JSONObject()
        features.put("acceptance_badge", true)
        features.put("order", true)
        features.put("multiple_currencies", true)

        val currency_conversions = JSONObject()
        currency_conversions.put("dynamic", true)
        currency_conversions.put("location", true)
        currency_conversions.put("payment", true)
        currency_conversions.put("cobadge", true)

        val alternative_card_inputs = JSONObject()
        alternative_card_inputs.put("card_scanner", true)
        alternative_card_inputs.put("card_nfc ", true)

        val customer_cards = JSONObject()
        customer_cards.put("save_card", true)
        customer_cards.put("auto_save_card", true)
        customer_cards.put("display_saved_cards", true)


        val payments = JSONObject()
        payments.put("card", true)
        payments.put("device", true)
        payments.put("wallet", true)
        payments.put("bnpl", true)
        payments.put("mobile", true)
        payments.put("cash", true)
        payments.put("redirect", true)

        features.put("currency_conversions", currency_conversions)
        features.put("payments", payments)
        features.put("alternative_card_inputs", alternative_card_inputs)
        features.put("customer_cards", customer_cards)

        val acceptance = JSONObject()
        val supported_regions = JSONArray()
        supported_regions.put("LOCAL")
        supported_regions.put("REGIONAL")
        supported_regions.put("GLOBAL")

        val supported_countries = JSONArray()
        supported_countries.put("AE")
        supported_countries.put("SA")
        supported_countries.put("KW")
        supported_countries.put("EG")

        val supported_currencies = JSONArray()
        supported_currencies.put("KWD")
        supported_currencies.put("SAR")
        supported_currencies.put("AED")
        supported_currencies.put("OMR")
        supported_currencies.put("QAR")
        supported_currencies.put("BHD")
        supported_currencies.put("EGP")
        supported_currencies.put("GBP")
        supported_currencies.put("USD")
        supported_currencies.put("EUR")
        supported_currencies.put("AED")


        val supported_payment_methods = JSONArray()
        supported_payment_methods.put("BENEFITPAY")

        /*val supported_payment_types = JSONArray()
        // supported_payment_methods.put ("CARD")
        supported_payment_types.put("DEVICE")
        supported_payment_types.put("WEB")*/

        val supported_schemes = JSONArray()
        // supported_schemes.put("CARD")
        supported_schemes.put("MADA")
        supported_schemes.put("OMANNET")
        supported_schemes.put("VISA")
        supported_schemes.put("MASTERCARD")
        supported_schemes.put("AMEX")
        supported_schemes.put("BENEFIT_CARD")
        val defaultHash = hashSetOf("VISA", "AMEX", "MASTERCARD", "BENEFIT_CARD", "OMANNET", "MADA")
        val defaultFundHash = hashSetOf("CREDIT", "DEBIT")

        println("defaultHash" + defaultHash)

        val supported_fund_source = JSONArray()
        // supported_fund_source.put("CARD")
        supported_fund_source.put("DEBIT")
        supported_fund_source.put("CREDIT")
        val supported_payment_flows = JSONArray()
        //supported_payment_flows.put("CARD")
        supported_payment_flows.put("POPUP")
        supported_payment_flows.put("PAGE")

        val supported_payment_authentications = JSONArray()
        supported_payment_authentications.put("3DS")
        supported_payment_authentications.put("EMV")
        supported_payment_authentications.put("PASSKEY")

        // acceptance.put("supported_regions",supported_regions)
        // acceptance.put("supported_countries",supported_countries)
        // acceptance.put("supported_payment_types",supported_payment_types)
        // acceptance.put("supported_currencies",supported_currencies)

        val paymethods = getPrefStringValue("buttonKey", "KNET")

        val arry = JSONArray()
        arry.put(paymethods)

        acceptance.put("supported_payment_methods", arry)


        acceptance.put(
            "supported_schemes",
            JSONArray(getPrefs().getStringSet("supportedSchemesKey", defaultHash.toHashSet()))
        )
        acceptance.put(
            "supported_fund_source",
            JSONArray(
                getPrefs().getStringSet(
                    "supportedFundSourceKey",
                    defaultFundHash.toHashSet()
                )
            )
        )
        acceptance.put("supported_payment_authentications", supported_payment_authentications)
        acceptance.put("supported_payment_flows", supported_payment_flows)

        val interfacee = JSONObject()
        interfacee.put("locale", intent.getStringExtra("selectedlangKey") ?: "EN")
        interfacee.put("theme", intent.getStringExtra("selectedthemeKey") ?: "LIGHT")
        interfacee.put("edges", intent.getStringExtra("selectedcardedgeKey") ?: "CURVED")
        interfacee.put("color_style", intent.getStringExtra("selectedcolorstyleKey") ?: "COLORED")
        interfacee.put("user_experience", "POPUP")
        interfacee.put("card_direction", "DYNAMIC")
        interfacee.put("loader", true)
        interfacee.put("powered", true)


        val fieldvisibility = JSONObject()

        val card = JSONObject()
        card.put("number", true)
        card.put("expiry", true)
        card.put("cvv", true)
        card.put("cardholder", true)

        val conatct = JSONObject()
        conatct.put("email", true)
        conatct.put("number", true)

        val shipping = JSONObject()
        shipping.put("address", true)

        fieldvisibility.put("name", true)
        fieldvisibility.put("card", card)
        fieldvisibility.put("contact", conatct)
        //  fieldvisibility.put("shipping",shipping)

        configOb.put("features", features)
        configOb.put("acceptance", acceptance)
        configOb.put("field_visibility", fieldvisibility)
        configOb.put("interface", interfacee)


        val checkout = JSONObject()
        checkout.put("auto", true)
        checkout.put("metadata", medata)

        val post = JSONObject()
        post.put("url", "osama.cm")

        val redirect = JSONObject()
        redirect.put("url", "osama.cm")

        val domain = JSONObject()
        domain.put("url", "demo.tap.PayButtonSDK")

        jsonObject.put("config", configOb)
        jsonObject.put("domain", domain)
        jsonObject.put("redirect", redirect)
        jsonObject.put("post", post)
        jsonObject.put("checkout", checkout)

        val intentObjc: HashMap<String, Any> = Gson().fromJson(
            jsonObject.toString(), object : TypeToken<HashMap<String?, Any?>?>() {}.type
        )

        if (intentId == null) {
            CheckoutConfiguration.configureWithPayButtonDictionary(
                this,
                publicKey,
                null,
                findViewById(R.id.redirect_pay),
                intentObjc,
                this
            )
        } else {
            CheckoutConfiguration.configureWithPayButtonDictionary(
                this,
                publicKey,
                intentId,
                findViewById(R.id.redirect_pay),
                null,
                this
            )
        }


    }

    override fun onCheckoutReady() {
        findViewById<TextView>(R.id.text).text = ""
        findViewById<TextView>(R.id.text).text = "onReady"
        Toast.makeText(this, "onReady", Toast.LENGTH_SHORT).show()
    }

    override fun onCheckoutSuccess(data: String) {
        Log.i("onSuccess", data)
        findViewById<TextView>(R.id.text).text = ""
        findViewById<TextView>(R.id.text).text = "onSuccess>>>> $data"
        findViewById<TextView>(R.id.text).movementMethod = ScrollingMovementMethod()

        Toast.makeText(this, "onSuccess $data", Toast.LENGTH_SHORT).show()

    }

    override fun onCheckoutClick() {
        Toast.makeText(this, "onClick", Toast.LENGTH_SHORT).show()
        findViewById<TextView>(R.id.text).text = ""
        findViewById<TextView>(R.id.text).text = "onClick "

    }


    override fun onCheckoutChargeCreated(data: String) {
        Log.e("data", data.toString())
        findViewById<TextView>(R.id.text).text = ""
        findViewById<TextView>(R.id.text).text = "onChargeCreated $data"
        Toast.makeText(this, "onChargeCreated $data", Toast.LENGTH_SHORT).show()

    }

   /* override fun onCheckoutOrderCreated(data: String) {
        findViewById<TextView>(R.id.text).text = ""
        findViewById<TextView>(R.id.text).text = "onOrderCreated >> $data"
        Log.e("mainactv", "onPayButtonOrderCreated: " + data)
        Toast.makeText(this, "onOrderCreated $data", Toast.LENGTH_SHORT).show()
    }*/

    override fun onCheckoutcancel() {
        Toast.makeText(this, "Cancel ", Toast.LENGTH_SHORT).show()
    }

    override fun onCheckoutError(error: String) {
        Log.e("error", error.toString())
        findViewById<TextView>(R.id.text).text = ""
        findViewById<TextView>(R.id.text).text = "onError $error"
        Toast.makeText(this, "onError received $error ", Toast.LENGTH_SHORT).show()

    }

    private fun callIntentAPI() {
        val builder: OkHttpClient.Builder = OkHttpClient().newBuilder()
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        builder.addInterceptor(interceptor)

        val okHttpClient: OkHttpClient = builder.build()
        val mediaType = "application/json".toMediaType()
        val scope = intent.getStringExtra("scopeKey")
        val charge = intent.getStringExtra("scopeKey")


        val jsonObject = JSONObject()
        jsonObject.put("scope", scope)
        jsonObject.put("purpose", charge)
        jsonObject.put("statement_descriptor", "statement_descriptor")
        jsonObject.put("description", "sd")
        jsonObject.put("reference", "uuid_testabcdfgkgdgd121992")
        jsonObject.put("customer_initiated", true)
        jsonObject.put("hash_string", "")
        jsonObject.put("idempotent", "")


        val merchant = JSONObject()
        val terminal = JSONObject()
        terminal.put("id", "")
        val terminaldevice = JSONObject()
        terminaldevice.put("id", "")
        terminal.put("terminal_device", terminaldevice)

        val medata = JSONObject()
        medata.put("uud", "qq")
        medata.put("uud2", "222")

        val operator = JSONObject()
        operator.put("id", "")
        val device = JSONObject()
        device.put("id", "")
        operator.put("device", device)
        merchant.put("id", intent.getStringExtra("merchantId"))
        merchant.put("terminal", terminal)
        merchant.put("operator", operator)
        val paymentprovider = JSONObject()
        val technology = JSONObject()
        technology.put("id", "")

        val institution = JSONObject()
        institution.put("id", "")

        paymentprovider.put("technology", technology)
        paymentprovider.put("institution", institution)

        val ddevelopmenthouse = JSONObject()
        ddevelopmenthouse.put("id", "")

        val platform = JSONObject()
        platform.put("id", "")

        merchant.put("payment_provider", paymentprovider)
        merchant.put("institution", institution)
        merchant.put("development_house", ddevelopmenthouse)
        merchant.put("platform", platform)

        jsonObject.put("merchant", merchant)

        val authenticate = JSONObject()
        authenticate.put("id", "")
        authenticate.put("required", true)

        jsonObject.put("authenticate", authenticate)
        val transaction = JSONObject()
        val cardholderlogin = JSONObject()
        cardholderlogin.put("type", "GUEST")
        cardholderlogin.put("timestamp", "123213213")

        val payment_agreement = JSONObject()

        val contract = JSONObject()
        contract.put("id", "")
        payment_agreement.put("id", "")
        payment_agreement.put("contract", contract)

        transaction.put("card_holder_login", cardholderlogin)
        transaction.put("metadata", medata)
        transaction.put("reference", "TEST")
        transaction.put("payment_agreement", payment_agreement)

        jsonObject.put("transaction", transaction)

        val invoice = JSONObject()
        invoice.put("id", "")
        jsonObject.put("invoice", invoice)


        val descriptiom = JSONObject()
        descriptiom.put("text", "name ")
        descriptiom.put("lang", "en ")

        val reference = JSONObject()
        reference.put("sku", "stock keeping unit ")
        reference.put("gtin", "global trade item number ")
        reference.put("code", "00dfd ")
        reference.put("financial_code", "0022343 ")


        val descArray = JSONArray()
        descArray.put(descriptiom)

        val product = JSONObject()
        product.put("id", "")
        product.put("amount", intent.getStringExtra("amountKey")?.toDouble())
        product.put("name", descArray)
        product.put("description", descArray)
        product.put("metadata", medata)
        product.put("category", "PHYSICAL_GOODS")
        product.put("reference", reference)
        val itemsList = JSONObject()
        itemsList.put("id", " ")
        itemsList.put("quantity", 1)
        itemsList.put("pickup", false)
        itemsList.put("product", product)

        val itemsArry = JSONArray()
        itemsArry.put(itemsList)
        val items = JSONObject()
        items.put("count", 1)
        items.put("list", itemsArry)


        val order = JSONObject()
        order.put("amount", (intent.getStringExtra("amountKey")?.toDouble()))
        // order.put("amount",3)
        order.put("currency", intent.getStringExtra("orderCurrencyKey"))
        order.put("description", descArray)
        order.put("reference", intent.getStringExtra("orderRefrenceKey"))
        order.put("items", items)

        val discount = JSONObject()
        discount.put("type", "F")
        discount.put("value", 1)

        val tax = JSONObject()
        tax.put("name", "VAT")
        tax.put("description", "test")
        tax.put("type", "F")
        tax.put("value", 1)

        val taxarry = JSONArray()
        taxarry.put(tax)


        val adddress = JSONObject()
        adddress.put("type", "home ")
        adddress.put("line1", "sdfghjk ")
        adddress.put("line2", "oiuytr ")
        adddress.put("line3", "line3 ")
        adddress.put("line4", "line4 ")
        adddress.put("apartment", " ")
        adddress.put("building", " ")
        adddress.put("street", " ")
        adddress.put("avenue", " ")
        adddress.put("block", " ")
        adddress.put("area", " ")
        adddress.put("city", "salmyia")
        adddress.put("state", "kuwait")
        adddress.put("country", "KW")
        adddress.put("zip_code", "30003")
        adddress.put("postal_code", " ")

        val provider = JSONObject()
        provider.put("id", "")


        val shippigobject = JSONObject()
        shippigobject.put("amount", 1)
        shippigobject.put("description", descArray)
        shippigobject.put("recipient_name", descArray)
        shippigobject.put("address", adddress)
        shippigobject.put("provider", provider)
        shippigobject.put("metadata", medata)

        // order.put("tax",taxarry)
        // order.put("discount",discount)
        //  order.put("shipping",shippigobject)
        order.put("metadata", medata)


        jsonObject.put("order", order)

        val name = JSONObject()
        name.put("first", "OSAMA ")
        name.put("last", "Ahmed ")
        name.put("middle", " ")
        name.put("title", "MR ")

        val nameList = JSONArray()
        nameList.put(name)

        val nameCard = JSONObject()
        nameCard.put("content", "OSAMA AHMED ")
        nameCard.put("editable", true)

        val phone = JSONObject()
        phone.put("country_code", "965")
        phone.put("number", "55683784")

        val contact = JSONObject()
        contact.put("email", "tap.test@company")
        contact.put("phone", phone)

        val customer = JSONObject()
        customer.put("id", intent.getStringExtra("customerIdKey"))
        customer.put("name", nameList)
        customer.put("name_on_card", nameCard)
        customer.put("contact", contact)




        customer.put("address", adddress)

        jsonObject.put("customer", customer)
        val receipt = JSONObject()
        receipt.put("email", false)
        receipt.put("sms", false)

        jsonObject.put("receipt", receipt)


        val configOb = JSONObject()
        configOb.put("initiator", "MERCHANT")
        configOb.put("type", "BUTTON")
        val features = JSONObject()
        features.put("acceptance_badge", true)
        features.put("order", true)
        features.put("multiple_currencies", true)

        val currency_conversions = JSONObject()
        currency_conversions.put("dynamic", true)
        currency_conversions.put("location", true)
        currency_conversions.put("payment", true)
        currency_conversions.put("cobadge", true)

        val alternative_card_inputs = JSONObject()
        alternative_card_inputs.put("card_scanner", true)
        alternative_card_inputs.put("card_nfc ", true)

        val customer_cards = JSONObject()
        customer_cards.put("save_card", true)
        customer_cards.put("auto_save_card", true)
        customer_cards.put("display_saved_cards", true)


        val payments = JSONObject()
        payments.put("card", true)
        payments.put("device", true)
        payments.put("wallet", true)
        payments.put("bnpl", true)
        payments.put("mobile", true)
        payments.put("cash", true)
        payments.put("redirect", true)

        features.put("currency_conversions", currency_conversions)
        features.put("payments", payments)
        features.put("alternative_card_inputs", alternative_card_inputs)
        features.put("customer_cards", customer_cards)

        val acceptance = JSONObject()
        val supported_regions = JSONArray()
        supported_regions.put("LOCAL")
        supported_regions.put("REGIONAL")
        supported_regions.put("GLOBAL")

        val supported_countries = JSONArray()
        supported_countries.put("AE")
        supported_countries.put("SA")
        supported_countries.put("KW")
        supported_countries.put("EG")

        val supported_currencies = JSONArray()
        supported_currencies.put("KWD")
        supported_currencies.put("SAR")
        supported_currencies.put("AED")
        supported_currencies.put("OMR")
        supported_currencies.put("QAR")
        supported_currencies.put("BHD")
        supported_currencies.put("EGP")
        supported_currencies.put("GBP")
        supported_currencies.put("USD")
        supported_currencies.put("EUR")
        supported_currencies.put("AED")


        val supported_payment_methods = JSONArray()
        supported_payment_methods.put("BENEFITPAY")

        /*val supported_payment_types = JSONArray()
        // supported_payment_methods.put ("CARD")
        supported_payment_types.put("DEVICE")
        supported_payment_types.put("WEB")*/

        val supported_schemes = JSONArray()
        // supported_schemes.put("CARD")
        supported_schemes.put("MADA")
        supported_schemes.put("OMANNET")
        supported_schemes.put("VISA")
        supported_schemes.put("MASTERCARD")
        supported_schemes.put("AMEX")
        supported_schemes.put("BENEFIT_CARD")
        val defaultHash = hashSetOf("VISA", "AMEX", "MASTERCARD", "BENEFIT_CARD", "OMANNET", "MADA")
        val defaultFundHash = hashSetOf("CREDIT", "DEBIT")

        println("defaultHash" + defaultHash)

        val supported_fund_source = JSONArray()
        // supported_fund_source.put("CARD")
        supported_fund_source.put("DEBIT")
        supported_fund_source.put("CREDIT")
        val supported_payment_flows = JSONArray()
        //supported_payment_flows.put("CARD")
        supported_payment_flows.put("POPUP")
        supported_payment_flows.put("PAGE")

        val supported_payment_authentications = JSONArray()
        supported_payment_authentications.put("3DS")
        supported_payment_authentications.put("EMV")
        supported_payment_authentications.put("PASSKEY")

        // acceptance.put("supported_regions",supported_regions)
        // acceptance.put("supported_countries",supported_countries)
        // acceptance.put("supported_payment_types",supported_payment_types)
        // acceptance.put("supported_currencies",supported_currencies)

        val paymethods = getPrefStringValue("buttonKey", "KNET")

        val arry = JSONArray()
        arry.put(paymethods)

        acceptance.put("supported_payment_methods", arry)


        acceptance.put(
            "supported_schemes",
            JSONArray(getPrefs().getStringSet("supportedSchemesKey", defaultHash.toHashSet()))
        )
        acceptance.put(
            "supported_fund_source",
            JSONArray(
                getPrefs().getStringSet(
                    "supportedFundSourceKey",
                    defaultFundHash.toHashSet()
                )
            )
        )
        acceptance.put("supported_payment_authentications", supported_payment_authentications)
        acceptance.put("supported_payment_flows", supported_payment_flows)

        val interfacee = JSONObject()
        interfacee.put("locale", intent.getStringExtra("selectedlangKey") ?: "EN")
        interfacee.put("theme", intent.getStringExtra("selectedthemeKey") ?: "LIGHT")
        interfacee.put("edges", intent.getStringExtra("selectedcardedgeKey") ?: "CURVED")
        interfacee.put("color_style", intent.getStringExtra("selectedcolorstyleKey") ?: "COLORED")
        interfacee.put("user_experience", "POPUP")
        interfacee.put("card_direction", "DYNAMIC")
        interfacee.put("loader", true)
        interfacee.put("powered", true)


        val fieldvisibility = JSONObject()

        val card = JSONObject()
        card.put("number", true)
        card.put("expiry", true)
        card.put("cvv", true)
        card.put("cardholder", true)

        val conatct = JSONObject()
        conatct.put("email", true)
        conatct.put("number", true)

        val shipping = JSONObject()
        shipping.put("address", true)

        fieldvisibility.put("name", true)
        fieldvisibility.put("card", card)
        fieldvisibility.put("contact", conatct)
        //  fieldvisibility.put("shipping",shipping)

        configOb.put("features", features)
        configOb.put("acceptance", acceptance)
        configOb.put("field_visibility", fieldvisibility)
        configOb.put("interface", interfacee)


        val checkout = JSONObject()
        checkout.put("auto", true)
        checkout.put("metadata", medata)

        val post = JSONObject()
        post.put("url", "osama.cm")

        val redirect = JSONObject()
        redirect.put("url", "osama.cm")

        val domain = JSONObject()
        domain.put("url", "demo.tap.PayButtonSDK")

        jsonObject.put("config", configOb)
        jsonObject.put("domain", domain)
        jsonObject.put("redirect", redirect)
        jsonObject.put("post", post)
        jsonObject.put("checkout", checkout)

        val body = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request: Request = Request.Builder()
            // .url("https://api.tap.company/v2/intent")
            .url(" https://mw-sdk.dev.tap.company/v2/intent")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            //  .addHeader("Authorization", "Bearer sk_test_NSln5js3fIeq0QU1MuKRXAkD")
            .addHeader(
                "Authorization",
                getPrefStringValue("publicKey", "pk_test_ohzQrUWRnTkCLD1cqMeudyjX")
            )
            .addHeader(
                "mdn",
                "alcREQ7HvHCihdZ889eq/I2EbXyOecV5KN2IvpjMxP8U2yx/50UDy0R86CixOsC1TzsfW40AhiuT6G3aRui4OocT3EcKBpSjeXgDH6aJKhfTO33zbrK8ZA3eLZxKRwmvH9bugKRodb6lfG1PPN5dDZWiDqA6Je/suSr9hVUzrzg="
            )
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    var responseBody: JSONObject? =
                        response.body?.string()
                            ?.let { JSONObject(it) } // toString() is not the response body, it is a debug representation of the response body
                    println("responseBody>>" + responseBody)
                    if (!responseBody.toString().contains("errors")) {
                        /*
                       *Pass to the sdk
                       ***/
                        var intentID: String? = null

                        intentID = responseBody?.getString("id")

                        configureSdk(intentID)


                    } else {


                        val errorObject = responseBody?.getJSONArray("errors")

                        for (j in 0 until errorObject?.length()!!) {
                            val mediaEntryObj = errorObject.getJSONObject(j)
                            val description = mediaEntryObj.getString("description")
                            Handler(Looper.getMainLooper()).post {
                                // write your code here
                                Toast.makeText(this@MainActivity, description, Toast.LENGTH_SHORT)
                                    .show()
                                finish()
                            }


                        }
                    }

                } catch (ex: JSONException) {
                    throw RuntimeException(ex)
                }
            }

        })
    }


}