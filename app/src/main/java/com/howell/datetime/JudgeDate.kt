package com.howell.datetime

import java.text.SimpleDateFormat

object JudgeDate {

    /**
     *
     * @param str_input
     * @param str_input
     * @return boolean;
     */
    fun isDate(str_input: String, rDateFormat: String): Boolean {
        if (!isNull(str_input)) {
            val formatter = SimpleDateFormat(rDateFormat)
            formatter.isLenient = false
            try {
                formatter.format(formatter.parse(str_input))
            } catch (e: Exception) {
                return false
            }

            return true
        }
        return false
    }

    fun isNull(str: String?): Boolean {
        return if (str == null)
            true
        else
            false
    }
}