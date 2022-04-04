package com.example.checkcoin.view.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.checkcoin.view.candlestick.CandleStickFragment
import com.example.checkcoin.view.ticker.TickerFragment

//ViewPager Adapter
class MyPagerAdapter(fragmentManager : FragmentManager, private val title : String) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    //        private val title : String = title
    companion object {
        var NUM_ITEMS : Int = 2;
    }
    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> { return CandleStickFragment.newInstance(0, title)
            }
            1 -> { return TickerFragment.newInstance(1, title) }
        }
        return Fragment()
    }

    override fun getCount(): Int {
        return NUM_ITEMS
    }
}