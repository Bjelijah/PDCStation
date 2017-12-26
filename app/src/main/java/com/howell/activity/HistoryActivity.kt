package com.howell.activity

import android.util.Log
import com.howell.modules.pdc.IPDCContract
import com.howell.modules.pdc.bean.PDCDevice
import com.howell.modules.pdc.presenter.PDCHttpPresenter
import com.howell.pdcstation.R
import com.howellsdk.net.http.bean.PDCSample

/**
 * Created by Administrator on 2017/12/26.
 */
class HistoryActivity : BaseActivity(),IPDCContract.IView {

    var mPresent:IPDCContract.IPresent?=null
    var mId:String? = null
    override fun bindPresenter() {
        if (mPresent==null) mPresent = PDCHttpPresenter()
        mPresent?.bindView(this)
        mPresent?.init(this)
    }

    override fun unbindPresenter() {
        mPresent?.unbindView()
        mPresent = null
    }

    override fun onQueryDeviceResult(deviceList: ArrayList<PDCDevice>) {
    }

    override fun onQuerySimpleResult(simpleList: ArrayList<PDCSample>, unit: String) {
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindPresenter()
    }

    override fun findView() {
        setContentView(R.layout.activity_history)
    }

    override fun initView() {
        bindPresenter()
    }

    override fun initData() {
        Log.i("123","initData  historyActivity ")
        mId = intent.getStringExtra("id")
        mPresent?.queryTest(mId)
    }
}