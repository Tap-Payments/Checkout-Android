package company.tap.tapcheckout_android.popup_window

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Message
import android.util.Log
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import company.tap.tapcheckout_android.R


import company.tap.tapcheckout_android.doAfterSpecificTime

const val dismissDialogTime = 3500L
class WebChrome(var context: Context) :WebChromeClient(){
    private  var dialog: Dialog?=null
    private var working_dialog: ProgressDialog? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {

        val newWebView = WebView(context)
        with(newWebView.settings){
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically=true

        }
        with(newWebView){
            requestFocus(LinearLayout.FOCUS_DOWN)
            requestFocusFromTouch()
            isFocusableInTouchMode = true
        }




        val wrapper = LinearLayout(view?.context)
        val keyboardHack = EditText(view?.context)
        keyboardHack.visibility = LinearLayout.GONE
        wrapper.orientation = LinearLayout.VERTICAL
        wrapper.addView(newWebView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        wrapper.addView(keyboardHack, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val alert = AlertDialog.Builder(view?.context)
        val transports = resultMsg?.obj as WebView.WebViewTransport

        alert.setView(wrapper)
        alert.setNegativeButton(context.resources.getString(R.string.exit)) { dialogInterface, i ->
            showWorkingDialog()
            destroyNewWebView(wrapper, newWebView)
            doAfterSpecificTime(time = dismissDialogTime){
                removeWorkingDialog()
            }

        }

        if (dialog ==null){
            dialog = alert.create()
            dialog?.setCanceledOnTouchOutside(false)
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog?.show()
            dialog?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        }
        dialog?.setOnDismissListener {
            dialog = null
        }

        // val transports = resultMsg?.obj as WebView.WebViewTransport
        transports.webView = newWebView
        resultMsg.sendToTarget()
        newWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }


            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                Log.e("intercepted webchrom",request?.url.toString())
                return super.shouldInterceptRequest(view, request)
            }
        }


        newWebView.webChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(window: WebView?) {
                window?.destroy()
                super.onCloseWindow(window)
            }
        }


        return true
    }

    private fun destroyNewWebView(wrapper: LinearLayout, newWebView: WebView) {
        wrapper.removeAllViews()
        newWebView.removeAllViews()
        newWebView.destroy()
        newWebView.clearHistory()
        dialog?.dismiss()
    }

    fun getdialog() = dialog

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        Log.e("javascript", consoleMessage?.message().toString())
        return true
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        return super.onJsAlert(view, url, message, result)
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        return super.onJsConfirm(view, url, message, result)
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        return super.onJsPrompt(view, url, message, defaultValue, result)
    }

    override fun onJsBeforeUnload(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        return super.onJsBeforeUnload(view, url, message, result)
    }

    override fun onCloseWindow(window: WebView?) {
        try {
            Log.e("closedWindow","closedWindow")
            window?.destroy()
        } catch (e: Exception) {
            Log.d("Destroyed with Error ", e.stackTrace.toString())
        }
        super.onCloseWindow(window)

    }
    private fun showWorkingDialog() {
        working_dialog = ProgressDialog.show(context, "", context.resources.getString(R.string.dismissing), true)
    }
    private fun removeWorkingDialog() {
        if (working_dialog != null) {
            working_dialog?.dismiss()
            working_dialog = null
        }
    }

}
