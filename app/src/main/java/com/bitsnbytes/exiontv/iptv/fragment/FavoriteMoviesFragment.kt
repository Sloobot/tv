package com.bitsnbytes.exiontv.iptv.fragment

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.adapter.MovieAdapter
import com.bitsnbytes.exiontv.iptv.database.DataSource
import com.bitsnbytes.exiontv.iptv.databinding.FragmentFavoriteMoviesBinding
import com.bitsnbytes.exiontv.iptv.models.Movie

class FavoriteMoviesFragment : Fragment() {

    private var _binding: FragmentFavoriteMoviesBinding? = null
    private val binding get() = _binding!!

    private var isAlreadyGotList = false
    private lateinit var adapter: MovieAdapter
    private lateinit var dataSource: DataSource
    private var movieList: MutableList<Movie> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFavoriteMoviesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dataSource = DataSource(requireContext())
        isAlreadyGotList = true
        movieList.addAll(dataSource.getAllFavouriteMovies().toMutableList())
        val layoutManager = GridLayoutManager(requireContext(), calculateSpanCount(), RecyclerView.VERTICAL, false)

        if (movieList.isNotEmpty()) {
            adapter = MovieAdapter(requireContext(), movieList)
            binding.rvFavoriteMovies.layoutManager = layoutManager
            binding.rvFavoriteMovies.adapter = adapter
        } else {
            binding.rvFavoriteMovies.visibility = View.GONE
            binding.noFavoriteMovie.visibility = View.VISIBLE
        }
    }

    private fun calculateSpanCount(): Int {
        if (activity == null) return 3

        val display = requireActivity().windowManager.defaultDisplay
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

    override fun onResume() {
        super.onResume()

        if (isAlreadyGotList) {
            isAlreadyGotList = false
            return
        }

        if (movieList.isNotEmpty()) {
            movieList.clear()
            movieList.addAll(dataSource.getAllFavouriteMovies())

            adapter.notifyDataSetChanged()
            if (movieList.isEmpty()) {
                adapter.notifyDataSetChanged()
                binding.rvFavoriteMovies.visibility = View.GONE
                binding.noFavoriteMovie.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (movieList.isNotEmpty())
            movieList.clear()
    }
}
