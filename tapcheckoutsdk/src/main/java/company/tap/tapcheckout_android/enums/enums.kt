package company.tap.tapcheckout_android.enums


const val rawFolderRefrence = "raw"
const val webPrefix = "tap{buttonType}websdk://"
const val cardPrefix = "tapcardwebsdk://"

const val keyValueName = "data"
const val urlWebStarter = "https://button.staging.tap.company/wrapper/{url}?configurations="
//const val urlWebStarter = "https://button.dev.tap.company/wrapper/{url}?configurations="
const val publicKeyToGet = "publicKey"
const val HeadersApplication = "application"
const val HeadersMdn = "mdn"
const val operatorKey = "operator"
const val headersKey = "headers"
const val intentKey = "intent"

const val redirectKey = "redirect"
const val tapID = "tap_id"
const val redirectValue = "onTapRedirect://"

const val urlKey = "url"
const val careemPayUrlHandler ="https://checkout"

enum class TapCheckoutDelegates {
    onReady, onClick, onOrderCreated, onChargeCreated, onError, onSuccess, cancel,onCancel, onClosePopup,onHeightChange,on3dsRedirect,onBinIdentification, launch,
    onClose,onRedirectUrl,onScannerClick,onNfcClick
}

enum class SCHEMES(var value: Pair<String, String>) {
    KNET(Pair(urlWebStarter.replace("{url}", "knet"), webPrefix.replace("{buttonType}", "knet"))),
    BENEFIT(
        Pair(
            urlWebStarter.replace("{url}", "benefit"),
            webPrefix.replace("{buttonType}", "benefit")
        )
    ),
    FAWRY(
        Pair(
            urlWebStarter.replace("{url}", "fawry"),
            webPrefix.replace("{buttonType}", "fawry")
        )
    ),
    PAYPAL(
        Pair(
            urlWebStarter.replace("{url}", "paypal"),
            webPrefix.replace("{buttonType}", "paypal")
        )
    ),
    TABBY(
        Pair(
            urlWebStarter.replace("{url}", "tabby"),
            webPrefix.replace("{buttonType}", "tabby")
        )
    ),
    GOOGLE(
        Pair(
            urlWebStarter.replace("{url}", "googlepay"),
            webPrefix.replace("{buttonType}", "googlepay")
        )
    ),
    CAREEMPAY(
        Pair(
            urlWebStarter.replace("{url}", "careempay"),
            webPrefix.replace("{buttonType}", "careempay")
        )
    ),
    SAMSUNGPAY(
        Pair(
            urlWebStarter.replace("{url}", "samsungpay"),
            webPrefix.replace("{buttonType}", "samsungpay")
        )
    ),
    VISA(
        Pair(
            urlWebStarter.replace("{url}", "card/VISA"),
            cardPrefix
        )
    ),
    AMERICANEXPRESS(
        Pair(
            urlWebStarter.replace("{url}", "card/AMERICAN_EXPRESS"),
            cardPrefix
        )
    ),
    MADA(
        Pair(
            urlWebStarter.replace("{url}", "card/MADA"),
            cardPrefix
        )
    ),
    MASTERCARD(
        Pair(
            urlWebStarter.replace("{url}", "card/MASTERCARD"),
            cardPrefix
        )
    ),
    CARD(    Pair(
        urlWebStarter.replace("{url}", "card"),
        cardPrefix
    ))

}

enum class ThreeDsPayButtonType {
    KNET, BENEFIT, BENEFITPAY, FAWRY, PAYPAL, TABBY, GOOGLEPAY, CAREEMPAY,SAMSUNGPAY, VISA, AMERICANEXPRESS, MADA, MASTERCARD,CARD
}