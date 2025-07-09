# Card-Andriod

We at [Tap Payments](https://www.tap.company/) strive to make your payments easier than ever. We as a PCI compliant company, provide you a from the self solution to process card payments in your Android app.

[![Platform](https://img.shields.io/badge/platform-Android-inactive.svg?style=flat)](https://tap-payments.github.io/goSellSDK-Android/)
[![Documentation](https://img.shields.io/badge/documentation-100%25-bright%20green.svg)](https://tap-payments.github.io/goSellSDK-Android/)
[![SDK Version](https://img.shields.io/badge/minSdkVersion-24-blue.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)
[![SDK Version](https://img.shields.io/badge/targetSdkVersion-33-informational.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)
[![SDK Version](https://img.shields.io/badge/latestVersion-0.0.52-informational.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)

# Demo
![Imgur](https://i.imgur.com/qLaQdN5.gif)

# Requirements

 1. We support from Android minSdk 24

# Steps overview
```mermaid
sequenceDiagram

participant  A  as  App
participant  T  as  Tap
participant  C  as  Card Android

A->>T:  Regsiter app.
T-->>A: Public key.
A ->> C : Install SDK
A ->> C : Init TapCardView
C -->> A : tapCardView
A ->> C : tapCardView.initTapCardSDK(configurations,delegate)
C -->> A: onReady()
C -->> C : Enter card data
C -->> A : onBinIdentification(data)
C -->> A : onValidInput
A ->> C : tapCardView.generateTapToken()
C -->> A : onSuccess(data(
```

# Get your Tap keys
You can always use the example keys within our example app, but we do recommend you to head to our [onboarding](https://register.tap.company/sell)  page. You will need to register your `package name` to get your `Tap Key` that you will need to activate our `Card-Android`.

# Installation

We got you covered, `Card-Android` can be installed with all possible technologies.

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
  implementation : 'com.github.Tap-Payments:Card-Android:{latest-tag-release}'
}
```

# Simple Integration
You can initialize `Card-Android` in different ways

 1. XML.
 2. Code.
## XML

```kotlin
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:orientation="vertical"
    android:layout_height="match_parent"
    tools:context=".main_activity.MainActivity">

 <company.tap.tapcardformkit.open.web_wrapper.TapCardKit
        android:id="@+id/tapCardForm"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        />

</LinearLayout>
 
- in your activity class : 

  TapCardConfiguration.configureWithTapCardDictionaryConfiguration(
            this,
            findViewById<TapCardKit>(R.id.tapCardForm),
            configuration,
            TapCardStatusDelegate: this) 
```

## Code

 ```kotlin

       lateinit var tapCardKitView: TapCardKit

       override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * operator
         */
        val operator = HashMap<String,Any>()
        operator.put("publicKey","")
          /**
         * order
         */
        val order = HashMap<String,Any>()
        order.put("id", "")
        order.put("amount",  1)
        order.put("currency","SAR")
        order.put("description","")
        order.put("reference","")
	
        /**
         * name
         */
        val name = java.util.HashMap<String,Any>()
        name.put("lang","en")
        name.put("first",  "first")
        name.put("middle",  "middle")
        name.put("last",  "last")
        /**
         * phone
         */
        val phone = java.util.HashMap<String,Any>()
        phone.put("countryCode","+20")
        phone.put("number","011")
        /**
         * contact
         */
        val contact = java.util.HashMap<String,Any>()
        contact.put("email","test@gmail.com")
        contact.put("phone",phone)

        /**
         * customer
         */
        val customer = java.util.HashMap<String,Any>()
        customer.put("nameOnCard", "test")
        customer.put("editable","true")
        customer.put("contact",contact)
        customer.put("name", listOf(name))

        /**
         * configuration 
         */
        val configuration = LinkedHashMap<String,Any>()

        configuration.put("operator",operator)
        configuration.put("scope","Authentication")
        configuration.put("order",order)
        configuration.put("customer",customer)

        val linearLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        /** create dynamic view of TapCardKit view **/ 
        tapCardKitView  = TapCardKit(this)
        tapCardKitView.layoutParams = linearLayoutParams
        /** refrence to parent layout view **/  
        this.findViewById<LinearLayout>(R.id.linear_layout).addView(tapCardKitView)


      TapCardConfiguration.configureWithTapCardDictionaryConfiguration(
            this,
            tapCardKitView,
            configuration,
            TapCardStatusDelegate : this) 

}
        
```

## Tokenize the card

Once you get notified that the `TapCardKit` now has a valid input from the delegate. You can start the tokenization process by calling the public interface:

```kotlin
///  Wil start the process of generating a `TapToken` with the current card data
tapCardKitView.generateTapToken()
```

## Simple TapCardStatusDelegate
A protocol that allows integrators to get notified from events fired from the `Card-Android`. 

```kotlin
    interface TapCardStatusDelegate {
    /// Will be fired whenever the validity of the card data changes.
    /// - Parameter valid: Will be true if the card data is valid and false otherwise.
    override fun  onValidInput(invalid: Bool) {
    }
    
     ///   Will be fired whenever the card sdk finishes successfully the task assigned to it. Whether `TapToken` or `AuthenticatedToken`
    override fun  onSuccess(data: String) {
     }
    /// Will be fired whenever there is an error related to the card connectivity or apis
    /// - Parameter data: includes a JSON format for the error description and error
    override fun onError(data: String){
    }
}
```

# Advanced Integration

## Advanced Documentation

### Main input documentation
To make our sdk as dynamic as possible, we accept the input in a form of a `HashMap dictionary` . We will provide you with a sample full one for reference.
It is always recommended, that you generate this `HashMap dictionary` from your server side, so in future if needed you may be able to change the format if we did an update.


|Configuration|Description | Required | Type| Sample
|--|--|--| --|--|
| operator| This is the `Key` that you will get after registering you package name. | True  | String| `var operator=HashMap<String,Any>(),operator.put("publicKey","pk_test_YhUjg9PNT8oDlKJ1aE2fMRz7")` |
| scope| Defines the intention of using the `Card-Android`. | True  | String| ` var scope:String = "Token" ` |
| purpose| Defines the intention of using the `Token` after generation. | True  | String| ` var purpose:String = "Transaction" ` |
| order| This is the `order id` that you created before or `amount` , `currency` , `transaction` to generate a new order .   It will be linked this token. | True  | `Dictionary`| ` var order = HashMap<String, Any>(), order.put("id","") order.put("amount",1),order.put("currency","SAR"),order.put("description",""), order.put("reference":"A reference to this order in your system"))` |
| invoice| This is the `invoice id` that you want to link this token to if any. | False  | `Dictionary`| ` var invoice = HashMap<String,Any>.put("id","")` |
| merchant| This is the `Merchant id` that you will get after registering you bundle id. | True  | `Dictionary`| ` var merchant = HashMap<String,Any>.put("id","")` |
| customer| The customer details you want to attach to this tokenization process. | True  | `Dictionary`| ` var customer =  HashMap<String,Any> ,customer.put("id,""), customer.put("nameOnCard","Tap Payments"),customer.put("editable",true),) var name :HashMap<String,Any> = [["lang":"en","first":"TAP","middle":"","last":"PAYMENTS"]] "contact":["email":"tap@tap.company", "phone":["countryCode":"+965","number":"88888888"]]] customer.put("name",name) , customer.put("contact",contact)` |
| features| Some extra features that you can enable/disable based on the experience you want to provide.. | False  | `Dictionary`| ` var features=HashMap<String,Any> ,features.add("scanner":true), features.add("acceptanceBadge":true), features.add("customerCards":HashMapof("saveCard":false, "autoSaveCard":false),features.add("alternativeCardInputs":HashMapof("cardScanner":true, "cardNFC":true)`|
| acceptance| The acceptance details for the transaction. Including, which card brands and types you want to allow for the customer to tokenize. | False  | `Dictionary`| ` var acceptance = HashMap<String,Any> ,acceptance.put("supportedSchemes",["AMERICAN_EXPRESS","VISA","MASTERCARD","OMANNET","MADA"]),acceptance.put("supportedFundSource",["CREDIT","DEBIT"]),acceptance.put("supportedPaymentAuthentications",["3DS"])` |
| fieldVisibility| Needed to define visibility of the optional fieldVisibility in the card form. | False  | `Dictionary`| ` var fieldVisibility = HashMap<String,Any> ,fieldVisibility.put("cardHolder",true)` |
| interface| Needed to defines look and feel related configurations. | False  | `Dictionary`| ` var interface = HashMap<String,Any> ,interface.put("locale","en"), interface.put("theme","light"), interface.put("edges","curved"), interface.put("direction","dynamic"),interface.put(powered,true),interface.put("colorStyle","colored"),interface.put("loader",true) // Allowed values for theme : light/dark. locale: en/ar, edges: curved/flat, direction:ltr/dynaimc,colorStyle:colored/monochrome` |
| post| This is the `webhook` for your server, if you want us to update you server to server. | False  | `Dictionary`| ` var post = HashMap<String,Any>.put("url","")` |


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
		 -  `Token` : This means you will get a Tap token to use afterwards.
		 - `AuthenticatedToken` This means you will get an authenticated Tap token to use in our charge api right away.
		 - `SaveToken` This means you will get a token to use multiple times with authentication each time.
		 - `SaveAuthenticatedToken` This means you will get an authenticated token to use in multiple times right away.
 - purpose:
	 - Defines the intention of using the `Token` after generation.
	 - Possible values:
		 - `Transaction` Using the token for a single charge.
		 - `Milestone Transaction` Using the token for paying a part of a bigger order, when reaching a certain milestone.
		 - `Installment Transaction` Using the token for a charge that is a part of an installement plan.
		 - `Billing Transaction` Using the token for paying a bill.
		 - `Subscription Transaction` Using the token for a recurring based transaction.
		 - `Verify Cardholder` Using the token to verify the ownership of the card.
		 - `Save Card` Using the token to save this card and link it to a certain customer.
		 - `Maintain Card` Used to renew a saved card.
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

 - invoice.id:
	 - Optional string to pass an invoice id, that you want to link to this token afterwards.
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
- interface.edges:
	- How do you want the edges of the card form to. Accepted values as of now are:
		- curved
		- flat
- interface.cardDirection:
	- The layout of the fields (card logo, number, date & CVV) within the card element itself. Accepted values as of now are:
		- ltr // fields will inflate from left to right
		- rtl // fields will inflate from right to left
		- dynamic // fields will inflate in the locale's direction
- interface.powered:
	- A boolean to indicate wether or not you want to show powered by tap.
	- Note, that you have to have the permission to hide it from the integration team. Otherwise, you will get an error if you pass it as false.
- interface.colorStyle:
	- How do you want the icons rendered inside the card form to. Accepted values as of now are:
		- colored
		- monochrome

## Initialization of the input

You can create a Dictionary HashMap to pass the data to our sdk. The good part about this, is that you can generate the data from one of your apis. Whenever we have an update to the configurations, you can update your api. This will make sure, that you will not have to update your app on the Google Play Store.

```kotlin
     /**
       * operator
       */
      val operator = HashMap<String,Any>()
        operator.put("publicKey","pk_test_YhUjg9PNT8oDlKJ1aE2fMRz7")

        /**
         * merchant
         */
        val merchant = HashMap<String,Any>()
        merchant.put("id","")

        /**
         * invoice
         */
        val invoice = java.util.HashMap<String,Any>()
        invoice.put("id","")
        /**
         * post
         */
        val post = java.util.HashMap<String,Any>()
        post.put("url","")


        /**
         * phone
         */
        val phone = HashMap<String,Any>()
        phone.put("countryCode","+20")
        phone.put("number","011")

        /**
         * contact
         */
        val contact = HashMap<String,Any>()
        contact.put("email","test@gmail.com")
        contact.put("phone",phone)
        /**
         * name
         */
        val name = HashMap<String,Any>()
        name.put("lang","en")
        name.put("first","Tap")
        name.put("middle","")
        name.put("last","Payment")

        /**
         * customer
         */
        val customer = HashMap<String,Any>()
        customer.put("nameOnCard","")
        customer.put("editable",true)
        customer.put("contact",contact)
        customer.put("name", listOf(name))




        /**
         * acceptance
         */
        val acceptance = HashMap<String,Any>()
        acceptance.put("supportedSchemes", listOf("MADA","VISA","MASTERCARD","AMEX"))
        acceptance.put("supportedFundSource",listOf("CREDIT","DEBIT"))
        acceptance.put("supportedPaymentAuthentications",listOf("3DS"))

        /**
         * fields
         */
        val fieldVisibility = HashMap<String,Any>()

        /**
         * card
         */
        val card = HashMap<String,Any>()
         card.put("cvv",true)
         card.put("cardHolder",true)
        fieldVisibility.put("card",card)

        /**
         * customerCards
         */
        val customerCards = HashMap<String,Any>()
        customerCards.put("saveCard",true)
        customerCards.put("autoSaveCard",true)

        /**
         * alternative cards
         */
        val alternativeCardInput = HashMap<String,Any>()
        alternativeCardInput.put("cardScanner",true)
        alternativeCardInput.put("cardNFC",true)
        /**
         * features
         */
        val features = HashMap<String,Any>()
        features.put("acceptanceBadge",true)
        features.put("customerCards",customerCards)
        features.put("alternativeCardInputs",alternativeCardInput)



        /**
         * order
         */
        val order = HashMap<String,Any>()
        order.put("id","order_id")
        order.put("amount","1")
        order.put("currency","SAR")
        order.put("description","description")
        order.put("reference","refrence_id")

        /**
         * interface
         */
        val tapInterface = HashMap<String,Any>()
        tapInterface.put("locale","en")
        tapInterface.put("theme","light")
        tapInterface.put("edges","curved")
        tapInterface.put("cardDirection","dynamic")
        tapInterface.put("powered",true)
        tapInterface.put("colorStyle","colored")
        tapInterface.put("loader",true)



        /**
         * configuration request
         */

        val configuration = LinkedHashMap<String,Any>()
        configuration.put("operator", operator)
        configuration.put("merchant",merchant)
        configuration.put("transaction",transaction)
        configuration.put("order",order)
        configuration.put("invoice",invoice)
        configuration.put("post",post)
        configuration.put("purpose","PAYMENT_TRANSACTION")
        configuration.put("fieldVisibility",fields)
        configuration.put("features",features)
        configuration.put("redirect",redirect)
        configuration.put("acceptance",acceptance)
        configuration.put("interface",tapInterface) 
        configuration.put("scope","Authenticate") // or  configuration.put("scope","Token")
        configuration.put("customer",customer)

```



## Advanced TapCardStatusDelegate
An interface that allows integrators to get notified from events fired from the `TapCardKit`. 
```kotlin

interface TapCardStatusDelegate {


    override fun onCardSuccess(data: String)
/**
        Will be fired whenever the card sdk finishes successfully the task assigned to it. Whether `TapToken` or `AuthenticatedToken`
     - Parameter data: will include the data in JSON format. For `TapToken`:
     {
         "id": "tok_MrL97231045SOom8cF8G939",
         "created": 1694169907939,
         "object": "token",
         "live_mode": false,
         "type": "CARD",
         "source": "CARD-ENCRYPTED",
         "used": false,
         "card": {
             "id": "card_d9Vj7231045akVT80B8n944",
             "object": "card",
             "address": {},
             "funding": "CREDIT",
             "fingerprint": "gRkNTnMrJPtVYkFDVU485Gc%2FQtEo%2BsV44sfBLiSPM1w%3D",
             "brand": "VISA",
             "scheme": "VISA",
             "category": "",
             "exp_month": 4,
             "exp_year": 24,
             "last_four": "4242",
             "first_six": "424242",
             "name": "AHMED",
             "issuer": {
                "bank": "",
                "country": "GB",
                "id": "bnk_TS07A0720231345Qx1e0809820"
            }
         },
         "url": ""
     }
     */

    override fun onReady()
    /**
     *  Will be fired whenever the card is rendered and loaded
     */

    override fun onCardFocus()
    /**
     *  Will be fired once the user focuses any of the card fields
     */

 
    override fun onCardBindIdentification(data: String)
/** - Parameter data: will include the data in JSON format. example :
     *{
        "bin": "424242",
        "bank": "",
        "card_brand": "VISA",
        "card_type": "CREDIT",
        "card_category": "",
        "card_scheme": "VISA",
        "country": "GB",
        "address_required": false,
        "api_version": "V2",
        "issuer_id": "bnk_TS02A5720231337s3YN0809429",
        "brand": "VISA"
     }*     */

    override fun onValidInput(isValid: String)
    /**
     *  Will be fired whenever the validity of the card data changes.
     *  Parameter isValid: Will be true if the card data is valid and false otherwise.
     */


    override fun onCardError(error: String)=
   /**
     *  Will be fired whenever there is an error related to the card connectivity or apis
     *  Parameter data: includes a JSON format for the error description and error
     */

    override fun onCardHeightChange(heightChange:String)
   /**
     *  Will be fired whenever the card element changes its height for your convience
     *   Parameter height: The new needed height
     */

   override fun onChangeSaveCard(enabled: Boolean) {
       
   }


}


```
