package com.howell.activity

import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.howell.activity.fragment.LineChartDayFragment
import com.howell.activity.fragment.LineChartHourFragment
import com.howell.activity.fragment.LineColumnChartFragment
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/12/12.
 */
class ChartActivity : BaseActivity() {

    @BindView(R.id.charts_tab) lateinit var mTab:TabLayout
    @BindView(R.id.charts_vp) lateinit var mVp:ViewPager

    override fun findView() {
        setContentView(R.layout.activity_charts)
        ButterKnife.bind(this)
    }

    override fun initView() {
//        supportFragmentManager.beginTransaction().add(R.id.charts_container, LineChartFragment(intent.getStringExtra("id"))).commit()
        mTab.addTab(mTab.newTab().setText(getString(R.string.charts_hour_tab)))
        mTab.addTab(mTab.newTab().setText(getString(R.string.charts_day_tab)))
        mTab.addTab(mTab.newTab().setText(getString(R.string.charts_week_tab)))
        mTab.tabGravity = TabLayout.GRAVITY_FILL
        mVp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTab))
        mTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabUnselected(p0: TabLayout.Tab?) {}

            override fun onTabSelected(t: TabLayout.Tab?) { mVp.currentItem = t?.position?:0 }

            override fun onTabReselected(p0: TabLayout.Tab?) {}
        })
        mVp.adapter = MyChartsPageAdapter(supportFragmentManager,getChartsFragmnet())
    }

    override fun initData() {

    }

    private fun getChartsFragmnet():ArrayList<Fragment>{
        val id = intent.getStringExtra("id")
        val l:ArrayList<Fragment> = ArrayList()
        l.add(LineChartHourFragment(id))
        l.add(LineChartDayFragment(id))
        l.add(LineColumnChartFragment(id))
        return l
    }


    inner class MyChartsPageAdapter(fm:FragmentManager) : FragmentPagerAdapter(fm){

        constructor(fm:FragmentManager,l:ArrayList<Fragment>):this(fm){
            mList = l
        }

        private var mList:ArrayList<Fragment> = ArrayList()
        override fun getItem(position: Int): Fragment = mList[position]

        override fun getCount(): Int = mList.size


    }

}