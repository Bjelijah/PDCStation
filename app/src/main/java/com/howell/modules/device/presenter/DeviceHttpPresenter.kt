package com.howell.modules.device.presenter

import android.util.Log
import com.howell.action.ConfigAction
import com.howell.bean.CameraItemBean
import com.howellsdk.api.ApiManager
import com.howellsdk.net.http.utils.Util
import io.reactivex.Observable
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Administrator on 2017/11/30.
 */
class DeviceHttpPresenter :DeviceBasePresenter() {

    override fun queryDevices() {
        ApiManager.getInstance().getHWHttpService(mURL)
                .queryBusinessVideoInputChannel(ApiManager.HttpHelp.getCookie(ApiManager.HttpHelp.Type.BUSINESS_VIDEO_INPUT),null,null)
                .map { videoList -> videoList.videoInputChannelPermissiones }
                .flatMap { list-> Observable.fromIterable(list)}
                .map { video ->
//                    Log.i("123","video=$video")
                    var bean = CameraItemBean(video.id)
                    bean.cameraName         = video.name
                    bean.cameraDescription  = ""
                    bean.indensity          = 0
                    bean.deviceId           = Util.transformItemId2DeviceId(video.id)
                    bean.channelNo          = Util.transfromItemId2DeviceChannel(video.id)
                    bean.isOnline           = video.videoInputChannel.online
                    bean.isPtz              = video.videoInputChannel.isPtz
                    bean.isStore            = true
                    bean.upnpIP             = ConfigAction.instance.mTurnIP
                    bean.upnpPort           = ConfigAction.instance.mTurnPort
                    bean.deVer              = ""
                    bean.picturePath        = "/sdcard/eCamera/cache/${video.id}.jpg"
                    return@map bean
                }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :SingleObserver<List<CameraItemBean>>{
                    override fun onSuccess(list: List<CameraItemBean>) {
                       mView?.onQueryResult(list as ArrayList<CameraItemBean>)
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }

                })

    }
}