package com.howell.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.howell.jni.JniUtil
import com.howell.modules.login.ILoginContract
import com.howell.modules.login.bean.Type
import com.howell.modules.login.presenter.LoginHttpPresenter
import com.howell.pdcstation.R
import com.howellsdk.utils.SDKDebugLog

/**
 * Created by Administrator on 2017/11/24.
 */
class LogoActivity : BaseActivity(),ILoginContract.IVew {

    private val MSG_START = 0x01
    private val mHander = Handler(Handler.Callback { msg ->
        when(msg.what){
            MSG_START->doLogin()
        }
        false
    })
    private var mPresenter:ILoginContract.IPresenter?=null

    override fun findView() {
        setContentView(R.layout.activity_logo)
    }

    override fun initView() {
        bindPresenter()
    }

    override fun initData() {
        JniUtil.logEnable(true)
        SDKDebugLog.LogEnable(true)
        mHander.sendEmptyMessageDelayed(MSG_START,1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindPresenter()
    }


    private fun doLogin(){
        mPresenter?.login(null,null)
    }

    override fun bindPresenter() {
        if (mPresenter==null)mPresenter=LoginHttpPresenter()
        mPresenter?.bindView(this)


        mPresenter?.init(this)
    }

    override fun unbindPresenter() {
        if (mPresenter!=null)mPresenter?.unbindView()
    }

    override fun onError(type: Type) {
        when(type){
            Type.FIRST_LOGIN-> {Log.i("123","first login");startActivity(Intent(this,LoginActivity::class.java))}
            else->{Log.i("123","login on error");startActivity(Intent(this,LoginActivity::class.java))}
        }
        finish()
    }

    override fun onLoginSuccess(account: String, email: String) {
        startActivity(Intent(this@LogoActivity,HomeActivity::class.java)
                .putExtra("account",account)
                .putExtra("email",email))
        finish()
    }

    override fun onLogoutResult(type: Type) {
        //todo nothing to do
    }



}