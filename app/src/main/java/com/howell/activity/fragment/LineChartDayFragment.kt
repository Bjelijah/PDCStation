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
import com.howellsdk.utils.RxUtil
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

    private var hourX: ArrayList<AxisValue>?=null


    @SuppressLint("ValidFragment")
    constructor(id:String):this(){
        mID = id
    }



    override fun onQuerySimpleResult(simpleList: ArrayList<PDCSample>, unit: String) {
        Log.i("123","l=$simpleList")
        RxUtil.doRxTask(object :RxUtil.CommonTask<Objects>(){
            var top = 0f
            override fun doInIOThread() {
                top = fillData(simpleList,Calendar.HOUR_OF_DAY)
            }

            override fun doInUIThread() {
                generateData(DATA_LEN,"时",hourX!!,top)
                mChartView?.visibility = View.VISIBLE
            }
        })
    }

    override fun getData(){
        val dateNow  = Date()
        val c = Calendar.getInstance()
        c.time = dateNow
//        c.add(Calendar.DAY_OF_MONTH,-5)
        c.set(Calendar.HOUR_OF_DAY,0)
        c.set(Calendar.MINUTE,0)
        val dateBefore = c.time
        val nowStr = Util.Date2ISODate(dateNow)
        val beforeStr = Util.Date2ISODate(dateBefore)
//        val nstr = Util.ISODateString2Date(nowStr)
//        val bstr = Util.ISODateString2Date(beforeStr)
//        Log.e("123","day   bef=$beforeStr  $bstr          now=$nowStr   $nstr")

        mPresent?.querySamples(mID!!,"Hour",beforeStr,nowStr)
    }

    override fun fillData(simpleList: ArrayList<PDCSample>, calendarField: Int): Float {
        var topValue = 0
        for (s in simpleList){
            val c = Calendar.getInstance()
            c.time = Util.ISODateString2ISODate(s.begTime)
            var str = Util.ISODateString2Date(s.begTime)
            val hour = c.get(calendarField)
//            Log.i("123","time=${s.begTime}  str=$str   field=$hour   in=${s.enterNumber}   out=${s.leaveNumber}")
            val index = (0 until hourX!!.size).firstOrNull { String(hourX!![it].labelAsChars).substring(0,2).equals(String.format("%02d",hour)) } ?: -1
            mData!![0][index] = s.enterNumber
            mData!![1][index] = s.leaveNumber
            if (s.enterNumber>topValue)topValue = s.enterNumber
            if (s.leaveNumber>topValue)topValue = s.leaveNumber
        }
        return topValue+0f
    }

    override fun init() {
        mData = Array(DATA_TYPE) { IntArray(DATA_LEN) }
        hourX =  ArrayList<AxisValue>()
        (0 until DATA_LEN).mapTo(hourX!!) { AxisValue(it.toFloat()).setLabel(String.format("%02d:00", it)) }
    }



}