package company.tap.tapcheckout_android

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.os.postDelayed
import com.bumptech.glide.Glide

import com.google.gson.Gson
import company.tap.tapcheckout_android.enums.rawFolderRefrence
import company.tap.tapcheckout_android.models.ThreeDsResponseCardPayButtons


import company.tap.tapuilibrary.themekit.ThemeManager
import jp.wasabeef.blurry.Blurry
import java.net.URLEncoder
import java.util.*
fun View.applyBluryToView(
    radiusNeeded: Int = 8,
    sampling: Int = 2,
    animationDuration: Int = 1000,
    showOriginalView: Boolean = false
) {

//    (this as ViewGroup).children.forEachIndexed { index, view ->
//        if (index != 0) removeView(view)
//    }

    Blurry.with(context).radius(radiusNeeded).sampling(sampling).animate(animationDuration)
        .onto(this as ViewGroup).apply {
            when (showOriginalView) {
                true -> this@applyBluryToView.getChildAt(0).visibility = View.VISIBLE
                false -> this@applyBluryToView.getChildAt(0).visibility = View.GONE
            }

        }


}


fun Context.px(@DimenRes dimen: Int): Int = resources.getDimension(dimen).toInt()

fun Context.dp(@DimenRes dimen: Int): Float = px(dimen) / resources.displayMetrics.density

fun Context.getDimensionsInDp(dimension: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dimension.toFloat(),
        this.resources?.displayMetrics
    ).toInt()

}

fun Context.getAssetFile(filename: String): Int {
    Log.e("packagename", this.packageName.toString())
    return resources.getIdentifier(
        filename,
        rawFolderRefrence,
        this.packageName
    )
}

fun String.getModelFromJson(): ThreeDsResponseCardPayButtons {
    return Gson().fromJson(this, ThreeDsResponseCardPayButtons::class.java)
}

fun getRandomNumbers(length: Int): String {
//    val allowedChars = ('A'..'Z') + ('0'..'9')
    val allowedChars = ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }.shuffled()
        .joinToString("")
}

fun getRandomTrx(length: Int=23): String {

    val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val prefix = "tck_LV"
    return prefix+(1..length)
        .map { letters.random() }.shuffled()
        .joinToString("")
}
fun <T> List<T>?.jointToStringForUrl(): String? {
    return this?.joinToString()?.replace(", ", "%22%2C%22")
}

/**
 * function to get query data and decode it
 */
@RequiresApi(Build.VERSION_CODES.O)
fun Uri.getQueryParameterFromUri(keyValue: String): String {
    val decodedBytes = String(Base64.getDecoder().decode(this.getQueryParameter(keyValue).toString()))

    return decodedBytes
}


 fun encodeConfigurationMapToUrl(configuraton: HashMap<String,Any>?): String? {
    val gson = Gson()
    val json: String = gson.toJson(configuraton)
    val encodedUrl = URLEncoder.encode(json, "UTF-8")
    return encodedUrl

}


fun ViewGroup.addLoaderWithBlurryToView(showLoadingIcon: Boolean, invokeAfterLoad: () -> Unit) {
    val progressBarSize = 65


    @DrawableRes
    val loaderGif: Int =
        if (ThemeManager.currentTheme.isNotEmpty() && ThemeManager.currentTheme.contains("dark")) {
            company.tap.tapuilibrary.R.drawable.loader
        } else if (ThemeManager.currentTheme.isNotEmpty() && !ThemeManager.currentTheme.contains(
                "dark"
            )
        ) {
          R.drawable.output_black_loader_nobg
           // R.drawable.loader
        } else
            company.tap.tapuilibrary.R.drawable.loader



    this.applyBluryToView()

    if (showLoadingIcon) {
        val progressImage = ImageView(this.context)
        val params = FrameLayout.LayoutParams(progressBarSize, progressBarSize)
        params.gravity = Gravity.CENTER
        progressImage.layoutParams = params
        Glide.with(this.context).asGif().load(loaderGif).into(progressImage)

        this.addView(progressImage)
    }
    doAfterSpecificTime(5000) {
        invokeAfterLoad.invoke()
    }

}

fun getScreenHeight(): Int {
    return Resources.getSystem().getDisplayMetrics().heightPixels
}
fun Context.twoThirdHeightView(): Double {
    return getDeviceSpecs().first.toDouble()
}
fun Context.getDeviceSpecs(): Pair<Int, Int> {
    val displayMetrics = DisplayMetrics()
    (this.getActivity())?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
    val height = Resources.getSystem().displayMetrics.heightPixels
    val width = displayMetrics.widthPixels
    val pair: Pair<Int, Int> = Pair(height, width)
    return pair
}

fun getDeviceLocale(): Locale? {
    val defaultLocale = Resources.getSystem().configuration.locales.get(0);
    return defaultLocale

}
fun Context.getDeviceTheme(): String {
    return when(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES){
        true ->"dark"
        false->"light"
    }
}
fun Context.getActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.getActivity()
        else -> null
    }
}
fun doAfterSpecificTime(time: Long = 1000L, execute: () -> Unit) =
    Handler(Looper.getMainLooper()).postDelayed(time) {
        execute.invoke()
    }

