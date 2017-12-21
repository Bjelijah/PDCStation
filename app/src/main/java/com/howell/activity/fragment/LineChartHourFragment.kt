package com.howell.activity.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.howellsdk.net.http.bean.PDCSample
import com.howellsdk.utils.RxUtil
import com.howellsdk.utils.Util
import lecho.lib.hellocharts.model.AxisValue
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Administrator on 2017/12/14.
 */
class LineChartHourFragment() : LineChartBaseFragment() {

    companion object {
        val DATA_TYPE = 2 //0进入 1出去
        val DATA_LEN  = 60 // 60分钟
    }
    private var minuteX: ArrayList<AxisValue>?=null
    @SuppressLint("ValidFragment")
    constructor(id:String):this(){
        mID = id
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onQuerySimpleResult(simpleList: ArrayList<PDCSample>, unit: String) {
        Log.i("123","l=$simpleList")
        RxUtil.doRxTask(object :RxUtil.CommonTask<Objects>(){
            var top = 0f
            override fun doInIOThread() {
                top = fillData(simpleList,Calendar.MINUTE)
            }

            override fun doInUIThread() {
                generateData(DATA_LEN,"分",minuteX!!,top)
                mChartView?.visibility = View.VISIBLE
            }

        })
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


    override fun fillData(simpleList: ArrayList<PDCSample>, calendarField: Int): Float {
        var topValue = 0
        for (s in simpleList){
            val c = Calendar.getInstance()
            c.time = Util.ISODateString2ISODate(s.begTime)
            var str = Util.ISODateString2Date(s.begTime)
            val min = c.get(calendarField)
//            Log.i("123","time=${s.begTime}  str=$str      field=$min   in=${s.enterNumber}   out=${s.leaveNumber}")
            val index = (0 until minuteX!!.size).firstOrNull { String(minuteX!![it].labelAsChars).substring(3,5).equals(String.format("%02d",min)) } ?: -1
//            Log.i("123","index=$index")
            mData!![0][index] = s.enterNumber
            mData!![1][index] = s.leaveNumber
            if (s.enterNumber>topValue)topValue = s.enterNumber
            if (s.leaveNumber>topValue)topValue = s.leaveNumber
        }
        return topValue+0f
    }

    override fun init(){
        mData = Array(DATA_TYPE) { IntArray(DATA_LEN) }
        minuteX = ArrayList<AxisValue>()
        val data = Date()
        val c = Calendar.getInstance()
        c.time = data
        c.add(Calendar.HOUR_OF_DAY, -1)
        Log.i("123", "c=" + c.toString())
        for (i in 0 until DATA_LEN ) {
            val str = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
            minuteX!!.add(AxisValue(i.toFloat()).setLabel(str))
            c.add(Calendar.MINUTE, 1)
        }
    }
}