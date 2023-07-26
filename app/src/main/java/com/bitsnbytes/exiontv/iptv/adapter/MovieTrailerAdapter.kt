package com.bitsnbytes.exiontv.iptv.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.YoutubePlayerActivity
import com.bitsnbytes.exiontv.iptv.databinding.MovieTrailerBinding
import com.bitsnbytes.exiontv.iptv.models.MovieTrailer
import com.bitsnbytes.exiontv.iptv.models.Trailer
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso


class MovieTrailerAdapter(private val context: Context, private val movieTrailerList: List<Trailer>) :
    RecyclerView.Adapter<MovieTrailerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = MovieTrailerBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trailer = movieTrailerList[position]

        holder.binding.apply {
            trailerTitle.text = trailer.name
            trailerSite.text = "Youtube"

            Picasso.get()
                .load("https://img.youtube.com/vi/" + trailer.key + "/hqdefault.jpg")
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.color.background)
                .error(R.color.background)
                .into(trailerThumbnail)

            root.setOnClickListener {
                val intent = Intent(context, YoutubePlayerActivity::class.java)
                intent.putExtra("MOVIE_TRAILER", trailer)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return movieTrailerList.size
    }

    class ViewHolder(val binding: MovieTrailerBinding) : RecyclerView.ViewHolder(binding.root)
}
