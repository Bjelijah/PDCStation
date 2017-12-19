package com.howell.activity

import android.app.ProgressDialog
import android.os.Handler
import android.support.v7.widget.Toolbar

import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.howell.action.ConfigAction
import com.howell.pdcstation.R
import com.howell.utils.Util



/**
 * Created by Administrator on 2017/12/18.
 */
class ServerSetActivity :BaseActivity() {
    @BindView(R.id.server_set_toolbar)      lateinit var mTb: Toolbar
    @BindView(R.id.server_set_et_ip)        lateinit var mIp: AutoCompleteTextView
    @BindView(R.id.server_set_et_port)      lateinit var mPort: AutoCompleteTextView
    @BindView(R.id.server_set_default_btn)  lateinit var mDefault:Button
    @BindView(R.id.server_set_btn)          lateinit var mBtn:Button


    override fun findView() {
        setContentView(R.layout.activity_server_address)
        ButterKnife.bind(this)
    }

    override fun initView() {
        mTb.title = getString(R.string.server_setting_title)
        setSupportActionBar(mTb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        mTb.setNavigationOnClickListener { finish() }



    }

    override fun initData() {
        ConfigAction.instance.load(this)
        mIp.setText(ConfigAction.instance.mServerIP)
        mPort.setText("${ConfigAction.instance.mServerPort}")
    }

    @OnClick(R.id.server_set_default_btn) fun onDefaultBtn(v: View){
        mIp.setText("116.228.67.70")
        mPort.setText("8800")
    }

    @OnClick(R.id.server_set_btn) fun onSaveBtn(v:View){
        mIp.error = null
        mPort.error = null

        val ip = mIp.text.toString()
        val port = mPort.text.toString()

        var v: View? = null
        if (!Util.isInteger(port)) {
            mPort.error = getString(R.string.add_ap_port_error)
            v = mPort
        }
        if (port == "") {
            mPort.error = getString(R.string.reg_field_empty)
            v = mPort
        }
        if (!Util.hasDot(ip)) {
            mIp.error = getString(R.string.add_ap_ip_error)
            v = mIp
        }
        if (ip == "") {
            mIp.error = getString(R.string.reg_field_empty)
            v = mIp
        }

        if (v != null) {
            Log.i("123", "v!=null")
            v.requestFocus()
            return
        } else {
            Log.i("123", "v=null")
        }
        // todo save
        ConfigAction.instance.saveThisServerInfo(this,ip,port.toInt(),false)
        waitShow(resources.getString(R.string.camera_setting_save_title), resources.getString(R.string.camera_setting_save_msg), 1000)
    }

    private fun waitShow(title: String, msg: String, autoDismissMS: Long) {
        val pd = ProgressDialog(this)
        pd.setTitle(title)
        pd.setMessage(msg)
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd.show()
        Handler().postDelayed({
            pd.dismiss()
            finish()
        },autoDismissMS)
    }


}