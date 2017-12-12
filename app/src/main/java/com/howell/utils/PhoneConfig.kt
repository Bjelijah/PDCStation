package com.howell.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.WindowManager
import com.howell.utils.PhoneConfig.wm

import java.io.UnsupportedEncodingException
import java.util.UUID

/**
 * @author huozhihao
 *
 * 用于获取手机屏幕长度和宽度的工具类
 */
@SuppressLint("MissingPermission")
object PhoneConfig {
    private var wm: WindowManager? = null

    val phoneModel: String
        get() {
            Log.i("123", "getPhoneModel model = " + android.os.Build.MODEL)
            return android.os.Build.MODEL
        }

    val osVersion: String
        get() {
            Log.i("123", "getOSVersion version = " + android.os.Build.VERSION.RELEASE)
            return android.os.Build.VERSION.RELEASE
        }

    val phoneManufactory: String
        get() {
            Log.i("123", "getPhoneManufactory factory = " + android.os.Build.MANUFACTURER)
            return android.os.Build.MANUFACTURER
        }

    fun getPhoneWidth(context: Context): Int {
        wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm!!.defaultDisplay.width
    }

    fun getPhoneHeight(context: Context): Int {
        wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm!!.defaultDisplay.height
    }


    fun getPhoneDeveceID(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        Log.i("123", "deviceid = " + tm.deviceId)
        return tm.deviceId
        //		return TEST_DEVICE_ID;
    }

    fun showUserSerialNum(context: Context): Long {
        val um = context.getSystemService(Context.USER_SERVICE) as UserManager ?: return -1
        val userHandle = Process.myUserHandle()
        return um.getSerialNumberForUser(userHandle)
    }

    fun getPhoneUid(context: Context): String? {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        var uuid: UUID? = null
        try {
            uuid = if ("9774d56d682e549c" != androidId)
            //在主流厂商生产的设备上，有一个很经常的bug，就是每个设备都会产生相同的ANDROID_ID：9774d56d682e549c
            {
                UUID.nameUUIDFromBytes(androidId.toByteArray(charset("utf8")))
            } else {
                val deviceId = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
                if (deviceId != null) UUID.nameUUIDFromBytes(deviceId.toByteArray(charset("utf8"))) else UUID.randomUUID()
            }
        } catch (e: UnsupportedEncodingException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        if (uuid == null) {
            return null
        }
        var id = uuid.toString()
        id = id.replace("-", "")
        id = id.replace(":", "")
        return id
    }

    fun getIMEI(context: Context): String {
        return (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
    }

    fun getPhoneOperator(context: Context): String {
        val telMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val name = telMgr.simOperatorName
        Log.i("123", "name=" + name)
        return name
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getAppVersion(context: Context): String {

        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        return pi.versionName

    }

    fun isNewVersion(context: Context, version: String): Boolean {
        try {
            val thisAPPVersion = getAppVersion(context)
            Log.i("123", "thisAppVersion=$thisAPPVersion    version=$version")
            val str = version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            Log.e("123", "len=" + str.size)
            val vers = IntArray(str.size)
            for (i in str.indices) {
                Log.i("123", "~~~~~~~~~~~~  str=" + str[i])
                vers[i] = Integer.valueOf(str[i])!!
            }
            val thisStr = thisAPPVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val thisVers = IntArray(thisStr.size)
            for (i in thisStr.indices) {
                thisVers[i] = Integer.valueOf(thisStr[i])!!
            }

            val len = Math.min(vers.size, thisVers.size)
            (0 until len)
                    .filter { vers[it] > thisVers[it] }
                    .forEach { return true }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return false
    }


}
