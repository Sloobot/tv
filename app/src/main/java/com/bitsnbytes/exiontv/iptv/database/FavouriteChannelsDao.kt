package com.bitsnbytes.exiontv.iptv.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bitsnbytes.exiontv.iptv.models.Movie

@Dao
interface FavouriteChannelsDao {

    @Insert
    fun addToRFavourite(movie: Movie)

    @Query("SELECT * FROM favourite_movies")
    fun getAllFavouriteMovies(): List<Movie>

    @Query("SELECT count(*) from favourite_movies WHERE id = :id LIMIT 1")
    fun isFavoriteMovie(id: Int): Int

    @Delete
    fun removeFavouriteMovie(movie: Movie)
}