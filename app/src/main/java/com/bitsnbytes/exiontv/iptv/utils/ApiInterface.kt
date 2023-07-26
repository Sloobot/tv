package com.bitsnbytes.exiontv.iptv.utils

import com.bitsnbytes.exiontv.iptv.models.*
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url


interface ApiInterface {

    @GET("movie/now_playing?api_key=" + ApiClient.API_KEY)
    fun getLatestMovies(): Call<ServerResponse>

    @GET("genre/movie/list?api_key=" + ApiClient.API_KEY)
    fun getMoviesCategory(): Call<ServerResponse>

    @GET("movie/upcoming?api_key=" + ApiClient.API_KEY)
    fun getUpcomingMovies(): Call<ServerResponse>

    @GET("movie/top_rated?api_key=" + ApiClient.API_KEY)
    fun getTopRatedMovies(): Call<ServerResponse>

    @GET("movie/popular?api_key=" + ApiClient.API_KEY)
    fun getPopularMovies(): Call<ServerResponse>

    @GET("movie/{movie_id}?append_to_response=credits,trailers&api_key=" + ApiClient.API_KEY)
    fun getMovieDetails(@Path("movie_id") movieId: Int): Call<MovieInfo>

    @GET("movie/{movie_id}/similar?api_key=" + ApiClient.API_KEY)
    fun getSimilarMovies(@Path("movie_id") movieId: Int): Call<ServerResponse>

    @GET("discover/movie?api_key=" + ApiClient.API_KEY)
    fun getCategoryMovies(@Query("with_genres") categoryId: Int, @Query("page") pageNo: Int): Call<ServerResponse>

    @GET("search/movie?api_key=" + ApiClient.API_KEY)
    fun findMovie(@Query("query") movieName: String, @Query("page") pageNo: Int): Call<ServerResponse>

    @GET("Ads.json")
    fun getAdsIds(): Call<Ads>

    @GET("AppUpdates.json")
    fun getAppUpdates(): Call<AppUpdates>

    @GET
    fun fetchFile(@Url fileUrl: String): Call<ResponseBody>
}