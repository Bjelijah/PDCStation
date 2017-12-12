package com.howell.modules.player.bean

import java.io.Serializable

class VODRecord : Serializable {

    var startTime: String? = null
    var endTime: String? = null
    var fileSize: Long = 0
    var desc: String? = null
    var isWatched: Boolean = false
    private var hasTitle: Boolean = false
    var timeZoneStartTime: String? = null
    var timeZoneEndTime: String? = null

    constructor(startTime: String, endTime: String, fileSize: Long,
                desc: String) : super() {
        this.startTime = startTime
        this.endTime = endTime
        this.fileSize = fileSize
        this.desc = desc
        isWatched = false
        hasTitle = false
    }

    constructor(startTime: String, endTime: String, fileSize: Long, desc: String, isTitle: Boolean) : super() {
        this.startTime = startTime
        this.endTime = endTime
        this.fileSize = fileSize
        this.desc = desc
        isWatched = false
        hasTitle = isTitle
        timeZoneStartTime = startTime
        timeZoneEndTime = endTime
    }

    constructor(startTime: String, endTime: String, begTimeZone: String, endTimeZone: String, fileSize: Long, desc: String, isTitle: Boolean) : super() {
        this.startTime = startTime
        this.endTime = endTime
        this.fileSize = fileSize
        this.desc = desc
        isWatched = false
        hasTitle = isTitle
        timeZoneStartTime = begTimeZone
        timeZoneEndTime = endTimeZone
    }

    constructor() : super() {}


    fun hasTitle(): Boolean {
        return hasTitle
    }

    fun setHasTitle(hasTitle: Boolean) {
        this.hasTitle = hasTitle
    }


    override fun toString(): String {
        return ("VODRecord [StartTime=" + startTime + ", EndTime=" + endTime
                + ", FileSize=" + fileSize + ", Desc=" + desc + ", isWatched=" + isWatched + ", hasTitle="
                + hasTitle + ", TimeZoneStartTime=" + timeZoneStartTime
                + ", TimeZoneEndTime=" + timeZoneEndTime + "]")
    }

    companion object {

        /**
         *
         */
        private const val serialVersionUID = 4722894254660276688L
    }

}
