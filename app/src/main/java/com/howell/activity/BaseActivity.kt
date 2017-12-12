package com.howell.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by Administrator on 2017/11/27.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findView()
        initView()
        initData()
    }


    abstract fun findView()
    abstract fun initView()
    abstract fun initData()
}