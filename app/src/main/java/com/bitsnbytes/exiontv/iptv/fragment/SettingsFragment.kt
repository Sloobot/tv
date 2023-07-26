package com.bitsnbytes.exiontv.iptv.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bitsnbytes.exiontv.iptv.R
import com.bitsnbytes.exiontv.iptv.databinding.FragmentSettingsBinding
import com.bitsnbytes.exiontv.iptv.databinding.FragmentTvCategoryBinding


class SettingsFragment : Fragment() {

    private var _bindings: FragmentSettingsBinding? = null
    private val binding get() = _bindings!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().supportFragmentManager
            .beginTransaction()
            .add(R.id.settingsFragment, PreferenceFragment())
            .commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _bindings = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*requireActivity().supportFragmentManager
            .beginTransaction()
            .add(R.id.settingsFragment, PreferenceFragment(), "PREFERENCE_FRAGMENT")
            .commit()*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindings = null
    }
}