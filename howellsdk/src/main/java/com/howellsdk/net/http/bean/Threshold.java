package com.howellsdk.net.http.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/12/12.
 */

public class Threshold {
    @SerializedName("Max")      Integer max;
    @SerializedName("Min")      Integer min;

    @Override
    public String toString() {
        return "Threshold{" +
                "max=" + max +
                ", min=" + min +
                '}';
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Threshold() {

    }

    public Threshold(Integer max, Integer min) {

        this.max = max;
        this.min = min;
    }
}
