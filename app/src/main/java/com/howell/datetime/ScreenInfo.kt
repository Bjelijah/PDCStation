package com.howell.datetime

import android.app.Activity
import android.util.DisplayMetrics

/**
 *
 * @author
 * @email chenshi011@163.com
 */
class ScreenInfo(var activity: Activity?) {

    var width: Int = 0

    var height: Int = 0

    var density: Float = 0.toFloat()

    var densityDpi: Int = 0

    init {
        ini()
    }

    private fun ini() {
        val metric = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(metric)
        width = metric.widthPixels
        height = metric.heightPixels
        density = metric.density
        densityDpi = metric.densityDpi
    }


}
