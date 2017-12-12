package com.howell.modules.login.presenter

import android.util.Log
import com.howell.action.ConfigAction
import com.howell.modules.login.bean.Type
import com.howellsdk.api.ApiManager
import com.howellsdk.net.http.bean.ClientCredential
import com.howellsdk.net.http.bean.Fault
import com.howellsdk.net.http.bean.TeardownCredential
import com.howellsdk.net.http.bean.UserNonce
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Administrator on 2017/11/27.
 */
class LoginHttpPresenter :LoginBasePresenter() {


    private fun login2Server(userName:String,pwd:String,req:ClientCredential){
        ApiManager.getInstance().getHWHttpService(mUrl)
                .doUserAuthenticate(req)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :Observer<Fault>{
                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        mView?.onError(Type.ERROR)
                    }

                    override fun onNext(t: Fault) {
                        ApiManager.HttpHelp.setCookie(req.userName,req.domain,t.id,req.verifySession)
                        if (t.faultCode.equals("0",true)){
                            mName = userName
                            mPwd  = pwd
                            ConfigAction.instance.saveThisUserInfo(mContext!!, mName!!, mPwd!!)
                            mView?.onLoginSuccess(req.userName,req.userName)

                        }else{
                            mView?.onError(Type.AUTHENCATION)
                        }
                    }

                    override fun onComplete() {

                    }

                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }

                })
    }

    override fun login(name: String?, pwd: String?) {
        if (name==null||pwd==null){
            if (this!!.mISFirst!!){mView?.onError(Type.FIRST_LOGIN);return}
            if (mName==null||mPwd==null){mView?.onError(Type.ERROR);return}
        }else{
            mName=name
            mPwd=pwd
        }

        ApiManager.getInstance().initHttpClient(mContext, mIsSSL!!)
                .getHWHttpService(mUrl)
                .getUserNonce(mName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :Observer<UserNonce>{
                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        when {
                            e.message?.contains("Failed to connect",true) == true -> mView?.onError(Type.ERROR)
                            e.message?.contains("Not Found",true) == true         -> mView?.onError(Type.ACCOUNT_NOT_EXIST)
                            else                                                                   -> mView?.onError(Type.ERROR)
                        }
                    }

                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }

                    override fun onComplete() {
                    }

                    override fun onNext(t: UserNonce) {
                        login2Server(mName!!, mPwd!!, ClientCredential(
                                mName,
                                mPwd,
                                t.domain,
                                t.nonce
                        ))
                    }
                })




    }

    override fun logout() {
        ApiManager.getInstance().getHWHttpService(mUrl)
                .doUserTeardown(TeardownCredential(mName,ApiManager.HttpHelp.getSession(),null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Fault>{
                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }

                    override fun onNext(t: Fault) {
                        Log.i("123","fault=$t")
                    }

                    override fun onError(e: Throwable) {
                        mView?.onError(Type.ERROR)
                    }

                    override fun onComplete() {
                    }

                })
    }

    override fun changeUser(userName: String, email: String) {
    }


}