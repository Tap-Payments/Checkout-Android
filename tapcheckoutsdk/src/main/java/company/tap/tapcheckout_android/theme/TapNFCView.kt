package company.tap.tapcheckout_android.theme

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import company.tap.tapcheckout_android.R
import company.tap.taplocalizationkit.LocalizationManager


/**
 * Created by AhlaamK on 7/1/20.

Copyright (c) 2020    Tap Payments.
All rights reserved.
 **/

/**
 * TapNFCView is a molecule UI element for showing user till NFC scanning
 * is running .
 **/
class TapNFCView : LinearLayout {
    private var gifNFC: ImageView
    val topLinearNFC by lazy { findViewById<LinearLayout>(R.id.topLinearNFC) }
    val scanNfc by lazy { findViewById<TapTextViewNew>(R.id.scan_nfc) }
    val mainLinearNFC by lazy { findViewById<LinearLayout>(R.id.mainLinearNFC) }
    val aboutNFC by lazy { findViewById<TapTextViewNew>(R.id.aboutNFC) }


    /**
     * Simple constructor to use when creating a TapNFCView from code.
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

    /**
     *  @param context The Context the view is running in, through which it can
     *  access the current theme, resources, etc.
     *  @param attrs The attributes of the XML Button tag being used to inflate the view.
     * @param defStyleAttr The resource identifier of an attribute in the current theme
     * whose value is the the resource id of a style. The specified style’s
     * attribute values serve as default values for the button. Set this parameter
     * to 0 to avoid use of default values.
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        inflate(context, R.layout.tap_nfc_view, this)
        gifNFC = findViewById(R.id.gif_nfc)
        setNFCGif()
        if (context?.let { LocalizationManager.getLocale(it).language } == "en") setFontsEnglish() else setFontsArabic()
        setTheme()
        setTextData()
    }

    private fun setTextData(){
        scanNfc.text = LocalizationManager.getValue(
                    "scan",
                    "NFC"
                )
        aboutNFC.text = LocalizationManager.getValue(
                    "nfcDescription",
                    "NFC"
                )


    }

    fun setFontsEnglish(){
        scanNfc.typeface = Typeface.createFromAsset(
            context?.assets, TapFont.tapFontType(
                TapFont.RobotoLight
            )
        )
        aboutNFC.typeface = Typeface.createFromAsset(
            context?.assets, TapFont.tapFontType(
                TapFont.RobotoLight
            )
        )
    }

    fun setFontsArabic(){
        scanNfc.typeface = Typeface.createFromAsset(
            context?.assets, TapFont.tapFontType(
                TapFont.TajawalLight
            )
        )
        aboutNFC.typeface = Typeface.createFromAsset(
            context?.assets, TapFont.tapFontType(
                TapFont.TajawalLight
            )
        )
    }
    fun setTheme(){
        if (ThemeManager.currentTheme.isNotEmpty() && ThemeManager.currentTheme.contains("dark")) {
            mainLinearNFC.setBackgroundResource(R.drawable.blur_background_dark)
        } else {
            mainLinearNFC.setBackgroundResource(R.drawable.blurbackground)
        }
        topLinearNFC.setBackgroundColor(Color.parseColor(ThemeManager.getValue("Nfc.topTextBackgroundColor")))
        scanNfc.setTextColor(Color.parseColor(ThemeManager.getValue("Nfc.topTextColor")))
        aboutNFC.setTextColor(Color.parseColor(ThemeManager.getValue("Nfc.bottomTextColor")))
    }
    private fun setNFCGif(){
        println("ThemeManager.currentTheme are"+ ThemeManager.currentTheme)
        if (ThemeManager.currentTheme.isNotEmpty() && ThemeManager.currentTheme.contains("dark")) {
            Glide.with(context).load("https://tap-assets.b-cdn.net/card-sdk/nfc/nfcgif_dark.gif").into(gifNFC)
        } else {
            Glide.with(context).load("https://tap-assets.b-cdn.net/card-sdk/nfc/nfcgif_light.gif").into(gifNFC)
        }
    }


}