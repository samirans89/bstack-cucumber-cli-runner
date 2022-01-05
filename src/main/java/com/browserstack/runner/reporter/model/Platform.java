package com.browserstack.runner.reporter.model;

import java.time.Instant;
import java.util.List;

public class Platform {

    String platformName;

    Instant platformCompleteTime;
    List<Feature> featureList;

    public Platform(String platformName, Instant platformCompleteTime) {
        this.platformName = platformName;
        this.platformCompleteTime = platformCompleteTime;
    }

    public String getPlatformName() {
        return platformName;
    }

    public Instant getPlatformCompleteTime() {
        return platformCompleteTime;
    }

    public List<Feature> getFeatureList() {
        return featureList;
    }

    public void setFeatureList(List<Feature> featureList) {
        this.featureList = featureList;
    }

    public void setPlatformCompleteTime(Instant platformCompleteTime) {
        this.platformCompleteTime = platformCompleteTime;
    }

    @Override
    public String toString() {
        return "Platform{" +
                "platformName='" + platformName + '\'' +
                ", platformCompleteTime=" + platformCompleteTime +
                ", featureList=" + featureList +
                '}';
    }
}
