package com.bitsnbytes.exiontv.iptv.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.bitsnbytes.exiontv.iptv.BuildConfig
import com.bitsnbytes.exiontv.iptv.MainActivity
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.dialog.ConsentDialog
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.ads.consent.DebugGeography
import com.google.android.ump.*

class Gdrp(private val context: Context) {

    private var consentInformation: ConsentInformation

    init {
        consentInformation = ConsentInformation.getInstance(context)

        if(BuildConfig.DEBUG) {
            consentInformation.addTestDevice("8D9386C040CAEDC19F338A3072544D43")
            // Geography appears as in EEA for test devices.
            consentInformation.debugGeography = DebugGeography.DEBUG_GEOGRAPHY_EEA
            // Geography appears as not in EEA for debug devices.
            //consentInformation.debugGeography = DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA
        }

        val publisherIds: Array<String>
        val pubId = PrefsManager(context).getPrefs(Constants.ADMOB_PUB_ID)
        if(pubId.isNullOrEmpty())
            publisherIds = arrayOf(context.getString(R.string.admob_pub_id))
        else
            publisherIds = arrayOf(pubId)

        consentInformation.requestConsentInfoUpdate(publisherIds, object :
            ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus?) {
                // checking if user is located in the European Economic Area and consent is required or not
                if(consentInformation.isRequestLocationInEeaOrUnknown) {
                    when(consentStatus) {
                        ConsentStatus.PERSONALIZED, ConsentStatus.NON_PERSONALIZED -> {
                            // user has already granted consent...
                        }
                        ConsentStatus.UNKNOWN -> {
                            // Collect consent from a user
                            createConsentDialog()
                        }
                    }
                }
                else {
                    // make ad request..
                }
            }

            override fun onFailedToUpdateConsentInfo(reason: String?) {

            }
        })
    }

    private fun createConsentDialog() {
        val consentDialog = ConsentDialog(consentInformation)
        consentDialog.show((context as MainActivity).supportFragmentManager, "CONSENT_DIALOG")
    }
}