package com.bitsnbytes.exiontv.iptv.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bitsnbytes.exiontv.iptv.models.Movie
import com.bitsnbytes.exiontv.iptv.models.MovieCategory
import com.bitsnbytes.exiontv.iptv.utils.AppUpdates

@Database(entities = [Movie::class, MovieCategory::class, AppUpdates::class], version = 1, exportSchema = false)
abstract class DBHelper : RoomDatabase() {

    companion object {
        private var INSTANCE: DBHelper? = null
        private var DATABASE_NAME = "exion_tv.db"

        fun getInstance(context: Context): DBHelper {
            if(INSTANCE == null) {
                // Standard way to create singleton class
                synchronized(DBHelper::class.java) {
                    INSTANCE = Room.databaseBuilder(context, DBHelper::class.java, DATABASE_NAME)
                            .allowMainThreadQueries()
                            .build()
                }
            }

            return INSTANCE!!
        }
    }

    abstract fun getFavouriteMoviesDao(): FavouriteChannelsDao
    abstract fun getMoviesCategoryDao(): MoviesCategoryDao
    abstract fun getAppUpdatesDao(): AppUpdatesDao
}