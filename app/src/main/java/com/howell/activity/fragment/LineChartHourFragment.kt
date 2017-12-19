package com.howell.activity.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.howellsdk.net.http.bean.PDCSample
import com.howellsdk.utils.Util
import lecho.lib.hellocharts.model.AxisValue
import java.util.*

/**
 * Created by Administrator on 2017/12/14.
 */
class LineChartHourFragment() : LineChartBaseFragment() {

    companion object {
        val DATA_TYPE = 2 //0进入 1出去
        val DATA_LEN  = 60 // 60分钟
    }

    private val minuteX: List<AxisValue>
        get() {
//            val list = ArrayList<AxisValue>()
//            (0 until DATA_LEN -1).mapTo(list) { AxisValue(it.toFloat()).setLabel(String.format("%02d", it)) }
//            return list

            val list = ArrayList<AxisValue>()
            val data = Date()
            val c = Calendar.getInstance()
            c.time = data
            c.add(Calendar.HOUR_OF_DAY, -1)
            Log.i("123", "c=" + c.toString())
            for (i in 0 until DATA_LEN -1) {
                val str = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
                list.add(AxisValue(i.toFloat()).setLabel(str))
                c.add(Calendar.MINUTE, 1)
            }
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
        var top = fillData(simpleList,Calendar.MINUTE)
        generateData(DATA_LEN,"分",minuteX,top)
        mChartView?.visibility = View.VISIBLE
    }

    override fun getData() {
        val dateNow  = Date()
        val c = Calendar.getInstance()
        c.time = dateNow
        c.add(Calendar.HOUR_OF_DAY,-1)
        val dateBefore = c.time
        val nowStr = Util.Date2ISODate(dateNow)
        val beforeStr = Util.Date2ISODate(dateBefore)
        mPresent?.querySamples(mID!!,"Minute",beforeStr,nowStr)
    }
}