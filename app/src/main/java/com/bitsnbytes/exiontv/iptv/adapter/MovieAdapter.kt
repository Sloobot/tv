package com.bitsnbytes.exiontv.iptv.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitsnbytes.exiontv.iptv.MovieDetailActivity
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.databinding.ItemMovieBinding
import com.bitsnbytes.exiontv.iptv.models.Movie
import com.bitsnbytes.exiontv.iptv.utils.ApiClient
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class MovieAdapter(private val context: Context, private val movieList: List<Movie>) :
    RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemMovieBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movieList[position]

        if(movie.poster.isNullOrEmpty()) movie.poster = ""
        if(movie.banner.isNullOrEmpty()) movie.banner = ""

        val posterImage: String
        if(movie.poster.isNullOrEmpty())
            posterImage = "https://my-goodlife.com/assets/images/products/not-available.png"
        else
            posterImage = ApiClient.IMAGE_URL + movie.poster

        holder.binding.apply {
            movieName.text = movie.title

            val picasso = Picasso.get()
                .load(posterImage)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.color.background)
                .error(R.color.background)
            picasso.into(movieImage)
            picasso.fetch(object : Callback {
                override fun onSuccess() {
                    picasso.fetch(null)
                    progressBar.visibility = View.GONE
                }

                override fun onError(e: Exception) {
                    picasso.fetch(null)
                    progressBar.visibility = View.GONE
                }
            })

            root.setOnClickListener {
                val intent = Intent(context, MovieDetailActivity::class.java)
                intent.putExtra("MOVIE", movie)
                context.startActivity(intent)

                if(context.javaClass.simpleName == MovieDetailActivity::class.simpleName) {
                    (context as MovieDetailActivity).finish()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    class ViewHolder(val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root)
}