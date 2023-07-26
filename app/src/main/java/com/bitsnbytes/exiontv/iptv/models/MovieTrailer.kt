package com.bitsnbytes.exiontv.iptv.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class MovieTrailer(
    @SerializedName("youtube")
    val trailers: List<Trailer>,
)

@Parcelize
data class Trailer(
    @SerializedName("name")
    val name: String,

    @SerializedName("source")
    val key: String,
) : Parcelable