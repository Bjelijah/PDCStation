package com.howell.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.SeekBar
import com.howell.activity.view.WheelTimeDialog
import com.howell.adapter.MyPagerAdapter
import com.howell.modules.player.bean.PTZ
import com.howell.modules.player.bean.VODRecord
import com.howell.pdcstation.R
import com.howell.transformer.CubeInTransformer
import com.howell.utils.DialogUtils
import com.howell.utils.PhoneConfig
import com.howellsdk.utils.RxUtil
import com.howellsdk.utils.Util
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Administrator on 2017/12/4.
 */
class PlayViewActivity : BasePlayActivity(), View.OnTouchListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private var mGestureDetector:GestureDetector?=null
    private var mListener = MyOnGestureListener()
    private var mAnimationNum        = 0
    private var bAnimationFinish     = true
    private var mIsShowPtz: Boolean  = false
    private var mIsPlayBack: Boolean = false
    private var mBegTime:String?     = null
    private var mEndTime:String?     = null

    private var mCurBeg:Long         = 0 //当前滑杆条开始位置
    private var mCurEnd:Long         = 0
    private var mLastProgressOffset: Long = 0
    private var mKeepProgress        = false
    private var mUseProgress         = false

    private val MSG_PLAY_PLAY_BACK_FUN:Int = 0xf1

    private var mHandler = Handler(Handler.Callback { msg ->
                when(msg.what){
                    MSG_PLAY_PLAY_BACK_FUN->playbackFun()
                }
        false
    })


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(v?.id){
            R.id.play_ptz_left,R.id.play_ptz_right,R.id.play_ptz_top,R.id.play_ptz_bottom -> {funPtz(v?.id,event!!);return false}
        }
        return mGestureDetector?.onTouchEvent(event)?:false
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.pop_layout_sd ->{
                mIsSub=true
                mPopupWindow.dismiss()
                mStreamChange.text = getString(R.string.popup_window_sd)
                mWaitProgressBar.visibility = View.VISIBLE
                mPresent?.reLink(true,null,null)
            }
            R.id.pop_layout_hd ->{
                mIsSub=false
                mPopupWindow.dismiss()
                mStreamChange.text = getString(R.string.popup_window_hd)
                mWaitProgressBar.visibility = View.VISIBLE
                mPresent?.reLink(false,null,null)
            }
            R.id.player_change_stream -> mPopupWindow.showAsDropDown(v)
            R.id.player_imagebutton_back->{
                mPresent?.catchPic("/sdcard/eCamera/cache")
                finish()
            }
            R.id.ib_pause ->{
                if(mPresent?.pause()==true){
//                    mPause.setImageDrawable(getDrawable(R.mipmap.img_play))
                    mPause.setImageDrawable(IconicsDrawable(this, GoogleMaterial.Icon.gmd_play).actionBar().color(Color.WHITE))
                }else{
//                    mPause.setImageDrawable(getDrawable(R.mipmap.img_pause))
                    mPause.setImageDrawable(IconicsDrawable(this, GoogleMaterial.Icon.gmd_pause).actionBar().color(Color.WHITE))
                }
            }
            R.id.vedio_list->{
                 mHandler.removeMessages(MSG_PLAY_PLAY_BACK_FUN)
                 if (mIsPlayBack){
                    //当前是playback 切换为  playview
                    mIsPlayBack = false
                    initPlayView()
                    mBegTime = null
                    mEndTime = null
                    mPresent?.reLink(mIsSub,null,null)
                }else{
                    //当前是playview 切换为 playback

                    WheelTimeDialog(mCam).show(supportFragmentManager,"")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initData()
        super.onCreate(savedInstanceState)
        initPlayView()
        initFun()
    }

    override fun onDestroy() {
        mHandler.removeMessages(MSG_PLAY_PLAY_BACK_FUN)
        super.onDestroy()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mIsPlayBack = intent?.getBooleanExtra("isPlayback",false)?:false
        mBegTime = intent?.getStringExtra("BegTime")?:null
        mEndTime = intent?.getStringExtra("EndTime")?:null

        onPlaybackStartEndTime(Util.DateString2Date(mBegTime).time / 1000,
                Util.DateString2Date(mEndTime).time / 1000)

        initPlayView()
        mPresent?.reLink(mIsSub,mBegTime,mEndTime)
        mHandler?.sendEmptyMessageDelayed(MSG_PLAY_PLAY_BACK_FUN,1000)
    }





    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mPresent!!.catchPic("/sdcard/eCamera/cache")
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initData(){
        mIsPlayBack = intent.getBooleanExtra("isPlayback",false)
        Log.i("123","initData   misPlayback=$mIsPlayBack")
        if (mIsPlayBack){
            mBegTime = intent.getStringExtra("BegTime")
            mEndTime = intent.getStringExtra("EndTime")
        }
    }


    private fun initPlayView(){
        if(mIsPlayBack){
            mVodList.setImageDrawable(IconicsDrawable(this, GoogleMaterial.Icon.gmd_camera).actionBar().color(Color.WHITE))
            mPause.setImageDrawable(IconicsDrawable(this, GoogleMaterial.Icon.gmd_pause).actionBar().color(Color.WHITE))
            mReplaySeekBar.visibility   = View.VISIBLE
            mPause.visibility           = View.VISIBLE
            mStreamChange.visibility    = View.GONE
            mBtTalk.visibility          = View.GONE
            mVodList.visibility         = View.VISIBLE
            mBtCatch.visibility         = View.GONE
            mWaitProgressBar.visibility = View.GONE//?
        }else{
            mVodList.setImageDrawable(IconicsDrawable(this, GoogleMaterial.Icon.gmd_camera_roll).actionBar().color(Color.WHITE))
            mReplaySeekBar.visibility   = View.GONE
            mPause.visibility           = View.GONE
            mStreamChange.visibility    = View.VISIBLE
            mBtTalk.visibility          = View.GONE
            mVodList.visibility         = View.VISIBLE//todo 横向不要显示
            mBtCatch.visibility         = View.GONE
            mWaitProgressBar.visibility = View.VISIBLE//?
            fragmentInit()
        }
        mReplaySeekBar.progress = 0
    }

    private fun initFun() {

        mBtTalk.setOnTouchListener(this)
        mSound.setOnClickListener(this)
        mVodList.setOnClickListener(this)
        mBtCatch.setOnClickListener(this)
        mHD.setOnClickListener(this)
        mSD.setOnClickListener(this)
        mStreamChange.setOnClickListener(this)
        mBack.setOnClickListener(this)
        mPause.setOnClickListener(this)
        mReplaySeekBar.setOnSeekBarChangeListener(this)
    }

    private fun fragmentInit(){
        mGestureDetector = GestureDetector(this,mListener)
        if (mCam.isPtz != true) return
        fragmentPtzInit()
        fragmentFunInit()
    }
    private fun fragmentPtzInit(){
        mPtzLeft.setOnTouchListener(this)
        mPtzRight.setOnTouchListener(this)
        mPtzUp.setOnTouchListener(this)
        mPtzDown.setOnTouchListener(this)
        mIsShowPtz = false
    }
    private fun fragmentFunInit(){
        mPlayFun.adapter = MyPagerAdapter(supportFragmentManager)
        mPlayFun.currentItem = 200
        mPlayFun.setPageTransformer(true,CubeInTransformer())
        mPlayFun.mView = mSurfaceicon
    }

    private fun funPtz(id:Int,event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mPresent?.ptzCtrl(when (id) {
                R.id.play_ptz_top    -> PTZ.PTZ_UP
                R.id.play_ptz_bottom -> PTZ.PTZ_DOWN
                R.id.play_ptz_left   -> PTZ.PTZ_LEFT
                R.id.play_ptz_right  -> PTZ.PTZ_RIGHT
                else                 -> PTZ.PTZ_STOP
            })
        } else if (event.action == MotionEvent.ACTION_UP) {
            mPresent?.ptzCtrl(PTZ.PTZ_STOP)
        }
    }

    private fun translateTime(progress: Int): String {
        val sdf = SimpleDateFormat("HH:mm:ss")
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(mCurBeg * 1000 + progress)
    }

    private fun playbackFun(){
        if (!mIsPlayBack)return
        mHandler.sendEmptyMessageDelayed(MSG_PLAY_PLAY_BACK_FUN, 200)
//        if (PlayAction.getInstance().getPlayBackKeepProgress())return;
        if (mKeepProgress)return
        val begTimestamp = mBegTimeStamp
        //JniUtil.getBegPlayTimestamp();
        if (begTimestamp == 0L)return
        val curTimestamp = mCurTimeStamp
        //JniUtil.getCurPlayTimestamp();
        val offset = curTimestamp - begTimestamp
//        Log.i("123","offset=            "+offset);
        if (mLastProgressOffset != offset) {
            mReplaySeekBar.progress = offset.toInt()
            mLastProgressOffset = offset
//            Log.i("123","set seekBar = $offset    curTimeStamp=$curTimestamp    begTimestamp=$begTimestamp")
        }
    }




    override fun onError(flag: Int) {
        when(flag){
            0->{
                //do not need relink
                DialogUtils.postMsgDialog(this,getString(R.string.play_play_error_msg_title),
                        getString(R.string.play_play_error_msg_msg),null)
            }
            1->{//need relink
                mPresent?.reLink(mIsSub,mBegTime,mEndTime)
            }
            else->{}
        }
    }

    override fun onConnect(b: Boolean) {//只有第一次basePlay init 完毕会调
        if(mIsPlayBack){
            //todo 查询有无录像
//            mPresent?.getVodRecord(mBegTime!!,mEndTime!!)
            mKeepProgress = false
            mPresent?.playback(mIsSub,mBegTime!!,mEndTime!!)
        }else{
            mPresent?.play(mIsSub)
        }
        mHandler.sendEmptyMessage(MSG_PLAY_PLAY_BACK_FUN)
    }

    override fun onRecord(vods: ArrayList<VODRecord>?) {
        if (vods!=null && vods?.size > 0 ){
            mPresent?.playback(mIsSub,mBegTime!!,mEndTime!!)
        } else{
            playErrorFun(getString(R.string.play_play_error_msg_vod))
        }
    }

    override fun onDisconnect(b: Boolean) {
        Log.i("123","ondisconnect")
    }

    override fun onPlaybackStartEndTime(beg: Long, end: Long) {
        val max = ((end - beg) * 1000).toInt()//开始到结束时间的毫秒
        Log.i("123", "!!!!!!!!!!!beg=$beg  end=$end")
        mCurBeg = beg
        mCurEnd = end
        Log.e("123", "!!!!!!!!!!onPlayback beg=$beg end=$end    max=$max")
        mReplaySeekBar.post {
            mReplaySeekBar.max = max
            mReplaySeekBar.progress = 0
        }
    }


    override fun onTime(speed: Int, timestamp: Long, firstTimestamp: Long, bWait: Boolean) {
        super.onTime(speed, timestamp, firstTimestamp, bWait)
        RxUtil.doInUIThread(object : RxUtil.RxSimpleTask<Objects>(){
            override fun doTask() {
                mStreanLen.text = "$speed kbit/s"
                mWaitProgressBar.visibility = if (bWait) View.VISIBLE else View.INVISIBLE
            }
        })
        if (!bWait && mKeepProgress && ! mUseProgress) {
            mKeepProgress = false
            mHandler.removeMessages(MSG_PLAY_PLAY_BACK_FUN)
            mHandler.sendEmptyMessageDelayed(MSG_PLAY_PLAY_BACK_FUN, 1000)
        }

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        mReplaySeekBar.setSeekBarText(translateTime(progress))
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        mKeepProgress = true
        mUseProgress  = true
        val progress = seekBar?.progress
        mReplaySeekBar.setSeekBarText(translateTime(progress?:0))
        mHandler.removeMessages(MSG_PLAY_PLAY_BACK_FUN)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        mUseProgress = false
//        PlayAction.getInstance().setPlayBackProgressByUser(false);
        val progress = seekBar?.progress
        mLastProgressOffset = progress?.toLong()?:0
        Log.i("123", "!!!!!!progress=" + progress)
//        PlayAction.getInstance().playBackRePlay(mCurBeg,progress);
//        Log.i("123","~~~~~~~~mcur beg="+Util.Date2ISODate(new Date(mCurBeg)));
//        long curSec = mCurBeg +progress/1000;
        val curSec = Util.DateString2Date(mBegTime)!!.time + (progress?:0)//毫秒
        val curEnd = Util.DateString2Date(mEndTime)!!.time

//        String beg = Util.Date2ISODate(new Date(curSec));
//        String end = Util.Date2ISODate(new Date(curEnd));

        val beg = Util.Date2String(Date(curSec))
        val end = Util.Date2String(Date(curEnd))

        Log.i("123", "~~~~~~~~beg=" + beg + " end=" + end + "   curSec=" + curSec + "  startSec=" + Util.DateString2Date(mBegTime)!!.time)
        mPresent?.playMoveTo(mIsSub, beg, end)
    }


    private fun bAnimating():Boolean{
        return mAnimationNum >= 2
    }

    private fun ptzAnimationStart(context: Context,view:View,fromXDelta:Float,toXDelta:Float,fromYDelta:Float,toYDelta:Float,bShow:Boolean,bLeft:Boolean){
        if (bAnimating())return
        val hMax = PhoneConfig.getPhoneHeight(context)
        val animationSet = AnimationSet(true)
        var scaleAnimation : Animation
        var alphaAnimation : Animation
        var transAnimation : Animation = TranslateAnimation(fromXDelta,toXDelta,fromYDelta,toYDelta)
        if (bShow){
            scaleAnimation = if (bLeft) ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f, Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.5f)
            else ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f)
            alphaAnimation = AlphaAnimation(0.1f,1.0f)
        }else{
            scaleAnimation = if (bLeft) ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f, Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.5f)
            else ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f)
            alphaAnimation = AlphaAnimation(1.0f,0.1f)
        }
        transAnimation.duration = 1000
        scaleAnimation.duration = 1000
        alphaAnimation.duration = 500
        transAnimation.setAnimationListener(object :Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                val left  = view.left + (toXDelta-fromXDelta)
                val right = view.right+ (toXDelta-fromXDelta)
                if (bShow){
                    view.layout(left.toInt(),0, right.toInt(),hMax)
                    view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake_x))
                }else{
                    view.visibility = View.GONE
                }
                if (bAnimating()){
                    mAnimationNum = 0
                    bAnimationFinish = true
                }
            }

            override fun onAnimationStart(animation: Animation?) {
                mAnimationNum++
                bAnimationFinish = false
            }
        })
        animationSet.addAnimation(transAnimation)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(alphaAnimation)
        view.startAnimation(animationSet)
    }

    inner class MyOnGestureListener : GestureDetector.OnGestureListener {
        override fun onShowPress(e: MotionEvent?) {

        }
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if(PhoneConfig.getPhoneHeight(this@PlayViewActivity)>PhoneConfig.getPhoneWidth(this@PlayViewActivity))return false
            if (isShowSurfaceIcon){
                showSurfaceIcon(false)
                mPlayFun.mView = null
            }else{
                showSurfaceIcon(true)
                mPlayFun.mView = mSurfaceicon
            }
            return true
        }
        override fun onDown(e: MotionEvent?): Boolean {
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (mCam.isPtz != true)return false
            val hMax = PhoneConfig.getPhoneHeight(this@PlayViewActivity)
            val wMax = PhoneConfig.getPhoneWidth(this@PlayViewActivity)
            val FLING_MIN_DISTANCE = 100
            val FLING_MIN_VELOCITY = 200
            if (hMax>wMax)return false
            if (!bAnimationFinish)return false
            mPlayPtzMove.visibility = View.VISIBLE
            mPlayFun.visibility     = View.VISIBLE
            if (e1!!.y - e2!!.y >FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY && e1!!.x < wMax/2){
                mIsShowPtz = if (mIsShowPtz){
                    ptzAnimationStart(this@PlayViewActivity,mPlayPtzMove,0f,0f,0f,-hMax+0f,false,true)
                    ptzAnimationStart(this@PlayViewActivity,mPlayFun,0f,0f,0f,hMax+0f,false,false)
                    false
                }else{
                    ptzAnimationStart(this@PlayViewActivity,mPlayPtzMove,0f,0f,hMax+0f,0f,true,true)
                    ptzAnimationStart(this@PlayViewActivity,mPlayFun,0f,0f,-hMax+0f,0f,true,false)
                    true
                }
            }else if(e2!!.y - e1!!.y > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY && e1!!.x < wMax/2){
                mIsShowPtz = if (mIsShowPtz){
                    ptzAnimationStart(this@PlayViewActivity,mPlayPtzMove,0f,0f,0f,hMax+0f,false,true)
                    ptzAnimationStart(this@PlayViewActivity,mPlayFun,0f,0f,0f,-hMax+0f,false,false)
                    false
                }else{
                    ptzAnimationStart(this@PlayViewActivity,mPlayPtzMove,0f,0f,-hMax+0f,0f,true,true)
                    ptzAnimationStart(this@PlayViewActivity,mPlayFun,0f,0f,hMax+0f,0f,true,false)
                    true
                }
            }
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent?) {

        }

    }





}