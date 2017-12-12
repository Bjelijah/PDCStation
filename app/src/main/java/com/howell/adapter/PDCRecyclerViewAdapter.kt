package com.howell.adapter


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/12/11.
 */
class PDCRecyclerViewAdapter(o:OnItemClick) : RecyclerView.Adapter<PDCRecyclerViewAdapter.ViewHolder>() {

    var mList:ArrayList<String>? = null
    var mContext :Context?       = null
    private val mClickListener   = o

    fun setData(l:ArrayList<String>){
        mList = l
        notifyDataSetChanged()
        Log.i("123","notifyDataSetChanged   mList=$mList  size=${mList?.size}")
    }

    override fun getItemCount(): Int {Log.i("123","getconunt size=${mList?.size?:0}");return mList?.size?:0}

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

        val bean = mList?.get(position)
        Log.i("123","onBindVIew bean=$bean")
        holder?.tv?.text = bean
        holder?.ll?.setOnClickListener({v->mClickListener.onItmeClick(v,position)})
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        Log.i("123","~~~~~~~~onCreateViewHolder")
        mContext = parent?.context
        return ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_pdc,parent,false))
    }

    interface OnItemClick{
        fun onItmeClick(v:View,pos:Int)
    }

    inner class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        val ll = v.findViewById<RelativeLayout>(R.id.item_pdc_ll)!!
        val tv = v.findViewById<TextView>(R.id.item_pdc_tv)!!
    }

}