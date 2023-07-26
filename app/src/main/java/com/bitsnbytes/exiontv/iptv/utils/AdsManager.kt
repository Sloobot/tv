package com.bitsnbytes.exiontv.iptv.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.widget.LinearLayout
import com.adcolony.sdk.*
import com.bitsnbytes.exiontv.iptv.BuildConfig
import com.facebook.ads.AudienceNetworkAds
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import java.util.*

class AdsManager(private val context: Context) {

    private var prefsManager: PrefsManager
    private var consentInformation: ConsentInformation

    private var adMobBannerAd: AdView? = null
    private var adMobInterstitialAd: InterstitialAd? = null

    private var facebookBannerAd: com.facebook.ads.AdView? = null
    private var facebookInterstitialAd: com.facebook.ads.InterstitialAd? = null

    private var adColonyBannerAd: AdColonyAdView? = null
    private var adColonyInterstitialAd: AdColonyInterstitial? = null

    private var unityBannerAd: BannerView? = null

    init {
        prefsManager = PrefsManager(context)
        consentInformation = ConsentInformation.getInstance(context)
        val isGdrpApplicable = consentInformation.isRequestLocationInEeaOrUnknown

        val displayAds = prefsManager.getPrefs("display_ads").toLowerCase(Locale.ROOT)
        when(displayAds) {
            "admob" -> {
                tryToChangeAppID()
                MobileAds.initialize(context)
            }
            "facebook" -> {
                AudienceNetworkAds.initialize(context)
            }
            "adcolony" -> {
                val appOptions = AdColonyAppOptions()
                appOptions.setPrivacyFrameworkRequired(AdColonyAppOptions.GDPR, isGdrpApplicable)
                if (isGdrpApplicable && prefsManager.getPrefs("gdrp").isNotEmpty())
                    appOptions.setPrivacyConsentString(
                        AdColonyAppOptions.GDPR,
                        prefsManager.getPrefs("gdrp")
                    )
                else
                    appOptions.setPrivacyConsentString(AdColonyAppOptions.GDPR, "1")

                AdColony.configure(
                    context as Activity,
                    appOptions,
                    prefsManager.getPrefs(Constants.ADCOLONY_APP_ID),
                    prefsManager.getPrefs(Constants.ADCOLONY_BANNER_ID),
                    prefsManager.getPrefs(Constants.ADCOLONY_INTERSTITIAL_ID)
                )
            }
            "unityad" -> {
                UnityAds.initialize(context, prefsManager.getPrefs(Constants.UNITY_GAME_ID), BuildConfig.DEBUG)
            }
            "startapp" -> {
                StartAppSDK.init(context, prefsManager.getPrefs(Constants.STARTAPP_APP_ID), false)
                StartAppSDK.setUserConsent(context, "pas", System.currentTimeMillis(), isGdrpApplicable)
                StartAppAd.disableAutoInterstitial()
                StartAppAd.disableSplash()
            }
        }
    }

    fun loadBannerAd(linearLayout: LinearLayout) {
        when(prefsManager.getPrefs("display_ads").toLowerCase(Locale.ROOT)) {
            "admob" -> loadGoogleBannerAd(linearLayout)
            "facebook" -> loadFacebookBannerAd(linearLayout)
            "adcolony" -> loadAdColonyBannerAd(linearLayout)
            "unityad" -> loadUnityBannerAd(linearLayout)
            "startapp" -> loadStartAppBannerAd(linearLayout)
        }
    }

    fun loadInterstitialAd() {
        when(prefsManager.getPrefs("display_ads").toLowerCase(Locale.ROOT)) {
            "admob" -> loadGoogleInterstitialAd()
            "facebook" -> loadFacebookInterstitialAd()
            "adcolony" -> loadAdColonyInterstitialAd()
            "unityad" -> loadUnityInterstitialAd()
            "startapp" -> loadStartAppInterstitialAd()
        }
    }

    fun showInterstitialAd() {
        adMobInterstitialAd?.show(context as Activity)
        adColonyInterstitialAd?.show()
        facebookInterstitialAd?.apply {
            if(isAdLoaded) {
                show()
            }
        }

        if (UnityAds.isReady (prefsManager.getPrefs(Constants.UNITY_INTERSTITIAL_ID))) {
            UnityAds.show (context as Activity, prefsManager.getPrefs(Constants.UNITY_INTERSTITIAL_ID))
        }
    }

    fun revokeConsent() {
        consentInformation.reset()
    }

    /* AdMob*/
    private fun loadGoogleBannerAd(linearLayout: LinearLayout) {
        adMobBannerAd = AdView(context)
        val adWidthSize = getAdSize()
        adMobBannerAd?.apply {
            adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthSize)
            adUnitId = prefsManager.getPrefs(Constants.ADMOB_BANNER_ID)

            val extras = Bundle()
            if(consentInformation.consentStatus == ConsentStatus.NON_PERSONALIZED)
                extras.putString("npa", "1")

            val adRequest = AdRequest
                .Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
            loadAd(adRequest)
            linearLayout.addView(this)
        }
    }

    private fun loadGoogleInterstitialAd() {
        val extras = Bundle()
        if(consentInformation.consentStatus == ConsentStatus.NON_PERSONALIZED)
            extras.putString("npa", "1")

        val adRequest = AdRequest
            .Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        InterstitialAd.load(context,
            prefsManager.getPrefs(Constants.ADMOB_INTERSTITIAL_ID), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                adMobInterstitialAd = interstitialAd
            }
        })

    }

    /* Facebook */
    private fun loadFacebookBannerAd(linearLayout: LinearLayout) {
        facebookBannerAd = com.facebook.ads.AdView(context,
            prefsManager.getPrefs(Constants.FB_BANNER_ID), com.facebook.ads.AdSize.BANNER_HEIGHT_50)
        linearLayout.addView(facebookBannerAd)
        facebookBannerAd?.loadAd()
    }

    private fun loadFacebookInterstitialAd() {
        facebookInterstitialAd = com.facebook.ads.InterstitialAd(context, prefsManager.getPrefs(Constants.FB_INTERSTITIAL_ID))
        facebookInterstitialAd?.apply {
            loadAd(buildLoadAdConfig().build())
        }
    }

    /* AdColony*/
    private fun loadAdColonyBannerAd(linearLayout: LinearLayout) {
        val listener = object : AdColonyAdViewListener() {
            override fun onRequestFilled(adView: AdColonyAdView?) {
                adColonyBannerAd = adView
                linearLayout.addView(adColonyBannerAd)
            }
        }

        AdColony.requestAdView(prefsManager.getPrefs(Constants.ADCOLONY_BANNER_ID), listener, AdColonyAdSize.BANNER)
    }

    private fun loadAdColonyInterstitialAd() {
        val listener = object : AdColonyInterstitialListener() {
            override fun onRequestFilled(interstitialAd: AdColonyInterstitial?) {
                adColonyInterstitialAd = interstitialAd
            }

            override fun onClosed(ad: AdColonyInterstitial?) {
                adColonyInterstitialAd = null
            }
        }
        AdColony.requestInterstitial(prefsManager.getPrefs(Constants.ADCOLONY_INTERSTITIAL_ID), listener)
    }

    /* UnityAds */
    private fun loadUnityBannerAd(linearLayout: LinearLayout) {
        unityBannerAd = BannerView(context as Activity, prefsManager.getPrefs(Constants.UNITY_BANNER_ID), UnityBannerSize(getAdSize(), 50))
        unityBannerAd?.apply {
            listener = object : BannerView.IListener {
                override fun onBannerLoaded(p0: BannerView?) {
                    linearLayout.removeAllViews()
                    linearLayout.addView(p0)
                }

                override fun onBannerClick(p0: BannerView?) { }

                override fun onBannerFailedToLoad(p0: BannerView?, p1: BannerErrorInfo?) {
                    linearLayout.removeAllViews()
                }

                override fun onBannerLeftApplication(p0: BannerView?) { }
            }

            load()
        }
    }

    private fun loadUnityInterstitialAd() {
        UnityAds.load(prefsManager.getPrefs(Constants.UNITY_INTERSTITIAL_ID), object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(p0: String?) { }

            override fun onUnityAdsFailedToLoad(p0: String?, p1: UnityAds.UnityAdsLoadError?, p2: String?) { }
        })
    }

    /* StartApp */
    private fun loadStartAppBannerAd(linearLayout: LinearLayout) {
        val bannerAd = com.startapp.sdk.ads.banner.Banner(context as Activity, object : com.startapp.sdk.ads.banner.BannerListener {
            override fun onReceiveAd(p0: View?) {
                linearLayout.removeAllViews()
                linearLayout.addView(p0)
            }

            override fun onFailedToReceiveAd(p0: View?) {
                linearLayout.removeAllViews()
            }

            override fun onImpression(p0: View?) { }

            override fun onClick(p0: View?) { }
        })
        bannerAd.loadAd()
    }

    private fun loadStartAppInterstitialAd() {
        StartAppAd.showAd(context)
    }

    private fun tryToChangeAppID() {
        try {
            val ai = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            ai.metaData.putString(
                "com.google.android.gms.ads.APPLICATION_ID",
                prefsManager.getPrefs(Constants.ADMOB_APP_ID)
            )
        }
        catch (e: PackageManager.NameNotFoundException) { }
        catch (e: NullPointerException) { }
    }

    private fun getAdSize(): Int {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val display: Display = (context as Activity).windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        return (widthPixels / density).toInt()
    }

    fun onPause() {
        adMobBannerAd?.pause()
    }

    fun onResume() {
        adMobBannerAd?.resume()
    }

    fun onDestroy() {
        adMobBannerAd?.destroy()
        adColonyBannerAd?.destroy()
        facebookBannerAd?.destroy()
        facebookInterstitialAd?.destroy()
        unityBannerAd?.destroy()
    }
}