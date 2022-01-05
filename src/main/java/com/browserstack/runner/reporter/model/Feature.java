package com.browserstack.runner.reporter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Feature {
    @JsonProperty("id")
    private String featureId;
    private String name;
    private String uri;
    private List<Scenario> scenarios = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();

    private String platformName;


    public Feature() {

    }


    public Feature(String featureId, String name, String uri, List<Tag> tags, String platformName) {
        this.featureId = featureId;
        this.name = name;
        this.uri = uri;
        this.tags = tags;
        this.platformName = platformName;
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public boolean passed() {
        return scenarios.stream().allMatch(Scenario::passed);
    }

    @Override
    public String toString() {
        return "Feature{" +
                "featureId='" + featureId + '\'' +
                ", name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                ", scenarios=" + scenarios +
                ", tags=" + tags +
                '}';
    }
}