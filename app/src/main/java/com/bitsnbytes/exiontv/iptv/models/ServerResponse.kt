package com.bitsnbytes.exiontv.iptv.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ServerResponse (

    @SerializedName("results")
    val moviesLists: List<Movie>,

    @SerializedName("page")
    val page: Int = 0,

    @SerializedName("total_pages")
    val totalPages: Int = 0,

    @SerializedName("total_results")
    val totalResults: Int = 0,

    @SerializedName("genres")
    val movieCategory: List<MovieCategory>,

    //@SerializedName("cast")
    //val movieCastList: List<Cast>? = null
)