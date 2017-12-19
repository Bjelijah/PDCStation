package com.howell.modules.pdc.bean

import java.io.Serializable

/**
 * Created by Administrator on 2017/12/12.
 */
data class PDCDevice(var id:String) :Serializable{
    var createTime:String?      = null
    var name:String?            = null
    var model:String?           = null
    var firmware:String?        = null
    var serial:String?          = null
    var information:String?     = null
    var userName:String?        = null
    var password:String?        = null
    var uri:String?             = null
    var protocolType:String?    = null
    var parentId:String?        = null
    var timeSync:Boolean?       = null
    var resetTime:String?       = null
    var structuredable:Int?     = null
    var lastSecond:Int?         = null
    var inDatabase:Boolean?     = null
    var onLine:Boolean?         = null
    var lastUpdate:String?      = null
    var leaveNumber:Int?        = null
    var enterNumber:Int?        = null
    var deviationNumber:Int?    = null
    var lastResetTime:String?   = null
    var lastLeaverNumber:Int?   = null
    var lastEnterNumber:Int?    = null
    var thresholdable:Boolean?  = null
    override fun toString(): String {
        return "PDCDevice(id='$id', createTime=$createTime, name=$name, model=$model, firmware=$firmware, serial=$serial, information=$information, userName=$userName, password=$password, uri=$uri, protocolType=$protocolType, parentId=$parentId, timeSync=$timeSync, resetTime=$resetTime, structuredable=$structuredable, lastSecond=$lastSecond, inDatabase=$inDatabase, onLine=$onLine, lastUpdate=$lastUpdate, leaveNumber=$leaveNumber, enterNumber=$enterNumber, deviationNumber=$deviationNumber, lastResetTime=$lastResetTime, lastLeaverNumber=$lastLeaverNumber, lastEnterNumber=$lastEnterNumber, thresholdable=$thresholdable)"
    }

}