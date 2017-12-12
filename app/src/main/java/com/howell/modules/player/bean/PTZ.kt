package com.howell.modules.player.bean

/**
 * Created by Administrator on 2017/9/20.
 */

enum class PTZ private constructor(val `val`: String) {
    PTZ_LEFT("Left"),
    PTZ_RIGHT("Right"),
    PTZ_UP("Up"),
    PTZ_DOWN("Down"),
    PTZ_ZOOM_TELE("ZoomTele"),
    PTZ_ZOOM_WIDE("ZoomWide"),
    PTZ_STOP("Stop"),
    PTZ_ZOOM_STOP("Stop")
}
