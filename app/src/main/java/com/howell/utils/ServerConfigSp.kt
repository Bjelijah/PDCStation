package com.howell.utils

import android.content.Context

/**
 * Created by Administrator on 2017/11/27.
 */
object ServerConfigSp {
    private val SP_NAME:String="server_set"
    /** url user server **/
    fun saveServerURL(c: Context, ip:String,port:Int,isSSL:Boolean){
        var url:String = if (isSSL)
            "https://$ip:$port"
        else
            "http://$ip:$port"
        c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).edit()
                .putString("server_ip",ip)
                .putInt("server_port",port)
                .putString("server_url",url)
                .putBoolean("server_ssl",isSSL)
                .commit()
    }

    fun loadServerURL(c:Context):String?  = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getString("server_url",null)
    fun loadServerIP(c:Context):String?   = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getString("server_ip",null)
    fun loadServerPort(c:Context):Int?    = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getInt("server_port",8800)
    fun loadServerSSL(c:Context):Boolean? = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getBoolean("server_ssl",false)
    /** turn server **/
    fun saveTurnServer(c:Context,ip:String,port:Int,isSSL:Boolean)
            = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).edit()
            .putString("turn_server",ip)
            .putInt("turn_port",port)
            .putBoolean("turn_ssl",isSSL)
            .commit()
    fun loadTurnIP(c:Context):String?   = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getString("turn_server",null)
    fun loadTurnPort(c:Context):Int?    = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getInt("turn_port",8812)
    fun loadTurnSSL(c:Context):Boolean? = c.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getBoolean("turn_ssl",false)
}