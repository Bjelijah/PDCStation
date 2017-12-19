package com.howell.activity.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.hardware.fingerprint.FingerprintManager

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.howell.action.FingerprintUiHelper
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/12/18.
 */
@SuppressLint("NewApi")
open class FingerPrintBaseDialog : DialogFragment(), FingerprintUiHelper.Callback {


    protected var mCancel: TextView?              = null
    protected var mTvState: TextView?             = null
    protected var mWait: TextView?                = null
    protected var mIvState: ImageView?            = null
    protected var mFinger: FingerprintUiHelper?   = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater!!.inflate(R.layout.fingerprint_dialog_container,container,false)
        mCancel   = v?.findViewById(R.id.tv_finger_cancel)
        mCancel?.setOnTouchListener({_, event ->
            if (event.action== MotionEvent.ACTION_DOWN){mCancel?.postDelayed({dismiss()},300)}
            false
        })
        mTvState  = v?.findViewById(R.id.fingerprint_status)
        mIvState  = v?.findViewById(R.id.fingerprint_icon)
        mWait     = v?.findViewById(R.id.tv_finger_wait)
        mFinger = FingerprintUiHelper(context, context?.getSystemService(FingerprintManager::class.java)!!,this)
        mFinger?.startListening(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setTitle(getString(R.string.finger_title))
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mFinger?.stopListening()
    }


    override fun onAuthenticated(id: Int) {
        showAuthenticationInfo(MyState.OK)
    }

    override fun onFailed() {
        showAuthenticationInfo(MyState.FAIL)
    }

    override fun onHelp(helpCode: Int, str: CharSequence) {
        when(helpCode){
            1001-> showAuthenticationInfo(MyState.WAIT)
        }
    }

    override fun onError(code: Int, s: CharSequence) {
        showAuthenticationInfo(MyState.ERROR)
    }

    override fun onFingerCancel() {
        dismiss()
    }

    override fun onFingerNoSupport() {
        dismiss()
        Snackbar.make(view!!, "no support", Snackbar.LENGTH_LONG).show()
    }


    protected enum class MyState {
        WAIT,
        FAIL,
        OK,
        ERROR,
        SIGN_ERROR
    }

    protected fun showAuthenticationInfo(state: MyState) {
        when (state) {
            MyState.WAIT -> {
                mIvState?.setImageDrawable(context.resources.getDrawable(R.mipmap.ic_fp))
                mTvState?.text = getString(R.string.fingerprint_hint)
                mTvState?.setTextColor(context.resources.getColor(R.color.hint_color))
            }
            MyState.FAIL -> {
                //			mIvFingerState.setImageDrawable(getResources().getDrawable(R.drawable.common_no_default));
                mIvState?.setImageDrawable(context.resources.getDrawable(R.mipmap.ic_fp_fail))
                mTvState?.text = getString(R.string.fingerprint_failed)
                mTvState?.setTextColor(context.resources.getColor(R.color.finger_fail))
            }
            MyState.OK -> {
                //			mIvFingerState.setImageDrawable(getResources().getDrawable(R.drawable.ok_default_green));
                mIvState?.setImageDrawable(context.resources.getDrawable(R.mipmap.ic_fp_ok))
                mTvState?.text = getString(R.string.fingerprint_ok)
                mTvState?.setTextColor(context.resources.getColor(R.color.finger_green))
            }
            MyState.ERROR -> {
                mIvState?.setImageDrawable(context.resources.getDrawable(R.mipmap.common_no_highlighted))
                mTvState?.text = getString(R.string.fingerprint_error)
                mTvState?.setTextColor(context.resources.getColor(R.color.red))
            }
            MyState.SIGN_ERROR -> {
                mIvState?.setImageDrawable(context.resources.getDrawable(R.mipmap.common_no_default))
                mTvState?.text = getString(R.string.fingerprint_sign_error)
                mTvState?.setTextColor(context.resources.getColor(R.color.finger_orgeen))
            }
            else -> {
            }
        }
    }



}