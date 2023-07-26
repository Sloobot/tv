package com.bitsnbytes.exiontv.iptv.fragment

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.adapter.ChannelAdapter
import com.bitsnbytes.exiontv.iptv.databinding.FragmentTvCategoryBinding
import com.bitsnbytes.exiontv.iptv.models.Channel
import com.bitsnbytes.exiontv.iptv.utils.ApiClient
import com.bitsnbytes.exiontv.iptv.utils.FileDownloader


class TvCategoryFragment : Fragment(), FileDownloader.FileDownloadListener {

    private var _bindings: FragmentTvCategoryBinding? = null
    private val binding get() = _bindings!!

    private lateinit var adapter: ChannelAdapter
    private lateinit var mPlaylist: MutableList<Channel>
    private lateinit var fileDownloader: FileDownloader

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _bindings = FragmentTvCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.progressBar.visibility = View.VISIBLE
        binding.rvChannelCategory.visibility = View.GONE

        fileDownloader = FileDownloader(requireContext())
        fileDownloader.setFileDownloadListener(this)
        val channelBy = arguments?.getString("CHANNEL_BY") ?: ""

        when(channelBy) {
            "By Country" -> fileDownloader.getIPTVFile(ApiClient.IPTV_INDEX_FILE_URL, "index.country.m3u")
            "By Category" -> fileDownloader.getIPTVFile(ApiClient.IPTV_INDEX_FILE_URL, "index.category.m3u")
            "By Language" -> fileDownloader.getIPTVFile(ApiClient.IPTV_INDEX_FILE_URL, "index.language.m3u")
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if(query.isNotEmpty()) {
                    val filterChannels = mutableListOf<Channel>()
                    mPlaylist.forEach { channel ->
                        if(channel.name.contains(query, true))
                            filterChannels.add(channel)
                    }
                    adapter.filterChannels(filterChannels)
                }
                else {
                    adapter.filterChannels(mPlaylist)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    override fun onFileDownloaded(playlist: ArrayList<Channel>) {
        //gotIPTVFile = true
        mPlaylist = playlist

        if(mPlaylist.isEmpty()) {
            binding.apply {
                progressBar.visibility = View.GONE
                rvChannelCategory.visibility = View.GONE
                textNoChannelFound.visibility = View.VISIBLE
            }
        }
        else {
            binding.progressBar.visibility = View.GONE
            binding.rvChannelCategory.visibility = View.VISIBLE
            val channelBy = arguments?.getString("CHANNEL_BY") ?: ""
            when (channelBy) {
                "By Country" -> adapter = ChannelAdapter(requireContext(), mPlaylist)
                "By Category", "By Language" -> adapter = ChannelAdapter(requireContext(), playlist)
            }
            binding.rvChannelCategory.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arguments?.remove("CHANNEL_BY")
        _bindings = null
        mPlaylist.clear()
    }

    companion object {
        fun getInstance(channelCategory: String): TvCategoryFragment {
            return TvCategoryFragment().apply {
                arguments = Bundle().apply {
                    putString("CHANNEL_BY", channelCategory)
                }
            }
        }
    }
}