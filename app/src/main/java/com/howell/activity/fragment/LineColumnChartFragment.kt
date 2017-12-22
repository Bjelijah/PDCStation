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

import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.AxisValue
import lecho.lib.hellocharts.model.Column
import lecho.lib.hellocharts.model.ColumnChartData
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.model.SubcolumnValue
import lecho.lib.hellocharts.model.Viewport
import lecho.lib.hellocharts.view.ColumnChartView
import lecho.lib.hellocharts.view.LineChartView
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Administrator on 2017/10/31.
 */

class LineColumnChartFragment() : Fragment(), IPDCContract.IView {

    companion object {
        private val HOUR_OF_DAY = arrayOf("0:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00")

        private val MIN_OF_HOUR = arrayOf(":00", ":05", ":10", ":15", ":20", ":25", ":30", ":35", ":40", ":45", ":50", ":55")

        val DATA_TYPE = 2 //0进入 1出去
        val DATA_WEEK_LEN  = 7 // 7 day
        val DATA_DAY_LEN = 24 //24 hour
    }


    private var mChartTop: LineChartView? = null
    private var mChartBottom: ColumnChartView? = null
    private var colorUtil = intArrayOf(Color.parseColor("#2a7ac2"), Color.parseColor("#c09237"))
    private var lineData: LineChartData? = null
    private var columnData: ColumnChartData? = null
    private var mID:String ?= null
    private var mDataDay:Array<IntArray>? = null
    private var mDataWeek:Array<IntArray>? = null
    private var weekX: ArrayList<DataBeanX>?=null
    private var dayX: ArrayList<DataBeanX>?=null
    private var mPresenter:IPDCContract.IPresent?=null
    @SuppressLint("ValidFragment")
    constructor(id:String):this(){
        mID = id
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_line_column_chart, container, false)
        mChartTop = v.findViewById(R.id.lcc_chart)
        mChartBottom = v.findViewById(R.id.lcc_column_chart)
        init()
        bindPresenter()

//        generateColumnData()
        getData()
        return v
    }

    override fun bindPresenter() {
        if (mPresenter==null)  mPresenter = PDCHttpPresenter()
        mPresenter?.bindView(this)
        mPresenter?.init(context)
    }

    override fun unbindPresenter() {
        mPresenter?.unbindView()
        mPresenter = null
    }

    override fun onQueryDeviceResult(deviceList: ArrayList<PDCDevice>) {
    }

    override fun onQuerySimpleResult(simpleList: ArrayList<PDCSample>, unit: String) {
        Log.e("123","$unit ->   list=$simpleList")
        when(unit){
            "Day"->{
                RxUtil.doRxTask(object :RxUtil.CommonTask<Objects>(){
                    override fun doInIOThread() {
                        fillWeek(simpleList)
                    }

                    override fun doInUIThread() {
                        generateColumnData()
                    }
                })
            }
            "Hour"->{
                RxUtil.doRxTask(object :RxUtil.CommonTask<Objects>(){
                    var top = 0
                    override fun doInIOThread() {


                        top = fillDay(simpleList)
                    }

                    override fun doInUIThread() {
                        generateLineData(top,false)
                    }
                })
            }
        }

    }

    private fun init(){
        mDataWeek = Array(DATA_TYPE) { IntArray(DATA_WEEK_LEN) }
        mDataDay = Array(DATA_TYPE) { IntArray(DATA_DAY_LEN) }
        weekX =  ArrayList<DataBeanX>()
        val data = Date()
        val c = Calendar.getInstance()
        c.time = data
        c.set(Calendar.DAY_OF_WEEK,2)//周一
        c.set(Calendar.HOUR_OF_DAY,0)
        Log.i("123", "c=" + c.toString())
        for (i in 0 until DATA_WEEK_LEN) {
            var day = ""
            when(i){
                0->day=getString(R.string.charts_week_monday)
                1->day=getString(R.string.charts_week_tuesday)
                2->day=getString(R.string.charts_week_wednesday)
                3->day=getString(R.string.charts_week_thursday)
                4->day=getString(R.string.charts_week_friday)
                5->day=getString(R.string.charts_week_saturday)
                6->day=getString(R.string.charts_week_sunday)
            }
            val str = String.format("%02d/%02d($day)", c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH))

            weekX!!.add(DataBeanX(c.time,AxisValue(i.toFloat()).setLabel(str)))
            c.add(Calendar.DAY_OF_MONTH, 1)
        }
        dayX = ArrayList()
        (0 until DATA_DAY_LEN).mapTo(dayX!!) {

            DataBeanX(AxisValue(it.toFloat()).setLabel(String.format("%02d:00", it)))
        }
        generateInitialLineData()
    }

    private fun getData(){
        val dateNow  = Date()
        val c = Calendar.getInstance()
        c.time = dateNow
        c.set(Calendar.DAY_OF_WEEK,2)//周一
        c.set(Calendar.HOUR_OF_DAY,0)
        val dateBefore = c.time
        val nowStr = Util.Date2ISODate(dateNow)
        val beforeStr = Util.Date2ISODate(dateBefore)
        val nstr = Util.ISODateString2Date(nowStr)
        val bstr = Util.ISODateString2Date(beforeStr)
        Log.e("123","day   bef=$beforeStr  $bstr          now=$nowStr   $nstr")

        mPresenter?.querySamples(mID!!,"Day",beforeStr,nowStr)
    }

    private fun fillWeek(samples: ArrayList<PDCSample>){
        for(s in samples){
            val c = Calendar.getInstance()
            c.time = Util.ISODateString2ISODate(s.begTime)
            var str = Util.ISODateString2Date(s.begTime)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val index = (0 until weekX!!.size).firstOrNull {
                c.time = weekX!![it].date
                c.get(Calendar.DAY_OF_MONTH)==day
            }?:-1
            Log.i("123","index=$index")
            mDataWeek!![0][index] = s.enterNumber
            mDataWeek!![1][index] = s.leaveNumber
        }
    }

    private fun fillDay(samples: ArrayList<PDCSample>):Int{
        var top = 0
        (0 until DATA_DAY_LEN).forEach { mDataDay!![0][it] = 0;mDataDay!![1][it] = 0 }
        for (s in samples){
            val c = Calendar.getInstance()
            c.time = Util.ISODateString2ISODate(s.begTime)
            var str = Util.ISODateString2Date(s.begTime)
            Log.i("123","simple  str=$str")
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val index = (0 until dayX!!.size).firstOrNull { String(dayX!![it].value!!.labelAsChars).substring(0,2).equals(String.format("%02d",hour)) }?:-1
            Log.i("123","index=$index")
            mDataDay!![0][index] = s.enterNumber
            mDataDay!![1][index] = s.leaveNumber
            if (s.enterNumber>top) top = s.enterNumber
            if (s.leaveNumber>top) top = s.leaveNumber
        }
        return top
    }




    private fun generateInitialLineData() {
        val axisValues = ArrayList<AxisValue>()
        val valuesIn = ArrayList<PointValue>()
        val valuesOut = ArrayList<PointValue>()
        for (i in 0 until DATA_DAY_LEN) {
            valuesIn.add(PointValue(i.toFloat(), 0f))
            valuesOut.add(PointValue(i.toFloat(), 0f))
            axisValues.add(AxisValue(i.toFloat()).setLabel(""))
        }
        val lineIn = Line(valuesIn)
        lineIn.setColor(colorUtil[0])
                .setCubic(true)
                .setFilled(true)
                .setHasLabels(true)
                .setHasLabelsOnlyForSelected(true)
                .setPointRadius(2).strokeWidth = 1

        val lineOut = Line(valuesOut)
        lineOut.setColor(colorUtil[1])
                .setCubic(true)
                .setFilled(true)
                .setHasLabels(true)
                .setHasLabelsOnlyForSelected(true)
                .setPointRadius(2).strokeWidth = 1

        val lines = ArrayList<Line>()
        lines.add(lineIn)
        lines.add(lineOut)

        lineData = LineChartData(lines)
        lineData!!.axisXBottom = Axis(axisValues).setHasLines(true)
        lineData!!.axisYLeft = Axis().setHasLines(true).setMaxLabelChars(3).setName(getString(R.string.charts_week_day))

        mChartTop!!.lineChartData = lineData
        mChartTop!!.isViewportCalculationEnabled = false
        mChartTop!!.isValueSelectionEnabled = true

        val v = Viewport(0f, 100f, DATA_DAY_LEN-1f, 0f)
        mChartTop!!.maximumViewport = v
        mChartTop!!.currentViewport = v
    }

    private fun generateColumnData() {
//        getDataDay()
//        val numSubcolumns = 2
//        val numColumns = 24//24小时

        val axisValues = ArrayList<AxisValue>()
        val columns = ArrayList<Column>()
        var values: MutableList<SubcolumnValue>

        for (i in 0 until DATA_WEEK_LEN) {
            values = ArrayList()
            for (j in 0 until DATA_TYPE) {
                values.add(SubcolumnValue(mDataWeek!![j][i].toFloat(), colorUtil[j]))
            }
            axisValues.add(AxisValue(i.toFloat()).setLabel(String(weekX!![i].value!!.labelAsChars)))
            columns.add(Column(values).setHasLabelsOnlyForSelected(true))
        }
        columnData = ColumnChartData(columns)
        columnData!!.axisXBottom = Axis(axisValues).setHasLines(true)
        columnData!!.axisYLeft = Axis().setHasLines(true).setMaxLabelChars(2).setName(getString(R.string.charts_week_week))

        mChartBottom!!.columnChartData = columnData

        mChartBottom!!.onValueTouchListener = object :ColumnChartOnValueSelectListener{
            override fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue?) {
                //send
                Log.i("123"," columIndex=$columnIndex     subcolumnIndex=$subcolumnIndex     ${String( weekX!![columnIndex].value!!.labelAsChars)}" +
                        "${weekX!![columnIndex].date}")
                val c = Calendar.getInstance()
                c.time = weekX!![columnIndex].date
                c.set(Calendar.HOUR_OF_DAY,0)
                c.set(Calendar.MINUTE,0)
                val beg = Util.Date2ISODate(c.time)
                c.set(Calendar.HOUR_OF_DAY,23)
                c.set(Calendar.MINUTE,59)
                val end = Util.Date2ISODate(c.time)
                mPresenter?.querySamples(mID!!,"Hour",beg,end)


//                val t = String( weekX!![columnIndex].value!!.labelAsChars)


//                generateLineData(columnIndex, false)
            }

            override fun onValueDeselected() {
                generateLineData(0,true)
            }
        }
        mChartBottom!!.isValueSelectionEnabled = true
        mChartBottom!!.isZoomEnabled = false
    }


    private fun generateLineData(top:Int,isInit: Boolean) {//当前小时数据
        val flag = if (isInit) 0 else 1
        mChartTop!!.cancelDataAnimation()
        val lineIn = lineData!!.lines[0]
        val lineOut = lineData!!.lines[1]
        lineIn.color = colorUtil[0]
        lineOut.color = colorUtil[1]
        val valuesIn = lineIn.values
        val valuesOut = lineOut.values

        val axisValues = lineData!!.axisXBottom.values

        for (i in 0 until DATA_DAY_LEN) {
            val valueIn = valuesIn[i]
            valueIn.setTarget(valueIn.x, (mDataDay!![0][i] * flag).toFloat())
            valueIn.setLabel(if (isInit) "" else "进入：" + mDataDay!![0][i])

            val valueOut = valuesOut[i]
            valueOut.setTarget(valueOut.x, (mDataDay!![1][i] * flag).toFloat())
            valueOut.setLabel(if (isInit) "" else "出去：" + mDataDay!![1][i])

//            axisValues[i] = dayX!![i].value

            val axisValue = axisValues[i]
            axisValue.setLabel(String.format("%02d:00", i))
        }
        //        mDataHour[1][i]*flag
        val v = Viewport( mChartTop?.maximumViewport)
        v.bottom = 0f
        v.top = if (isInit) mChartTop?.maximumViewport?.top?:100f else top.times(1.25f)!!
        v.left = 0f
        v.right = DATA_DAY_LEN-1f
        mChartTop!!.maximumViewport = v
        mChartTop!!.currentViewport = v
        mChartTop!!.isZoomEnabled = false

        mChartTop!!.startDataAnimation(300)
    }


    private fun getDataDay() {
        for (i in 0..1) {
            for (j in 0..23) {
                mDataDay!![i][j] = (Math.random() * 100).toInt()
            }
        }
    }

    private fun getDataHour(hour: Int) {
        for (i in 0..1) {
            for (j in 0..11) {
                mDataDay!![i][j] = (Math.random() * 100).toInt()
            }
        }


    }



    inner class DataBeanX{
        constructor(d:Date?,v:AxisValue?){
            date = d
            value = v
        }
        constructor(v:AxisValue?){
            value = v
        }


        var date:Date?=null
        var value:AxisValue?=null
    }



}
