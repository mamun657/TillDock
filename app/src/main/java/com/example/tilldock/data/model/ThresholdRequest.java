package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public class ThresholdRequest {

    @SerializedName("threshold")
    private Integer threshold;

    public ThresholdRequest() {
    }

    public ThresholdRequest(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }
}