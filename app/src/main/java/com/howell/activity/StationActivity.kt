package com.howell.activity

import android.app.ActivityOptions
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/12/19.
 */
class StationActivity :BaseActivity() {


    @BindView(R.id.station_iv)     lateinit var mView:ImageView
    @BindView(R.id.station_btn)    lateinit var mBtn:Button


    override fun findView() {
        setContentView(R.layout.activity_station)
        ButterKnife.bind(this)
    }

    override fun initView() {
        Glide.with(this).load("https://unsplash.it/600/300/?random").centerCrop().into(mView)
    }

    override fun initData() {

    }

    @OnClick(R.id.station_btn) fun onEntryClick(v: View){
        val intent = Intent(this,HomeActivity::class.java)
                .putExtra("account","howell")
                .putExtra("email","howell")
        startActivity(intent)
//        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, mView, "myImage").toBundle())
        finish()
//        mView.postDelayed({finish()},1000)
    }

}