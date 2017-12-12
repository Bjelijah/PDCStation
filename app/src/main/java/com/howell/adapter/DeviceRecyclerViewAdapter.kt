package com.howell.adapter

import android.content.Context
import android.graphics.Color
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.howell.activity.view.SwipeLinearLayout
import com.howell.bean.CameraItemBean
import com.howell.pdcstation.R
import com.howell.utils.PhoneConfig
import com.howell.utils.ScaleImageUtils
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.octicons_typeface_library.Octicons
import java.io.File

/**
 * Created by Administrator on 2017/11/30.
 */
class DeviceRecyclerViewAdapter(c:Context,o:OnItemClickListener) :RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>(),SwipeLinearLayout.OnSwipeListener{


    private var mSllList:ArrayList<SwipeLinearLayout> = ArrayList()
    private var mContext:Context                      = c
    private val mImageWidth:Int                       = PhoneConfig.getPhoneWidth(c)/2
    private val mImageHeight:Int                      = PhoneConfig.getPhoneWidth(c)/2 * 10 / 16
    private var mClickListener:OnItemClickListener    = o
    var mList : ArrayList<CameraItemBean>?            = null
//        set(value) {
//            Log.i("123","mList set value list=$mList");notifyDataSetChanged()}

    fun setData(list:ArrayList<CameraItemBean>){
        mList = list
        Log.i("123","mList set value list=$mList")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_camera_swipebk,parent,false)
        val holder = ViewHolder(v)
        holder.sll.setOnSwipeListener(this)
        mSllList.add(holder.sll)

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val item = mList?.get(position)
        initView(holder!!, item!!)
        initClick(holder!!,position,item!!)
    }

    override fun onDirectionJudged(thisSll: SwipeLinearLayout, isHorizontal: Boolean) {
        if (!isHorizontal) {
            mSllList
                    .filter { null != it && !it.isClose }
                    .forEach { it.scrollAuto(SwipeLinearLayout.DIRECTION_SHRINK) }
        } else {
            mSllList
                    .filter { null != it && it != thisSll && !it.isClose }
                    .forEach { it.scrollAuto(SwipeLinearLayout.DIRECTION_SHRINK) }
        }
    }

    override fun getItemCount(): Int = mList?.size?:0


    private fun initView(holder:ViewHolder, item:CameraItemBean){
        holder.ivCamera.layoutParams = FrameLayout.LayoutParams(mImageWidth,mImageHeight)
        var bm = ScaleImageUtils.decodeFile(mImageWidth,mImageHeight, File(item.picturePath))
        if (bm == null) {
            holder.ivCamera.setImageBitmap(null)
            holder.ivCamera.setBackgroundColor(mContext.resources.getColor(R.color.item_camera_video))
        } else {
            holder.ivCamera.setImageBitmap(bm)
        }
        holder.tvName.text = item.cameraName
        if (item.isOnline!!){
            when {
                item.indensity!! > 75 -> holder.ivWifi.setImageResource(R.mipmap.wifi_4)
                item.indensity!! > 50 -> holder.ivWifi.setImageResource(R.mipmap.wifi_3)
                item.indensity!! > 25 -> holder.ivWifi.setImageResource(R.mipmap.wifi_2)
                else                  -> holder.ivWifi.setImageResource(R.mipmap.wifi_1)
            }
            holder.ivInOffLine.setImageResource(R.mipmap.card_online_image_blue)
        }else{
            holder.ivInOffLine.setImageResource(R.mipmap.card_offline_image_gray)
            holder.ivWifi.setImageResource(R.mipmap.wifi_0)
        }
        holder.ivReplay.setImageDrawable(mContext.getDrawable(R.mipmap.ic_theaters_white_24dp))

        holder.ivSetting.setImageDrawable(IconicsDrawable(mContext, Octicons.Icon.oct_settings).actionBar().color(Color.WHITE))
        holder.ivInfo.setImageDrawable(IconicsDrawable(mContext, Octicons.Icon.oct_info).actionBar().color(Color.WHITE))
        holder.ivDelete.setImageDrawable(IconicsDrawable(mContext, Octicons.Icon.oct_trashcan).actionBar().color(Color.WHITE))
        holder.llDelete.background = IconicsDrawable(mContext, Octicons.Icon.oct_trashcan).actionBar().colorRes(R.color.item_camera_detele_bk)

        holder.ivSetting.visibility = View.GONE
        holder.ivInfo.visibility = View.GONE
        holder.ivDelete.visibility = View.GONE
        holder.llDelete.visibility = View.GONE

        holder.sll.scrollAuto(SwipeLinearLayout.DIRECTION_SHRINK)
        holder.itemView.visibility = View.VISIBLE
    }

    private fun initClick(holder:ViewHolder,pos:Int,item:CameraItemBean){
        holder.ivReplay.setOnClickListener  { v -> mClickListener.onItemReplayClickListener(v,pos,item) }
        holder.ivSetting.setOnClickListener { v -> mClickListener.onItemSettingClickListener(v,pos) }
        holder.ivInfo.setOnClickListener    { v -> mClickListener.onItemInfoClickListener(v,pos) }
        holder.ivDelete.setOnClickListener  { v -> mClickListener.onItemDeleteClickListener(v,pos) }
        holder.ivCamera.setOnClickListener  { v -> mClickListener.onItemVideoClickListener(v,holder,pos,item) }
    }


    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        var sll:SwipeLinearLayout           = v.findViewById(R.id.item_camera_sll)
        var ll_back:View                    = v.findViewById(R.id.item_camera_bk)
        var ll_top:View                     = v.findViewById(R.id.item_camera_top)
        var tvName:TextView                 = v.findViewById(R.id.item_camera_name)
        var ivCamera:ImageView              = v.findViewById(R.id.item_camera_iv_picture)
        var ivWifi:ImageView                = v.findViewById(R.id.item_camera_iv_wifi_idensity)
        var ivInOffLine:ImageView           = v.findViewById(R.id.item_camera_iv_offline)
        var ivReplay:FloatingActionButton   = v.findViewById(R.id.item_camera_iv_replay)
        var ivSetting:FloatingActionButton  = v.findViewById(R.id.item_camera_iv_setting)
        var ivInfo:FloatingActionButton     = v.findViewById(R.id.item_camera_iv_info)
        var ivDelete:FloatingActionButton   = v.findViewById(R.id.item_camera_iv_delete)
        var llDelete:LinearLayout           = v.findViewById(R.id.item_camera_ll_delete)

    }

    interface OnItemClickListener{
        fun onItemVideoClickListener(v:View, itemView: ViewHolder, pos:Int,item: CameraItemBean)
        fun onItemReplayClickListener(v: View?, pos:Int,item:CameraItemBean)
        fun onItemSettingClickListener(v:View,pos:Int)
        fun onItemInfoClickListener(v:View,pos:Int)
        fun onItemDeleteClickListener(v:View,pos:Int)
    }

}