package com.howell.activity.fragment

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
import lecho.lib.hellocharts.formatter.AxisValueFormatter
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter
import lecho.lib.hellocharts.gesture.ContainerScrollType
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import java.util.*
import kotlin.collections.ArrayList
import lecho.lib.hellocharts.model.Viewport



/**
 * Created by Administrator on 2017/12/14.
 */
 abstract class LineChartBaseFragment:Fragment(),IPDCContract.IView {

    protected var mChartView: LineChartView?=null
    protected var mPresent:IPDCContract.IPresent?=null
    protected var mID:String ?= null
    protected var mData: Array<IntArray>?=null
    protected var colorUtil = intArrayOf(Color.parseColor("#2a7ac2"), Color.parseColor("#c09237"))
    protected var mLcData: LineChartData?    = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v  = inflater?.inflate(R.layout.fragment_line_charts, container, false)
        mChartView = v?.findViewById(R.id.lcv)
        bindPresenter()
        getData()
        return v
    }

    override fun bindPresenter() {
        if (mPresent==null)mPresent = PDCHttpPresenter()
        mPresent?.bindView(this)
        mPresent?.init(context)
    }

    override fun unbindPresenter() {
        mPresent?.unbindView()
        mPresent = null
    }


    protected abstract fun getData()

    override fun onQueryDeviceResult(deviceList: ArrayList<PDCDevice>) {}


    protected fun fillData(simpleList:ArrayList<PDCSample>,calendarField:Int):Float{
//        if (calendarField==Calendar.MINUTE)return test()
        var topValue = 0
        for (s in simpleList){
            val c = Calendar.getInstance()
            c.time = Util.ISODateString2ISODate(s.begTime)
            val hour = c.get(calendarField)
            Log.i("123","time=${s.begTime}    hour=$hour   in=${s.enterNumber}   out=${s.leaveNumber}")
            mData!![0][hour] = s.enterNumber
            mData!![1][hour] = s.leaveNumber
            if (s.enterNumber>topValue)topValue = s.enterNumber
            if (s.leaveNumber>topValue)topValue = s.leaveNumber
        }
        return topValue+0f
    }

    fun test():Float{
        var max = 0
        for (i in 0..59){
            mData!![0][i] = (Math.random() * 100).toInt()
            mData!![1][i] = (Math.random() * 100).toInt()
            if (mData!![0][i]>max) max =  mData!![0][i]
            if (mData!![1][i]>max) max =  mData!![1][i]
        }
        return max+0f
    }


    protected fun generateData(len:Int, title:String,xVal: List<AxisValue>,topVal:Float){
        val lines = ArrayList<Line>()
        for (i in 0..1) {
            val values = ArrayList<PointValue>()
            for (j in 0 until len) {
                val str = "进入：" + mData!![0][j] + "\u000d" +
                        "出去：" + mData!![1][j]
                values.add(PointValue(j.toFloat(), mData!![i][j].toFloat()).setLabel(str))
            }
            val line = Line(values)

            line.color = colorUtil[i]
            line.shape = ValueShape.CIRCLE
            line.strokeWidth = 1//线粗细
            line.pointRadius = if(len<30) 3 else 1
            line.isCubic  = true
            line.isFilled = true
            line.setHasLabels(true)
            line.setHasLabelsOnlyForSelected(true)
            line.setHasLines(true)
            line.setHasPoints(true)

            lines.add(line)
        }


        mLcData = LineChartData(lines)

        mLcData!!.axisXBottom = Axis().setName(title).setValues(xVal)
        mLcData!!.axisXBottom.maxLabelChars = 5
//        if(len>24)
//        mLcData!!.axisXBottom.maxLabelChars = if(len<25) 1 else 5
        mLcData!!.axisYLeft = Axis().setHasLines(true).setName("人数")
        mLcData!!.baseValue = java.lang.Float.NEGATIVE_INFINITY
//        mLcData!!.baseValue = java.lang.Float.NaN

        mChartView!!.lineChartData = mLcData

        mChartView?.isViewportCalculationEnabled = false

        val v = Viewport(mChartView?.maximumViewport)
        v.bottom = 0f
        v.top = mChartView?.maximumViewport?.top?.times(1.25f)!!
        v.left = 0f
        v.right = (len - 1).toFloat()
        mChartView?.maximumViewport = v
        mChartView?.currentViewport = v



        mChartView?.setCurrentViewportWithAnimation(v)
        mChartView?.isZoomEnabled = false
        mChartView?.isValueSelectionEnabled = true

       // mChartView?.setZoomLevelWithAnimation(0f, 0f, 3f)
//        mChartView?.isInteractive = true
//        mChartView?.zoomType = ZoomType.HORIZONTAL
//        mChartView?.maxZoom = 2.5f
//        mChartView?.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

    }
}