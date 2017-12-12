package com.howell.activity

import android.content.Intent
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.howell.action.ConfigAction
import com.howell.modules.login.ILoginContract
import com.howell.modules.login.bean.Type
import com.howell.modules.login.presenter.LoginHttpPresenter
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/11/28.
 */
class LoginActivity :BaseActivity(),ILoginContract.IVew {

    @BindView(R.id.login_et_username) lateinit var mUserNameView:AutoCompleteTextView
    @BindView(R.id.login_et_password) lateinit var mPasswordView:EditText
    @BindView(R.id.login_btn_login)   lateinit var mloginBtn:Button
    @BindView(R.id.login_form)        lateinit var mLoginFormView:View


    private var mPresenter:ILoginContract.IPresenter?=null

    override fun onDestroy() {
        super.onDestroy()
        unbindPresenter()
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
        startActivity(Intent(this@LoginActivity,HomeActivity::class.java)
                .putExtra("account",account)
                .putExtra("email",email))
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
        }
    }

    @OnClick(R.id.login_btn_login)fun onLoginBtnClick(view: View){
        testFoo()

        attemptLogin()
    }

    private fun testFoo(){
        ConfigAction.instance.saveThisServerInfo(this,"116.228.67.70",8800,false)
    }
}