package com.bitsnbytes.exiontv.iptv.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.bitsnbytes.exiontv.iptv.MainActivity
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.fragment.TvCategoryFragment
import com.bitsnbytes.exiontv.iptv.models.ChannelPlaylist

class TvChannelAdapter(val context: Context, val channelPlaylist: List<ChannelPlaylist>) : BaseAdapter() {

    private var mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = null

        if (view == null) {
            view = mInflater.inflate(R.layout.channel_playlist, parent, false)
        }

        view?.let {
            val categoryName: TextView = it.findViewById(R.id.categoryName)
            val categoryIcon: ImageView = it.findViewById(R.id.categoryIcon)

            val channelPlaylist = channelPlaylist[position]
            categoryName.text = channelPlaylist.name
            categoryIcon.setImageDrawable(ResourcesCompat.getDrawable(context.resources, channelPlaylist.icon, null))

            view.setOnClickListener{
                val fragment = TvCategoryFragment.getInstance(channelPlaylist.name)
               (context as MainActivity).supportFragmentManager.beginTransaction()
                   .add(R.id.fragment_container, fragment, "CHANNELS_CATEGORY_FRAGMENT")
                   .addToBackStack(null)
                   .commit()
            }
        }

        return view!!
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return channelPlaylist.size
    }
}