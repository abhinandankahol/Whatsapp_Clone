package com.abhinandankahol.whatsappclone

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    fa: FragmentActivity,
    val frag1: Fragment,
    val frag2: Fragment,
    val frag3: Fragment
) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> frag1
            1 -> frag2
            2 -> frag3
            else -> frag1
        }
    }
}