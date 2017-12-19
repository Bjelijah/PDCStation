package com.howell.activity.view

import android.annotation.SuppressLint
import android.util.Log
import com.howell.action.ConfigAction
import com.howell.bean.UserDbBean
import com.howell.db.UserDao
import com.howellsdk.utils.RxUtil
import java.util.*

/**
 * Created by Administrator on 2017/12/18.
 */
class FingerPrintLoadDialog() : FingerPrintBaseDialog() {

    @SuppressLint("ValidFragment")
    constructor(o:OnFignerPrintIDListener,title:String,msg:String):this(){
        onFinger = o
        mTitle = title
        mMsg = msg
    }


    override fun onAuthenticated(id: Int) {
        signIn(id)
    }

    private fun signIn(id:Int){
        Log.i("123"," signIn")
        RxUtil.doRxTask(object :RxUtil.CommonTask<Objects>(){
            var bean: UserDbBean?=null
            override fun doInIOThread() {
                val dao = UserDao(context,"user.db",1)
                val beans = dao.queryByfpID(id)
                dao.close()
                bean = beans?.get(0)
            }

            override fun doInUIThread() {
                Log.i("123","$bean")
                if (bean==null) {
                    showAuthenticationInfo(MyState.SIGN_ERROR)
                    return
                }
                val name = bean?.name
                val pwd  = bean?.password
                val ip   = bean?.ip
                val port = bean?.port
                val ssl  = bean?.ssl
                val st   = bean?.station
                showAuthenticationInfo(MyState.OK)
                ConfigAction.instance.saveThisServerInfo(context,ip?:"",port?:0,ssl==1)
                onFinger?.onFignerPrint(true,id,name?:"",pwd?:"")
            }

            override fun doError(throwable: Throwable?) {
                super.doError(throwable)
                showAuthenticationInfo(MyState.SIGN_ERROR)
            }


        })
    }
}