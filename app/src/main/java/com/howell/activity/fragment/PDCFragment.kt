package com.howell.activity.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.howell.activity.ChartActivity
import com.howell.adapter.PDCExpandableListAdapter
import com.howell.adapter.PDCRecyclerViewAdapter
import com.howell.modules.pdc.IPDCContract
import com.howell.modules.pdc.bean.PDCDevice
import com.howell.modules.pdc.presenter.PDCHttpPresenter
import com.howell.pdcstation.R
import com.howellsdk.net.http.bean.PDCSample
import com.howellsdk.utils.Util
import java.util.*

/**
 * Created by Administrator on 2017/12/11.
 */
class PDCFragment : HomeBaseFragment(),IPDCContract.IView {


    lateinit var mSfl: SwipeRefreshLayout
    lateinit var mRv:RecyclerView
    lateinit var mAdapter:PDCRecyclerViewAdapter
    lateinit var mPDCAdapter:PDCExpandableListAdapter
    var mPresent:IPDCContract.IPresent?=null



    override fun getData() {
//        val arr:ArrayList<String> = ArrayList()
//        arr.add("每小时人流统计")
//        arr.add("当天人流统计")
//        mAdapter.setData(arr)
        mPresent?.queryDevice()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_pdc,container,false)
        mRv = view.findViewById(R.id.pdc_rv)
        mSfl = view.findViewById(R.id.pdc_sfl)
        mSfl.setOnRefreshListener {
            Handler().postDelayed({
                getData()
                mSfl.isRefreshing = false
            }, 1000) }
        bindPresenter()
        mAdapter = PDCRecyclerViewAdapter(object :PDCRecyclerViewAdapter.OnItemClick{
            override fun onItmeClick(v: View, pos: Int,item:PDCDevice?) {
                //todo show activity

//                val dateNow  = Date()
//                val c = Calendar.getInstance()
//                c.time = dateNow
//                c.add(Calendar.MONTH,-2)
//                val dateBefore = c.time
//                val nowStr = Util.Date2ISODate(dateNow)
//                val beforeStr = Util.Date2ISODate(dateBefore)
                if (item?.onLine == true) {
//                    mPresent?.queryHistory(item?.id!!,beforeStr,nowStr,"LastNEnterNumber")
                    startActivity(Intent(context!!, ChartActivity::class.java).putExtra("id", item?.id))
                }else{
//                    startActivity(Intent(context!!, ChartActivity::class.java).putExtra("id", item?.id))
                    Snackbar.make(view,"设备不在线，无法打开",Snackbar.LENGTH_SHORT).show()
                }

            }
        })

        mPDCAdapter = PDCExpandableListAdapter(object :PDCExpandableListAdapter.OnItemClick{
            override fun onItemClick(item: PDCDevice?, flag: Int?) {
                when (flag){
                    0->{
                        Log.i("123","${item?.name}   on flag = 0")
//                        startActivity(Intent(context!!, ChartActivity::class.java).putExtra("id", item?.id))
                    }
                    1->{
                        Log.i("123","${item?.name}  on flag = 1")
                    }
                }
            }

            override fun onItemOffline() {
                Snackbar.make(view,"设备不在线，无法打开",Snackbar.LENGTH_SHORT).show()
            }
        })

        mRv.layoutManager = LinearLayoutManager(context)
//        mRv.adapter = mAdapter
        mRv.adapter = mPDCAdapter
        mRv.itemAnimator = DefaultItemAnimator()
        getData()
        return view
    }




    override fun onDestroy() {
        super.onDestroy()
        unbindPresenter()
    }

    override fun onQueryDeviceResult(deviceList: ArrayList<PDCDevice>) {
//        mAdapter.setDeviceData(deviceList)
        mPDCAdapter.setDeviceData(deviceList)
    }

    override fun onQuerySimpleResult(simpleList: ArrayList<PDCSample>, unit: String) {}

    override fun bindPresenter() {
        if (mPresent==null)mPresent = PDCHttpPresenter()
        mPresent?.bindView(this)
        mPresent?.init(context)
    }

    override fun unbindPresenter() {
        mPresent?.unbindView()
        mPresent = null
    }

}