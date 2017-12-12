package com.howell.activity

import com.howell.activity.fragment.LineChartFragment
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/12/12.
 */
class LineChartActivity : BaseActivity() {

    override fun findView() {
        setContentView(R.layout.activity_charts)
    }

    override fun initView() {
        supportFragmentManager.beginTransaction().add(R.id.charts_container, LineChartFragment()).commit()
    }

    override fun initData() {

    }
}