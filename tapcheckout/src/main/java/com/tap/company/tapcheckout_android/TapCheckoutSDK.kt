package com.tap.company.tapcheckout_android

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class TapCheckoutSDK {
    companion object {
        private val retrofit = ApiService.RetrofitClient.getClient1()
        private val tapSDKConfigsUrl = retrofit.create(ApiService.TapButtonSDKConfigUrls::class.java)
        private var testEncKey: String? = null
        private var prodEncKey: String? = null
        var  payButonurlFormat:String?="https://button.dev.tap.company/?intentId=%@&publicKey=%@&mdn=%@&platform=mobile"
        private var  encodedeky: String? = null
        private var   headers: Headers? = null

        fun configureWithPayButtonDictionary(
            context: Context,
            publicKey: String?,
            intentId: String?,
            tapRedirectViewWeb: PayButton?,
            tapMapConfiguration: java.util.HashMap<String, Any>?,
            payButtonStatusDelegate: PayButtonStatusDelegate? = null

        ) {
//ToDO test when cdn url ready
            MainScope().launch {
                getTapButtonSDKConfigUrls(
                    tapMapConfiguration,
                    publicKey,
                    intentId,
                    tapRedirectViewWeb,
                    context,
                    payButtonStatusDelegate

                )
            }

        }

        private suspend fun getTapButtonSDKConfigUrls(tapMapConfiguration: HashMap<String, Any>?, publicKey: String?, intentId: String?, tapCardInputViewWeb: PayButton?, context: Context, payButtonStatusDelegate: PayButtonStatusDelegate?) {
            try {
                /**
                 * request to get Tap configs
                 */

                val tapButtonSDKConfigUrlResponse = tapSDKConfigsUrl.getButtonSDKConfigUrl()
                println("tapButtonSDKConfigUrlResponse>>>>"+tapButtonSDKConfigUrlResponse)

                BASE_URL_1 = tapButtonSDKConfigUrlResponse.baseURL
                prodEncKey = tapButtonSDKConfigUrlResponse.prodEncKey
                payButonurlFormat = tapButtonSDKConfigUrlResponse.payButtonUrlFormat


                println("payButonurlFormat is"+payButonurlFormat)
                //testEncKey = tapButtonSDKConfigUrlResponse.testEncKey
                testEncKey = context.resources.getString(R.string.enryptkeyTest)
                //  urlWebStarter = tapButtonSDKConfigUrlResponse.baseURL


                startWithSDKConfigs(
                    context,
                    publicKey ,
                    intentId,
                    tapCardInputViewWeb,
                    tapMapConfiguration,
                    payButtonStatusDelegate

                )

            } catch (e: Exception) {
                BASE_URL_1 = urlWebStarter //Todo what should be here
                testEncKey =  tapCardInputViewWeb?.context?.resources?.getString(R.string.enryptkeyTest)
                prodEncKey = tapCardInputViewWeb?.context?.resources?.getString(R.string.enryptkeyProduction)

                startWithSDKConfigs(
                    context,
                    publicKey,
                    intentId,
                    tapCardInputViewWeb,
                    tapMapConfiguration,
                    payButtonStatusDelegate

                )
                Log.e("error Config", e.message.toString())
                e.printStackTrace()
            }
        }

        @SuppressLint("RestrictedApi")
        fun addOperatorHeaderField(
            tapCardInputViewWeb: PayButton?,
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
                // context.packageName,  //TODO
                "demo.tap.PayButtonSDK",
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
                    // context.packageName.toString(), //TODO remove hardcoding
                    "demo.tap.PayButtonSDK",
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
                                        intentId:String?,
                                        tapRedirectViewWeb: PayButton?,
                                        tapMapConfiguration: java.util.HashMap<String, Any>?,
                                        payButtonStatusDelegate: PayButtonStatusDelegate? = null){
            with(tapMapConfiguration) {
                android.util.Log.e("map vals>>", tapMapConfiguration.toString())
                PayButtonDataConfiguration.configurationsAsHashMap = tapMapConfiguration
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

                PayButtonDataConfiguration.addTapBenefitPayStatusDelegate(payButtonStatusDelegate)

                headers?.let { tapRedirectViewWeb?.init(tapMapConfiguration, it,intentId,publickKey) }

            }
        }
    }
}