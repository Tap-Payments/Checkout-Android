package company.tap.tapcheckout_android.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TapButtonSDKConfigUrlResponse(    val baseURL: String,
                                             val testEncKey: String,
                                             val prodEncKey: String,
                                             val redirectionKeyWord: String,

):Parcelable



