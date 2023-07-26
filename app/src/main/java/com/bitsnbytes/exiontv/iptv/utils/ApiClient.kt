package com.bitsnbytes.exiontv.iptv.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val API_KEY = "PASTE_YOUR_TMDB_API_KEY_HERE"
        const val IMAGE_URL = "https://image.tmdb.org/t/p/w200"
        const val BANNER_IMAGE_URL = "https://image.tmdb.org/t/p/w500"
        const val COUNTRY_FLAG_URL = "https://www.countryflags.io/%s/flat/64.png"
        const val IPTV_URL = "https://iptv-org.github.io/iptv/"
        const val IPTV_INDEX_FILE_URL = "https://raw.githubusercontent.com/sharyrajpoot/exiontv/master/"
        const val FIREBASE_DATABASE_URL = "https://database-url.com/"

        private var retrofit: Retrofit? = null

        fun getClient(): Retrofit {
            if(retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }

            return retrofit!!
        }

        fun getFile(URL: String): Retrofit {
            return Retrofit.Builder()
                .baseUrl(URL)
                .client(OkHttpClient.Builder().build())
                .build()
        }
    }

    fun getAdIds(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(FIREBASE_DATABASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getAppUpdates(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(FIREBASE_DATABASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}