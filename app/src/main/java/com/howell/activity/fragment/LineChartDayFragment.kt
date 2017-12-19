package com.howell.activity.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.howell.modules.pdc.IPDCContract
import com.howell.modules.pdc.bean.PDCDevice
import com.howell.modules.pdc.presenter.PDCHttpPresenter
import com.howell.pdcstation.R
import com.howellsdk.net.http.bean.PDCSample
import com.howellsdk.utils.Util
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Administrator on 2017/12/14.
 */
class LineChartDayFragment() :LineChartBaseFragment(),IPDCContract.IView {

    companion object {
        val DATA_TYPE = 2 //0进入 1出去
        val DATA_LEN  = 24 // 24小时
    }

    private val hourX: List<AxisValue>
        get() {
            val list = ArrayList<AxisValue>()
            (0 until DATA_LEN-1).mapTo(list) { AxisValue(it.toFloat()).setLabel(String.format("%02d:00", it)) }
            return list
        }

    @SuppressLint("ValidFragment")
    constructor(id:String):this(){
        mID = id
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mData = Array(DATA_TYPE) { IntArray(DATA_LEN) }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onQuerySimpleResult(simpleList: ArrayList<PDCSample>, unit: String) {
        Log.i("123","l=$simpleList")
        var top = fillData(simpleList,Calendar.HOUR_OF_DAY)
        generateData(DATA_LEN,"时",hourX,top)
        mChartView?.visibility = View.VISIBLE
    }

    override fun getData(){
        val dateNow  = Date()
        val c = Calendar.getInstance()
        c.time = dateNow
        c.add(Calendar.DAY_OF_MONTH,-5)
        val dateBefore = c.time
        val nowStr = Util.Date2ISODate(dateNow)
        val beforeStr = Util.Date2ISODate(dateBefore)
        mPresent?.querySamples(mID!!,"Hour",beforeStr,nowStr)
    }

}