package com.bitsnbytes.exiontv.iptv.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.databinding.MovieCastBinding
import com.bitsnbytes.exiontv.iptv.models.Cast
import com.bitsnbytes.exiontv.iptv.utils.ApiClient
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso


class MovieCasterAdapter(private val context: Context, private val moveCasterList: List<Cast>) :
    RecyclerView.Adapter<MovieCasterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = MovieCastBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val caster = moveCasterList[position]

        holder.binding.apply {
            casterName.text = caster.name
            movieCharacterName.text = caster.character

            Picasso.get()
                .load(ApiClient.IMAGE_URL + caster.profileImage)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.color.background)
                .error(R.color.background)
                .into(casterImage)
        }
    }

    override fun getItemCount(): Int = moveCasterList.size

    inner class ViewHolder(val binding: MovieCastBinding) : RecyclerView.ViewHolder(binding.root)
}