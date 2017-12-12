package com.howell.activity.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import com.howell.pdcstation.R

import java.util.ArrayList
import java.util.Calendar
import java.util.Date

import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.AxisValue
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.model.ValueShape
import lecho.lib.hellocharts.model.Viewport
import lecho.lib.hellocharts.view.LineChartView

/**
 * Created by Administrator on 2017/10/27.
 */

class LineChartFragment : Fragment() {

    private var lcHour: LineChartView?      = null
    private var lcDay: LineChartView?       = null
    private var hourData: LineChartData?    = null
    private var dayData: LineChartData?     = null
    private var manHour: Array<IntArray>?   = null
    private var manDay: Array<IntArray>?    = null
    private var colorUtil = intArrayOf(Color.parseColor("#2a7ac2"), Color.parseColor("#c09237"))
    private val hourX: List<AxisValue>
        get() {
            val list = ArrayList<AxisValue>()
            val data = Date()
            val c = Calendar.getInstance()
            c.time = data
            c.add(Calendar.HOUR_OF_DAY, -1)
            Log.i("123", "c=" + c.toString())
            for (i in 0..11) {
                val str = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
                list.add(AxisValue(i.toFloat()).setLabel(str))
                c.add(Calendar.MINUTE, 5)
            }
            return list
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_line_charts, container, false)
        lcHour = v.findViewById(R.id.lc_hour)
        lcDay  = v.findViewById(R.id.lc_day)

        initData()
        generateData()
        generateData2()
        lcHour?.isViewportCalculationEnabled = false
        lcDay?.isViewportCalculationEnabled = false
        resetViewport(lcHour!!)
        resetViewport(lcDay!!)
        toggleCubic()
        return v
    }

    private fun initData() {
        if (manHour == null) manHour = Array(2) { IntArray(CHARTS_LEN) }
        for (i in 0..1) {
            for (j in 0 until CHARTS_LEN) {
                manHour!![i][j] = (Math.random() * 100).toInt()
            }
        }
        if (manDay == null) manDay = Array(2) { IntArray(CHARTS_LEN) }
        for (i in 0..1) {
            for (j in 0 until CHARTS_LEN) {
                manDay!![i][j] = (Math.random() * 100).toInt()
            }
        }

    }

    private fun generateData() {
        val lines = ArrayList<Line>()
        for (i in 0..1) {
            val values = ArrayList<PointValue>()
            for (j in 0 until CHARTS_LEN) {
                val str = "进入：" + manHour!![0][j] + "\n出去：" + manHour!![1][j]
                values.add(PointValue(j.toFloat(), manHour!![i][j].toFloat()).setLabel(str))
            }
            val line = Line(values)

            line.color = colorUtil[i]
            line.shape = ValueShape.CIRCLE
            line.strokeWidth = 1//线粗细
            line.pointRadius = 3
            line.isCubic = true
            line.isFilled = true
            line.setHasLabels(true)
            line.setHasLabelsOnlyForSelected(true)
            line.setHasLines(true)
            line.setHasPoints(true)
            lines.add(line)
        }
        hourData = LineChartData(lines)
        hourData!!.axisXBottom = Axis().setName("1小时").setValues(hourX)
        hourData!!.axisYLeft = Axis().setHasLines(true).setName("人数")
        hourData!!.baseValue = java.lang.Float.NEGATIVE_INFINITY
        lcHour!!.lineChartData = hourData
    }

    private fun generateData2() {
        val lines = ArrayList<Line>()
        for (i in 0..1) {
            val values = ArrayList<PointValue>()
            for (j in 0 until CHARTS_LEN) {
                val str = "进入：" + manDay!![0][j] + "\n出去：" + manDay!![1][j]
                values.add(PointValue(j.toFloat(), manDay!![i][j].toFloat()).setLabel(str))
            }
            val line = Line(values)

            line.color = colorUtil[i]
            line.shape = ValueShape.CIRCLE
            line.strokeWidth = 1//线粗细
            line.pointRadius = 3
            line.isCubic = true
            line.isFilled = true
            line.setHasLabels(true)
            line.setHasLabelsOnlyForSelected(true)
            line.setHasLines(true)
            line.setHasPoints(true)
            lines.add(line)
        }
        val list = ArrayList<AxisValue>()
        list.add(AxisValue(0f).setLabel("0:00"))
        list.add(AxisValue(1f).setLabel("2:00"))
        list.add(AxisValue(2f).setLabel("4:00"))
        list.add(AxisValue(3f).setLabel("6:00"))
        list.add(AxisValue(4f).setLabel("8:00"))
        list.add(AxisValue(5f).setLabel("10:00"))
        list.add(AxisValue(6f).setLabel("12:00"))
        list.add(AxisValue(7f).setLabel("14:00"))
        list.add(AxisValue(8f).setLabel("16:00"))
        list.add(AxisValue(9f).setLabel("18:00"))
        list.add(AxisValue(10f).setLabel("20:00"))
        list.add(AxisValue(11f).setLabel("22:00"))

        dayData = LineChartData(lines)
        dayData!!.axisXBottom = Axis().setName("1天").setValues(list)
        dayData!!.axisYLeft = Axis().setHasLines(true).setName("人数")
        dayData!!.baseValue = java.lang.Float.NEGATIVE_INFINITY
        lcDay!!.lineChartData = dayData
    }

    private fun toggleCubic() {
        val v = Viewport(lcHour?.maximumViewport)
        val v2 = Viewport(lcDay?.maximumViewport)
        lcHour?.setCurrentViewportWithAnimation(v)
        lcHour?.isZoomEnabled = false
        lcHour?.isValueSelectionEnabled = true

        lcDay?.setCurrentViewportWithAnimation(v2)
        lcDay?.isZoomEnabled = false
        lcDay?.isValueSelectionEnabled = true

    }


    private fun resetViewport(chart: LineChartView) {
        // Reset viewport height range to (0,100)
        val v = Viewport(chart.maximumViewport)
        v.bottom = 0f
        v.top = 105f
        v.left = 0f
        v.right = (CHARTS_LEN - 1).toFloat()
        chart.maximumViewport = v
        chart.currentViewport = v
    }

    companion object {
        private val CHARTS_LEN = 12
    }
}
