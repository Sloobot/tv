package com.bitsnbytes.exiontv.iptv.fragment

import am.appwise.components.ni.NoInternetDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bitsnbytes.exiontv.iptv.BuildConfig
import com.bitsnbytes.exiontv.iptv.MainActivity
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.adapter.MovieAdapter
import com.bitsnbytes.exiontv.iptv.adapter.SliderAdapter
import com.bitsnbytes.exiontv.iptv.adapter.TvChannelAdapter
import com.bitsnbytes.exiontv.iptv.database.DataSource
import com.bitsnbytes.exiontv.iptv.databinding.FragmentHomeBinding
import com.bitsnbytes.exiontv.iptv.models.ChannelPlaylist
import com.bitsnbytes.exiontv.iptv.models.MovieCategory
import com.bitsnbytes.exiontv.iptv.models.ServerResponse
import com.bitsnbytes.exiontv.iptv.utils.ApiClient
import com.bitsnbytes.exiontv.iptv.utils.ApiInterface
import com.google.android.exoplayer2.util.Util
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class HomeFragment : Fragment() {

    private var _bindings: FragmentHomeBinding? = null
    private val binding get() = _bindings!!

    private lateinit var dataSource: DataSource
    private var gotLatestMovie: Boolean = false
    private var gotMovieCategory: Boolean = false
    private var gotUpcomingMovies: Boolean = false
    private var gotTopRatedMovies: Boolean = false
    private var gotPopularMovies: Boolean = false
    private lateinit var noInternetDialog: NoInternetDialog

    private lateinit var mLayoutParams: LinearLayout.LayoutParams

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _bindings = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.progressBar.visibility = View.VISIBLE
        binding.movieContainer.visibility = View.GONE
        dataSource = DataSource(requireContext())
        noInternetDialog()
        setLayoutPrams()
        getMovieCategories()
        getLatestMovies()
        getUpcomingMovies()
        getTopRatedMovies()
        getPopularMovies()
        setupChannelsCategory()

        binding.apply {
            btnUpcomingMovies.setOnClickListener {
                loadMovieSearchFragment("Upcoming Movies")
            }

            btnTopRatedMovies.setOnClickListener {
                loadMovieSearchFragment("Top Rated Movies")
            }

            btnPopularMovies.setOnClickListener {
                loadMovieSearchFragment("Popular Movies")
            }
        }
    }

    private fun getMovieCategories() {
        val moviesCategory = dataSource.getAllGenre()

        if (moviesCategory.isEmpty()) {
            val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
            apiInterface.getMoviesCategory().enqueue(object : Callback<ServerResponse> {
                override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                    try {
                        if (response.isSuccessful) {

                            val moviesCategory = response.body()?.movieCategory
                            if (!moviesCategory.isNullOrEmpty()) {
                                dataSource.addAllGenre(moviesCategory)
                                for (movieCategory in moviesCategory) {
                                    setupMovieCategoryList(movieCategory)
                                }
                            }

                            gotMovieCategory = true
                            checkDataCompletion()
                        } else {
                            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    catch (e: Exception) { }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) { }
            })
        }
        else {
            gotMovieCategory = true
            for (movieCategory in moviesCategory) {
                setupMovieCategoryList(movieCategory)
            }
        }
    }

    private fun getLatestMovies() {
        val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        val getLatestMovies: Call<ServerResponse> = apiInterface!!.getLatestMovies()

        getLatestMovies.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                if(response.isSuccessful) {
                    try {
                        val latestMoviesList = response.body()!!.moviesLists.subList(0, 7)
                        val adapter = SliderAdapter(context!!, latestMoviesList)
                        binding.imageSlider.setSliderAdapter(adapter)
                        binding.imageSlider.startAutoCycle()

                        gotLatestMovie = true
                        checkDataCompletion()
                    }
                    catch (e: Exception) { }
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) { }
        })
    }

    private fun getUpcomingMovies() {
        val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        val upcomingMovies = apiInterface!!.getUpcomingMovies()

        upcomingMovies.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                try {
                    if (response.isSuccessful) {
                        val upcomingMoviesList = response.body()!!.moviesLists
                        val adapterUpcomingMovies =
                            MovieAdapter(requireContext(), upcomingMoviesList)
                        binding.rvUpcomingMovies.adapter = adapterUpcomingMovies

                        gotUpcomingMovies = true
                        checkDataCompletion()
                    } else {
                        Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception) { }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) { }
        })
    }

    private fun getTopRatedMovies() {
        val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        val topRatedMovies = apiInterface.getTopRatedMovies()

        topRatedMovies.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                try {
                    if (response.isSuccessful) {
                        val topRatedMoviesList = response.body()!!.moviesLists
                        val adapterTopRatedMovies =
                            MovieAdapter(requireContext(), topRatedMoviesList)
                        binding.rvTopRatedMovies.adapter = adapterTopRatedMovies

                        gotTopRatedMovies = true
                        checkDataCompletion()
                    } else {
                        Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception) { }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) { }
        })
    }

    private fun getPopularMovies() {
        val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        val popularMovies = apiInterface.getPopularMovies()

        popularMovies.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                try {
                    if (response.isSuccessful) {
                        val popularMoviesList = response.body()!!.moviesLists
                        val adapterPopularMovies = MovieAdapter(requireContext(), popularMoviesList)
                        binding.rvPopularMovies.adapter = adapterPopularMovies

                        gotPopularMovies = true
                        checkDataCompletion()
                    } else {
                        Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception) { }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) { }
        })
    }

    private fun checkDataCompletion() {
        if (gotMovieCategory && gotLatestMovie && gotUpcomingMovies && gotTopRatedMovies && gotPopularMovies) {
            binding.progressBar.visibility = View.GONE
            binding.movieContainer.visibility = View.VISIBLE
        }
    }

    private fun noInternetDialog() {
        noInternetDialog = NoInternetDialog.Builder(this).apply {
            setCancelable(false)
            setBgGradientStart(Color.parseColor("#303030"))
            setBgGradientCenter(Color.parseColor("#202020"))
            setBgGradientEnd(Color.parseColor("#151515"))
            setButtonColor(Color.parseColor("#FF5722"))
            if(Util.SDK_INT > 25) {
                setTitleTypeface(requireContext().resources.getFont(R.font.proxima_nova_alt_bold))
                setMessageTypeface(requireContext().resources.getFont(R.font.proxima_nova_alt_bold))
            }
        }.build()

        noInternetDialog.setOnDismissListener {
            if (!gotLatestMovie)
                getLatestMovies()

            if (!gotMovieCategory)
                getMovieCategories()

            if (!gotUpcomingMovies)
                getUpcomingMovies()

            if (!gotTopRatedMovies)
                getTopRatedMovies()

            if (!gotPopularMovies)
                getPopularMovies()
        }
    }

    private fun loadMovieSearchFragment(movieType: String) {
        val fragment = MovieSearchFragment.getInstance(null, null, movieType)
        requireActivity().supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment, "MOVIE_SEARCH_FRAGMENT")
            .addToBackStack(null)
            .commit()
    }

    private fun setupMovieCategoryList(movieCategory: MovieCategory) {
        val view = LayoutInflater.from(context).inflate(R.layout.movie_category, null)
        val textView: TextView = view.findViewById(R.id.movieCategory)
        textView.text = movieCategory.categoryName
        textView.setOnClickListener {
            val fragment = MovieSearchFragment.getInstance(movieCategory, null, null)
            (requireContext() as MainActivity).supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment, "MOVIE_SEARCH_FRAGMENT")
                .addToBackStack(null)
                .commit()
        }

        view.layoutParams = mLayoutParams
        binding.movieCategoryList.addView(view)
    }

    private fun setLayoutPrams() {
        // Converting and Setting up margins of dynamically create controls.
        mLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val margin = resources.getDimension(R.dimen._5sdp).toInt()
        mLayoutParams.setMargins(0, 0, margin, 0)
    }

    private fun setupChannelsCategory() {
        val channelCategoryList = listOf(
            ChannelPlaylist("By Country", R.mipmap.country),
            ChannelPlaylist("By Category", R.mipmap.category),
            ChannelPlaylist("By Language", R.mipmap.language)
        )

        val adapter = TvChannelAdapter(requireContext(), channelCategoryList)
        binding.gvChannelType.adapter = adapter
    }

    override fun onDestroyView() {
        _bindings = null
        noInternetDialog.onDestroy()
        super.onDestroyView()
    }
}