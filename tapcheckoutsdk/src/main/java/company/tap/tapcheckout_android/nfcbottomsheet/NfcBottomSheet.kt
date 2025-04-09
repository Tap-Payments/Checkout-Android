package company.tap.tapcheckout_android.nfcbottomsheet

import TapLocal
import TapTheme
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import company.tap.tapcheckout_android.R
import company.tap.tapcheckout_android.TapBrandView
import company.tap.tapcheckout_android.TapCheckout
import company.tap.tapcheckout_android.doAfterSpecificTime



class NfcBottomSheet : BottomSheetDialogFragment() {

    private lateinit var  mShimmerViewContainer :LottieAnimationView
    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.nfc_bottom_sheet, null)
        mShimmerViewContainer =  view.findViewById(R.id.shimmer_view)
        loadLottie()
        return view
    }

    fun loadLottie() {
        when(TapCheckout.languageThemePair.first){
            TapLocal.en.name->{
                when(TapCheckout.languageThemePair.second){
                    TapTheme.light.name ->setAnimationUrl("https://tap-assets.b-cdn.net/card-sdk/nfc/nfc_light_en.json")
                    TapTheme.dark.name->setAnimationUrl(
                        "https://tap-assets.b-cdn.net/card-sdk/nfc/nfc_dark_en.json"
                    )
                    else -> {

                    }
                }
            }
            TapLocal.ar.name->{
                when(TapCheckout.languageThemePair.second){
                    TapTheme.light.name ->setAnimationUrl(
                        "https://tap-assets.b-cdn.net/card-sdk/nfc/nfc_light_ar.json"
                    )
                    TapTheme.dark.name->setAnimationUrl(
                        "https://tap-assets.b-cdn.net/card-sdk/nfc/nfc_dark_ar.json"
                    )
                    else -> {}
                }
            }
        }

    }
    fun setAnimationUrl(url: String){
        mShimmerViewContainer?.setAnimationFromUrl(url)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tapBrandView = view.findViewById<TapBrandView>(R.id.tab_brand_view_nfc)

        tapBrandView.backButtonLinearLayout.setOnClickListener {
            this.dismiss()
            requireActivity().finish()

        }
        this.dialog?.setOnDismissListener {
            doAfterSpecificTime(time = 500) {
                requireActivity().finish()
            }
        }


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimations
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogFragment)

    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogFragment




}