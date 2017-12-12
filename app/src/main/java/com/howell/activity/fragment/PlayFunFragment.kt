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
class PlayFunFragment : Fragment(), View.OnTouchListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(v?.id){
            R.id.play_fun2_photo->{}
            R.id.play_fun2_sound->{}
        }
        return false
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.play_fun2_fragment,null)
        view?.findViewById<LinearLayout>(R.id.play_fun2_sound)?.setOnTouchListener(this)
        view?.findViewById<LinearLayout>(R.id.play_fun2_photo)?.setOnTouchListener(this)
        return view
    }
}