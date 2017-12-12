package com.howell.modules.device.presenter

import android.content.Context
import com.howell.action.ConfigAction
import com.howell.modules.BasePresenter
import com.howell.modules.ImpBaseView
import com.howell.modules.device.IDeviceContract

/**
 * Created by Administrator on 2017/11/30.
 */
abstract class DeviceBasePresenter:BasePresenter(), IDeviceContract.IPresenter {
    var mView:IDeviceContract.IVew? = null
    var mContext:Context?           = null
    var mURL:String?                = null


    override fun bindView(view: ImpBaseView) {
        mView = view as IDeviceContract.IVew
    }

    override fun unbindView() {
        dispose()
        mView = null
    }

    override fun init(c:Context) {
        mContext = c
        mURL     = ConfigAction.instance.mUrl
    }


}