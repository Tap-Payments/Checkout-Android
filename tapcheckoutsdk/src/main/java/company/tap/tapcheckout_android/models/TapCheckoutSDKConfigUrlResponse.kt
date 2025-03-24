package company.tap.tapcheckout_android.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TapCheckoutSDKConfigUrlResponse(val baseURL: String,
                                           val testEncKey: String,
                                           val prodEncKey: String,
                                           val redirectionKeyWord: String,

                                           ):Parcelable



