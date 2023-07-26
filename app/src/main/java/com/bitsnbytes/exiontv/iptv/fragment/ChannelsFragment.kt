package com.bitsnbytes.exiontv.iptv.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bitsnbytes.exiontv.iptv.adapter.ChannelAdapter
import com.bitsnbytes.exiontv.iptv.databinding.FragmentTvCategoryBinding
import com.bitsnbytes.exiontv.iptv.models.Channel
import com.bitsnbytes.exiontv.iptv.utils.AdsManager
import com.bitsnbytes.exiontv.iptv.utils.ApiClient
import com.bitsnbytes.exiontv.iptv.utils.FileDownloader

class ChannelsFragment : Fragment(), FileDownloader.FileDownloadListener {

    private var _bindings: FragmentTvCategoryBinding? = null
    private val binding get() = _bindings!!

    private var displayAdIn = 3
    private var adsManager: AdsManager? = null
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

        val fileUrl = arguments?.getString("PLAYLIST_URL")
        if(fileUrl == null) {
            binding.apply {
                progressBar.visibility = View.GONE
                rvChannelCategory.visibility = View.GONE
                textNoChannelFound.visibility = View.VISIBLE
            }
        }

        fileDownloader = FileDownloader(requireContext())
        fileDownloader.setFileDownloadListener(this)
        fileDownloader.getIPTVFile(ApiClient.IPTV_URL, fileUrl!!)

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

        adsManager = AdsManager(requireContext())
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
            adapter = ChannelAdapter(requireContext(), mPlaylist, true)
            binding.apply {
                progressBar.visibility = View.GONE
                rvChannelCategory.visibility = View.VISIBLE
                rvChannelCategory.adapter = adapter
            }
        }
    }

    override fun onResume() {
        super.onResume()

        displayAdIn--
        if(displayAdIn == 1) {
            adsManager?.loadInterstitialAd()
        }

        if(displayAdIn == 0) {
            displayAdIn = 3
            adsManager?.showInterstitialAd()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mPlaylist.isNotEmpty())
            mPlaylist.clear()
        adsManager?.onDestroy()
        fileDownloader.removeListener()
        arguments?.remove("PLAYLIST_URL")
        _bindings = null
    }

    companion object {
        fun getInstance(playlistURL: String): ChannelsFragment {
            return ChannelsFragment().apply {
                arguments = Bundle().apply {
                    putString("PLAYLIST_URL", playlistURL)
                }
            }
        }
    }
}