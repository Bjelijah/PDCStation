package com.howell.modules.device

import android.content.Context
import com.howell.bean.CameraItemBean
import com.howell.modules.ImpBasePresenter
import com.howell.modules.ImpBaseView

/**
 * Created by Administrator on 2017/11/30.
 */
interface IDeviceContract {
    interface IVew:ImpBaseView{
        fun onQueryResult(beanList: ArrayList<CameraItemBean>)
        fun onError()
    }
    interface IPresenter:ImpBasePresenter{
        fun init(c:Context)
        fun queryDevices()
    }
}