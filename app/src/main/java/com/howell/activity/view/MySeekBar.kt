package com.howell.activity.view

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow

import android.widget.TextView

import com.howell.pdcstation.R


class MySeekBar//	private int phoneWidth,phoneHeight = 800;
//	private LinearLayout iv;
(context: Context, attrs: AttributeSet) : AppCompatSeekBar(context, attrs) {
    private val mPopupWindow: PopupWindow?

    private val mInflater: LayoutInflater
    private val mView: View
    private val mPosition: IntArray

    private val mThumbWidth = 25
    private val mTvProgress: TextView

    init {
        // TODO Auto-generated constructor stub

        mInflater = LayoutInflater.from(context)
        mView = mInflater.inflate(R.layout.popwindow_layout, null)
        mTvProgress = mView.findViewById<View>(R.id.tvPop) as TextView
        mPopupWindow = PopupWindow(mView, mView.width, mView.height, true)
        mPosition = IntArray(2)
    }

    fun setSeekBarText(str: String) {
        mTvProgress.text = str
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // TODO Auto-generated method stub

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                this.getLocationOnScreen(mPosition)
                //			mPopupWindow.showAsDropDown(this, (int) event.getX(),
                //					mPosition[1] - 30);
                val thumb_x = this.progress * (this.width - mThumbWidth) / this.max
                val middle = mPosition[1] - mPopupWindow!!.height
                println(middle.toString() + ",,," + getViewHeight(this))
                mPopupWindow.showAtLocation(mView, Gravity.LEFT or Gravity.TOP, thumb_x + mPosition[0] - getViewWidth(mView) / 2 + mThumbWidth / 2,
                        middle)
            }
            MotionEvent.ACTION_UP -> mPopupWindow!!.dismiss()
        }

        return super.onTouchEvent(event)
    }

    private fun getViewWidth(v: View): Int {
        val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        v.measure(w, h)
        return v.measuredWidth
    }

    private fun getViewHeight(v: View): Int {
        val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        v.measure(w, h)
        return v.measuredHeight
    }

    @Synchronized override fun onDraw(canvas: Canvas) {
        // TODO Auto-generated method stub
        var thumb_x = 0
        try {
            thumb_x = this.progress * (this.width - mThumbWidth) / this.max
        } catch (e: Exception) {

        }

        val middle = mPosition[1] - mPopupWindow!!.height
        super.onDraw(canvas)

        if (mPopupWindow != null) {
            try {
                this.getLocationOnScreen(mPosition)
                mPopupWindow.update(thumb_x + mPosition[0] - getViewWidth(mView) / 2 + mThumbWidth / 2,
                        middle, getViewWidth(mView), getViewHeight(mView))

            } catch (e: Exception) {
                // TODO: handle exception
            }

        }

    }

}
