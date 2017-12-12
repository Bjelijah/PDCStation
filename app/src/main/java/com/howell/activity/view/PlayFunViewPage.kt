package com.howell.activity.view

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


/**
 * Created by Administrator on 2017/12/4.
 */
class PlayFunViewPage: ViewPager {

    constructor(context: Context):super(context){

    }

    constructor(context: Context,attrs:AttributeSet):super(context,attrs){}



    var mView: View?=null
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN && mView!=null){
            if (ev.y > mView?.top!!)return false
        }
        return super.onTouchEvent(ev)
    }
}