package com.bitsnbytes.exiontv.iptv.utils

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bitsnbytes.exiontv.iptv.R
import com.google.gson.annotations.SerializedName

@Entity(tableName = "app_updates")
data class AppUpdates (
    @PrimaryKey
    @ColumnInfo(defaultValue = "100")
    val id: Int,

    @SerializedName("app_name")
    val name: String,

    @SerializedName("app_url")
    val url: String,

    @SerializedName("app_version")
    val version: String,

    @SerializedName("app_updates")
    val updates: String
)