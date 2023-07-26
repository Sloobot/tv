package com.bitsnbytes.exiontv.iptv.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    var name: String,
    var url: String,
    var icon: String
) : Parcelable {
    constructor() : this("", "", "")
}