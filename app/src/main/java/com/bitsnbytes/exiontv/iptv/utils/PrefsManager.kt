package com.bitsnbytes.exiontv.iptv.utils

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    init {
        prefs = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
    }

    fun savePrefs (key: String, value: Boolean) {
        if(editor == null)
            editor = prefs?.edit()
        editor?.putBoolean(key, value);
        editor?.apply();
    }

    fun getPrefs(key: String, isBoolean: Boolean): Boolean {
        return prefs?.getBoolean(key, false) ?: false
    }

    fun savePrefs (key: String, value: String) {
        if (editor == null)
            editor = prefs?.edit()

        editor?.putString(key, value)

        editor?.apply()
    }

    fun savePrefs (prefsMap: Map<String, String>) {
        if(editor == null)
            editor = prefs?.edit()

        for((key, value) in prefsMap) {
            editor?.putString(key, value)
        }

        editor?.apply()
    }

    fun getPrefs(key: String): String {
        return prefs?.getString(key, "") ?: ""
    }
}