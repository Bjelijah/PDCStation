package com.howellsdk.net.http.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/11/22.
 */

public class PDCThreshold {
    @SerializedName("Enabled")              Boolean enable;
    @SerializedName("DeviationNumber")      Threshold deviationNumber;
    @SerializedName("LastNLeaveNumber")     Threshold lastNLeaveNumber;
    @SerializedName("LastNEnterNumber")     Threshold lastNEnterNumber;

    @Override
    public String toString() {
        return "PDCThreshold{" +
                "max=" + enable +
                ", deviationNumber=" + deviationNumber +
                ", lastNLeaveNumber=" + lastNLeaveNumber +
                ", lastNEnterNumber=" + lastNEnterNumber +
                '}';
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Threshold getDeviationNumber() {
        return deviationNumber;
    }

    public void setDeviationNumber(Threshold deviationNumber) {
        this.deviationNumber = deviationNumber;
    }

    public Threshold getLastNLeaveNumber() {
        return lastNLeaveNumber;
    }

    public void setLastNLeaveNumber(Threshold lastNLeaveNumber) {
        this.lastNLeaveNumber = lastNLeaveNumber;
    }

    public Threshold getLastNEnterNumber() {
        return lastNEnterNumber;
    }

    public void setLastNEnterNumber(Threshold lastNEnterNumber) {
        this.lastNEnterNumber = lastNEnterNumber;
    }

    public PDCThreshold() {

    }

    public PDCThreshold(Boolean enable, Threshold deviationNumber, Threshold lastNLeaveNumber, Threshold lastNEnterNumber) {

        this.enable = enable;
        this.deviationNumber = deviationNumber;
        this.lastNLeaveNumber = lastNLeaveNumber;
        this.lastNEnterNumber = lastNEnterNumber;
    }
}
