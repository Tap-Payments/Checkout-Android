package com.tap.company.tapcheckout_android

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Headers(var mdn: String?=null, var application: String?=null) : Parcelable