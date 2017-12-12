package com.howell.activity.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.howell.datetime.ScreenInfo
import com.howell.datetime.WheelDate
import com.howell.datetime.WheelMain
import com.howell.pdcstation.R
import java.util.*

/**
 * Created by Administrator on 2017/12/6.
 */
class WheelTimeFragment() : Fragment() {

    var mWheel : WheelMain?=null

    var mYear  = 0
    var mMonth = 0
    var mDay   = 0
    var mHour  = 0
    var mMin   = 0
    var mSec   = 0
    var mTimeStr:String = ""
    var mListener : OnTimeChangedLinstener? = null


    @SuppressLint("ValidFragment")
    constructor(o:OnTimeChangedLinstener):this(){
        mListener = o
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.timepicker,null)
        val info = ScreenInfo(activity)
        val country = context.resources.configuration.locale.country
        mWheel = WheelMain(v,country)
        mWheel?.screenheight = info.height
        val calendar = Calendar.getInstance()
        val date = Date()
        calendar.time = date
        mYear   = calendar.get(Calendar.YEAR)
        mMonth  = calendar.get(Calendar.MONTH) + 1
        mDay    = calendar.get(Calendar.DAY_OF_MONTH)
        mHour   = calendar.get(Calendar.HOUR_OF_DAY)
        mMin = calendar.get(Calendar.MINUTE)
        mSec = calendar.get(Calendar.SECOND)
        mWheel?.initDateTimePicker(mYear, mMonth -1 , mDay,mHour,mMin,mSec,{v,flag->
            when(flag){
                WheelDate.YEAR    ->{mYear=v;setTime()}
                WheelDate.MONTH   ->{mMonth=v;setTime()}
                WheelDate.DAY     ->{mDay=v;setTime()}
                WheelDate.HOUR    ->{mHour=v;setTime()}
                WheelDate.MIN     ->{mMin=v;setTime()}
                WheelDate.SEC     ->{mSec=v;setTime()}
            }
            mListener?.onTimeChanged(mTimeStr)
        })
        setTime()
        mListener?.onTimeChanged(mTimeStr)
        return mWheel?.view
    }

    private fun setTime(){
        mTimeStr = String.format("%04d-%02d-%02d %02d:%02d:%02d",mYear,mMonth,mDay,mHour,mMin,mSec)
    }

    interface OnTimeChangedLinstener{
        fun onTimeChanged(s:String)
    }

}