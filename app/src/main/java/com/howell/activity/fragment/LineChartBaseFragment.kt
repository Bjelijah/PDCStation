package com.howell.activity.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.howell.modules.pdc.IPDCContract
import com.howell.modules.pdc.bean.PDCDevice
import com.howell.modules.pdc.presenter.PDCHttpPresenter
import com.howell.pdcstation.R
import com.howellsdk.net.http.bean.PDCSample
import com.howellsdk.utils.Util
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import java.util.*
import kotlin.collections.ArrayList
import lecho.lib.hellocharts.model.Viewport



/**
 * Created by Administrator on 2017/12/14.
 */
 abstract open class LineChartBaseFragment:Fragment(),IPDCContract.IView {

    protected var mChartView: LineChartView?=null
    protected var mPresent:IPDCContract.IPresent?=null
    protected var mID:String ?= null
    protected var mData: Array<IntArray>?=null
    protected var colorUtil = intArrayOf(Color.parseColor("#2a7ac2"), Color.parseColor("#c09237"))
    protected var colorBkUtil = intArrayOf(Color.parseColor("#a02a7ac2"), Color.parseColor("#a0c09237"))
    protected var mLcData: LineChartData?    = null

    lateinit var mPopWindowView: LinearLayout
    lateinit var mPopupWindow: PopupWindow
    lateinit var mPopTimeView:TextView
    lateinit var mPopEntryView:TextView
    lateinit var mPopOutView:TextView

    var mPointX:Float?=null
    var mPointY:Float?=null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v  = inflater?.inflate(R.layout.fragment_line_charts, container, false)
        mChartView = v?.findViewById(R.id.lcv)
        init()
        bindPresenter()
        initPopWindow()
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

    protected open fun init(){}

    protected abstract fun getData()

    override fun onQueryDeviceResult(deviceList: ArrayList<PDCDevice>) {}


    abstract fun fillData(simpleList:ArrayList<PDCSample>,calendarField:Int):Float//fixme
//        if (calendarField==Calendar.MINUTE)return test()

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

    private fun initPopWindow(){
        mPopWindowView = LayoutInflater.from(context).inflate(R.layout.popupwindow_chart_data,null) as LinearLayout

        mPopTimeView = mPopWindowView.findViewById(R.id.pop_chart_time)
        mPopEntryView = mPopWindowView.findViewById(R.id.pop_chart_entry)
        mPopOutView = mPopWindowView.findViewById(R.id.pop_chart_out)
        mPopupWindow = PopupWindow(mPopWindowView, WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT)
        mPopupWindow.isOutsideTouchable = true
    }

    protected fun generateData(len:Int, title:String, xVal: List<AxisValue>, topVal:Float){
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
            line.pointRadius = if(len<30) 3 else 2
            line.isCubic  = true
            line.isFilled = true
            line.setHasLabels(false)
            line.setHasLabelsOnlyForSelected(false)
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

        mChartView?.onValueTouchListener = object : LineChartOnValueSelectListener{
            override fun onValueSelected(lineIndex: Int, dataIndex: Int, p: PointValue?) {
                val time  = String(xVal[dataIndex].labelAsChars)
                val entry = mData!![0][dataIndex]
                val out   = mData!![1][dataIndex]

                mPopTimeView.text = "时间：$time"
                mPopEntryView.text = "进入：$entry"
                mPopOutView.text = "出去：$out"
                mPopupWindow.setBackgroundDrawable(ColorDrawable(colorBkUtil[lineIndex]))
                mPopupWindow.showAtLocation(mChartView, Gravity.NO_GRAVITY,mPointX?.toInt()?:0,mPointY?.toInt()?:0)
            }

            override fun onValueDeselected() {
            }
        }

        mChartView?.setOnTouchListener { _, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN->{
                    mPointX = event.rawX - mPopWindowView.width/2
                    mPointY = event.rawY - mPopWindowView.height
                }
                else->{}
            }
            false
        }

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