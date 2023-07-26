package com.bitsnbytes.exiontv.iptv.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitsnbytes.exiontv.iptv.MainActivity
import com.bitsnbytes.exiontv.iptv.adapter.MovieAdapter
import com.bitsnbytes.exiontv.iptv.databinding.FragmentMovieSearchBinding
import com.bitsnbytes.exiontv.iptv.models.Movie
import com.bitsnbytes.exiontv.iptv.models.MovieCategory
import com.bitsnbytes.exiontv.iptv.models.ServerResponse
import com.bitsnbytes.exiontv.iptv.utils.AdsManager
import com.bitsnbytes.exiontv.iptv.utils.ApiClient
import com.bitsnbytes.exiontv.iptv.utils.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MovieSearchFragment : Fragment() {

    private var _binding: FragmentMovieSearchBinding? = null
    private val binding get() = _binding!!

    private var mPageNo: Int = 1
    private var totalResults: Int = 0
    private var getMovies: String? = null
    private var isLastItem: Boolean = false
    private var movieSearch: Boolean = false
    private var queryMovie: String? = null
    private var isAlreadyFetchingList: Boolean = false
    private var mMovieList: MutableList<Movie> = mutableListOf()
    private lateinit var adapter: MovieAdapter

    private var displayAdIn = 2
    private var adsManager: AdsManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMovieSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        getMovies = arguments?.getString("MOVIE_TYPE")
        var movieCategory = arguments?.getParcelable<MovieCategory>("MOVIE_CATEGORY")
        movieSearch = arguments?.getBoolean("MOVIE_SEARCH", false) ?: false

        adsManager = AdsManager(requireContext())
        val layoutManager = GridLayoutManager(requireContext(), calculateSpanCount(), RecyclerView.VERTICAL, false)
        adapter = MovieAdapter(requireContext(), mMovieList)
        binding.rvMoviesList.layoutManager = layoutManager
        binding.rvMoviesList.adapter = adapter

        if(movieCategory != null) {
            findMovies(movieCategoryId = movieCategory.categoryId, pageNo = mPageNo)
        }
        else if(getMovies != null) {
            findMovies(movieType = getMovies, pageNo = mPageNo)
        }
        else if(movieSearch) {
            binding.apply {
                rvMoviesList.visibility = View.GONE
                progressBarMovie.visibility = View.GONE
                searchLayout.visibility = View.VISIBLE
            }
        }

        binding.rvMoviesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                isLastItem = layoutManager.findLastCompletelyVisibleItemPosition() + 1 == adapter.itemCount

                if (!isAlreadyFetchingList && isLastItem) {
                    if (mPageNo == totalResults) {
                        binding.loadingData.visibility = View.GONE
                        binding.noMoreRecord.visibility = View.VISIBLE
                        return
                    }

                    displayAdIn--
                    if(displayAdIn == 0) {
                        displayAdIn = 2
                        adsManager?.showInterstitialAd()
                    }
                    if(displayAdIn == 1) {
                        adsManager?.loadInterstitialAd()
                    }

                    binding.loadingData.visibility = View.VISIBLE

                    if (isLastItem && !isAlreadyFetchingList) {
                        // Fetch more list items...
                        mPageNo += 1
                        isAlreadyFetchingList = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (queryMovie != null && getMovies == null && movieSearch)
                                findMovies(movieName = queryMovie, pageNo = mPageNo)
                            else if (movieCategory != null && getMovies == null && queryMovie == null)
                                findMovies(movieCategoryId = movieCategory!!.categoryId, pageNo = mPageNo)
                            else if (getMovies != null)
                                findMovies(movieType = getMovies, pageNo = mPageNo)
                        }, 1000)
                    }

                    isLastItem = false
                }
            }
        })

        binding.searchMovie.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mPageNo = 1
                    getMovies = null
                    movieCategory = null
                    queryMovie = binding.searchMovie.text.toString()

                    binding.apply {
                        noDataFound.visibility = View.GONE
                        noMoreRecord.visibility = View.GONE
                        searchLayout.visibility = View.GONE
                        progressBarMovie.visibility = View.VISIBLE
                    }

                    if(mMovieList.isNotEmpty()) {
                        mMovieList.clear()
                        adapter.notifyDataSetChanged()
                    }

                    findMovies(movieName = queryMovie, pageNo = mPageNo)

                    // hide virtual keyboard
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.searchMovie.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                    return true
                }

                return false
            }
        })
    }

    private fun findMovies(movieType: String? = null, movieName: String? = null, movieCategoryId: Int? = null, pageNo: Int) {
        isAlreadyFetchingList = true
        var moviesList: Call<ServerResponse>? = null
        val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)

        if (movieCategoryId != null && movieType == null && movieName == null) {
            // Finds the movie that matches with the relevant category e.g: Action, Adventure... etc
            moviesList = apiInterface.getCategoryMovies(movieCategoryId, pageNo)
        }
        else if (movieName != null && movieType == null && movieCategoryId == null) {
            // Finds movie by its name
            moviesList = apiInterface.findMovie(movieName, pageNo)
        }
        else if (movieName == null && movieCategoryId == null) {
            // Finds movie by its time released, rated... etc
            moviesList = when (getMovies) {
                "Upcoming Movies" -> apiInterface.getUpcomingMovies()
                "Top Rated Movies" -> apiInterface.getTopRatedMovies()
                "Popular Movies" -> apiInterface.getPopularMovies()
                else -> null
            }
        }

        if (moviesList != null) {
            moviesList.enqueue(object : Callback<ServerResponse> {
                override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                    if (response.isSuccessful) {
                        var isListUpdated = false
                        val previousDataSize = mMovieList.size

                        if(mMovieList.isNotEmpty()) {
                            isListUpdated = true
                            binding.loadingData.visibility = View.GONE
                        }

                        mPageNo = response.body()?.page ?: mPageNo
                        totalResults = response.body()?.totalPages ?: 0
                        mMovieList.addAll(response.body()?.moviesLists ?: mutableListOf())

                        if(isListUpdated) {
                            adapter.notifyItemRangeChanged(previousDataSize - 1, mMovieList.size)
                        }
                        else {
                            adapter.notifyDataSetChanged()
                        }

                        // No Movies found
                        if(mMovieList.isEmpty() || totalResults == 0) {
                            binding.rvMoviesList.visibility = View.GONE
                            binding.progressBarMovie.visibility = View.GONE
                            binding.noDataFound.visibility = View.VISIBLE
                        }
                        else {
                            binding.rvMoviesList.visibility = View.VISIBLE
                            binding.progressBarMovie.visibility = View.GONE
                        }
                    }
                    else {
                        binding.rvMoviesList.visibility = View.GONE
                        binding.progressBarMovie.visibility = View.GONE
                        binding.noDataFound.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "Error " + response.message(), Toast.LENGTH_SHORT).show()
                    }

                    isAlreadyFetchingList = false
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    isAlreadyFetchingList = false
                }
            })
        } else {
            isAlreadyFetchingList = false
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateSpanCount(): Int {
        val display = (context as MainActivity).windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = resources.displayMetrics.density
        val dpWidth = outMetrics.widthPixels / density

        return when(dpWidth.toInt()) {
            in 0..480 -> 3
            in 481..640 -> 4
            in 641..720 -> 5
            else -> 6
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mMovieList.clear()
        arguments?.let {
            it.remove("MOVIE_TYPE")
            it.remove("MOVIE_CATEGORY")
            it.remove("MOVIE_SEARCH")
        }
    }

    companion object {
        fun getInstance(movieCategory: MovieCategory?, searchMovie: Boolean?, movieType: String?): MovieSearchFragment {
            val bundle = Bundle()
            val fragment = MovieSearchFragment()

            if(movieType != null)
                bundle.putString("MOVIE_TYPE", movieType)

            if(movieCategory != null)
                bundle.putParcelable("MOVIE_CATEGORY", movieCategory)

            if(searchMovie != null)
                bundle.putBoolean("MOVIE_SEARCH", searchMovie)

            fragment.arguments = bundle
            return fragment
        }
    }
}