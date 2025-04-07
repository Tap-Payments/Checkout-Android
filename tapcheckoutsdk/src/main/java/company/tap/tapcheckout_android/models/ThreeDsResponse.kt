package company.tap.tapcheckout_android.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ThreeDsResponse(var id: String, var url: String,var powered:Boolean,var stopRedirection:Boolean=false) :
    Parcelable

@Parcelize
data class ThreeDsResponseCardPayButtons(var threeDsUrl: String, var redirectUrl: String,var powered:Boolean,var keyword:String) :
    Parcelable


@Parcelize
data class RedirectResponse(
    var redirectUrl: String,
    var powered: Boolean,
    var keyword: String
) : Parcelable




