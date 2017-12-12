package com.howell.activity

import com.howell.activity.fragment.LineColumnChartFragment
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/12/12.
 */
class LineColumnChartsActivity :BaseActivity() {
    override fun findView() {
        setContentView(R.layout.activity_line_column_charts)
    }

    override fun initView() {
        supportFragmentManager.beginTransaction().add(R.id.lc_charts_container, LineColumnChartFragment()).commit()
    }

    override fun initData() {
    }
}