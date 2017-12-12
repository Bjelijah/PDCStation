package com.howell.activity.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import com.howell.pdcstation.R

import java.util.ArrayList

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

/**
 * Created by Administrator on 2017/10/31.
 */

class LineColumnChartFragment : Fragment() {
    private var mChartTop: LineChartView? = null
    private var mChartBottom: ColumnChartView? = null
    private var colorUtil = intArrayOf(Color.parseColor("#2a7ac2"), Color.parseColor("#c09237"))
    private var lineData: LineChartData? = null
    private var columnData: ColumnChartData? = null

    private var mDataDay = Array(2) { IntArray(24) }
    private var mDataHour = Array(2) { IntArray(12) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_line_column_chart, container, false)
        mChartTop = v.findViewById(R.id.lcc_chart)
        mChartBottom = v.findViewById(R.id.lcc_column_chart)

        generateInitialLineData()
        generateColumnData()
        return v
    }

    private fun generateInitialLineData() {
        val numValues = 12//一小时12个点
        val axisValues = ArrayList<AxisValue>()
        val valuesIn = ArrayList<PointValue>()
        val valuesOut = ArrayList<PointValue>()
        for (i in 0 until numValues) {
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
                .setPointRadius(4).strokeWidth = 1

        val lineOut = Line(valuesOut)
        lineOut.setColor(colorUtil[1])
                .setCubic(true)
                .setFilled(true)
                .setHasLabels(true)
                .setHasLabelsOnlyForSelected(true)
                .setPointRadius(4).strokeWidth = 1

        val lines = ArrayList<Line>()
        lines.add(lineIn)
        lines.add(lineOut)

        lineData = LineChartData(lines)
        lineData!!.axisXBottom = Axis(axisValues).setHasLines(true)
        lineData!!.axisYLeft = Axis().setHasLines(true).setMaxLabelChars(3).setName("人数")

        mChartTop!!.lineChartData = lineData
        mChartTop!!.isViewportCalculationEnabled = false
        mChartTop!!.isValueSelectionEnabled = true

        val v = Viewport(0f, 110f, 11f, 0f)
        mChartTop!!.maximumViewport = v
        mChartTop!!.currentViewport = v
    }

    private fun generateColumnData() {
        getDataDay()
        val numSubcolumns = 2
        val numColumns = 24//24小时

        val axisValues = ArrayList<AxisValue>()
        val columns = ArrayList<Column>()
        var values: MutableList<SubcolumnValue>

        for (i in 0 until numColumns) {
            values = ArrayList()
            for (j in 0 until numSubcolumns) {
                values.add(SubcolumnValue(mDataDay[j][i].toFloat(), colorUtil[j]))
            }
            axisValues.add(AxisValue(i.toFloat()).setLabel(HOUR_OF_DAY[i]))
            columns.add(Column(values).setHasLabelsOnlyForSelected(true))
        }
        columnData = ColumnChartData(columns)
        columnData!!.axisXBottom = Axis(axisValues).setHasLines(true)
        columnData!!.axisYLeft = Axis().setHasLines(true).setMaxLabelChars(2).setName("人数")

        mChartBottom!!.columnChartData = columnData

        mChartBottom!!.onValueTouchListener = ValueTouchListener()
        mChartBottom!!.isValueSelectionEnabled = true
        mChartBottom!!.isZoomEnabled = false
    }

    private fun getDataDay() {
        for (i in 0..1) {
            for (j in 0..23) {
                mDataDay[i][j] = (Math.random() * 100).toInt()
            }
        }
    }

    private fun getDataHour(hour: Int) {
        for (i in 0..1) {
            for (j in 0..11) {
                mDataHour[i][j] = (Math.random() * 100).toInt()
            }
        }


    }

    private fun generateLineData(hour: Int, isInit: Boolean) {//当前小时数据
        val flag = if (isInit) 0 else 1
        getDataHour(hour)
        mChartTop!!.cancelDataAnimation()
        val lineIn = lineData!!.lines[0]
        lineIn.color = colorUtil[0]
        val valuesIn = lineIn.values
        for (i in 0..11) {
            val value = valuesIn[i]
            value.setTarget(value.x, (mDataHour[0][i] * flag).toFloat())
            value.setLabel(if (isInit) "" else "进入：" + mDataHour[0][i])
        }

        val lineOut = lineData!!.lines[1]
        lineOut.color = colorUtil[1]
        val valuesOut = lineOut.values
        //        mDataHour[1][i]*flag
        for (i in 0..11) {
            val value = valuesOut[i]
            value.setTarget(value.x, (mDataHour[1][i] * flag).toFloat())
            value.setLabel(if (isInit) "" else "出去：" + mDataHour[1][i])
        }
        val v = Viewport(0f, 110f, 11f, 0f)
        mChartTop!!.maximumViewport = v
        mChartTop!!.currentViewport = v
        mChartTop!!.isZoomEnabled = false
        val axisValues = lineData!!.axisXBottom.values
        for (i in 0..11) {
            val axisValue = axisValues[i]
            val label = String.format("%02d", hour) + MIN_OF_HOUR[i]
            axisValue.setLabel(if (isInit) "" else label)
        }
        mChartTop!!.startDataAnimation(300)
    }

    private inner class ValueTouchListener : ColumnChartOnValueSelectListener {

        override fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) {
            //            Log.i("123","columnIndex="+columnIndex+" subcolumnIndex="+subcolumnIndex+" value="+value);
            generateLineData(columnIndex, false)
        }

        override fun onValueDeselected() {
            generateLineData(0, true)
        }
    }

    companion object {
        private val HOUR_OF_DAY = arrayOf("0:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00")

        private val MIN_OF_HOUR = arrayOf(":00", ":05", ":10", ":15", ":20", ":25", ":30", ":35", ":40", ":45", ":50", ":55")
    }


}
