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
import androidx.fragment.app.DialogFragment
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.database.DataSource
import com.bitsnbytes.exiontv.iptv.databinding.AppUpdateDialogBinding

class AppUpdatesDialog : DialogFragment() {

    private var _binding: AppUpdateDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create AlertDialog.Builder Instance
        val builder = Dialog(requireContext())

        // Creating the custom layout using Inflater Class
        val inflater = requireActivity().layoutInflater
        _binding = AppUpdateDialogBinding.inflate(inflater)
        builder.setContentView(binding.root)

        builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dataSource = DataSource(requireContext())
        val appUpdates = dataSource.getAppUpdates()
        if(appUpdates != null) {
            binding.apply {
                this.appVersion.text =
                    String.format("${getString(R.string.app_name)} ${getString(R.string.app_updates_version)}", appUpdates.version)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    this.appUpdates.text = Html.fromHtml(appUpdates.updates, Html.FROM_HTML_MODE_COMPACT);
                } else {
                    this.appUpdates.text = Html.fromHtml(appUpdates.updates)
                }

                btnDownloadNow.setOnClickListener {
                    val uri: Uri
                    if (appUpdates.url.startsWith("http://play.google.com"))
                        uri = Uri.parse("market://details?id=${requireContext().packageName}")
                    else
                        uri = Uri.parse(appUpdates.url)

                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    if (Build.VERSION.SDK_INT >= 21)
                        flags = flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    else
                        flags = flags or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET

                    intent.addFlags(flags)

                    try { startActivity(intent) }
                    catch (e: ActivityNotFoundException) { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                }
            }
        }

        return builder
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}