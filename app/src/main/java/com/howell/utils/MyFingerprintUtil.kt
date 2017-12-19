package com.howell.utils

import android.annotation.TargetApi
import android.hardware.fingerprint.FingerprintManager
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log

import com.howell.bean.FingerprintBean

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.ArrayList


object MyFingerprintUtil {

    @TargetApi(Build.VERSION_CODES.M)
    @Deprecated("")
    @Throws(ClassNotFoundException::class, NoSuchMethodException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun getAllFingerPrint(fm: FingerprintManager): List<FingerprintBean> {
        val list = ArrayList<FingerprintBean>()
        val fpmClass = FingerprintManager::class.java
        var l: List<*>? = null
        var fpmMethod: Method? = null
        var idMethod: Method? = null
        var nameMethod: Method? = null

        fpmMethod = fpmClass.getMethod("getEnrolledFingerprints")
        fpmMethod!!.isAccessible = true
        val o1 = fpmMethod.invoke(fm)
        l = o1 as List<*>

        var fingerprint: Class<*>? = null

        for (i in l.indices) {
            fingerprint = Class.forName("android.hardware.fingerprint.Fingerprint")
            idMethod = fingerprint!!.getMethod("getFingerId")
            nameMethod = fingerprint.getMethod("getName")
            val fpIdObj = idMethod!!.invoke(l[i])
            val nameObj = nameMethod!!.invoke(l[i])
            val fpId = Integer.valueOf(fpIdObj.toString())!!
            val name = nameObj.toString()
            val bean = FingerprintBean(fpId, name)
            list.add(bean)
        }
        return list
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Throws(NoSuchMethodException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class, ClassNotFoundException::class, NullPointerException::class)
    fun getFingerprint(result: AuthenticationResult): FingerprintBean {
        val bean = FingerprintBean(0, "")
        val c = AuthenticationResult::class.java
        val method1 = c.getMethod("getFingerprint")
        method1.isAccessible = true
        val o = method1.invoke(result)
        Log.i("123", "o.getClass().getName=" + o.javaClass.name)
        val className = o.javaClass.name
        val fingerprint = Class.forName(className)
        val method2 = fingerprint.getMethod("getFingerId")
        val idObj = method2.invoke(o)
        val fingerID = Integer.valueOf(idObj.toString())!!
        val method3 = fingerprint.getMethod("getName")
        val nameObj = method3.invoke(o)
        val name = nameObj.toString()
        bean.fpID = fingerID
        bean.name = name
        Log.e("123", "  +++++  " + bean.toString())
        return bean
    }
}
