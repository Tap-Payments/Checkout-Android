# CheckOut-Andriod

We at [Tap Payments](https://www.tap.company/) strive to make your payments easier than ever. We as a PCI compliant company, provide you a from the self solution to process card payments in your Android app.

[![Platform](https://img.shields.io/badge/platform-Android-inactive.svg?style=flat)](https://tap-payments.github.io/goSellSDK-Android/)
[![Documentation](https://img.shields.io/badge/documentation-100%25-bright%20green.svg)](https://tap-payments.github.io/goSellSDK-Android/)
[![SDK Version](https://img.shields.io/badge/minSdkVersion-24-blue.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)
[![SDK Version](https://img.shields.io/badge/targetSdkVersion-33-informational.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)
[![SDK Version](https://img.shields.io/badge/latestVersion-0.0.52-informational.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)


# Requirements

 1. We support from Android minSdk 24

# Steps overview
```mermaid
sequenceDiagram

participant  A  as  App
participant  T  as  Tap
participant  C  as  Checkout Android

A->>T:  Regsiter app.
T-->>A: Public key.
A ->> C : Install SDK
A ->> C : Init TapCheckOutView
C -->> A : tapcheckoutsdk
A ->> C :   if (intentId == null) {
            CheckoutConfiguration.configureWithTapCheckoutDictionary(
                this,
                publicKey,
                findViewById(R.id.redirect_pay),
                configuration,
                this
            )
        } 
C -->> A: onReady()

C -->> A : onSuccess(data(
```

# Get your Tap keys
You can always use the example keys within our example app, but we do recommend you to head to our [onboarding](https://register.tap.company/sell)  page. You will need to register your `package name` to get your `Tap Key` that you will need to activate our `Card-Android`.

# Installation

We got you covered, `Checkout-Android` can be installed with all possible technologies.

## Gradle

in project module gradle 

```kotlin
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```

Then get latest dependency  in your app module gradle
```kotlin
dependencies {
  implementation : 'com.github.Tap-Payments:Checkout-Android:{latest-tag-release}'
}
```

# Simple Integration
You can initialize `Checkout-Android` in different ways

 1. XML.
 2. Code.
## XML

```kotlin
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:tools="http://schemas.android.com/tools"
android:layout_width="match_parent"
android:layout_height="match_parent"
android:background="@drawable/background_wallpaper"
android:orientation="vertical">


<company.tap.tapcheckout_android.TapCheckout
android:layout_width="match_parent"
android:layout_height="match_parent"
android:layout_marginTop="@dimen/_2sdp"
android:id="@+id/checkout_pay"
/>
</LinearLayout>
 
- in your activity class :

	CheckoutConfiguration.configureWithTapCheckoutDictionary(
		this,
		publicKey,
		findViewById(R.id.checkout_pay),
		configuration,
		this
	)
```

## Code

 ```kotlin
val publicKey = getPrefStringValue("publicKey", "pk_test_ohzQrUWRnTkCLD1cqMeudyjX")

/**
 * intent
 */
val intentObj = HashMap<String, Any>()
if (intentId != null) {
	intentObj.put("intent", intentId)
}

/**
 * configureWithTapCheckoutDictionary and calling the Checkout SDK
 */
val scope = intent.getStringExtra("scopeKey")
// val charge = intent.getStringExtra("scopeKey")
val configuration = LinkedHashMap<String, Any>()
// val configuration = JSONObject()

configuration.put("open", true)
configuration.put("hashString", "")
configuration.put("checkoutMode", "page")
configuration.put("language", getPrefStringValue("selectedlangKey", default = "en"))
configuration.put("themeMode",  getPrefStringValue("selectedthemeKey","dark"))

val selectedSet= getPrefStringSetValue("supportedPaymentMethodKey", emptySet())
val jsonArrayPaymentMethod = JSONArray(selectedSet)


if(selectedSet.contains("ALL")){
	configuration.put("supportedPaymentMethods", "ALL")
}else
{ configuration.put("supportedPaymentMethods", jsonArrayPaymentMethod)
}

configuration.put("paymentType",  getPrefStringValue("paymentTypeKey","ALL"))

configuration.put("selectedCurrency", getPrefStringValue("orderCurrencyKey","KWD").toUpperCase())
configuration.put("supportedCurrencies", "ALL")

val gateway = JSONObject()
gateway.put("publicKey", getPrefStringValue("publicKey","pk_test_gznOhsfdL0QMV8AW7tSN2wKP"))
gateway.put("merchantId", getPrefStringValue("merchantId",""))
configuration.put("gateway", gateway)

val customer = JSONObject()
customer.put("firstName", getPrefStringValue("customerFNameKey","First Android"))
customer.put("lastName", getPrefStringValue("customerLKey","Test "))
customer.put("email", getPrefStringValue("customerEmailKey","example@gmail.com"))

val phone = JSONObject()
phone.put("countryCode", getPrefStringValue("customerCountryCodeKey","965"))
phone.put("number",  getPrefStringValue("customerPhoneKey","55567890"))
customer.put("phone", phone)

configuration.put("customer", customer)

val transaction = JSONObject()
transaction.put("mode", getPrefStringValue("scopeKey","charge"))

val charge = JSONObject()
charge.put("saveCard", true)

val auto = JSONObject()
auto.put("type", "VOID")
auto.put("time", 100)
charge.put("auto", auto)

val redirect = JSONObject()
redirect.put("url", "https://demo.staging.tap.company/v2/sdk/checkout")
charge.put("redirect", redirect)

charge.put("threeDSecure", true)

val subscription = JSONObject()
subscription.put("type", "SCHEDULED")
subscription.put("amount_variability", "FIXED")
subscription.put("txn_count", 0)
charge.put("subscription", subscription)

val airline = JSONObject()
val reference = JSONObject()
reference.put("booking", "")
airline.put("reference", reference)
charge.put("airline", airline)

if(getPrefStringValue("scopeKey","charge").contains("authorize")){
	transaction.put("authorize", charge)
}else {
	transaction.put("charge", charge)
}

configuration.put("transaction", transaction)

configuration.put("amount", getPrefStringValue("amountKey","1"))

val order = JSONObject()
order.put("id", "")
order.put("currency", getPrefStringValue("orderCurrencyKey","KWD").toUpperCase())
order.put("amount", getPrefStringValue("amountKey","1"))

val items = JSONArray()
val item = JSONObject()
item.put("amount", getPrefStringValue("amountKey","1"))
item.put("currency", getPrefStringValue("orderCurrencyKey","KWD").toUpperCase())
item.put("name", "Item Title 1")
item.put("quantity", 1)
item.put("description", "item description 1")
items.put(item)

order.put("items", items)
configuration.put("order", order)

val cardOptions = JSONObject()
cardOptions.put("showBrands",  getPrefBooleanValue("displayPymtBrndKey",true))
cardOptions.put("showLoadingState", getPrefBooleanValue("showLoadingStateKey",false))
cardOptions.put("collectHolderName", getPrefBooleanValue("collectCardHodlernameKey",true))
cardOptions.put("preLoadCardName", "")
cardOptions.put("cardNameEditable", getPrefBooleanValue("cardNameEditableeKey",true))
/*  val cardFundingSource= getPrefStringSetValue("supportedFundSourceKey", emptySet())
  val jsonArraycardFundingSource = JSONArray(cardFundingSource)


  if(cardFundingSource.contains("all")){
      cardOptions.put("cardFundingSource", "all")
  }else
  { cardOptions.put("cardFundingSource", jsonArraycardFundingSource)
  }*/
cardOptions.put("cardFundingSource", getPrefStringValue("supportedFundSourceKey","all"))

// cardOptions.put("cardFundingSource", "all")
cardOptions.put("saveCardOption", "all")
cardOptions.put("forceLtr", getPrefBooleanValue("forceLtrKey",false))

val alternativeCardInputs = JSONObject()
alternativeCardInputs.put("cardScanner", getPrefBooleanValue("displayScannerKey",true))
alternativeCardInputs.put("cardNFC", getPrefBooleanValue("displayNFCKey",true))

cardOptions.put("alternativeCardInputs", alternativeCardInputs)

configuration.put("cardOptions", cardOptions)
configuration.put("isApplePayAvailableOnClient", true)


CheckoutConfiguration.configureWithTapCheckoutDictionary(
	this,
	publicKey,
	findViewById(R.id.checkout_pay),
	configuration,
	this
)

}
        
```


## Simple TapCheckoutStatusDelegate
A protocol that allows integrators to get notified from events fired from the `Checkout-Android`. 

```kotlin
    interface TapCheckoutStatusDelegate {
    /// Will be fired whenever the ready is .
	fun onCheckoutReady(){}
	/// Will be fired whenever the cancelled  .
	fun onCheckoutcancel(){}
     ///   Will be fired whenever the card sdk finishes successfully the task assigned to it. Whether `TapToken` or `AuthenticatedToken`
	 fun onCheckoutSuccess(data: String) {
     }
    /// Will be fired whenever there is an error related to the card connectivity or apis
    /// - Parameter data: includes a JSON format for the error description and error
	fun onCheckoutError(error: String){
    }
}
```

# Advanced Integration

## Advanced Documentation

### Main input documentation
To make our sdk as dynamic as possible, we accept the input in a form of a `HashMap dictionary` . We will provide you with a sample full one for reference.
It is always recommended, that you generate this `HashMap dictionary` from your server side, so in future if needed you may be able to change the format if we did an update.


|Configuration| Description                                                                                                                                       | Required | Type| Sample
|--|---------------------------------------------------------------------------------------------------------------------------------------------------|--| --|--|
| operator| This is the `Key` that you will get after registering you package name.                                                                           | True  | String| `var operator=HashMap<String,Any>(),operator.put("publicKey","pk_test_YhUjg9PNT8oDlKJ1aE2fMRz7")` |
| scope| Defines the intention of using the `Checkout-Android`.                                                                                            | True  | String| ` var scope:String = "Token" ` |
| purpose| Defines the intention of using the `Token` after generation.                                                                                      | True  | String| ` var purpose:String = "Transaction" ` |
| order| This is the `order id` that you created before or `amount` , `currency` , `transaction` to generate a new order .   It will be linked this token. | True  | `Dictionary`| ` var order = HashMap<String, Any>(), order.put("id","") order.put("amount",1),order.put("currency","SAR"),order.put("description",""), order.put("reference":"A reference to this order in your system"))` |
| invoice| This is the `invoice id` that you want to link this token to if any.                                                                              | False  | `Dictionary`| ` var invoice = HashMap<String,Any>.put("id","")` |
| merchant| This is the `Merchant id` that you will get after registering you bundle id.                                                                      | True  | `Dictionary`| ` var merchant = HashMap<String,Any>.put("id","")` |
| customer| The customer details you want to attach to this tokenization process.                                                                             | True  | `Dictionary`| ` var customer =  HashMap<String,Any> ,customer.put("id,""), customer.put("nameOnCard","Tap Payments"),customer.put("editable",true),) var name :HashMap<String,Any> = [["lang":"en","first":"TAP","middle":"","last":"PAYMENTS"]] "contact":["email":"tap@tap.company", "phone":["countryCode":"+965","number":"88888888"]]] customer.put("name",name) , customer.put("contact",contact)` |
| features| Some extra features that you can enable/disable based on the experience you want to provide..                                                     | False  | `Dictionary`| ` var features=HashMap<String,Any> ,features.add("scanner":true), features.add("acceptanceBadge":true), features.add("customerCards":HashMapof("saveCard":false, "autoSaveCard":false),features.add("alternativeCardInputs":HashMapof("cardScanner":true, "cardNFC":true)`|
| acceptance| The acceptance details for the transaction. Including, which card brands and types you want to allow for the customer to tokenize.                | False  | `Dictionary`| ` var acceptance = HashMap<String,Any> ,acceptance.put("supportedSchemes",["AMERICAN_EXPRESS","VISA","MASTERCARD","OMANNET","MADA"]),acceptance.put("supportedFundSource",["CREDIT","DEBIT"]),acceptance.put("supportedPaymentAuthentications",["3DS"])` |
| fieldVisibility| Needed to define visibility of the optional fieldVisibility in the card form.                                                                     | False  | `Dictionary`| ` var fieldVisibility = HashMap<String,Any> ,fieldVisibility.put("cardHolder",true)` |
| interface| Needed to defines look and feel related configurations.                                                                                           | False  | `Dictionary`| ` var interface = HashMap<String,Any> ,interface.put("locale","en"), interface.put("theme","light"), interface.put("edges","curved"), interface.put("direction","dynamic"),interface.put(powered,true),interface.put("colorStyle","colored"),interface.put("loader",true) // Allowed values for theme : light/dark. locale: en/ar, edges: curved/flat, direction:ltr/dynaimc,colorStyle:colored/monochrome` |
| post| This is the `webhook` for your server, if you want us to update you server to server.                                                             | False  | `Dictionary`| ` var post = HashMap<String,Any>.put("url","")` |


### Documentation per variable

 - operator:
	 - Responsible for passing the data that defines you as a merchant within Tap system.
 - operator.publicKey:
	 - A string, which you get after registering the app bundle id within the Tap system. It is required to correctly identify you as a merchant.
	 - You will receive a sandbox and a production key. Use, the one that matches your environment at the moment of usage.
 - scope:
	 - Defines the intention of the token you are generating.
	 - When the token is used afterwards, the usage will be checked against the original purpose to make sure they are a match.
	 - Possible values:
		 -  `Charge` : This means you will get charge objects.
		 - `Authorize` This means you will get an authenticated Tap token to use in our charge api right away.

 - order:
	 - The details about the order that you will be using the token you are generating within.
 - order.id:
	 - The id of the `order` if you already created it using our apis.
 - order.currency:
	 - The intended currency you will perform the order linked to this token afterwards.
 -  order.amount:
	 - The intended amount you will perform the order linked to this token afterwards.
 - order.description:
	 - Optional string to put some clarifications about the order if needed.
 - order.reference:
	 - Pass this value if you want to link this order to the a one you have within your system.

 
 - merchant.id:
	 - Optional string to pass to define a sub entity registered under your key in Tap. It is the `Merchant id` that you get from our onboarding team.
 - customer.id:
	 - If you have previously have created a customer using our apis and you want to link this token to him. please pass his id.
 - customer.name:
	 - It is a list of localized names. each name will have:
		 - lang : the 2 iso code for the locale of this name for example `en`
		 - first : The first name.
		 - middle: The middle name.
		 - last : The last name.
 - customer.nameOnCard:
	 - If you want to prefill the card holder's name field.
 - customer.editable:
	 - A boolean that controls whether the customer can edit the card holder's name field or not.
 - customer.contact.email:
	 - An email string for  the customer we are creating. At least the email or the phone is required.
 - customer.contact.phone:
	 - The customer's phone:
		 - countryCode
		 - number
 - features:
	 - Some extra features/functionalities that can be configured as per your needs.	 
 - features.acceptanceBadge:
	 - A boolean to indicate wether or not you want to display the list of supported card brands that appear beneath the card form itself.
 - features.customerCards.saveCard:
	 - A boolean to indicate wether or not you want to display the save card option to the customer.
	 - Must be used with a combination of these scopes:
		 - SaveToken
		 - SaveAuthenticatedToken
 - features.customerCards.autoSave:
	 - A boolean to indicate wether or not you want the save card switch to be on by default.
 - features.alternativeCardInput.cardScanner:
	 - A boolean to indicate whether or not you want to display the scan card icon.
	 - Make sure you have access to camera usage, before enabling the scanner function.
 - features.alternativeCardInput.cardNFC
	- A boolean to indicate whether or not you want to display the NFC icon.
- acceptance:
	- List of configurations that control the payment itself.
- acceptance.supportedSchemes:
	- A list to control which card schemes the customer can pay with. For example:
		- AMERICAN_EXPRESS
		- VISA
		- MASTERCARD
		- MADA
		- OMANNET
- acceptance.supportedFundSource:
	- A list to control which card types are allowed by your customer. For example:
		- DEBIT
		- CREDIT
- acceptance.supportedPaymentAuthentications:
	- A list of what authentication techniques you want to enforce and apple. For example:
		- 3DS
- fieldVisibility.card.cardHolder:
	- A boolean to indicate wether or not you want to show/collect the card holder name.
- interface.loader:
	- A boolean to indicate wether or not you want to show a loading view on top of the card form while it is performing api requests.
- interface.locale:
	- The language of the card form. Accepted values as of now are:
		- en
		- ar
- interface.theme:
	- The display style of the card form. Accepted values as of now are:
		- light
		- dark
		- dynamic // follow the device's display style


## Initialization of the input

You can create a Dictionary HashMap to pass the data to our sdk. The good part about this, is that you can generate the data from one of your apis. Whenever we have an update to the configurations, you can update your api. This will make sure, that you will not have to update your app on the Google Play Store.

```kotlin
     val publicKey = getPrefStringValue("publicKey", "pk_test_ohzQrUWRnTkCLD1cqMeudyjX")

/**
 * intent
 */
val intentObj = HashMap<String, Any>()
if (intentId != null) {
	intentObj.put("intent", intentId)
}

/**
 * configureWithTapCheckoutDictionary and calling the Checkout SDK
 */
val scope = intent.getStringExtra("scopeKey")
// val charge = intent.getStringExtra("scopeKey")
val configuration = LinkedHashMap<String, Any>()
// val configuration = JSONObject()

configuration.put("open", true)
configuration.put("hashString", "")
configuration.put("checkoutMode", "page")
configuration.put("language", getPrefStringValue("selectedlangKey", default = "en"))
configuration.put("themeMode",  getPrefStringValue("selectedthemeKey","dark"))

val selectedSet= getPrefStringSetValue("supportedPaymentMethodKey", emptySet())
val jsonArrayPaymentMethod = JSONArray(selectedSet)


if(selectedSet.contains("ALL")){
	configuration.put("supportedPaymentMethods", "ALL")
}else
{ configuration.put("supportedPaymentMethods", jsonArrayPaymentMethod)
}

configuration.put("paymentType",  getPrefStringValue("paymentTypeKey","ALL"))

configuration.put("selectedCurrency", getPrefStringValue("orderCurrencyKey","KWD").toUpperCase())
configuration.put("supportedCurrencies", "ALL")

val gateway = JSONObject()
gateway.put("publicKey", getPrefStringValue("publicKey","pk_test_gznOhsfdL0QMV8AW7tSN2wKP"))
gateway.put("merchantId", getPrefStringValue("merchantId",""))
configuration.put("gateway", gateway)

val customer = JSONObject()
customer.put("firstName", getPrefStringValue("customerFNameKey","First Android"))
customer.put("lastName", getPrefStringValue("customerLKey","Test "))
customer.put("email", getPrefStringValue("customerEmailKey","example@gmail.com"))

val phone = JSONObject()
phone.put("countryCode", getPrefStringValue("customerCountryCodeKey","965"))
phone.put("number",  getPrefStringValue("customerPhoneKey","55567890"))
customer.put("phone", phone)

configuration.put("customer", customer)

val transaction = JSONObject()
transaction.put("mode", getPrefStringValue("scopeKey","charge"))

val charge = JSONObject()
charge.put("saveCard", true)

val auto = JSONObject()
auto.put("type", "VOID")
auto.put("time", 100)
charge.put("auto", auto)

val redirect = JSONObject()
redirect.put("url", "https://demo.staging.tap.company/v2/sdk/checkout")
charge.put("redirect", redirect)

charge.put("threeDSecure", true)

val subscription = JSONObject()
subscription.put("type", "SCHEDULED")
subscription.put("amount_variability", "FIXED")
subscription.put("txn_count", 0)
charge.put("subscription", subscription)

val airline = JSONObject()
val reference = JSONObject()
reference.put("booking", "")
airline.put("reference", reference)
charge.put("airline", airline)

if(getPrefStringValue("scopeKey","charge").contains("authorize")){
	transaction.put("authorize", charge)
}else {
	transaction.put("charge", charge)
}

configuration.put("transaction", transaction)

configuration.put("amount", getPrefStringValue("amountKey","1"))

val order = JSONObject()
order.put("id", "")
order.put("currency", getPrefStringValue("orderCurrencyKey","KWD").toUpperCase())
order.put("amount", getPrefStringValue("amountKey","1"))

val items = JSONArray()
val item = JSONObject()
item.put("amount", getPrefStringValue("amountKey","1"))
item.put("currency", getPrefStringValue("orderCurrencyKey","KWD").toUpperCase())
item.put("name", "Item Title 1")
item.put("quantity", 1)
item.put("description", "item description 1")
items.put(item)

order.put("items", items)
configuration.put("order", order)

val cardOptions = JSONObject()
cardOptions.put("showBrands",  getPrefBooleanValue("displayPymtBrndKey",true))
cardOptions.put("showLoadingState", getPrefBooleanValue("showLoadingStateKey",false))
cardOptions.put("collectHolderName", getPrefBooleanValue("collectCardHodlernameKey",true))
cardOptions.put("preLoadCardName", "")
cardOptions.put("cardNameEditable", getPrefBooleanValue("cardNameEditableeKey",true))
/*  val cardFundingSource= getPrefStringSetValue("supportedFundSourceKey", emptySet())
  val jsonArraycardFundingSource = JSONArray(cardFundingSource)


  if(cardFundingSource.contains("all")){
      cardOptions.put("cardFundingSource", "all")
  }else
  { cardOptions.put("cardFundingSource", jsonArraycardFundingSource)
  }*/
cardOptions.put("cardFundingSource", getPrefStringValue("supportedFundSourceKey","all"))

// cardOptions.put("cardFundingSource", "all")
cardOptions.put("saveCardOption", "all")
cardOptions.put("forceLtr", getPrefBooleanValue("forceLtrKey",false))

val alternativeCardInputs = JSONObject()
alternativeCardInputs.put("cardScanner", getPrefBooleanValue("displayScannerKey",true))
alternativeCardInputs.put("cardNFC", getPrefBooleanValue("displayNFCKey",true))

cardOptions.put("alternativeCardInputs", alternativeCardInputs)

configuration.put("cardOptions", cardOptions)
configuration.put("isApplePayAvailableOnClient", true)


```



## Advanced TapCheckoutStatusDelegate
An interface that allows integrators to get notified from events fired from the `TapCheckout`. 
```kotlin

interface TapCheckoutStatusDelegate {


	override fun onCheckoutReady() {
		findViewById<TextView>(R.id.text).text = ""
		findViewById<TextView>(R.id.text).text = "onReady"
		// Toast.makeText(this, "onReady", Toast.LENGTH_SHORT).show()
		Log.e("<<<<<MainAcityv>>>>>>>>>>>","<<<<<<onCheckoutReady>>>>>>>>>.")

	}

	override fun onCheckoutSuccess(data: String) {
		Log.i("onSuccess", data)
		findViewById<TextView>(R.id.text).text = ""
		findViewById<TextView>(R.id.text).text = "onSuccess>>>> $data"
		findViewById<TextView>(R.id.text).movementMethod = ScrollingMovementMethod()

		//  customAlertBox("onCheckoutSuccess",data)
		Toast.makeText(this, "onSuccess $data", Toast.LENGTH_SHORT).show()

	}


	override fun onCheckoutcancel() {
		//  customAlertBox("onCheckoutcancel","Canceled")
		Toast.makeText(this, "Cancel ", Toast.LENGTH_SHORT).show()
	}

	override fun onCheckoutError(error: String) {
		Log.e("onCheckoutError", error.toString())
		findViewById<TextView>(R.id.text).text = ""
		findViewById<TextView>(R.id.text).text = "onError $error"
		//  customAlertBox("onCheckoutError",error)
		Toast.makeText(this, "onError received $error ", Toast.LENGTH_SHORT).show()

	}


```
