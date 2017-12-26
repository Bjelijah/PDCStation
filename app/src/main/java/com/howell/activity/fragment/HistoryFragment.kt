package com.howell.activity.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.howell.modules.pdc.IPDCContract
import com.howell.modules.pdc.bean.PDCDevice
import com.howell.modules.pdc.presenter.PDCHttpPresenter
import com.howell.pdcstation.R
import com.howellsdk.net.http.bean.PDCSample
import com.howellsdk.utils.Util
import java.util.*

/**
 * Created by Administrator on 2017/12/25.
 */
class HistoryFragment :HomeBaseFragment() ,IPDCContract.IView{

    var mPresenter:IPDCContract.IPresent?=null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.i("123","oncreateview  history ")
        val view = inflater!!.inflate(R.layout.fragment_history,container,false)
        bindPresenter()
        getData()
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindPresenter()
    }

    override fun bindPresenter() {
        if (mPresenter==null) mPresenter = PDCHttpPresenter()
        mPresenter?.bindView(this)
        mPresenter?.init(context)
        Log.i("123","history fragment bindPresenter")
    }

    override fun unbindPresenter() {
        mPresenter?.unbindView()
        mPresenter = null
    }

    override fun onQueryDeviceResult(deviceList: ArrayList<PDCDevice>) {}

    override fun onQuerySimpleResult(simpleList: ArrayList<PDCSample>, unit: String) {}

    override fun getData() {
        val id =  ""
        val dateNow  = Date()
        val c = Calendar.getInstance()
        c.time = dateNow
        c.add(Calendar.DAY_OF_MONTH,-5)
        val dateBefore = c.time
        val nowStr = Util.Date2ISODate(dateNow)
        val beforeStr = Util.Date2ISODate(dateBefore)

        mPresenter?.queryHistory(id,beforeStr,nowStr,"None")


    }

}