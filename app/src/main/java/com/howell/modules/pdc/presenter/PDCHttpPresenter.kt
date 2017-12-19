package com.howell.modules.pdc.presenter

import android.util.Log
import com.howell.modules.pdc.bean.PDCDevice
import com.howellsdk.api.ApiManager
import com.howellsdk.net.http.bean.PDCDeviceList
import com.howellsdk.net.http.bean.PDCSample
import com.howellsdk.net.http.bean.PDCSampleList
import com.howellsdk.utils.Util
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Administrator on 2017/12/12.
 */
class PDCHttpPresenter : PDCBasePresenter() {


    var mId:String? = null

    override fun queryDevice() {
        ApiManager.getInstance().getHWHttpService(mUrl)
                .queryPdcDevices(ApiManager.HttpHelp.getCookie(ApiManager.HttpHelp.Type.PDC_DEVICE),null,null,null)
                .map { pdcDeviceList -> pdcDeviceList.pdcDevices }
                .flatMap { list -> Observable.fromIterable(list) }
                .map { d ->
                    val bean = PDCDevice(d.id)
                    bean.createTime       = d.creationTime
                    bean.name             = d.name
                    bean.model            = d.model
                    bean.firmware         = d.firmware
                    bean.serial           = d.serialNumber
                    bean.information      = d.information
                    bean.userName         = d.userName
                    bean.password         = d.password
                    bean.uri              = d.uri
                    bean.protocolType     = d.protocolType
                    bean.parentId         = d.parentDeviceId
                    bean.timeSync         = d.timeSynchronizing
                    bean.resetTime        = d.resetTime
                    bean.structuredable   = d.structuredAbilities
                    bean.lastSecond       = d.lastNSeconds
                    bean.inDatabase       = d.existedInDatabase
                    bean.onLine           = d.deviceStatus.online
                    bean.lastUpdate       = d.deviceStatus.lastUpdateTime
                    bean.leaveNumber      = d.deviceStatus.leaveNumber
                    bean.enterNumber      = d.deviceStatus.enterNumber
                    bean.deviationNumber  = d.deviceStatus.deviationNumber
                    bean.lastResetTime    = d.deviceStatus.lastResetTime
                    bean.lastLeaverNumber = d.deviceStatus.lastNLeaverNumber
                    bean.lastEnterNumber  = d.deviceStatus.lastNEnterNumber
                    bean.thresholdable    = d.threshold.enable
                    bean
                }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<PDCDevice>> {
                    override fun onSuccess(l: List<PDCDevice>) {
                        Log.i("123","$l")
                        mView?.onQueryDeviceResult(l as ArrayList<PDCDevice>)
                    }
                    override fun onError(e: Throwable)  = e.printStackTrace()
                    override fun onSubscribe(d: Disposable)  = addDisposable(d)
                })
    }

    override fun querySamples(id: String, unit: String, beg: String, end: String) {
        ApiManager.getInstance().hwHttpService.queryPdcDeviceSamples(
                ApiManager.HttpHelp.getCookie(ApiManager.HttpHelp.Type.PDC_DEVICE_SAMPLES,id),
                id, unit, beg, end, null, null)
                .map { l -> l.pdcSamples }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :Observer<ArrayList<PDCSample>>{
                    override fun onError(e: Throwable) = e.printStackTrace()

                    override fun onNext(l: ArrayList<PDCSample>) {
                        mView?.onQuerySimpleResult(l,unit)
                    }

                    override fun onSubscribe(d: Disposable) = addDisposable(d)
                    override fun onComplete() {}
                })
    }

}