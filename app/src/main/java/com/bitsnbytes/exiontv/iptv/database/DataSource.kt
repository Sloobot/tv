package com.bitsnbytes.exiontv.iptv.database

import android.content.Context
import com.bitsnbytes.exiontv.iptv.models.Movie
import com.bitsnbytes.exiontv.iptv.models.MovieCategory
import com.bitsnbytes.exiontv.iptv.utils.AppUpdates

class DataSource(context: Context) {

    private val moviesCategoryDao: MoviesCategoryDao
    private val favouriteChannelsDao: FavouriteChannelsDao
    private val appUpdatesDao: AppUpdatesDao

    init {
        val dbHelper = DBHelper.getInstance(context)
        moviesCategoryDao = dbHelper.getMoviesCategoryDao()
        favouriteChannelsDao = dbHelper.getFavouriteMoviesDao()
        appUpdatesDao = dbHelper.getAppUpdatesDao()
    }

    /* Favourite Movies */
    fun addToFavourite(movie: Movie) {
        favouriteChannelsDao.addToRFavourite(movie)
    }

    fun removeFromFavourite(movie: Movie) {
        favouriteChannelsDao.removeFavouriteMovie(movie)
    }

    fun isFavouriteMovie(movie: Movie): Boolean {
        return favouriteChannelsDao.isFavoriteMovie(movie.id) == 1
    }

    fun getAllFavouriteMovies(): List<Movie> {
        return favouriteChannelsDao.getAllFavouriteMovies()
    }

    /* Movies Genre */
    fun addAllGenre(moviesCategory: List<MovieCategory>) {
        moviesCategoryDao.addAll(moviesCategory)
    }

    fun getAllGenre(): List<MovieCategory> {
        return moviesCategoryDao.getAll()
    }

    fun getMovieGenre(movieId: Int): String {
        return moviesCategoryDao.getMovieCategory(movieId)
    }

    /* App Updates */
    fun addAppUpdates(updates: AppUpdates) {
        appUpdatesDao.addAppUpdates(updates)
    }

    fun getAppUpdates(): AppUpdates? {
        return appUpdatesDao.getAppUpdates()
    }

    fun clearAppUpdates() {
        appUpdatesDao.clearUpdates()
    }
}