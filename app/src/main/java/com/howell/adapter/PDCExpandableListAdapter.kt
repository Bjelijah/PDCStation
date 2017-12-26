package com.howell.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.howell.modules.pdc.bean.PDCDevice
import com.howell.pdcstation.R
import kotlin.collections.ArrayList

/**
 * Created by Administrator on 2017/12/25.
 */
class PDCExpandableListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    companion object {
        val HEADER = 1
        val CHILD  = 2
    }


    var mContext : Context?              = null
    var mList: ArrayList<PDCItem>?       = null
    var mLinstener:OnItemClick?          = null

    constructor(o:OnItemClick):this(){
        mLinstener = o
    }


    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val item = mList?.get(position)
        when (item?.type){
            HEADER ->{
                val holdHead = holder as ViewHolderHead
                holdHead?.tv?.text = item?.device?.name
                if (item?.device?.onLine == true){
                    holdHead?.rb?.setImageResource(R.drawable.ic_cloud_black_24dp)
                    holdHead?.tv?.setTextColor(mContext!!.getColor(R.color.pdc_white_online))
                    holdHead?.iv?.setImageResource(R.drawable.ic_expand_more_white_online_24px)
                }else{
                    holdHead?.rb?.setImageResource(R.drawable.ic_cloud_off_black_24dp)
                    holdHead?.tv?.setTextColor(mContext!!.getColor(R.color.pdc_white_offline))
                    holdHead?.iv?.setImageResource(R.drawable.ic_expand_more_white_offline_24px)
                }

                holder?.ll?.setOnClickListener({ v->
                    if (item?.device?.onLine == false){
                        mLinstener?.onItemOffline(v)
//                        return@setOnClickListener//fixme return for test
                    }


                    if (item.showChild == true){
                        //删掉元素@drawable/abc_ic_arrow_drop_right_black_24dp
                        holdHead.iv.setImageResource(R.drawable.ic_expand_more_white_online_24px)

                        val pos = mList?.indexOf(item)?:0
                        var count = 0
                        item.showChild = false
                        while (mList?.size?:0 > pos+1 && mList!![pos+1].type == CHILD){
                            mList?.removeAt(pos+1)
                            count ++
                        }
                        notifyItemRangeRemoved(pos+1,count)
                    } else {
                        holdHead.iv.setImageResource(R.drawable.ic_expand_less_white_online_24px)
                        val pos = mList?.indexOf(item)?:0
                        item.showChild = true
                        mList?.add(pos +1, PDCItem(item.device,mContext?.getString(R.string.pdc_man_charts), CHILD).setFlag(0))
                        mList?.add(pos +2, PDCItem(item.device,mContext?.getString(R.string.pdc_man_mark), CHILD).setFlag(1))
                        notifyItemRangeInserted(pos + 1, 2)

                    }
                })
            }
            CHILD  ->{
                val holdItem = holder as ViewHolderItem
                holdItem.itemTv.text = item.text
                holdItem.itemll.setOnClickListener { v->
                    mLinstener?.onItemClick(v,item?.device,item.childFlag)
                }
                holdItem.itemIv.setImageResource(if (item.childFlag==0) R.drawable.ic_multiline_chart_white_24px else
                    R.drawable.ic_equalizer_white_24px)
            }
            else   ->{}
        }
    }






    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent?.context
        return when(viewType){
            HEADER->ViewHolderHead(LayoutInflater.from(mContext).inflate(R.layout.item_pdc_head,parent,false))
            CHILD ->ViewHolderItem(LayoutInflater.from(mContext).inflate(R.layout.item_pdc_item,parent,false))
            else  ->ViewHolderHead(LayoutInflater.from(mContext).inflate(R.layout.item_pdc_head,parent,false))
        }
    }

    override fun getItemCount(): Int = mList?.size?:0

    override fun getItemViewType(position: Int): Int = mList?.get(position)?.type?:0

    fun setDeviceData(l:ArrayList<PDCDevice>){
        mList = ArrayList()
        for (d in l){
            mList?.add(PDCItem(d,null, HEADER))
//            mList?.add(PDCItem("人流统计"))
//            mList?.add(PDCItem("人流报表"))
        }
        notifyDataSetChanged()
    }


    interface OnItemClick{
        fun onItemClick(v:View,item:PDCDevice?,flag:Int?)
        fun onItemOffline(v:View)
    }


    inner class ViewHolderHead(v: View) : RecyclerView.ViewHolder(v){
        val rb = v.findViewById<ImageView>(R.id.item_pdc_head_online)!!
        val ll = v.findViewById<RelativeLayout>(R.id.item_pdc_head_ll)!!
        val tv = v.findViewById<TextView>(R.id.item_pdc_head_tv)!!
        val iv = v.findViewById<ImageView>(R.id.item_pdc_head_iv)!!
    }

    inner class ViewHolderItem(v:View) : RecyclerView.ViewHolder(v){
        val itemTv = v.findViewById<TextView>(R.id.item_pdc_item_tv)!!
        val itemll = v.findViewById<LinearLayout>(R.id.item_pdc_item_ll)!!
        var itemIv = v.findViewById<ImageView>(R.id.item_pdc_item_iv)!!
    }



    class PDCItem{
        var type:Int?                 = null
        var device: PDCDevice?        = null
        var text:String?              = null
        var showChild:Boolean?        = null
        var childFlag:Int?            = null
        constructor(device:PDCDevice?,text:String?,type:Int){
            this.type   = type
            this.device = device
            this.text   = text
            showChild   = false
        }
        fun setFlag(flag:Int):PDCItem{
            childFlag = flag
            return this

        }

    }
}