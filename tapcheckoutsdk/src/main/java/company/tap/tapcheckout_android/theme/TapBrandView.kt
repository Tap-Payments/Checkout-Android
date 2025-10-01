package company.tap.tapcheckout_android.theme

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
import company.tap.tapcheckout_android.R

import company.tap.taplocalizationkit.LocalizationManager

class TapBrandView : LinearLayout {

    val poweredByImage by lazy { findViewById<AppCompatImageView>(R.id.poweredByImage) }
    val outerConstraint by lazy { findViewById<ConstraintLayout>(R.id.outerConstraint) }
    val constraint by lazy { findViewById<CardView>(R.id.outerConstraint) }
    val backButtonLinearLayout by lazy { findViewById<LinearLayout>(R.id.back_btn_linear) }
    val imageBack by lazy { findViewById<ImageView>(R.id.image_back) }
    val backTitle by lazy { findViewById<TextView>(R.id.back_title) }

    @DrawableRes
    private val logoIcon: Int =
        if (ThemeManager.currentTheme.contains("dark")) {
            R.drawable.poweredbytap2
        } else {
            R.drawable.poweredbytap2
        }

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        inflate(context, R.layout.tap_brandview, this)
        poweredByImage.setImageResource(logoIcon)
        backTitle.text = resources.getString(R.string.back)

        val typeface = loadTypefaceFromLibrary(context)

        backTitle?.typeface = typeface
        if (LocalizationManager.getLocale(context).language == "ar") {
            imageBack.rotation = 180f
        }
    }

    private fun loadTypefaceFromLibrary(context: Context): Typeface? {
        return try {
            val fontPath = if (LocalizationManager.getLocale(context).language == "ar") {
                TapFont.tapFontType(TapFont.TajawalMedium)
            } else {
                TapFont.tapFontType(TapFont.RobotoRegular)
            }

            // ✅ Use class loader to get library context for font access
            val assetManager = this.javaClass.classLoader?.let {
                context.createPackageContext(context.packageName, 0).assets
            } ?: context.assets

            Typeface.createFromAsset(assetManager, fontPath)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
