package com.howell.modules.login.presenter

import android.content.Context
import android.graphics.Bitmap
import com.howell.action.ConfigAction
import com.howell.modules.BasePresenter
import com.howell.modules.ImpBaseView
import com.howell.modules.login.ILoginContract
import com.howell.utils.UserConfigSp

/**
 * Created by Administrator on 2017/11/27.
 */
abstract class LoginBasePresenter : BasePresenter(),ILoginContract.IPresenter  {

    var mView:ILoginContract.IVew?  = null
    var mContext:Context?           = null
    var mName:String?               = null
    var mPwd:String?                = null
    var mUrl:String?                = null
    var mISFirst:Boolean?           = null
    var mIsSSL:Boolean?             = null
    override fun bindView(view: ImpBaseView ) {
        mView = view as? ILoginContract.IVew
    }

    override fun unbindView() {
        dispose()
        mView=null
    }

    override fun init(c: Context) {
        ConfigAction.instance.load(c)
        mContext  = c
        mName     = ConfigAction.instance.mName
        mPwd      = ConfigAction.instance.mPwd
        mUrl      = ConfigAction.instance.mUrl
        mISFirst  = ConfigAction.instance.mIsFirst
        mIsSSL    = ConfigAction.instance.mServerSSL
    }

}