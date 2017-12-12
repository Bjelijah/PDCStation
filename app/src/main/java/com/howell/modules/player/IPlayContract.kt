package com.howell.modules.player

import android.content.Context
import com.howell.modules.ImpBasePresenter
import com.howell.modules.ImpBaseView
import com.howell.modules.player.bean.CameraItemBean
import com.howell.modules.player.bean.PTZ
import com.howell.modules.player.bean.VODRecord

/**
 * Created by Administrator on 2017/12/4.
 */
interface IPlayContract {
    interface IView : ImpBaseView{
        fun onError(flag:Int)
        fun onConnect(b:Boolean)
        fun onDisconnect(b:Boolean)
        fun onTime(speed: Int, timestamp: Long, firstTimestamp: Long, bWait: Boolean)
        fun onRecord(vods:ArrayList<VODRecord>?)
        fun onPlaybackStartEndTime(beg:Long,end:Long)
    }
    interface IPresent : ImpBasePresenter{
        fun init(c: Context,bean: CameraItemBean)
        fun deInit()
        fun play(isSub:Boolean)
        fun playback(isSub:Boolean,beg:String,end:String)
        fun stop()
        fun pause():Boolean
        fun reLink(isSub:Boolean,beg:String?,end:String?)
        fun ptzCtrl(cmd:PTZ)
        fun catchPic(path:String)
        fun vodReset()
        fun getVodRecord(startTime:String,endTime:String)
        fun playMoveTo(isSub:Boolean,beg:String,end:String)
    }
}