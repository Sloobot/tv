package com.bitsnbytes.exiontv.iptv.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bitsnbytes.exiontv.iptv.AboutActivity
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.utils.AdsManager

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefResetGdrp = preferenceManager.findPreference<Preference>("reset_gdrp_consent")
        val privacyPolicy = preferenceManager.findPreference<Preference>("privacy_policy")
        val disclaimer = preferenceManager.findPreference<Preference>("disclaimer")
        val prefRateApp = preferenceManager.findPreference<Preference>("rate_app")
        val prefShareApp = preferenceManager.findPreference<Preference>("share_app")
        val prefAbout = preferenceManager.findPreference<Preference>("about")


        prefResetGdrp?.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
            override fun onPreferenceClick(preference: Preference?): Boolean {
                AdsManager(requireContext()).revokeConsent()
                Toast.makeText(activity, "GDRP consent revoked", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        privacyPolicy?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            openPrivacyPolicy()
            return@OnPreferenceClickListener true
        }

        disclaimer?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showDisclaimer()
            return@OnPreferenceClickListener true
        }

        prefRateApp?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            rateThisApp()
            return@OnPreferenceClickListener true
        }

        prefShareApp?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            shareApp()
            return@OnPreferenceClickListener true
        }

        prefAbout?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(context, AboutActivity::class.java)
            startActivity(intent)
            return@OnPreferenceClickListener true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey)
    }

    private fun openPrivacyPolicy() {
        try {
            val uri = Uri.parse(resources.getString(R.string.app_privacy_policy))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            if (Build.VERSION.SDK_INT >= 21)
                flags = flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
            else
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET

            intent.addFlags(flags)

            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Error " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDisclaimer() {
        AlertDialog.Builder(requireContext())
            .setTitle("Disclaimer")
            .setMessage(String.format(getString(R.string.disclaimer), getString(R.string.app_name)))
            .setPositiveButton("Ok") { dialog, which -> dialog?.dismiss() }
            .show()
    }

    private fun rateThisApp() {
        val uri = Uri.parse("market://details?id=${context?.packageName}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        if (Build.VERSION.SDK_INT >= 21)
            flags = flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        else
            flags = flags or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET

        goToMarket.addFlags(flags)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${context?.packageName}")
                )
            )
        }
    }

    private fun shareApp() {
        val appLink = "https://play.google.com/store/apps/details?id=${requireContext().applicationContext.packageName}&hl=en"

        val intent = Intent(Intent.ACTION_SEND)
        val text = "Hi, I'm using ${getString(R.string.app_name)} app to watch my favourite IPTV playlist on my phone.\n\nDownload Now:\n$appLink"
        intent.type = "text/plain"

        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, text)

        startActivity(Intent.createChooser(intent, "Share app using"));
    }
}