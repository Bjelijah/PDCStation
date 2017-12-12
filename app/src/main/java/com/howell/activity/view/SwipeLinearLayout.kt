package com.howell.activity.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.widget.LinearLayout
import android.widget.Scroller

/**
 * Created by taofangxin on 16/5/18.
 */
class SwipeLinearLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    internal var mScroller: Scroller
    internal var startScrollX: Int = 0
    internal var lastX: Float = 0.toFloat()
    internal var lastY: Float = 0.toFloat()
    internal var startX: Float = 0.toFloat()
    internal var startY: Float = 0.toFloat()
    internal var hasJudged = false
    internal var ignore = false

    internal var onSwipeListener: OnSwipeListener? = null

    // 左边部分, 即从开始就显示的部分的长度
    internal var width_left = 0
    // 右边部分, 即在开始时是隐藏的部分的长度
    internal var width_right = 0


    var isClose = true
    internal var foo = true

    init {
        mScroller = Scroller(context)
        this.orientation = LinearLayout.HORIZONTAL
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val left = getChildAt(0)
        val right = getChildAt(1)
        width_right = right.measuredWidth
        //        width_left = left.getMeasuredWidth();
        width_left = width_right * 2 / 3

        //        mScroller.startScroll(0,0,width_left,0);

        //   Log.e("123","width left="+width_left+ "  width_right="+width_right);
    }

    private fun disallowParentsInterceptTouchEvent(parent: ViewParent?) {
        if (null == parent) {
            return
        }
        parent.requestDisallowInterceptTouchEvent(true)
        disallowParentsInterceptTouchEvent(parent.parent)
    }

    private fun allowParentsInterceptTouchEvent(parent: ViewParent?) {
        if (null == parent) {
            return
        }
        parent.requestDisallowInterceptTouchEvent(false)
        allowParentsInterceptTouchEvent(parent.parent)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //        Log.i("123","dispathchTouch event");
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                disallowParentsInterceptTouchEvent(parent)
                hasJudged = false
                startX = ev.x
                startY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val curX = ev.x
                val curY = ev.y
                if (hasJudged == false) {
                    val dx = curX - startX
                    val dy = curY - startY

                    if (dx * dx + dy * dy > MOVE_JUDGE_DISTANCE * MOVE_JUDGE_DISTANCE) {
                        if (Math.abs(dy) > Math.abs(dx)) {
                            allowParentsInterceptTouchEvent(parent)

                            if (null != onSwipeListener) {
                                onSwipeListener!!.onDirectionJudged(this, false)
                            }
                        } else {
                            if (null != onSwipeListener) {
                                if (isClose && dx < 0) {
                                    //   Log.i("123","dispatchTouchEvent    dx="+dx  +"isclose="+isClose);
                                    allowParentsInterceptTouchEvent(parent)
                                }
                                onSwipeListener!!.onDirectionJudged(this, true)
                            }
                            lastX = curX
                            lastY = curY
                        }
                        hasJudged = true
                        ignore = true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
            }
            else -> {
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (hasJudged) {
            true
        } else super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (!isClose && event.x < width_left) {//打开 触发后面控件
                    return false
                }
                lastX = event.x
                lastY = event.y
                startScrollX = scrollX
            }
            MotionEvent.ACTION_MOVE -> {
                if (ignore) {
                    ignore = false
                    return true
                }
                val curX = event.x
                val dX = curX - lastX
                lastX = curX
                if (hasJudged) {
                    val targetScrollX = scrollX + (-dX).toInt()//
                    when {
                        targetScrollX < 0 - width_left -> //
                            scrollTo(0 - width_left, 0)
                    //全开
                        targetScrollX > 0 -> scrollTo(0, 0)
                    //全关
                        else -> //                        Log.e("123","滑动");
                            scrollTo(targetScrollX, 0)
                    //滑动
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                val finalX = event.x
                if (finalX < startX) {//左划  关闭
                    if (!isClose) {
                        scrollAuto(DIRECTION_SHRINK)
                    }
                } else if (finalX > startX) {//右划 展开
                    scrollAuto(DIRECTION_EXPAND)
                }
            }
            else -> {
            }
        }
        return true
    }

    /**
     * 自动滚动， 变为展开或收缩状态
     * @param direction
     */
    fun scrollAuto(direction: Int) {
        val curScrollX = scrollX
        if (direction == DIRECTION_EXPAND) {
            isClose = false
            mScroller.startScroll(curScrollX, 0, -curScrollX - width_left, 0, 300)
        } else {
            mScroller.startScroll(curScrollX, 0, -curScrollX, 0, 300)
            isClose = true
        }
        invalidate()
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            this.scrollTo(mScroller.currX, 0)
            invalidate()
        }
    }

    fun setOnSwipeListener(listener: OnSwipeListener) {
        this.onSwipeListener = listener
    }

    interface OnSwipeListener {
        /**
         * 手指滑动方向明确了
         * @param sll  拖动的SwipeLinearLayout
         * @param isHorizontal 滑动方向是否为水平
         */
        fun onDirectionJudged(sll: SwipeLinearLayout, isHorizontal: Boolean)
    }

    companion object {
        val DIRECTION_EXPAND = 0
        val DIRECTION_SHRINK = 1
        internal var MOVE_JUDGE_DISTANCE = 5f
    }
}
