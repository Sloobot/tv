package com.bitsnbytes.exiontv.iptv.utils

import android.util.Log

class Logger {
    companion object {
        fun log(msg: Any) {
            Log.d("USERID", msg.toString())
        }
    }
}