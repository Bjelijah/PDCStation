package com.howell.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import com.howell.activity.fragment.PlayFunFragment
import com.howell.activity.fragment.PtzFunFragment

/**
 * Created by Administrator on 2017/12/4.
 */
class MyPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {
    private lateinit var mFragment:Fragment
    override fun getCount(): Int = Int.MAX_VALUE
    override fun getItem(position: Int): Fragment {
        var newPos:Int = if (position>=0) (position+200)%2 else (-position)%2
        when(newPos){
            0 -> mFragment = PtzFunFragment()
            1 -> mFragment = PlayFunFragment()
        }
        return mFragment
    }
    override fun getItemPosition(`object`: Any?): Int = PagerAdapter.POSITION_NONE
}