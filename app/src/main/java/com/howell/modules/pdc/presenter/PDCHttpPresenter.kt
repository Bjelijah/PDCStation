package com.howell.modules.pdc.presenter

import android.util.Log
import com.howellsdk.api.ApiManager
import com.howellsdk.net.http.bean.PDCDeviceList
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Administrator on 2017/12/12.
 */
class PDCHttpPresenter : PDCBasePresenter() {



    override fun queryDevice() {
        ApiManager.getInstance().getHWHttpService(mUrl)
                .queryPdcDevices(ApiManager.HttpHelp.getCookie(ApiManager.HttpHelp.Type.PDC_DEVICE),null,null,null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<PDCDeviceList>{
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }

                    override fun onNext(l: PDCDeviceList) {
                        Log.i("123","l=$l")
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
    }


}