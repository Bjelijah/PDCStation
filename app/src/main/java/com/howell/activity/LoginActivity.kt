package com.howell.activity

import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.howell.action.ConfigAction
import com.howell.action.FingerprintUiHelper
import com.howell.activity.view.FingerPrintBaseDialog
import com.howell.activity.view.FingerPrintLoadDialog
import com.howell.bean.UserDbBean
import com.howell.db.UserDao
import com.howell.modules.login.ILoginContract
import com.howell.modules.login.bean.Type
import com.howell.modules.login.presenter.LoginHttpPresenter
import com.howell.pdcstation.R
import com.howell.utils.DialogUtils
import com.howell.utils.Util
import com.howellsdk.utils.RxUtil
import java.util.*

/**
 * Created by Administrator on 2017/11/28.
 */
class LoginActivity :BaseActivity(),ILoginContract.IVew, FingerPrintBaseDialog.OnFignerPrintIDListener {



    @BindView(R.id.login_et_username)     lateinit var mUserNameView:AutoCompleteTextView
    @BindView(R.id.login_et_password)     lateinit var mPasswordView:EditText
    @BindView(R.id.login_btn_login)       lateinit var mloginBtn:Button
    @BindView(R.id.login_form)            lateinit var mLoginFormView:View
    @BindView(R.id.login_progress)        lateinit var mProgressBar:ProgressBar
    @BindView(R.id.login_ll_custom)       lateinit var mCustom:LinearLayout
    @BindView(R.id.login_tv_server)       lateinit var mServerTv:TextView
    @BindView(R.id.login_fab_fingerprint) lateinit var mFinger: FloatingActionButton

    private var mPresenter:ILoginContract.IPresenter?=null

    override fun onDestroy() {
        super.onDestroy()
        unbindPresenter()
    }

    override fun onResume() {
        super.onResume()
        ConfigAction.instance.load(this)
        val ip = ConfigAction.instance.mServerIP
        val port = ConfigAction.instance.mServerPort
        mServerTv.text = getString(R.string.login_server_select) + ip + ":" + port
    }


    override fun bindPresenter() {
        if (mPresenter==null)mPresenter=LoginHttpPresenter()
        mPresenter?.bindView(this)
        mPresenter?.init(this)
    }

    override fun unbindPresenter() {
        if (mPresenter!=null){
            mPresenter?.unbindView()
            mPresenter=null
        }
    }

    override fun onError(type: Type) {
        Log.i("123","type=$type")
        mProgressBar.visibility = View.INVISIBLE
        when(type){
            Type.ACCOUNT_NOT_EXIST  ->{
                mUserNameView.requestFocus()
                mUserNameView.post { mUserNameView.error = getString(R.string.account_error) }
            }
            Type.AUTHENCATION       ->{
                mPasswordView.requestFocus()
                mPasswordView.post { mPasswordView.setError(getString(R.string.password_error),null)  }
            }
            Type.ERROR              ->{
                Snackbar.make(mLoginFormView,getString(R.string.login_error),Snackbar.LENGTH_LONG).show()
            }
            else                    ->{
                Snackbar.make(mLoginFormView,getString(R.string.login_fail),Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onLoginSuccess(account: String, email: String) {
        Log.i("123","onLoginSuccess account=$account email=$email")
        mProgressBar.visibility = View.INVISIBLE
        startActivity(Intent(this@LoginActivity,HomeActivity::class.java)
                .putExtra("account",account)
                .putExtra("email",email))

        //todo first station choice
//        startActivity(Intent(this,StationActivity::class.java))


        finish()
    }

    override fun findView() {
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this@LoginActivity)

    }

    override fun onLogoutResult(type: Type) {
    }

    override fun initView() {
        bindPresenter()
    }



    override fun onFignerPrint(res:Boolean,id:Int?,name: String?, pwd: String?) {
        mPresenter?.init(this)
        mPresenter?.login(name,pwd)
    }

    override fun initData() {

    }

    private fun attemptLogin(){
        mUserNameView.error = null
        mPasswordView.error = null
        var username:String = mUserNameView.text.toString()
        var password:String = mPasswordView.text.toString()
        var focusView:View? = null
        var cancel:Boolean = false
        if (TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.verification),null)
            focusView = mPasswordView
            cancel = true
        }
        if (TextUtils.isEmpty(username)){
            mUserNameView.error = getString(R.string.verification)
            focusView = mUserNameView
            cancel = true
        }
        if (cancel){
            focusView?.requestFocus()
        }else{
            //TODO get and save server info
            mPresenter?.init(this)
            mPresenter?.login(username,password)
            mProgressBar.visibility = View.VISIBLE
        }
    }

    @OnClick(R.id.login_btn_login)fun onLoginBtnClick(view: View){
        testFoo()

        attemptLogin()
    }

    @OnClick(R.id.login_ll_custom)fun onLoginCustomServerClick(v:View){
        startActivity(Intent(this,ServerSetActivity::class.java))
    }

    @OnClick(R.id.login_fab_fingerprint) fun onFingerClick(v:View){
        if (!Util.isNewApi || !FingerprintUiHelper.isFingerAvailable(this)) {
            DialogUtils.postMsgDialog(this, getString(R.string.login_other_fingerprint), getString(R.string.login_other_fingerprint_no_support), null)
            return
        }
        val fingerFragment = FingerPrintLoadDialog(this,getString(R.string.finger_title),getString(R.string.fingerprint_description))
//        fingerFragment.setHandler(mHandler)
        fingerFragment.show(supportFragmentManager, "fingerLogin")
    }

    private fun testFoo(){

        ConfigAction.instance.saveThisServerInfo(this,"116.228.67.70",8800,false)
    }
}