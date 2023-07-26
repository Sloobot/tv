package com.bitsnbytes.exiontv.iptv.models

import com.google.gson.annotations.SerializedName

data class Credits(
    @SerializedName("cast")
    var cast: List<Cast>
)

data class Cast(
    @SerializedName("cast_id")
    val castId: Int,

    @SerializedName("character")
    val character: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("profile_path")
    val profileImage: String
)