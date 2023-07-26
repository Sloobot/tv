package com.bitsnbytes.exiontv.iptv.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitsnbytes.exiontv.iptv.MainActivity
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.TvPlayerActivity
import com.bitsnbytes.exiontv.iptv.databinding.ChannelLayoutBinding
import com.bitsnbytes.exiontv.iptv.fragment.ChannelsFragment
import com.bitsnbytes.exiontv.iptv.models.Channel
import com.bitsnbytes.exiontv.iptv.utils.ApiClient
import com.bitsnbytes.exiontv.iptv.utils.PrefsManager
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class ChannelAdapter(context: Context, channelsList: List<Channel>, openPlayer: Boolean = false) :
    RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    private var mContext: Context
    private var mChannelsList: List<Channel>
    private var mOpenPlayer: Boolean
    private val openDefaultPlayer: Boolean

    init {
        mContext = context
        val prefManager = PrefsManager(context)
        val defaultPlayer = prefManager.getPrefs("default_player")
        openDefaultPlayer = defaultPlayer.isEmpty() || defaultPlayer == "1"
        mOpenPlayer = openPlayer
        mChannelsList = channelsList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChannelLayoutBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channel = mChannelsList[position]

        holder.binding.apply {
            channelName.text = channel.name

            val alpha2CountryCode = channel.url.substring(channel.url.indexOf('/') + 1, channel.url.indexOf('.'))
            if(alpha2CountryCode.length == 2) {
                Picasso.get()
                    .load(String.format(ApiClient.COUNTRY_FLAG_URL, alpha2CountryCode))
                    .placeholder(R.drawable.ic_live_tv)
                    .error(R.drawable.ic_live_tv)
                    .into(channelLogo)
            }
            else if(channel.icon.isNotEmpty()) {
                Picasso.get()
                    .load(channel.icon)
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.ic_live_tv)
                    .error(R.drawable.ic_live_tv)
                    .into(channelLogo)
            }

            root.setOnClickListener {
                if(!mOpenPlayer) {
                    val fragment = ChannelsFragment.getInstance(channel.url)
                    (mContext as MainActivity).supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, "CHANNELS_FRAGMENT")
                        .addToBackStack(null)
                        .commit()
                }
                else {
                    if(openDefaultPlayer) {
                        val intent = Intent(mContext, TvPlayerActivity::class.java)
                        intent.putExtra("CHANNEL", channel)
                        mContext.startActivity(intent)
                    }
                    else {
                        openInExternalPlayer(channel.url)
                    }
                }
            }
        }
    }

    private fun openInExternalPlayer(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "video/*");
        mContext.startActivity(Intent.createChooser(intent, "Play this channel using..."));
    }

    override fun getItemCount(): Int {
        return mChannelsList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun filterChannels(filterChannels: List<Channel>) {
        mChannelsList = filterChannels
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ChannelLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}