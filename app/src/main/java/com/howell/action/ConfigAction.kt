package com.howell.action

import android.content.Context
import com.howell.utils.PhoneConfig
import com.howell.utils.ServerConfigSp
import com.howell.utils.UserConfigSp

/**
 * Created by Administrator on 2017/11/27.
 */
class ConfigAction private constructor(){
    companion object {
        val instance:ConfigAction by lazy { ConfigAction() }
    }
    var mContext:Context?=null
    var mUrl:String?=null
    var mServerIP:String?=null
    var mServerPort:Int?=null
    var mServerSSL:Boolean?=null
    var mTurnIP:String?=null
    var mTurnPort:Int?=null
    var mTurnSSL:Boolean?=null
    var mName:String?=null
    var mPwd:String?=null
    var mIsFirst:Boolean=true
    var mImei:String? = null

    fun load(c:Context?):ConfigAction{
        if (c==null)return this
        mContext    = c
        mUrl        = ServerConfigSp.loadServerURL(c)
        mServerIP   = ServerConfigSp.loadServerIP(c)
        mServerPort = ServerConfigSp.loadServerPort(c)
        mServerSSL  = ServerConfigSp.loadServerSSL(c)
        mTurnIP     = ServerConfigSp.loadTurnIP(c)
        mTurnPort   = ServerConfigSp.loadTurnPort(c)
        mTurnSSL    = ServerConfigSp.loadTurnSSL(c)
        mName       = UserConfigSp.loadUserName(c)
        mPwd        = UserConfigSp.loadUserPwd(c)
        mIsFirst    = UserConfigSp.loadUserFirstLogin(c)
        mImei       = PhoneConfig.getIMEI(c)
        return this
    }

    fun saveThisServerInfo(c:Context,ip:String,port:Int,isSSL:Boolean):ConfigAction{//保存当前登入的服务器信息
        mContext    = c
        mServerIP   = ip
        mServerPort = port
        mTurnIP     = ip
        mTurnPort   = if (isSSL) 8862 else 8812
        mUrl        = if (isSSL)  "https://$ip:$port" else  "http://$ip:$port"
        ServerConfigSp.saveServerURL(c,ip,port,isSSL)
        ServerConfigSp.saveTurnServer(c,ip,if (isSSL)8862 else 8812,isSSL)
        return this
    }

    fun saveThisUserInfo(c: Context, name:String, pwd:String):ConfigAction{//保存当前登入的用户信息
        mContext         = c
        mName            = name
        mPwd             = pwd
        UserConfigSp.saveUserInfo(c,name,pwd)
        UserConfigSp.saveUserFirstLogin(c,false)
        return this
    }

}