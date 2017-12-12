package com.howell.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.howell.pdcstation.R
import com.howellsdk.utils.RxUtil
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList


/**
 * Created by Administrator on 2017/12/8.
 */
object DialogUtils {
    fun postMsgDialog(c: Context,title:String,msg:String,cb: DialogInterface.OnClickListener?){
        RxUtil.doInUIThread(object :RxUtil.RxSimpleTask<Objects>(){
            override fun doTask() {
                AlertDialog.Builder(c)
                        .setTitle(title).setMessage(msg)
                        .setPositiveButton(c.resources.getString(R.string.ok),cb)
                        .create().show()
            }

        })

    }

    fun postMsgDialog(c:Context,title:String,msg:String,posCb:DialogInterface.OnClickListener?,negCb:DialogInterface.OnClickListener?){
        RxUtil.doInUIThread(object :RxUtil.RxSimpleTask<Objects>(){
            override fun doTask() {
                AlertDialog.Builder(c)
                        .setTitle(title).setMessage(msg)
                        .setPositiveButton(c.resources.getString(R.string.ok),posCb)
                        .setNegativeButton(c.resources.getString(R.string.cancel),negCb)
                        .create().show()
            }

        })

    }

    fun postMsgDialog(c:Context,title: String,msg: String,posStr:String,negStr:String,posCb:DialogInterface.OnClickListener?,negCb:DialogInterface.OnClickListener?){
        RxUtil.doInUIThread(object :RxUtil.RxSimpleTask<Objects>(){
            override fun doTask() {
                AlertDialog.Builder(c)
                        .setTitle(title).setMessage(msg)
                        .setPositiveButton(posStr,posCb)
                        .setNegativeButton(negStr,negCb)
                        .create().show()
            }

        })

    }

}