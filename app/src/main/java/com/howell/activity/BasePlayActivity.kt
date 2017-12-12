package com.howell.activity

import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.howell.activity.view.MySeekBar
import com.howell.activity.view.PlayFunViewPage
import com.howell.modules.player.IPlayContract
import com.howell.modules.player.bean.CameraItemBean
import com.howell.modules.player.bean.VODRecord
import com.howell.modules.player.presenter.PlayTurnPresenter
import com.howell.pdcstation.R
import com.howell.utils.DialogUtils
import com.howell.utils.PhoneConfig
import com.howellsdk.api.player.GLESTextureView
import com.howellsdk.utils.RxUtil
import java.util.*

/**
 * Created by Administrator on 2017/12/4.
 */
open abstract class BasePlayActivity : FragmentActivity(), View.OnTouchListener,IPlayContract.IView {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }

    @BindView(R.id.gl_texture_view)         lateinit var mGlView: GLESTextureView
    @BindView(R.id.play_talk)               lateinit var mBtTalk:Button
    @BindView(R.id.catch_picture)           lateinit var mBtCatch:ImageButton
    @BindView(R.id.vedio_list)              lateinit var mVodList:ImageButton
    @BindView(R.id.sound)                   lateinit var mSound:ImageButton
    @BindView(R.id.player_title_bar)        lateinit var mTitle:FrameLayout
    @BindView(R.id.player_change_stream)    lateinit var mStreamChange:TextView
    @BindView(R.id.ib_pause)                lateinit var mPause:ImageButton
    @BindView(R.id.player_imagebutton_back) lateinit var mBack:ImageButton
    @BindView(R.id.replaySeekBar)           lateinit var mReplaySeekBar:MySeekBar
    @BindView(R.id.surface_icons)           lateinit var mSurfaceicon:LinearLayout
    @BindView(R.id.waitProgressBar)         lateinit var mWaitProgressBar:ProgressBar
    @BindView(R.id.tv_stream_len)           lateinit var mStreanLen:TextView
    @BindView(R.id.play_rl_ptz)             lateinit var mPlayPtzMove:RelativeLayout
    @BindView(R.id.play_ptz_left)           lateinit var mPtzLeft:LinearLayout
    @BindView(R.id.play_ptz_right)          lateinit var mPtzRight:LinearLayout
    @BindView(R.id.play_ptz_top)            lateinit var mPtzUp:LinearLayout
    @BindView(R.id.play_ptz_bottom)         lateinit var mPtzDown:LinearLayout
    @BindView(R.id.play_fun)                lateinit var mPlayFun:PlayFunViewPage

    lateinit var mPopupWindow:PopupWindow
    lateinit var mHD:LinearLayout
    lateinit var mSD:LinearLayout
    internal lateinit var mCam: CameraItemBean
    internal var mPresent:IPlayContract.IPresent?=null
    internal var isShowSurfaceIcon = false
    var mIsSub = true
    protected var mSpeed: Int = 0
    protected var mBegTimeStamp: Long = 0
    protected var mCurTimeStamp: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initViewFun()
        initPlayer()
    }

    override fun onPause() {
        super.onPause()
        mGlView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mGlView.onResume()
    }

    override fun onDestroy() {
        camStop()
        mGlView.onDestroy()
        unbindPresenter()
        super.onDestroy()
    }

    fun camPlay(){}
    fun camStop(){
        mPresent?.stop()
    }


    private fun initView(){
        setContentView(R.layout.glsurface)
        ButterKnife.bind(this)
        initPopupWindow()
        if (PhoneConfig.getPhoneHeight(this) < PhoneConfig.getPhoneWidth(this)) {
            showSurfaceIcon(false)
        }
    }

    private fun initPopupWindow(){
        val v = LayoutInflater.from(this).inflate(R.layout.popup_window,null)
        mHD = v.findViewById(R.id.pop_layout_hd)
        mSD = v.findViewById(R.id.pop_layout_sd)
        val width = PhoneConfig.getPhoneWidth(this)
        val height = width * 5 / 3
        mPopupWindow = PopupWindow(v,width/4,height)
        mPopupWindow.setBackgroundDrawable(ColorDrawable(0))
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
    }


    private fun initViewFun(){
        mGlView.setOnTouchListener(this)
        mGlView.isFocusable = true
        mGlView.isClickable = true
        mGlView.isLongClickable = true

    }

    private fun initPlayer(){
        mCam = intent.getSerializableExtra("CameraItem") as CameraItemBean
        bindPresenter()
    }


    internal fun showSurfaceIcon(bShow:Boolean){
        mTitle.visibility       = if(bShow) View.VISIBLE else View.GONE
        mSurfaceicon.visibility = if(bShow) View.VISIBLE else View.GONE
        mStreanLen.visibility   = View.VISIBLE
        isShowSurfaceIcon       = bShow
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        when(resources.configuration.orientation){
            Configuration.ORIENTATION_LANDSCAPE->{showSurfaceIcon(false); mBtTalk.visibility = View.GONE}
            Configuration.ORIENTATION_PORTRAIT ->{showSurfaceIcon(true);  mBtTalk.visibility = View.GONE}
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun bindPresenter() {
        if(mPresent==null)mPresent = PlayTurnPresenter()
        mPresent?.bindView(this)
        mPresent?.init(this,mCam)
    }

    override fun unbindPresenter() {
        if (mPresent != null) {
            mPresent?.deInit()
            mPresent?.unbindView()
        }
    }

    override fun onTime(speed: Int, timestamp: Long, firstTimestamp: Long, bWait: Boolean) {
        mSpeed = speed
        mBegTimeStamp = firstTimestamp
        mCurTimeStamp = timestamp
    }

    override fun onRecord(vods: ArrayList<VODRecord>?) {
        //todo nothing to do in play activity
    }

    protected fun playErrorFun(title:String?){
        RxUtil.doInUIThread(object :RxUtil.RxSimpleTask<Objects>(){
            override fun doTask() {
                mWaitProgressBar.visibility = View.GONE
                DialogUtils.postMsgDialog(this@BasePlayActivity,
                        resources.getString(R.string.play_play_error_msg_title),
                        title?:resources.getString(R.string.play_play_error_msg_msg), null)//resources.getString(R.string.play_play_error_msg_msg)
            }
        })
    }

}