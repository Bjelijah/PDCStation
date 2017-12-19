package com.howell.utils


import android.content.Context
import android.net.TrafficStats
import android.util.Log


import java.net.InetAddress
import java.net.UnknownHostException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author yangyu
 * ��������������������
 */
object Util {


    private var lastTotalRxBytes: Long = 0
    private var lastTimeStamp: Long = 0

    val isNewApi: Boolean
        get() = android.os.Build.VERSION.SDK_INT > 22

    /**
     * �õ��豸��Ļ�Ŀ��
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * �õ��豸��Ļ�ĸ߶�
     */
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * �õ��豸���ܶ�
     */
    fun getScreenDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }

    /**
     * ���ܶ�ת��Ϊ����
     */
    fun dip2px(context: Context, px: Float): Int {
        val scale = getScreenDensity(context)
        return (px * scale + 0.5).toInt()
    }

    private fun getTotalRxBytes(context: Context): Long {
        return if (TrafficStats.getUidRxBytes(context.applicationInfo.uid) == TrafficStats.UNSUPPORTED.toLong()) 0 else TrafficStats.getTotalRxBytes() / 1024
    }

    fun getDownloadSpeed(context: Context): String {
        val nowTotalRxBytes = getTotalRxBytes(context)
        val nowTimeStemp = System.currentTimeMillis()
        val speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStemp - lastTimeStamp)
        lastTimeStamp = nowTimeStemp
        lastTotalRxBytes = nowTotalRxBytes
        //		if(speed == 0 ){
        //			if(!isNetConnect(context)){
        //				return null;
        //			}
        //		}

        return speed.toString() + "kb/s"
    }


    fun isIP(addr: String): Boolean {
        if (addr.length < 7 || addr.length > 15 || "" == addr) {
            return false
        }
        /**
         * 判断IP格式和范围
         */
        val rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}"
        val pat = Pattern.compile(rexp)
        val mat = pat.matcher(addr)
        return mat.find()
    }

    fun hasDot(addr: String): Boolean {
        return if ("" == addr) false else addr.contains(".")
    }

    fun parseIP(domainName: String): String? {
        var addr: InetAddress? = null
        try {
            addr = InetAddress.getByName(domainName)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            return null
        }

        return addr!!.hostAddress
    }

    fun isInteger(str: String): Boolean {
        val pattern = Pattern.compile("^[-\\+]?[\\d]*$")
        return pattern.matcher(str).matches()
    }


}
