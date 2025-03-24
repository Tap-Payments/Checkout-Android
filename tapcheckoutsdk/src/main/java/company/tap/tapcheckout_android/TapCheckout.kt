package company.tap.tapcheckout_android


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.os.postDelayed

import com.google.gson.Gson
import company.tap.tapcheckout_android.ApiService.BASE_URL_1
import company.tap.tapcheckout_android.CheckoutConfiguration.Companion.payButonurlFormat
import company.tap.tapcheckout_android.enums.SCHEMES
import company.tap.tapcheckout_android.enums.TapCheckoutDelegates
import company.tap.tapcheckout_android.enums.ThreeDsPayButtonType
import company.tap.tapcheckout_android.enums.careemPayUrlHandler
import company.tap.tapcheckout_android.enums.keyValueName
import company.tap.tapcheckout_android.enums.operatorKey
import company.tap.tapcheckout_android.enums.publicKeyToGet
import company.tap.tapcheckout_android.models.ThreeDsResponse
import company.tap.tapcheckout_android.models.ThreeDsResponseCardPayButtons
import company.tap.tapcheckout_android.popup_window.WebChrome
import company.tap.tapcheckout_android.threeDsWebview.ThreeDsWebViewActivityButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URISyntaxException
import java.util.*
import kotlin.collections.HashMap


@SuppressLint("ViewConstructor")
class TapCheckout : LinearLayout , ApplicationLifecycle {
    lateinit var webviewStarterUrl: String
    private var isBenefitPayUrlIntercepted =false
    // lateinit var webViewScheme: String
    var webViewScheme: String = "tapcheckoutsdk://"
    private lateinit var webChrome: WebChrome

    lateinit var webViewFrame: FrameLayout
    lateinit var urlToBeloaded: String
    var firstTimeOnReadyCallback = true
    lateinit var linearLayout: LinearLayout
    lateinit var dialog: Dialog
    lateinit var redirectConfiguration: java.util.HashMap<String, Any>
    lateinit var headersVal: Headers
    lateinit var publickKeyVal: String
    lateinit var intentVal: String
    var iSAppInForeground = true
    var onSuccessCalled = false
    var pair =  Pair("",false)

    companion object {
        lateinit var threeDsResponse: ThreeDsResponse
        lateinit var threeDsResponseCardPayButtons: ThreeDsResponseCardPayButtons

        private lateinit var redirectWebView: WebView

        lateinit var buttonTypeConfigured: ThreeDsPayButtonType
        fun cancel() {
            redirectWebView.loadUrl("javascript:window.cancel()")
        }

        fun generateTapAuthenticate(authIdPayerUrl: String) {
            redirectWebView.loadUrl("javascript:window.loadAuthentication('$authIdPayerUrl')")
        }

        fun retrieve(value: String) {
            redirectWebView.loadUrl("javascript:window.retrieve('$value')")
        }


    }

    /**
     * Simple constructor to use when creating a TapPayCardSwitch from code.
     *  @param context The Context the view is running in, through which it can
     *  access the current theme, resources, etc.
     **/
    constructor(context: Context) : super(context)

    /**
     *  @param context The Context the view is running in, through which it can
     *  access the current theme, resources, etc.
     *  @param attrs The attributes of the XML Button tag being used to inflate the view.
     *
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)


    init {
        LayoutInflater.from(context).inflate(R.layout.activity_button_web_wrapper, this)
        initWebView()

    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        redirectWebView = findViewById(R.id.webview)
        webViewFrame = findViewById(R.id.webViewFrame)


        with(redirectWebView) {

            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                allowContentAccess = true
                setSupportMultipleWindows(true)
                cacheMode = WebSettings.LOAD_NO_CACHE
                useWideViewPort = true
                loadWithOverviewMode = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

            }
        }
        redirectWebView.setBackgroundColor(Color.TRANSPARENT)
        redirectWebView.setLayerType(LAYER_TYPE_SOFTWARE, null)
        webChrome = WebChrome(context)
        redirectWebView.webChromeClient = webChrome
        redirectWebView.webViewClient = MyWebViewClient()
        redirectWebView.clearCache(true)
        redirectWebView.clearHistory()



    }



    fun init(configuraton: java.util.HashMap<String, Any>, headers: Headers, _publickey:String?) {

        if (configuraton != null) {
            redirectConfiguration = configuraton
        }
        headersVal = Headers(headers.mdn,headers.application)

        if (_publickey != null) {
            publickKeyVal = _publickey
        }
        //  initializePaymentData(buttonType)
        /**
         * Check for data in configuration has operator and intent id
         * else sends error
         * */


       // val operator = configuraton?.get(operatorKey) as HashMap<*, *>
       // val publickKey = operator.get(publicKeyToGet)
        val publickKey = _publickey

        if ( publickKey.toString().isNullOrBlank()) {
            TapCheckoutDataConfiguration.getTapCheckoutListener()
                ?.onCheckoutError("public key and intent id are required")
        }else {
            callConfigAPI(configuraton)
        }







        when (configuraton) {

            // KnetConfiguration.MapConfigruation -> {

            /* urlToBeloaded =
                    "${webviewStarterUrl}${encodeConfigurationMapToUrl(KnetDataConfiguration.configurationsAsHashMap)}"*/
            // knetWebView.loadUrl(urlToBeloaded)
            // }


        }
        //    Log.e("urlToBeloaded",urlToBeloaded)

    }


    private fun applySchemes(scheme: SCHEMES) {
        webviewStarterUrl = scheme.value.first
        webViewScheme = scheme.value.second
    }


    inner class MyWebViewClient : WebViewClient() {


        @RequiresApi(Build.VERSION_CODES.O)
        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            request: WebResourceRequest?
        ): Boolean {

            /**
             * main checker if url start with "tapCardWebSDK://"
             */
            Log.e("url Here>>>>", request?.url.toString())


            if (request?.url.toString().startsWith(careemPayUrlHandler)) {
                webViewFrame.layoutParams =
                    LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                threeDsResponse = ThreeDsResponse(
                    id = "",
                    url = request?.url.toString(),
                    powered = true,
                    stopRedirection = false
                )
                navigateTo3dsActivity(PaymentFlow.PAYMENTBUTTON.name)
                return true
            } else {
                if (request?.url.toString().startsWith(webViewScheme, ignoreCase = true)) {
                    if (request?.url.toString().contains(TapCheckoutDelegates.onReady.name)) {


                        /* if (buttonTypeConfigured == ThreeDsPayButtonType.CARD) {
                             if (firstTimeOnReadyCallback) {
                                 Thread.sleep(1500)
                                 firstTimeOnReadyCallback = false
                             }
                             *//**
                         *
                         *  todo enhance in a better way
                         *//*


                        }*/


                        TapCheckoutDataConfiguration.getTapCheckoutListener()?.onCheckoutReady()

                    }
                    if (request?.url.toString().contains(TapCheckoutDelegates.onSuccess.name)) {
                        onSuccessCalled = true
                        var datafromUrl = request?.url?.getQueryParameter(keyValueName).toString()
                        println("datafromUrl>>"+datafromUrl)
                        var decoded = decodeBase64(datafromUrl)
                        println("decoded>>"+decoded)
                        if (decoded != null) {
                            TapCheckoutDataConfiguration.getTapCheckoutListener()?.onCheckoutSuccess(
                                decoded
                            )

                        }
                        pair = Pair(request?.url?.getQueryParameterFromUri(keyValueName).toString(),true)

                        when(iSAppInForeground) {

                            true ->{//closePayment()
                                dismissDialog()
                                Log.e("success","one")
                            }
                            false ->{}
                        }
                    }
                    if (request?.url.toString().contains(TapCheckoutDelegates.onChargeCreated.name)) {

                        val data = decodeBase64(request?.url?.getQueryParameter(keyValueName).toString())
                        Log.e("chargedData", data.toString())
                        val jsonObject = JSONObject(data);
                        var jsonObject1 = JSONObject()
                        if(jsonObject.has("gateway_response")){
                            jsonObject1 = jsonObject.getJSONObject("gateway_response")
                            // println("jsonObject1"+jsonObject1.get("name"))
                        }
                        val gson = Gson()
                        /**Check added for benefitpay ***/
                        if(jsonObject1!=null && jsonObject1.has("name") &&jsonObject1.get("name").toString().equals("BENEFITPAY")){

                        }else {
                            threeDsResponse = gson.fromJson(data, ThreeDsResponse::class.java)
                            when (threeDsResponse.stopRedirection) {
                                false -> navigateTo3dsActivity(PaymentFlow.PAYMENTBUTTON.name)
                                else -> {}
                            }
                        }
                        TapCheckoutDataConfiguration.getTapCheckoutListener()?.onCheckoutChargeCreated(
                            request?.url?.getQueryParameterFromUri(keyValueName).toString()
                        )
                    }
                    if (request?.url.toString().contains(TapCheckoutDelegates.onOrderCreated.name)) {
                        val orderResponse = request?.url?.getQueryParameter(keyValueName).toString()
                        println("orderResponse>>"+orderResponse)
                        //TODO check if decode required
                        TapCheckoutDataConfiguration.getTapCheckoutListener()
                            ?.onCheckoutOrderCreated(
                                orderResponse
                            )



                    }
                    if (request?.url.toString().contains(TapCheckoutDelegates.onClick.name)) {
                        isBenefitPayUrlIntercepted=false
                        onSuccessCalled = false
                        pair = Pair("",false)
                        TapCheckoutDataConfiguration.getTapCheckoutListener()?.onCheckoutClick()

                    }
                    if (request?.url.toString().contains(TapCheckoutDelegates.cancel.name)) {

                        TapCheckoutDataConfiguration.getTapCheckoutListener()?.onCheckoutcancel()



                    }
                    if (request?.url.toString().contains(TapCheckoutDelegates.onCancel.name)) {
                        android.os.Handler(Looper.getMainLooper()).postDelayed(3000) {
                            if(!onSuccessCalled){
                                TapCheckoutDataConfiguration.getTapCheckoutListener()?.onCheckoutcancel()
                            }


                        }

                        if (!(pair.first.isNotEmpty() and pair.second)) {
                            dismissDialog()
                        }

                    }
                    if (request?.url.toString()
                            .contains(TapCheckoutDelegates.onBinIdentification.name)
                    ) {
                       /* RedirectDataConfiguration.getTapCheckoutListener()
                            ?.onPayButtonBindIdentification(
                                request?.url?.getQueryParameterFromUri(keyValueName).toString()
                            )*/
                    }
                    if (request?.url.toString().contains(TapCheckoutDelegates.onHeightChange.name)) {
                        val newHeight = request?.url?.getQueryParameter(keyValueName)
                        val params: ViewGroup.LayoutParams? = webViewFrame.layoutParams
                        params?.height =
                            webViewFrame.context.getDimensionsInDp(newHeight?.toInt() ?: 95)
                        webViewFrame.layoutParams = params

                       /* RedirectDataConfiguration.getTapCheckoutListener()
                            ?.onPayButtonHeightChange(newHeight.toString())*/

                    }
                    if (request?.url.toString().contains(TapCheckoutDelegates.on3dsRedirect.name)) {
                        /**
                         * navigate to 3ds Activity
                         */
                        val queryParams =
                            request?.url?.getQueryParameterFromUri(keyValueName).toString()
                        Log.e("data card", queryParams.toString())

                        threeDsResponseCardPayButtons = queryParams.getModelFromJson()
                        navigateTo3dsActivity(PaymentFlow.CARDPAY.name)
                        Log.e("data card", threeDsResponseCardPayButtons.toString())


                    }
                    /**
                     * for google button specifically
                     */
                    if (request?.url.toString().contains(TapCheckoutDelegates.onClosePopup.name)) {
                        webChrome.getdialog()?.dismiss()

                    }

                    /* if (request?.url.toString().contains(KnetStatusDelegate.onError.name)) {

                         RedirectDataConfiguration.getTapCheckoutListener()
                             ?.onPayButtonError(
                                 request?.url?.getQueryParameterFromUri(keyValueName).toString()
                             )
                     }*/
                    if (request?.url.toString().contains(TapCheckoutDelegates.onError.name)) {
                        decodeBase64(request?.url?.getQueryParameter(keyValueName).toString())?.let {
                            TapCheckoutDataConfiguration.getTapCheckoutListener()
                                ?.onCheckoutError(
                                    it
                                )
                        }
                        pair = Pair(request?.url?.getQueryParameterFromUri(keyValueName).toString(),true)

                    }
                    if (request?.url.toString().startsWith("intent://")) {
                        try {
                            val context: Context = context
                            val intent: Intent = Intent.parseUri(request?.url.toString(), Intent.URI_INTENT_SCHEME)
                            if (intent != null) {
//                            view.stopLoading()
                                val packageManager: PackageManager = context.packageManager
                                val info: ResolveInfo? = packageManager.resolveActivity(
                                    intent,
                                    PackageManager.MATCH_DEFAULT_ONLY
                                )
                                if (info != null) {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent)
                                } else {
                                    return false
                                }
                                return true
                            }
                        } catch (e: URISyntaxException) {
                            Log.e("error", "Can't resolve intent://", e)

                        }
                        //   progressBar.visibility = GONE
                    }
                    if (request?.url.toString().startsWith("intent://")) {
                        try {
                            val context: Context = context
                            val intent: Intent = Intent.parseUri(request?.url.toString(), Intent.URI_INTENT_SCHEME)
                            if (intent != null) {
//                            view.stopLoading()
                                val packageManager: PackageManager = context.packageManager
                                val info: ResolveInfo? = packageManager.resolveActivity(
                                    intent,
                                    PackageManager.MATCH_DEFAULT_ONLY
                                )
                                if (info != null) {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent)
                                } else {
                                    return false
                                }
                                return true
                            }
                        } catch (e: URISyntaxException) {
                            Log.e("error", "Can't resolve intent://", e)

                        }
                        //   progressBar.visibility = GONE
                    }

                    return true
                }

                else {

                    return false
                }
            }
        }




        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)


        }

        fun navigateTo3dsActivity(paymentbutton: String) {
            val intent = Intent(context, ThreeDsWebViewActivityButton()::class.java)
            ThreeDsWebViewActivityButton.tapCheckout = this@TapCheckout
            intent.putExtra("flow", paymentbutton)
            (context).startActivity(intent)
        }


        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            Log.e("intercepted",request?.url.toString())

            when(request?.url?.toString()?.contains("https://benefit-checkout")?.and((!isBenefitPayUrlIntercepted))) {

                true ->{
                    view?.post{
                        (webViewFrame as ViewGroup).removeView(redirectWebView)


                        dialog= Dialog(context,android.R.style.Theme_Translucent_NoTitleBar)
                        //Create LinearLayout Dynamically
                        linearLayout = LinearLayout(context)
                        //Setup Layout Attributes
                        val params = LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        linearLayout.layoutParams = params
                        linearLayout.orientation = VERTICAL

                        /**
                         * onBackPressed in Dialog
                         */
                        dialog.setOnKeyListener { view, keyCode, keyEvent ->
                            if (keyEvent.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                                dismissDialog()
                                init(redirectConfiguration,headersVal ,publickKeyVal)
                                return@setOnKeyListener  true
                            }
                            return@setOnKeyListener false
                        }


                        if (redirectWebView.parent == null){
                            linearLayout.addView(redirectWebView)
                        }

                        dialog.setContentView(linearLayout)
                        dialog.show()
                    }

                    isBenefitPayUrlIntercepted = true
                }
                else -> {}
            }

            return super.shouldInterceptRequest(view, request)
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            Log.e("error code",error.errorCode.toString())
            Log.e("error description ",error.description.toString())

            Log.e("request header ",request.requestHeaders.toString())
            super.onReceivedError(view, request, error)

        }
    }


    override fun onDetachedFromWindow() {
        redirectWebView.destroy()
        super.onDetachedFromWindow()
    }



    fun toBase64(value: String?): String? {
        var value = value
        if (value == null) value = ""
        return Base64.encodeToString(value.trim { it <= ' ' }.toByteArray(), Base64.DEFAULT)
    }
    fun decodeBase64(base64String: String): String? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            String(decodedBytes, Charsets.UTF_8) // Convert bytes to string using UTF-8
        } catch (e: IllegalArgumentException) {
            println("Invalid Base64 input: ${e.message}")
            null
        }
    }
    private fun dismissDialog() {
        if (::dialog.isInitialized) {
            linearLayout.removeView(redirectWebView)
            dialog.dismiss()
            if (redirectWebView.parent == null){
                (webViewFrame as ViewGroup).addView(redirectWebView)
            }
        }
    }

    private fun closePayment() {

        if (pair.second) {
            Log.e("app","one")
            dismissDialog()

            TapCheckoutDataConfiguration.getTapCheckoutListener()?.onCheckoutSuccess(pair.first)

        }
    }
    override fun onEnterForeground() {
        iSAppInForeground = true
        Log.e("applifeCycle","onEnterForeground")
        //  closePayment()





    }
    override fun onEnterBackground() {
        iSAppInForeground = false
        Log.e("applifeCycle","onEnterBackground")

    }

    private fun callConfigAPI(configuraton: java.util.HashMap<String, Any>) {
        try {
            val baseURL = "https://mw-sdk.staging.tap.company/v2/checkout/config"
            val builder: OkHttpClient.Builder = OkHttpClient().newBuilder()
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            builder.addInterceptor(interceptor)

            val body = (configuraton as Map<*, *>?)?.let { JSONObject(it).toString().toRequestBody("application/json".toMediaTypeOrNull()) }
            val okHttpClient: OkHttpClient = builder.build()
            val request: Request = Request.Builder()
                .url(baseURL )
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("application", headersVal.application.toString())
                .addHeader("mdn", headersVal.mdn.toString())
                .build()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        var responseBody: JSONObject? =
                            response.body?.string()?.let { JSONObject(it) } // toString() is not the response body, it is a debug representation of the response body

                        if(!responseBody.toString().contains("errors")){
                            var redirectURL = responseBody?.getString("redirect_url")
                            if (redirectURL != null) {
                                // knetWebView.loadUrl(redirectURL)
                                urlToBeloaded = redirectURL
                                Handler(Looper.getMainLooper()).post {
                                  // redirectWebView.loadUrl(redirectURL)
                                   redirectWebView.loadUrl("https://tap-checkout-wrapper.netlify.app/?platform=mobile&themeMode=dark&language=en&token=eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjY3ZGZmZWIyY2NlOTRjMDE2MjhlNDczNiJ9.O442WVCwjpJElD3RlhtfITUBtbIzEpB7YDJB4Ad5Cjg")
                                   // redirectWebView.loadUrl("https://checkout.staging.tap.company/v2/?mode=page&themeMode=dark&language=en&token=eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjY3ZDY3ZWQxMThjNzNkNDhhNjI2MzkzNCJ9.BMfm_TNmEol-K6vhUTkTEad6DftTZJzQJqgISzpxn4c")
                                    //redirectWebView.loadUrl("https://www.google.com/");



                                }
                            }
                        }else{


                        }

                    } catch (ex: JSONException) {
                        throw RuntimeException(ex)
                    } catch (ex: IOException) {
                        throw RuntimeException(ex)
                    }

                }

                override fun onFailure(call: Call, e: IOException) {}
            })
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}
enum class KnetConfiguration() {
    MapConfigruation
}

enum class PaymentFlow {
    CARDPAY, PAYMENTBUTTON
}




