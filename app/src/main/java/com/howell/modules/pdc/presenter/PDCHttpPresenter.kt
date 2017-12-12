package com.howell.modules.pdc.presenter

import android.util.Log
import com.howellsdk.api.ApiManager
import com.howellsdk.net.http.bean.PDCDeviceList
import com.howellsdk.net.http.bean.PDCSampleList
import com.howellsdk.utils.Util
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by Administrator on 2017/12/12.
 */
class PDCHttpPresenter : PDCBasePresenter() {


    var mId:String? = null

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
                        l.pdcDevices
                                .filter { it.deviceStatus.online }
                                .forEach { mId = it.id }

                        if (mId==null)mId = l.pdcDevices[0]!!.id

                        val nowDate = Date()
                        val calendar = Calendar.getInstance()
                        calendar.time = nowDate
                        calendar.add(Calendar.DATE, -1)
                        val beforeDate = calendar.time
                        val nowStr = Util.Date2ISODate(nowDate)
                        val beforeStr = Util.Date2ISODate(beforeDate)

                        querySamples(mId?:"","Minute",nowStr,beforeStr)


                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
    }


    override fun querySamples(id: String, unit: String, beg: String, end: String) {
        ApiManager.getInstance().hwHttpService.queryPdcDeviceSamples(
                ApiManager.HttpHelp.getCookie(ApiManager.HttpHelp.Type.PDC_DEVICE_SAMPLES,id),
                id, unit, beg, end, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :Observer<PDCSampleList>{
                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                    override fun onNext(l: PDCSampleList) {
                        Log.i("123","$l")
                    }

                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }

                    override fun onComplete() {
                    }
                })





    }

}