package com.browserstack.runner.reporter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Scenario {
    @JsonProperty("id")
    private String scenarioId;
    private String name;
    private int line;
    private String keyword;

    private String featureId;
    private List<Hook> before = new ArrayList<>();;
    private List<Hook> after = new ArrayList<>();;
    private List<Step> steps = new ArrayList<>();;
    private List<Tag> tags = new ArrayList<>();
    @JsonProperty("start_timestamp")
    private String startTimestamp;

    private String platformName;

    private boolean isRerun;

    public Scenario() {

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getName() {
        return name;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getLine() {
        return line;
    }

    public List<Hook> getBefore() {
        return before;
    }

    public List<Hook> getAfter() {
        return after;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getPlatformName() {
        return this.platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public boolean isRerun() {
        return isRerun;
    }

    public void setRerun(boolean rerun) {
        isRerun = rerun;
    }

    public boolean passed() {
         return before.stream().allMatch(Hook::passed)
                && after.stream().allMatch(Hook::passed)
                 && steps.stream().allMatch(Step::passed);
    }
}
