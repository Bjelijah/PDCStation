package com.howell.modules.player.bean

import java.io.Serializable

/**
 * Created by Administrator on 2017/11/30.
 */
data class CameraItemBean(var id:String):Serializable{
    var cameraName:String?          = null
    var cameraDescription:String?   = null
    var deviceId:String?            = null
    var channelNo:Int?              = null
    var isOnline:Boolean?           = null
    var isPtz:Boolean?              = null
    var isStore:Boolean?            = null
    var deVer:String?               = null
    var model:String?               = null
    var indensity:Int?              = null
    var picturePath:String?         = null
    var upnpIP:String?              = null
    var upnpPort:Int?               = null

    constructor(id:String,name:String?,description:String?,deviceId:String,channelNo:Int?,isOnline:Boolean?,isPtz:Boolean?,isStore:Boolean?,
    devVer:String?,model:String?,indensity:Int?,picPath:String?,ip:String?,port:Int?) : this(id){
        this.cameraName                     = name
        this.cameraDescription              = description
        this.deviceId                       = deviceId
        this.channelNo                      = channelNo
        this.isOnline                       = isOnline
        this.isPtz                          = isPtz
        this.isStore                        = isStore
        this.deVer                          = devVer
        this.model                          = model
        this.indensity                      = indensity
        this.picturePath                    = picPath
        this.upnpIP                         = ip
        this.upnpPort                       = port
    }

    override fun toString(): String {
        return "CameraItemBean(id='$id', cameraName=$cameraName, cameraDescription=$cameraDescription, deviceId=$deviceId, channelNo=$channelNo, isOnline=$isOnline, isPtz=$isPtz, isStore=$isStore, deVer=$deVer, model=$model, indensity=$indensity, picturePath=$picturePath, upnpIP=$upnpIP, upnpPort=$upnpPort)"
    }

}