package com.browserstack.runner.reporter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {
    private String status;
    private long duration;
    @JsonProperty("error_message")
    private String errorMessage;

    public Result() {

    }

    public String getStatus() {
        return status;
    }

    public long getDuration() {
        return duration > 0 ? duration / 1000000 : 0;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "Result{" +
                "status='" + status + '\'' +
                ", duration=" + duration +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}