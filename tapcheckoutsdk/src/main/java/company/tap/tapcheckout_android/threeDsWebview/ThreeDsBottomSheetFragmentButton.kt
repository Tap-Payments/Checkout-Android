package company.tap.tapcheckout_android.threeDsWebview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.annotation.Nullable

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import company.tap.tapcheckout_android.R
import company.tap.tapcheckout_android.doAfterSpecificTime
import company.tap.tapcheckout_android.getDeviceSpecs

import company.tap.tapcheckout_android.TapCheckout
import company.tap.tapcheckout_android.theme.TapBrandView


class ThreeDsBottomSheetFragmentButton(var webView: WebView?, var onCancel:()->Unit): BottomSheetDialogFragment() {

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog_button, null)
        val linearLayout= view.findViewById<LinearLayout>(R.id.webLinear)
        linearLayout.addView(webView)
        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tapBrandView = view.findViewById<TapBrandView>(R.id.tab_brand_view)

        try {
            val powerd  = TapCheckout.threeDsResponse.powered
            when(powerd){
                false ->tapBrandView.poweredByImage.visibility = View.INVISIBLE
                else -> {}
            }
        }catch (e:java.lang.Exception){
            Log.e("excption",e.toString())
        }
        ( dialog as BottomSheetDialog).behavior.isFitToContents = true
        ( dialog as BottomSheetDialog).behavior.peekHeight = (context?.getDeviceSpecs()?.first?: 950) - 250



        this.dialog?.setOnDismissListener {
            if (isAdded){
                doAfterSpecificTime {
                    requireActivity().finish()
                }
            }

        }
        isCancelable = false
        tapBrandView.backButtonLinearLayout.setOnClickListener {
            this.dialog?.dismiss()
            webView?.loadUrl("javascript:window.cancel()")
            onCancel.invoke()

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimations
        setStyle(STYLE_NORMAL,R.style.CustomBottomSheetDialogFragment)

    }


    override fun getTheme(): Int = R.style.CustomBottomSheetDialogFragment




}