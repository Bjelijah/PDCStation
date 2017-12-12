package com.howell.activity.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.howell.activity.LineChartActivity
import com.howell.activity.LineColumnChartsActivity
import com.howell.adapter.PDCRecyclerViewAdapter
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/12/11.
 */
class PDCFragment : HomeBaseFragment() {

    lateinit var mRv:RecyclerView
    lateinit var mAdapter:PDCRecyclerViewAdapter

    override fun getData() {
        val arr:ArrayList<String> = ArrayList()
        arr.add("每小时人流统计")
        arr.add("当天人流统计")
        mAdapter.setData(arr)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_pdc,container,false)
        mRv = view.findViewById(R.id.pdc_rv)
        mAdapter = PDCRecyclerViewAdapter(object :PDCRecyclerViewAdapter.OnItemClick{
            override fun onItmeClick(v: View, pos: Int) {
                //todo show activity
                when(pos){
                    0->{ startActivity(Intent(container?.context,LineChartActivity::class.java)) }
                    1->{ startActivity(Intent(container?.context,LineColumnChartsActivity::class.java)) }
                    else->{}
                }


            }
        })
        mRv.layoutManager = LinearLayoutManager(context)
        mRv.adapter = mAdapter
        mRv.itemAnimator = DefaultItemAnimator()
        getData()
        return view
    }


}