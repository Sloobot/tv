package com.bitsnbytes.exiontv.iptv.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bitsnbytes.exiontv.iptv.MovieDetailActivity
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.databinding.SliderLayoutBinding
import com.bitsnbytes.exiontv.iptv.models.Movie
import com.bitsnbytes.exiontv.iptv.utils.ApiClient
import com.smarteist.autoimageslider.SliderViewAdapter
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class SliderAdapter(private val context: Context, private val latestMoviesList: List<Movie>) :
    SliderViewAdapter<SliderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val view = SliderLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val movie = latestMoviesList[position]

        viewHolder.binding.apply {
            movieName.text = movie.title

            Picasso.get()
                .load(ApiClient.BANNER_IMAGE_URL + movie.banner)
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                .placeholder(R.color.background)
                .error(R.color.background)
                .into(movieImage)

            root.setOnClickListener {
                val intent = Intent(context, MovieDetailActivity::class.java)
                intent.putExtra("MOVIE", movie)
                context.startActivity(intent)
            }
        }
    }

    override fun getCount(): Int {
        return latestMoviesList.size
    }

    inner class ViewHolder(val binding: SliderLayoutBinding) : SliderViewAdapter.ViewHolder(binding.root)
}