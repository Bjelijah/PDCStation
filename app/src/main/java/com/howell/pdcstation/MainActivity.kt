package com.howell.pdcstation

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.howell.jni.JniUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        JniUtil.logEnable(true)


    }
}
