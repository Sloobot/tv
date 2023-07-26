package com.bitsnbytes.exiontv.iptv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.bitsnbytes.exiontv.iptv.adapter.MovieAdapter
import com.bitsnbytes.exiontv.iptv.adapter.MovieCasterAdapter
import com.bitsnbytes.exiontv.iptv.adapter.MovieTrailerAdapter
import com.bitsnbytes.exiontv.iptv.database.DataSource
import com.bitsnbytes.exiontv.iptv.databinding.ActivityMovieDetailBinding
import com.bitsnbytes.exiontv.iptv.models.Movie
import com.bitsnbytes.exiontv.iptv.models.MovieInfo
import com.bitsnbytes.exiontv.iptv.models.ServerResponse
import com.bitsnbytes.exiontv.iptv.models.Trailer
import com.bitsnbytes.exiontv.iptv.utils.AdsManager
import com.bitsnbytes.exiontv.iptv.utils.ApiClient
import com.bitsnbytes.exiontv.iptv.utils.ApiInterface
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var dataSource: DataSource
    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var mLayoutParams: LinearLayout.LayoutParams

    private var isFavoriteMovie: Boolean = false
    private var adsManager: AdsManager? = null

    // UI Reference
    private var isAdLoaded = false
    private var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.apply {
            title = ""
            setNavigationIcon(R.drawable.ic_back)
        }
        setSupportActionBar(binding.toolbar)

        val movie = intent.getParcelableExtra<Movie>("MOVIE")
        if(movie == null) {
            finish()
            return
        }

        adsManager = AdsManager(this)
        adsManager?.loadBannerAd(binding.adContainer)
        val noImageFound = "https://icon-library.com/images/no-photo-available-icon/no-photo-available-icon-4.jpg"
        val posterImage = if(movie.poster.isEmpty()) noImageFound else ApiClient.IMAGE_URL + movie.poster
        val bannerImage = if(movie.banner.isEmpty()) noImageFound else ApiClient.BANNER_IMAGE_URL + movie.banner

        // Banner Image
        val bannerPicasso = Picasso.get()
            .load(bannerImage)
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .placeholder(R.color.background)
            .error(R.color.background)
        bannerPicasso.into(binding.movieBanner)
        bannerPicasso.fetch(object : com.squareup.picasso.Callback {
            override fun onSuccess() {
                binding.bannerImageProgressBar.visibility = View.GONE
            }

            override fun onError(e: Exception) {
                binding.bannerImageProgressBar.visibility = View.GONE
            }
        })

        // Poster Image
        val picasso = Picasso.get()
            .load(posterImage)
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .placeholder(R.color.background)
            .error(R.color.background)
        picasso.into(binding.moviePoster)
        picasso.fetch(object : com.squareup.picasso.Callback {
            override fun onSuccess() {
                binding.imageProgressBar.visibility = View.GONE
            }

            override fun onError(e: Exception) {
                binding.imageProgressBar.visibility = View.GONE
            }
        })

        dataSource = DataSource(this)
        isFavoriteMovie = dataSource.isFavouriteMovie(movie)
        setLayoutPrams()
        getMovieDetails(movie.id)
        getSimilarMovies(movie.id)
    }

    private fun getMovieDetails(movieId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.movieDetailsLayout.visibility = View.GONE

        val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        apiInterface.getMovieDetails(movieId).enqueue(object : Callback<MovieInfo> {
            override fun onResponse(call: Call<MovieInfo>, response: Response<MovieInfo>) {
                if (response.isSuccessful && response.body() != null) {
                    val movie = response.body() as MovieInfo
                    binding.apply {
                        movieReleaseDate.text = movie.releaseDate
                        movieLanguage.text = movie.language
                        movieVoteCount.text = String.format(getString(R.string.votes), movie.totalVoted)
                        movieRating.text = DecimalFormat("#.#").format(movie.movieRating / 2)
                        movieDescription.text = movie.overview

                        /* Genre */
                        val length = if (movie.category.size > 2) 2 else movie.category.size
                        for (i in 0 until length) {
                            val movieCategory = movie.category[i]
                            val textView = TextView(applicationContext)
                            textView.text = movieCategory.categoryName
                            textView.textSize = 12f
                            textView.layoutParams = mLayoutParams
                            textView.setTextColor(ResourcesCompat.getColor(resources, R.color.text_color, theme))
                            textView.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_movie_category, theme)
                            movieCategoryLayout.addView(textView)
                        }

                        /* Trailers */
                        if(!movie.trailers.trailers.isNullOrEmpty()) {
                            val adapter = MovieTrailerAdapter(this@MovieDetailActivity, movie.trailers.trailers)
                            binding.rvMovieTrailers.adapter = adapter
                            binding.rvMovieTrailers.visibility = View.VISIBLE
                        }
                        else {
                            binding.msgNoTrailersFound.visibility = View.VISIBLE
                        }

                        /* Credits/Casts */
                        if(!movie.credits.cast.isNullOrEmpty()) {
                            val adapter = MovieCasterAdapter(this@MovieDetailActivity, movie.credits.cast)
                            binding.rvMovieCasts.adapter = adapter
                            binding.rvMovieCasts.visibility = View.VISIBLE
                        }
                        else {
                            binding.msgNoCastersFound.visibility = View.VISIBLE
                        }
                    }

                    binding.progressBar.visibility = View.GONE
                    binding.movieDetailsLayout.visibility = View.VISIBLE
                }
                else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MovieDetailActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MovieInfo>, t: Throwable) { }
        })
    }

    private fun getSimilarMovies(movieId: Int) {
        val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        apiInterface.getSimilarMovies(movieId).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    if (response.body()!!.totalResults != 0) {
                        val similarMovies = response.body()!!.moviesLists
                        val adapter = MovieAdapter(this@MovieDetailActivity, similarMovies)
                        binding.rvRelatedMovies.adapter = adapter
                        binding.progressBar3.visibility = View.GONE
                        binding.rvRelatedMovies.visibility = View.VISIBLE
                    } else {
                        binding.progressBar3.visibility = View.GONE
                        binding.errorMessage.visibility = View.VISIBLE
                    }
                } else {
                    binding.progressBar3.visibility = View.GONE
                    Toast.makeText(this@MovieDetailActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {

            }
        })
    }

    private fun setLayoutPrams() {
        // Converting and Setting up margins of dynamically create controls.
        mLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val margin = resources.getDimension(R.dimen._3sdp).toInt()
        mLayoutParams.setMargins(0, 0, margin, 0)
    }

    private fun favouriteMovie() {
        val movie = intent?.getParcelableExtra<Movie>("MOVIE") ?: return
        if(isFavoriteMovie) {
            // Remove from favourites
            isFavoriteMovie = false
            dataSource.removeFromFavourite(movie)
            menuItem?.let {
                it.title = "Remove from Favorite"
                it.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_heart_blank, theme)
            }
            Toast.makeText(this, "Removed from favourite", Toast.LENGTH_SHORT).show()
        }
        else {
            // Add to favourite
            isFavoriteMovie = true
            dataSource.addToFavourite(movie)
            menuItem?.let {
                it.title = "Add to Favorite"
                it.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_heart, theme)
            }
            Toast.makeText(this, "Added to favourite", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.favorite_menu, menu)
        menuItem = menu?.findItem(R.id.action_favorite)

        menuItem?.let {
            it.title = if (isFavoriteMovie) "Add to favourite" else "Removed from favourite"
            it.icon = ResourcesCompat.getDrawable(
                resources,
                if (isFavoriteMovie) R.drawable.ic_heart else R.drawable.ic_heart_blank,
                theme
            )
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        else if (item.itemId == R.id.action_favorite) {
            favouriteMovie()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        adsManager?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adsManager?.onResume()

        if(!isAdLoaded) {
            isAdLoaded = true
            adsManager?.loadInterstitialAd()
        }
        else {
            adsManager?.showInterstitialAd()
        }
    }

    override fun onDestroy() {
        intent.removeExtra("MOVIE")
        adsManager?.onDestroy()
        super.onDestroy()
    }
}