package com.howell.activity.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.howell.activity.PlayViewActivity
import com.howell.activity.view.WheelTimeDialog
import com.howell.adapter.DeviceRecyclerViewAdapter
import com.howell.modules.device.IDeviceContract
import com.howell.modules.device.presenter.DeviceHttpPresenter
import com.howell.modules.player.bean.CameraItemBean
import com.howell.pdcstation.R
import pullrefreshview.layout.BaseHeaderView
import pullrefreshview.layout.PullRefreshLayout
import java.util.*

/**
 * Created by Administrator on 2017/11/29.
 */
class DeviceFragment : HomeBaseFragment(),IDeviceContract.IVew {

    lateinit var mRv:RecyclerView
    lateinit var mBhv:BaseHeaderView

    private lateinit var mView:View
    private var mPresenter:IDeviceContract.IPresenter?=null
    private lateinit var mAdapter:DeviceRecyclerViewAdapter



    override fun getData() {
       // mList.clear()
        mPresenter?.queryDevices()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater!!.inflate(R.layout.fragment_device,container,false)
        mBhv  = mView.findViewById(R.id.device_header)
        mRv   = mView.findViewById(R.id.device_rv)
        bindPresenter()
        (mView as PullRefreshLayout).setHasFooter(true)
        mBhv.setOnRefreshListener { getData() }
        //mrv set adapter
        mAdapter = DeviceRecyclerViewAdapter(context,object :DeviceRecyclerViewAdapter.OnItemClickListener{
            override fun onItemVideoClickListener(v: View, itemView: DeviceRecyclerViewAdapter.ViewHolder, pos: Int,item: CameraItemBean) {
                startActivity(Intent(context,PlayViewActivity::class.java)
                        .putExtra("CameraItem",item)
                        .putExtra("isPlayback",false))
            }

            override fun onItemReplayClickListener(v: View?, pos: Int,item:CameraItemBean) {
                WheelTimeDialog(item).show(fragmentManager,"")
            }

            override fun onItemSettingClickListener(v: View, pos: Int) {
            }

            override fun onItemInfoClickListener(v: View, pos: Int) {
            }

            override fun onItemDeleteClickListener(v: View, pos: Int) {
            }
        })
        mRv.layoutManager = LinearLayoutManager(context)
        mRv.adapter = mAdapter
        mRv.itemAnimator = DefaultItemAnimator()
        getData()
        return mView
    }



    override fun bindPresenter() {
        if (mPresenter==null)mPresenter = DeviceHttpPresenter()
        mPresenter?.bindView(this)
        mPresenter?.init(context)
    }

    override fun unbindPresenter() {
        if (mPresenter!=null){
            mPresenter?.unbindView()
            mPresenter = null
        }
    }

    override fun onQueryResult(beanList: ArrayList<CameraItemBean>) {
//        mList.clear()
//        mList = beanList
        Log.i("123","list=$beanList")
        //update list
        mAdapter.setData(beanList)
        mBhv.post { mBhv.stopRefresh() }
    }

    override fun onError() {
        Log.e("123","onError")
        mBhv.post { mBhv.stopRefresh() }
    }

}