package com.bitsnbytes.exiontv.iptv.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bitsnbytes.exiontv.iptv.models.Movie
import com.bitsnbytes.exiontv.iptv.models.MovieCategory

@Dao
interface MoviesCategoryDao {

    @Insert
    fun addAll(movieCategory: List<MovieCategory>)

    @Query("SELECT * from movies_category")
    fun getAll(): List<MovieCategory>

    @Query("SELECT categoryName from movies_category WHERE categoryId = :movieId")
    fun getMovieCategory(movieId: Int): String
}