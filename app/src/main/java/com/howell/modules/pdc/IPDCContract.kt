package com.howell.modules.pdc

import android.content.Context
import com.howell.modules.ImpBasePresenter
import com.howell.modules.ImpBaseView
import com.howell.modules.pdc.bean.PDCDevice
import com.howellsdk.net.http.bean.PDCSample

/**
 * Created by Administrator on 2017/12/12.
 */
interface IPDCContract {
    interface IView : ImpBaseView{
        fun onQueryDeviceResult(deviceList:ArrayList<PDCDevice>)
        fun onQuerySimpleResult(simpleList:ArrayList<PDCSample>,unit: String)
    }

    interface IPresent : ImpBasePresenter{
        fun init(c:Context)
        fun queryDevice()
        fun querySamples(id:String,unit:String,beg:String,end:String)
        fun queryHistory(id:String,beg:String,end:String,eventType:String)
        fun queryTest(id:String?)
    }
}