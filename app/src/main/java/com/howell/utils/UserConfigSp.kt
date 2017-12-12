package com.howell.utils

import android.content.Context

/**
 * Created by Administrator on 2017/11/27.
 */
object UserConfigSp {
    private val SP_NAME:String = "user_set"
    /** name & pwd **/
    fun saveUserInfo(c: Context,name:String,pwd:String)
            = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).edit()
            .putString("account",name)
            .putString("password",pwd)
            .commit()
    fun loadUserName(c:Context):String? = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getString("account",null)
    fun loadUserPwd(c:Context):String?  = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getString("password",null)
    /** first login **/
    fun saveUserFirstLogin(c:Context,isFirst:Boolean)
            = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).edit()
            .putBoolean("isFirstLogin",isFirst)
            .commit()
    fun loadUserFirstLogin(c:Context):Boolean = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getBoolean("isFirstLogin",true)

}