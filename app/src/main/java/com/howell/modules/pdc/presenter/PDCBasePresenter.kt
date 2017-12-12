package com.howell.modules.pdc.presenter

import android.content.Context
import com.howell.action.ConfigAction
import com.howell.modules.BasePresenter
import com.howell.modules.ImpBaseView
import com.howell.modules.pdc.IPDCContract
import com.howell.modules.player.IPlayContract

/**
 * Created by Administrator on 2017/12/12.
 */
abstract class PDCBasePresenter :BasePresenter(),IPDCContract.IPresent {
    var mView: IPDCContract.IView?           = null
    var mContext:Context?                    = null
    var mUrl:String?                         = null
    override fun bindView(view: ImpBaseView) {
        mView = view as IPDCContract.IView
    }

    override fun unbindView() {
        dispose()
        mView = null
    }


    override fun init(c: Context) {
        ConfigAction.instance.load(c)
        mContext = c
        mUrl     = ConfigAction.instance.mUrl
    }

}