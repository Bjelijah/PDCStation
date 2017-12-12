package com.howell.modules.login

import android.content.Context
import com.howell.modules.ImpBasePresenter
import com.howell.modules.ImpBaseView
import com.howell.modules.login.bean.Type

/**
 * Created by Administrator on 2017/11/27.
 */
interface ILoginContract {
    interface IVew:ImpBaseView{
        fun onError(type:Type)
        fun onLoginSuccess(account:String,email:String)
        fun onLogoutResult(type : Type)
    }
    interface IPresenter:ImpBasePresenter{
        fun init(c:Context)
        fun login(name:String?,pwd:String?)
        fun logout()
        fun changeUser(userName:String,email:String)
    }
}