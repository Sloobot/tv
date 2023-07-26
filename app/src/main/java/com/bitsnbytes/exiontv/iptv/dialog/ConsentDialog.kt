package com.bitsnbytes.exiontv.iptv.dialog

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.databinding.ConsentDialogBinding
import com.bitsnbytes.exiontv.iptv.utils.PrefsManager
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus


class ConsentDialog(private val consentInformation: ConsentInformation) : DialogFragment() {

    private var _binding: ConsentDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create AlertDialog.Builder Instance
        val builder = Dialog(requireContext())

        // Creating the custom layout using Inflater Class
        val inflater = requireActivity().layoutInflater
        _binding = ConsentDialogBinding.inflate(inflater)
        builder.setContentView(binding.root)

        builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val prefsManager = PrefsManager(requireContext())

        val mainTextBuilder = getSpannableStringBuilder(R.string.main_text)
        val mainTextSpan =  mainTextBuilder.getSpans(0, mainTextBuilder.length, URLSpan::class.java)
        for(span in mainTextSpan) {
            val start = mainTextBuilder.getSpanStart(span)
            val end = mainTextBuilder.getSpanEnd(span)

            // The original URLSpan needs to be removed to block the behavior of browser opening
            mainTextBuilder.removeSpan(span)

            mainTextBuilder.setSpan( object : ClickableSpan() {
                override fun onClick(widget: View) {
                    binding.consentForm.visibility = View.GONE
                    binding.consentInfo.visibility = View.VISIBLE
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.mainText.text = mainTextBuilder
        binding.mainText.movementMethod = LinkMovementMethod.getInstance()     // No action if this is not set

        // Consent Info
        val consentInfoBuilder = getSpannableStringBuilder(R.string.third_party_services)
        val consentInfoURLSpans = consentInfoBuilder.getSpans(0, consentInfoBuilder.length, URLSpan::class.java)
        for(span in consentInfoURLSpans) {
            val start = consentInfoBuilder.getSpanStart(span)
            val end = consentInfoBuilder.getSpanEnd(span)

            // The original URLSpan needs to be removed to block the behavior of browser opening
            consentInfoBuilder.removeSpan(span)
            consentInfoBuilder.setSpan( object : ClickableSpan() {
                override fun onClick(widget: View) {
                    when(span.url) {
                        "admob" -> {
                            openURL("https://policies.google.com/privacy")
                        }
                        "partners" -> {
                            openURL("https://support.google.com/admob/answer/9012903")
                        }
                        "app_privacy" -> {
                            openURL(resources.getString(R.string.app_privacy_policy))
                        }
                    }
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.servicesText.text = consentInfoBuilder
        binding.servicesText.movementMethod = LinkMovementMethod.getInstance()  // No action if this is not set


        binding.btnBack.setOnClickListener {
            binding.consentForm.visibility = View.VISIBLE
            binding.consentInfo.visibility = View.GONE
        }

        binding.btnAgree.setOnClickListener {
            prefsManager.savePrefs("gdpr", "1")
            consentInformation.consentStatus = ConsentStatus.PERSONALIZED
            dismiss()
        }

        binding.btnDisagree.setOnClickListener {
            prefsManager.savePrefs("gdpr", "0")
            consentInformation.consentStatus = ConsentStatus.NON_PERSONALIZED
            dismiss()
        }

        return builder
    }

    private fun getSpannableStringBuilder(strResId: Int): SpannableStringBuilder {
        // Get SpannableStringBuilder object from HTML code
        val sequence: CharSequence =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                Html.fromHtml(resources.getString(strResId), Html.FROM_HTML_MODE_LEGACY, null, null)
            else
                Html.fromHtml(resources.getString(strResId), null, null)

        return SpannableStringBuilder(sequence)       // strBuilder
    }

    private fun openURL(url: String) {
        try {
            val uri = Uri.parse(url)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}