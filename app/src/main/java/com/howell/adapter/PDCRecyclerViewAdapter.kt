package com.howell.adapter


import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.AppCompatRadioButton
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import android.widget.RelativeLayout
import android.widget.TextView
import com.howell.modules.pdc.bean.PDCDevice
import com.howell.pdcstation.R


/**
 * Created by Administrator on 2017/12/11.
 */
class PDCRecyclerViewAdapter(o:OnItemClick) : RecyclerView.Adapter<PDCRecyclerViewAdapter.ViewHolder>() {


    var mPDCList:ArrayList<PDCDevice>?  =null
    var mContext :Context?              = null
    private val mClickListener          = o




    fun setDeviceData(l:ArrayList<PDCDevice>){
        mPDCList = l
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int  = mPDCList?.size?:0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val d = mPDCList?.get(position)
        holder?.tv?.text = d?.name //+ " : " + if (d?.onLine?:false)"在线" else "不在线"
        holder?.ll?.setOnClickListener({v->mClickListener.onItmeClick(v,position,d)})
        holder?.rb?.isChecked = true
        var c:ColorStateList = if (d?.onLine == true)
            ColorStateList.valueOf(mContext?.getColor(R.color.btn_green_color)!!)
        else
            ColorStateList.valueOf(mContext?.getColor(R.color.accent)!!)
        holder?.rb?.buttonTintList = c
//        holder?.rb?.setTextColor()

//        holder?.rb?.setBackgroundColor(mContext?.resources!!.getColor(R.color.btn_green_color)  )

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        mContext = parent?.context
        return ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_pdc,parent,false))
    }

    interface OnItemClick{
        fun onItmeClick(v:View,pos:Int,item:PDCDevice?)
    }

    inner class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        val rb = v.findViewById<AppCompatRadioButton>(R.id.item_pdc_online)!!
        val ll = v.findViewById<RelativeLayout>(R.id.item_pdc_ll)!!
        val tv = v.findViewById<TextView>(R.id.item_pdc_tv)!!
    }

}