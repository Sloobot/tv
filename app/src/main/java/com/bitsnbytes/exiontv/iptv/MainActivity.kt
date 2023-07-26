package com.bitsnbytes.exiontv.iptv

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bitsnbytes.exiontv.iptv.database.DataSource
import com.bitsnbytes.exiontv.iptv.databinding.ActivityMainBinding
import com.bitsnbytes.exiontv.iptv.dialog.AppUpdatesDialog
import com.bitsnbytes.exiontv.iptv.fragment.FavoriteMoviesFragment
import com.bitsnbytes.exiontv.iptv.fragment.HomeFragment
import com.bitsnbytes.exiontv.iptv.fragment.MovieSearchFragment
import com.bitsnbytes.exiontv.iptv.fragment.SettingsFragment
import com.bitsnbytes.exiontv.iptv.models.Ads
import com.bitsnbytes.exiontv.iptv.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var doExit: Boolean = false
    private var selectedNavPosition = 1
    private var adsManager: AdsManager? = null
    private lateinit var bindings: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        bindings.smoothBottomBar.apply {
            onItemSelected = { position ->
                selectedNavPosition = position
                when (position) {
                    0 -> {      // favourite
                        loadFavouriteFragment()
                    }
                    1 -> {      // home
                        loadHomeFragment()
                    }
                    2 -> {      // search
                        loadMovieSearchFragment()
                    }
                    3 -> {      // settings
                        loadSettingsFragment()
                    }
                }
            }

            onItemReselected = { position ->
                Toast.makeText(this@MainActivity, "Re-selected $position", Toast.LENGTH_SHORT).show()
            }
        }

        getAdIds()
        checkNewAppUpdates()
        loadHomeFragment()
        Gdrp(this)

        val dataSource = DataSource(this)
        val appUpdates = dataSource.getAppUpdates()
        if(appUpdates != null && appUpdates.version == BuildConfig.VERSION_NAME) {
            dataSource.clearAppUpdates()
        }
    }

    // Getting and Saving ad unit ids in shared prefs
    private fun getAdIds() {
        val apiInterface = ApiClient().getAdIds().create(ApiInterface::class.java)
        apiInterface.getAdsIds().enqueue(object : Callback<Ads> {
            override fun onResponse(call: Call<Ads>?, response: Response<Ads>) {
                if (response.isSuccessful) {
                    val ads: Ads = response.body() ?: return

                    val prefsManager = PrefsManager(this@MainActivity)
                    val prefsMap = mutableMapOf<String, String>()

                    // AdMob
                    if (prefsManager.getPrefs(Constants.ADMOB_PUB_ID) != ads.adMob.pubId)
                        prefsMap[Constants.ADMOB_PUB_ID] = ads.adMob.pubId

                    if (prefsManager.getPrefs(Constants.ADMOB_APP_ID) != ads.adMob.appId)
                        prefsMap[Constants.ADMOB_APP_ID] = ads.adMob.appId

                    if (prefsManager.getPrefs(Constants.ADMOB_BANNER_ID) != ads.adMob.bannerId)
                        prefsMap[Constants.ADMOB_BANNER_ID] = ads.adMob.bannerId

                    if (prefsManager.getPrefs(Constants.ADMOB_INTERSTITIAL_ID) != ads.adMob.interstitialId)
                        prefsMap[Constants.ADMOB_INTERSTITIAL_ID] = ads.adMob.interstitialId

                    // Facebook
                    if (prefsManager.getPrefs(Constants.FB_BANNER_ID) != ads.facebook.bannerId)
                        prefsMap[Constants.FB_BANNER_ID] = ads.facebook.bannerId

                    if (prefsManager.getPrefs(Constants.FB_INTERSTITIAL_ID) != ads.facebook.interstitialId)
                        prefsMap[Constants.FB_INTERSTITIAL_ID] = ads.facebook.interstitialId

                    // AdColony
                    if (prefsManager.getPrefs(Constants.ADCOLONY_APP_ID) != ads.adColony.appId)
                        prefsMap[Constants.ADCOLONY_APP_ID] = ads.adColony.appId

                    if (prefsManager.getPrefs(Constants.ADCOLONY_BANNER_ID) != ads.adColony.bannerId)
                        prefsMap[Constants.ADCOLONY_BANNER_ID] = ads.adColony.bannerId

                    if (prefsManager.getPrefs(Constants.ADMOB_INTERSTITIAL_ID) != ads.adColony.interstitialId)
                        prefsMap[Constants.ADCOLONY_INTERSTITIAL_ID] = ads.adColony.interstitialId

                    // UnityAds
                    if (prefsManager.getPrefs(Constants.UNITY_GAME_ID) != ads.unityAds.gameId)
                        prefsMap[Constants.UNITY_GAME_ID] = ads.unityAds.gameId

                    if (prefsManager.getPrefs(Constants.UNITY_BANNER_ID) != ads.unityAds.bannerId)
                        prefsMap[Constants.UNITY_BANNER_ID] = ads.unityAds.bannerId

                    if (prefsManager.getPrefs(Constants.UNITY_INTERSTITIAL_ID) != ads.unityAds.interstitialId)
                        prefsMap[Constants.UNITY_INTERSTITIAL_ID] = ads.unityAds.interstitialId

                    // StartApp
                    if (prefsManager.getPrefs(Constants.STARTAPP_APP_ID) != ads.startApp.appId)
                        prefsMap[Constants.STARTAPP_APP_ID] = ads.startApp.appId

                    prefsMap["display_ads"] = ads.displayAds

                    if(prefsMap.isNotEmpty())
                        prefsManager.savePrefs(prefsMap)

                    adsManager = AdsManager(this@MainActivity)
                    adsManager?.loadBannerAd(bindings.adContainer)
                }
            }

            override fun onFailure(call: Call<Ads?>?, t: Throwable?) {

            }
        })
    }

    private fun checkNewAppUpdates() {
        val dataSource = DataSource(this)

        val apiInterface = ApiClient().getAppUpdates().create(ApiInterface::class.java)
        apiInterface.getAppUpdates().enqueue(object : Callback<AppUpdates> {
            override fun onResponse(call: Call<AppUpdates>, response: Response<AppUpdates>) {
                if(response.isSuccessful) {
                    val appUpdates = response.body()
                    try {
                        val pkgInfo = packageManager.getPackageInfo(packageName, 0)
                        if(appUpdates != null && appUpdates.version != pkgInfo.versionName) {
                            // Checking and making sure we don't overwrite the same data again and again
                            val previousUpdate = dataSource.getAppUpdates()
                            if(previousUpdate == null) {
                                dataSource.addAppUpdates(appUpdates)
                            }
                            if(previousUpdate != null && appUpdates.version != previousUpdate.version) {
                                dataSource.clearAppUpdates()
                                dataSource.addAppUpdates(appUpdates)
                            }

                            val dialog = AppUpdatesDialog()
                            dialog.isCancelable = false
                            dialog.show(supportFragmentManager, "APP_UPDATE_DIALOG")
                        }
                    }
                    catch (e: PackageManager.NameNotFoundException) { }
                }
            }

            override fun onFailure(call: Call<AppUpdates>, t: Throwable) { }
        })
    }

    private fun loadFavouriteFragment() {
        if(supportFragmentManager.findFragmentByTag("FAVOURITE_MOVIES_FRAGMENT") == null) {
            val fragment = FavoriteMoviesFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, "FAVOURITE_MOVIES_FRAGMENT")
                .commit()
        }
    }

    private fun loadHomeFragment() {
        if(supportFragmentManager.findFragmentByTag("HOME_FRAGMENT") == null) {
            val fragment = HomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, "HOME_FRAGMENT")
                .commit()
        }
    }

    private fun loadMovieSearchFragment() {
        if(supportFragmentManager.findFragmentByTag("MOVIE_SEARCH_FRAGMENT") == null) {
            val fragment = MovieSearchFragment.getInstance(null, true, null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, "MOVIE_SEARCH_FRAGMENT")
                .commit()
        }
    }

    private fun loadSettingsFragment() {
        if(supportFragmentManager.findFragmentByTag("SETTINGS_FRAGMENT") == null) {
            val fragment = SettingsFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, "SETTINGS_FRAGMENT")
                .commit()
        }
    }

    private fun removeFragmentFromBackStack(): Boolean {
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentByTag("MOVIE_SEARCH_FRAGMENT") ?:
                            fragmentManager.findFragmentByTag("CHANNELS_CATEGORY_FRAGMENT") ?:
                                fragmentManager.findFragmentByTag("CHANNELS_FRAGMENT")
        if(fragment != null) {
            fragmentManager.popBackStack()
            return true
        }

        return false
    }

    override fun onBackPressed() {
        if(selectedNavPosition == 1) {
            if(removeFragmentFromBackStack())
                return
        }

        if(!doExit) {
            doExit = true
            Toast.makeText(this@MainActivity, "Tap again to exit", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                doExit = false
            }, 1500)
            return
        }

        doExit = false  // now exit the user if he wants...
        super.onBackPressed()
    }

    override fun onPause() {
        adsManager?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adsManager?.onResume()
    }

    override fun onDestroy() {
        adsManager?.onDestroy()
        super.onDestroy()
    }
}