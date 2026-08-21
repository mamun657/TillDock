package com.tilldock.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ThresholdRequest {

    @Min(value = 0, message = "threshold must be 0 or greater")
    @Max(value = 1_000_000, message = "threshold must be 1,000,000 or less")
    private int threshold;

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}