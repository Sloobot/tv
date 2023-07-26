package com.bitsnbytes.exiontv.iptv.models

import com.google.gson.annotations.SerializedName

class Ads {
    @SerializedName("AdMob")
    val adMob: AdsUnits = AdsUnits()

    @SerializedName("Facebook")
    val facebook: AdsUnits = AdsUnits()

    @SerializedName("AdColony")
    val adColony: AdsUnits = AdsUnits()

    @SerializedName("UnityAds")
    val unityAds: AdsUnits = AdsUnits()

    @SerializedName("StartApp")
    val startApp: AdsUnits = AdsUnits()

    @SerializedName("DisplayAds")
    val displayAds: String = ""
}

class AdsUnits {
    @SerializedName("app_id")
    val appId: String = ""

    @SerializedName("publisher_id")
    val pubId: String = ""

    @SerializedName("game_id")
    val gameId: String = ""

    @SerializedName("banner_id")
    val bannerId: String = ""

    @SerializedName("interstitial_id")
    val interstitialId: String = ""
}