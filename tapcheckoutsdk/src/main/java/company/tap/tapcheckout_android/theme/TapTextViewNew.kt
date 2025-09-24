package company.tap.tapcheckout_android.theme

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView


/**
 * TapTextView is user interface element that displays text to the user.
 * Construct a new Switch with default styling, overriding specific style
 * attributes as requested.
 * @param context The Context that will determine this widget's theming.
 * @param attributeSet Specification of attributes that should deviate from default styling.
 **/
open class TapTextViewNew(context: Context, attributeSet: AttributeSet?) :
    AppCompatTextView(context, attributeSet),
    TapView<TextViewTheme> {

    /**
     * Set the configure the current theme. If null is provided then the default Theme is returned
     * on the next call
     * @param theme Theme to consume in the wrapper, a value of null resets the theme to the default
     **/
    override fun setTheme(theme: TextViewTheme) {
        this.includeFontPadding = false
        theme.textColor?.let { setTextColor(it) }
        theme.textSize?.let { textSize = it.toFloat() }
        theme.backgroundColor?.let { setBackgroundColor(it) }
        invalidate()
    }
}