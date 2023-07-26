package com.bitsnbytes.exiontv.iptv.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "movies_category")
data class MovieCategory(
    @PrimaryKey
    @SerializedName("id")
    val categoryId: Int,

    @SerializedName("name")
    val categoryName: String
) : Parcelable