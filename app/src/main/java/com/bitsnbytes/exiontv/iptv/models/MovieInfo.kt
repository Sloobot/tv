package com.bitsnbytes.exiontv.iptv.models

import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MovieInfo(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("overview")
    val overview: String,

    @SerializedName("poster_path")
    val poster: String,

    @SerializedName("backdrop_path")
    val banner: String,

    @SerializedName("vote_average")
    val movieRating: Float = 0f,

    @SerializedName("vote_count")
    val totalVoted: Int = 0,

    @SerializedName("original_language")
    val language: String,

    @SerializedName("genres")
    val category: List<MovieCategory>,

    @SerializedName("release_date")
    val releaseDate: String,

    @SerializedName("video")
    val haveVideo: Boolean,

    @SerializedName("credits")
    val credits: Credits,

    @SerializedName("trailers")
    val trailers: MovieTrailer
)