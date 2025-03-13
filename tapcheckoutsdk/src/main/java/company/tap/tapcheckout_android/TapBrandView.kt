package company.tap.tapcheckout_android
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout


import company.tap.taplocalizationkit.LocalizationManager
import company.tap.tapuilibrary.fontskit.enums.TapFont
import company.tap.tapuilibrary.themekit.ThemeManager

class TapBrandView : LinearLayout {

    val poweredByImage by lazy { findViewById<AppCompatImageView>(company.tap.tapuilibrary.R.id.poweredByImage) }
    val outerConstraint by lazy { findViewById<ConstraintLayout>(company.tap.tapuilibrary.R.id.outerConstraint) }
    val constraint by lazy { findViewById<CardView>(company.tap.tapuilibrary.R.id.outerConstraint) }
    val backButtonLinearLayout by lazy { findViewById<LinearLayout>(company.tap.tapuilibrary.R.id.back_btn_linear) }
    val imageBack by lazy { findViewById<ImageView>(company.tap.tapuilibrary.R.id.image_back) }
    val backTitle by lazy { findViewById<TextView>(company.tap.tapuilibrary.R.id.back_title) }

    @DrawableRes
    val logoIcon: Int =
        if (ThemeManager.currentTheme.isNotEmpty() && ThemeManager.currentTheme.contains("dark")) {
            company.tap.tapuilibrary.R.drawable.poweredbytap2
        } else if (ThemeManager.currentTheme.isNotEmpty() && ThemeManager.currentTheme.contains("light")) {
            company.tap.tapuilibrary.R.drawable.poweredbytap2
        } else company.tap.tapuilibrary.R.drawable.poweredbytap2


    /**
     * Simple constructor to use when creating a TapHeader from code.
     *  @param con] ext The Context the view is running in, through which it can
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
        inflate(context, R.layout.tap_brandview, this)
        poweredByImage.setImageResource(logoIcon)
        backTitle.text = resources.getString(company.tap.tapuilibrary.R.string.back)

        // backTitle.setTextColor(loadAppThemManagerFromPath(AppColorTheme.PoweredByTapBackButtonLabelColor))
        //imageBack.backgroundTintList =
        //  ColorStateList.valueOf(loadAppThemManagerFromPath(AppColorTheme.PoweredByTapBackButtonIconColor))



        if (LocalizationManager.getLocale(context).language == "ar") {
            imageBack.rotation = 180f
            backTitle?.typeface = Typeface.createFromAsset(
                context?.assets, TapFont.tapFontType(
                    TapFont.TajawalMedium
                )
            )
        }else{
            backTitle?.typeface = Typeface.createFromAsset(
                context?.assets, TapFont.tapFontType(
                    TapFont.RobotoRegular
                )
            )
        }
    }



}