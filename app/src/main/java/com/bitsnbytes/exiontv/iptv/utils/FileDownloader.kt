package com.bitsnbytes.exiontv.iptv.utils

import android.accounts.NetworkErrorException
import android.content.Context
import android.widget.Toast
import com.bitsnbytes.exiontv.iptv.models.Channel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FileDownloader(context: Context) {

    private var mContext: Context = context
    private var mListener: FileDownloadListener? = null

    fun setFileDownloadListener(listener: FileDownloadListener) {
        mListener = listener
    }

    fun removeListener() {
        mListener = null
    }

    fun getIPTVFile(baseUrl: String, fileName: String) {
        val apiInterface = ApiClient.getFile(baseUrl).create(ApiInterface::class.java)
        apiInterface.fetchFile(fileName).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful) {
                    val `is` = response.body()!!.byteStream()
                    val parser = M3UParser()
                    val playlist = parser.parseFile(`is`)
                    mListener?.onFileDownloaded(playlist)
                    removeListener()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is NetworkErrorException)
                    Toast.makeText(mContext, "No internet connectivity found", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    interface FileDownloadListener {
        fun onFileDownloaded(playlist: ArrayList<Channel>)
    }
}