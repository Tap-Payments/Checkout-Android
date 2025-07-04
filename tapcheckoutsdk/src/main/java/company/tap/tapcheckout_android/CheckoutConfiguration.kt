package company.tap.tapcheckout_android


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.Log


import company.tap.tapnetworkkit.connection.NetworkApp
import company.tap.tapnetworkkit.utils.CryptoUtil
import company.tap.tapcheckout_android.ApiService.BASE_URL_1
import company.tap.tapcheckout_android.TapCheckoutDataConfiguration.configurationsAsHashMap
import company.tap.tapcheckout_android.enums.HeadersApplication
import company.tap.tapcheckout_android.enums.HeadersMdn
import company.tap.tapcheckout_android.enums.headersKey
import company.tap.tapcheckout_android.enums.urlWebStarter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Locale


class CheckoutConfiguration {

    companion object {
        private val retrofit = ApiService.RetrofitClient.getClient1()
        private val tapSDKConfigsUrl = retrofit.create(ApiService.TapCheckoutSDKConfigUrls::class.java)
        private var testEncKey: String? = null
        private var prodEncKey: String? = null
        var  payButonurlFormat:String?="https://button.dev.tap.company/?intentId=%@&publicKey=%@&mdn=%@&platform=mobile"
        private var  encodedeky: String? = null
        private var   headers: Headers? = null

        fun configureWithTapCheckoutDictionary(
            context: Context,
            publicKey: String?,
            tapRedirectViewWeb: TapCheckout?,
            tapMapConfiguration: java.util.HashMap<String, Any>,
            tapCheckoutStatusDelegate: TapCheckoutStatusDelegate? = null

        ) {

            MainScope().launch {
                getTapButtonSDKConfigUrls(
                    tapMapConfiguration,
                    publicKey,
                    tapRedirectViewWeb,
                    context,
                    tapCheckoutStatusDelegate

                )
            }

        }

        private suspend fun getTapButtonSDKConfigUrls(tapMapConfiguration: HashMap<String, Any>, publicKey: String?,tapCardInputViewWeb: TapCheckout?, context: Context, tapCheckoutStatusDelegate: TapCheckoutStatusDelegate?) {
            try {
                /**
                 * request to get Tap configs
                 */

                val tapCheckoutSDKConfigUrlResponse = tapSDKConfigsUrl.getCheckoutSDKConfigUrl()
                println("tapCheckoutSDKConfigUrlResponse>>>>"+tapCheckoutSDKConfigUrlResponse)

                BASE_URL_1 = tapCheckoutSDKConfigUrlResponse.baseURL
                prodEncKey = tapCheckoutSDKConfigUrlResponse.prodEncKey




                //testEncKey = tapButtonSDKConfigUrlResponse.testEncKey
                testEncKey = context.resources.getString(R.string.enryptkeyTest)
                //  urlWebStarter = tapButtonSDKConfigUrlResponse.baseURL

                val themeMode = tapMapConfiguration["themeMode"] as? String
                if(themeMode?.contains("dynamic") == true) {
                    val currentNightMode =
                        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    val systemTheme = when (currentNightMode) {
                        Configuration.UI_MODE_NIGHT_YES -> "dark"
                        Configuration.UI_MODE_NIGHT_NO -> "light"
                        else -> "light"
                    }

                    tapMapConfiguration["themeMode"] = systemTheme
                }

                val language = tapMapConfiguration["language"] as? String
                if(language?.contains("dynamic") == true|| language?.contains("DYNAMIC") == true) {
                    // Detect current device language
                    val locale = Locale.getDefault()
                    val systemLanguage = if (locale.language == "ar") "ar" else "en"

                    tapMapConfiguration["language"] = systemLanguage
                }
                startWithSDKConfigs(
                    context,
                    publicKey ,
                    tapCardInputViewWeb,
                    tapMapConfiguration,
                    tapCheckoutStatusDelegate

                )

            } catch (e: Exception) {
                BASE_URL_1 = BASE_URL_1 //Todo what should be here
                testEncKey =  tapCardInputViewWeb?.context?.resources?.getString(R.string.enryptkeyTest)
                prodEncKey = tapCardInputViewWeb?.context?.resources?.getString(R.string.enryptkeyProduction)

                startWithSDKConfigs(
                    context,
                    publicKey,
                    tapCardInputViewWeb,
                    tapMapConfiguration,
                    tapCheckoutStatusDelegate

                )
                Log.e("error Config", e.message.toString())
                e.printStackTrace()
            }
        }

        @SuppressLint("RestrictedApi")
        fun addOperatorHeaderField(
            tapCardInputViewWeb: TapCheckout?,
            context: Context,
            modelConfiguration: KnetConfiguration,
            publicKey: String?
        ) {
            encodedeky = when(publicKey.toString().contains("test")){
                true->{
                    // context.resources?.getString(R.string.enryptkeyTest)
                    testEncKey
                }
                false->{
                    //  tapCardInputViewWeb?.context?.resources?.getString(R.string.enryptkeyProduction)
                    prodEncKey

                }
            }

            Log.e("packagedname",context.packageName.toString())
            Log.e("encodedeky",encodedeky.toString())
            NetworkApp.initNetwork(
                tapCardInputViewWeb?.context ,
                publicKey ?: "",
                 context.packageName,
               // "demo.tap.PayButtonSDK",
                ApiService.BASE_URL,
                "android-knet",
                true,
                /*   "-----BEGIN PUBLIC KEY-----\n" +
                           "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC8AX++RtxPZFtns4XzXFlDIxPB\n" +
                           "h0umN4qRXZaKDIlb6a3MknaB7psJWmf2l+e4Cfh9b5tey/+rZqpQ065eXTZfGCAu\n" +
                           "BLt+fYLQBhLfjRpk8S6hlIzc1Kdjg65uqzMwcTd0p7I4KLwHk1I0oXzuEu53fU1L\n" +
                           "SZhWp4Mnd6wjVgXAsQIDAQAB\n" +
                           "-----END PUBLIC KEY-----",*/encodedeky,
                null
            )
            headers = Headers(
                application = NetworkApp.getApplicationInfo(),
                mdn = CryptoUtil.encryptJsonString(
                     context.packageName.toString(),
                   // "demo.tap.PayButtonSDK",
                   encodedeky
                )
            )


            when (modelConfiguration) {
                KnetConfiguration.MapConfigruation -> {
                    val hashMapHeader = HashMap<String, Any>()
                    hashMapHeader[HeadersMdn] = headers?.mdn.toString()
                    hashMapHeader[HeadersApplication] = headers?.application.toString()
                    //val redirect = HashMap<String,Any>()
                    // redirect.put(urlKey, redirectValue)
                    configurationsAsHashMap?.put(headersKey, hashMapHeader)
                    //  configurationsAsHashMap?.put(redirectKey, redirect)


                }
                else -> {}
            }


        }
        private fun startWithSDKConfigs(context: Context,
                                        _publicKey: String?,
                                        tapRedirectViewWeb: TapCheckout?,
                                        tapMapConfiguration: java.util.HashMap<String, Any>,
                                        tapCheckoutStatusDelegate: TapCheckoutStatusDelegate? = null){
            with(tapMapConfiguration) {
                android.util.Log.e("map vals>>", tapMapConfiguration.toString())
               TapCheckoutDataConfiguration.configurationsAsHashMap = tapMapConfiguration
              /*  val operator = PayButtonDataConfiguration.configurationsAsHashMap?.get(
                    operatorKey
                ) as HashMap<*, *>*/
               // val publickKey = operator.get(publicKeyToGet)
                val publickKey = _publicKey

                val appLifecycleObserver = AppLifecycleObserver()
                androidx.lifecycle.ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

                addOperatorHeaderField(
                    tapRedirectViewWeb,
                    context,
                    KnetConfiguration.MapConfigruation,
                    publickKey.toString()
                )

                TapCheckoutDataConfiguration.addTapCheckOutStatusDelegate(tapCheckoutStatusDelegate)

                headers?.let { tapRedirectViewWeb?.init(tapMapConfiguration, it,publickKey) }

            }
        }
    }


}


