package com.howell.activity.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/12/4.
 */
class PtzFunFragment : Fragment(), View.OnTouchListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(v?.id){
            R.id.play_fragment_wide->{
                when(event?.action){
                    MotionEvent.ACTION_DOWN->{}
                    MotionEvent.ACTION_UP->{}
                }
            }
            R.id.play_fragment_tele->{
                when(event?.action){
                    MotionEvent.ACTION_DOWN->{}
                    MotionEvent.ACTION_UP->{}
                }
            }
        }
        return false
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.play_fun1_fragment,null)
        view?.findViewById<LinearLayout>(R.id.play_fragment_wide)?.setOnTouchListener(this)
        view?.findViewById<LinearLayout>(R.id.play_fragment_tele)?.setOnTouchListener(this)
        return view
    }
}