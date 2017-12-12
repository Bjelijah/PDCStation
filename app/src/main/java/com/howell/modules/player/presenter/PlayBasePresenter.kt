package com.howell.modules.player.presenter

import android.content.Context
import com.howell.action.ConfigAction
import com.howell.bean.CameraItemBean
import com.howell.modules.BasePresenter
import com.howell.modules.ImpBaseView
import com.howell.modules.player.IPlayContract

/**
 * Created by Administrator on 2017/12/5.
 */
abstract class PlayBasePresenter :BasePresenter(),IPlayContract.IPresent {
    var mView:IPlayContract.IView?           = null
    var mContext:Context?                   = null
    var mBean:CameraItemBean?               = null
    var mAccount:String?                    = null
    val F_TIME                              = 1//刷新率  s
    override fun bindView(view: ImpBaseView) {
        mView = view as IPlayContract.IView
    }

    override fun unbindView() {
        dispose()
        mView = null
    }

    override fun init(c: Context, bean: CameraItemBean) {
        mContext    = c
        mBean       = bean
        mAccount    = ConfigAction.instance.mName
    }

    override fun deInit() {
        mContext    = null
        mBean       = null
    }


    abstract fun startTimeTask()
    abstract fun stopTimeTask()
}