package com.howell.modules.player.presenter

import android.content.Context
import android.util.Log
import com.howell.action.ConfigAction
import com.howell.modules.player.bean.CameraItemBean
import com.howell.modules.player.bean.PTZ
import com.howell.modules.player.bean.VODRecord
import com.howellsdk.api.ApiManager
import com.howellsdk.api.HWPlayApi
import com.howellsdk.player.turn.bean.PTZ_CMD
import com.howellsdk.player.turn.bean.TurnGetRecordedFileAckBean
import com.howellsdk.player.turn.bean.TurnSubScribeAckBean
import com.howellsdk.utils.RxUtil
import com.howellsdk.utils.ThreadUtil
import com.howellsdk.utils.Util
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


/**
 * Created by Administrator on 2017/12/5.
 */
class PlayTurnPresenter : PlayBasePresenter() {

    var mWaiteNum = 0
    private var firstTimeStamp = 0L
    private var mCurPage: Int = 0
    private val mPageSize = 20
    private var mLastVODTime = ""

    override fun init(c: Context, bean: CameraItemBean) {
        super.init(c, bean)
        Observable.create<Boolean> { e ->
            val ret = ApiManager.getInstance()
                    .getTurnService(
                    mContext,
                    bean.upnpIP,
                    bean.upnpPort!!,
                    bean.deviceId,
                    bean.channelNo!!,
                    true,
                    ConfigAction.instance.mName,
                    ConfigAction.instance.mPwd,
                    ConfigAction.instance.mTurnSSL!!,
                    ConfigAction.instance.mImei,
                    object :HWPlayApi.ITurnCB{
                        override fun onConnect(sessionId: String?) {
                            Log.i("123","turn presenter onConnect we not callback view sessionId=$sessionId")
                        }

                        override fun onDisconnect() {
                            Log.i("123","turn onDisconnect")
                        }

                        override fun onDisconnectUnexpect(flag: Int) {
                            Log.i("123", "on disconnect unexpect flag=$flag")
                            when(flag){
                                2->mView?.onError(0)
                                3->mView?.onError(1)
                                else->{}
                            }
                        }

                        override fun onRecordFileList(fileList: TurnGetRecordedFileAckBean?) {
                            mCurPage++
                            if(fileList?.recordFileCount!! <= 0) {mView?.onRecord(null);return}
                            val list = fileList.recordedFiles
                            val vods:ArrayList<VODRecord> = ArrayList()
                            for (f in list){
                                val beg = Util.ISODateString2Date(f.beginTime)
                                val end = Util.ISODateString2Date(f.endTime)
                                val hasTitle = if (mLastVODTime != beg.substring(0,10)){
                                    mLastVODTime = beg.substring(0,10)
                                    true
                                }else false
                                vods.add(VODRecord(beg,end,f.beginTime,f.endTime,0,"",hasTitle))
                            }
                            mView?.onRecord(vods)
                        }

                        override fun onSubscribe(res: TurnSubScribeAckBean?) {
                            Log.i("123", "turn on subscribe res=" + res.toString())
                        }

                        override fun onUnsubscribe() {
                            Log.i("123", "turn on unsubscribe")
                        }

                    })
                    .bindCam().connect()
            e.onNext(ret)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }

                    override fun onComplete() {
                        Log.i("123","turn connect ok")
                    }

                    override fun onNext(b: Boolean) {
                        mView?.onConnect(b)////??
                    }

                    override fun onError(t: Throwable) {
                        t.printStackTrace()
                        mView?.onError(0)
                    }
                })

    }

    override fun deInit() {
        super.deInit()
        Observable.create<Boolean> { e->
            val ret = ApiManager.getInstance().turnService.disconnect()
            ApiManager.getInstance().turnService.unBindCam()
            e.onNext(ret)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<Boolean> { b-> mView?.onDisconnect(b) },
                        Consumer<Throwable> { e-> e.printStackTrace();mView?.onDisconnect(false) })
    }


    override fun play(isSub: Boolean) {
        Observable.create<Boolean> { e ->
            ApiManager.getInstance().turnService
                    .play(isSub)
            e.onNext(true)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :Observer<Boolean>{
                    override fun onNext(p0: Boolean)  = startTimeTask()

                    override fun onSubscribe(d: Disposable)  = addDisposable(d)

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        mView?.onError(0)
                    }

                    override fun onComplete() {}
                })
    }

    override fun playback(isSub: Boolean, beg: String, end: String) {
        firstTimeStamp = 0L
        val begISO = Util.DateString2ISODateString(beg)
        val endISO = Util.DateString2ISODateString(end)
        Observable.create<Boolean> { e ->
            ApiManager.getInstance().turnService
                    .playback(isSub,begISO,endISO)
            e.onNext(true)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean>{
                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        mView?.onError(0)
                    }

                    override fun onNext(p0: Boolean) {
                        mView?.onPlaybackStartEndTime(
                                Util.DateString2Date(beg).time / 1000,
                                Util.DateString2Date(end).time / 1000)
                        startTimeTask()
                    }

                    override fun onSubscribe(d: Disposable) = addDisposable(d)

                    override fun onComplete() {}
                })
    }

    override fun stop() {
        RxUtil.doInIOTthread(object : RxUtil.RxSimpleTask<Objects>() {
            override fun doTask() {
                ApiManager.getInstance().turnService.stop()
                stopTimeTask()
            }
        })


    }

    override fun pause(): Boolean = ApiManager.getInstance().turnService.playPause()

    override fun reLink(isSub: Boolean,startTime:String?,endTime:String?) {
        val beg = Util.DateString2ISODateString(startTime)
        val end = Util.DateString2ISODateString(endTime)
        RxUtil.doInIOTthread(object :RxUtil.RxSimpleTask<Objects>(){
            override fun doTask() {
                ApiManager.getInstance().turnService.reLink(isSub,beg,end)
                firstTimeStamp = 0
            }
        })
    }

    override fun ptzCtrl(cmd: PTZ) {
        val ptzCmd =
        when(cmd){
            PTZ.PTZ_UP          -> PTZ_CMD.ptz_up
            PTZ.PTZ_DOWN        -> PTZ_CMD.ptz_down
            PTZ.PTZ_LEFT        -> PTZ_CMD.ptz_left
            PTZ.PTZ_RIGHT       -> PTZ_CMD.ptz_right
            PTZ.PTZ_STOP        -> PTZ_CMD.ptz_stop
            PTZ.PTZ_ZOOM_WIDE   -> PTZ_CMD.ptz_zoomWide
            PTZ.PTZ_ZOOM_TELE   -> PTZ_CMD.ptz_zoomTele
            PTZ.PTZ_ZOOM_STOP   -> PTZ_CMD.ptz_zoomStop
            else                -> PTZ_CMD.ptz_null
        }
        RxUtil.doInIOTthread(object :RxUtil.RxSimpleTask<PTZ_CMD>(ptzCmd){
            override fun doTask() {
                ApiManager.getInstance().turnService.ptzControl(t,15,null)
            }
        })


    }

    override fun catchPic(path: String) {
        val destDir = File(path)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        val nameDirPath = path + "/" + mBean!!.id + ".jpg"
        RxUtil.doInIOTthread(object : RxUtil.RxSimpleTask<String>(nameDirPath) {
            override fun doTask() {
                ApiManager.getInstance().turnService.catchPic(t)
            }
        })
    }

    override fun vodReset() {
        mCurPage = 1
    }

    override fun getVodRecord(startTime: String, endTime: String) {
        val beg = Util.DateString2ISODateString(startTime)
        val end = Util.DateString2ISODateString(endTime)
        RxUtil.doInIOTthread(object :RxUtil.RxSimpleTask<Objects>(){
            override fun doTask() {
                ApiManager.getInstance().turnService.getRecordedFiles(beg,end,mCurPage,mPageSize)
            }
        })
    }

    override fun playMoveTo(isSub: Boolean, beg: String, end: String) {
        val isoBeg = Util.DateString2ISODateString(beg)
        val isoEnd = Util.DateString2ISODateString(end)
        RxUtil.doInIOTthread(object : RxUtil.RxSimpleTask<Any>() {
            override fun doTask() {
                ApiManager.getInstance().turnService
                        .reLink(isSub, isoBeg, isoEnd)
            }
        })

    }


    override fun startTimeTask() {
        ThreadUtil.scheduledSingleThreadStart({
            var bWait = true
            val streamLen = ApiManager.getInstance().turnService.streamLen
            if (streamLen != 0) {
                bWait = false
                mWaiteNum = 0
            } else {
                mWaiteNum++
                if (mWaiteNum === 3) {
                    bWait = true
                }
            }
            val speed = streamLen * 8 / 1024 / F_TIME
            val timestamp = ApiManager.getInstance().turnService.timestamp
            val firstTimestamp = ApiManager.getInstance().turnService.firstTimestamp
            if (firstTimeStamp == 0L && firstTimestamp != 0L) {
                firstTimeStamp = firstTimestamp
                Log.i("123","~~~~~~~~~~~~~~firstTimtstamp= $firstTimeStamp")
            }
//                Log.i("123","timestamp:"+timestamp+"  FIRST="+firstTimestamp+"   mF="+firstTimeStamp);
            if (firstTimestamp != 0L) {//第一针还没来
                mView!!.onTime(speed, timestamp, firstTimeStamp, bWait)
            }
        },0, F_TIME.toLong(), TimeUnit.SECONDS)
    }

    override fun stopTimeTask() {
        ThreadUtil.scheduledSingleThreadShutDown()
    }


}