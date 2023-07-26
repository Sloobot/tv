package com.bitsnbytes.exiontv.iptv

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import com.bitsnbytes.exiontv.iptv.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val pkgInfo = packageManager.getPackageInfo(packageName, 0)
            binding.appVersion.text = "Version ${pkgInfo.versionName}"
        }
        catch (e: PackageManager.NameNotFoundException) {
            binding.appVersion.text = "Version 0.0"
        }

        val htmlTag = "<a href=\"\">Privacy Policy</a>"
        binding.privacyPolicyUrl.text = Html.fromHtml(htmlTag)

        binding.privacyPolicyUrl.setOnClickListener{
            openPrivacyPolicy()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
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
            Toast.makeText(this, "Error " + e.message, Toast.LENGTH_SHORT).show()
        }
    }
}