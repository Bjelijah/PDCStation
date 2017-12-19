package com.howell.activity.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.howell.action.ConfigAction
import com.howell.bean.UserDbBean
import com.howell.db.UserDao
import com.howellsdk.utils.RxUtil
import java.util.*

/**
 * Created by Administrator on 2017/12/19.
 */
class FingerPrintSaveDialog() : FingerPrintBaseDialog() {

    @SuppressLint("ValidFragment")
    constructor(o:OnFignerPrintIDListener,title:String,msg:String):this(){
        onFinger = o
        mTitle = title
        mMsg = msg
    }

    override fun onAuthenticated(id: Int) {
        saveToDB(id)
    }

    private fun saveToDB(id:Int){
        RxUtil.doRxTask(object : RxUtil.CommonTask<Context>(context){
            var name:String? = null
            var pwd:String?  = null
            override fun doInIOThread() {
                ConfigAction.instance.load(context)
                name = ConfigAction.instance.mName
                pwd  = ConfigAction.instance.mPwd
                val ip   = ConfigAction.instance.mServerIP
                val port = ConfigAction.instance.mServerPort
                val ssl  = ConfigAction.instance.mServerSSL
                val dao = UserDao(context,"user.db",1)
                val bean = UserDbBean(id,name,pwd,ip,port,if (ssl==true)1 else 0,0)
                if(dao.findByfpId(id)){
                    dao.updataByFpID(bean)
                }else{
                    dao.insert(bean)
                }
                dao.close()
            }

            override fun doInUIThread() {
                showAuthenticationInfo(MyState.SAVE_OK)
                onFinger?.onFignerPrint(true,id,name,pwd)
                mCancel?.postDelayed({dismiss()},1000)
            }

            override fun doError(throwable: Throwable?) {
                super.doError(throwable)
                onFinger?.onFignerPrint(false,id,name,pwd)
                dismiss()
            }
        })
    }

}