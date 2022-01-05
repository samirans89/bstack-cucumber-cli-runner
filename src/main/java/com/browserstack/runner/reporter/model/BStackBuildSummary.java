package com.browserstack.runner.reporter.model;

public class BStackBuildSummary {
    String testPlatform;
    int passCount;
    int failCount;
    int unmarkedCount;

    public BStackBuildSummary(String testPlatform, int passCount, int failCount, int unmarkedCount) {
        this.testPlatform = testPlatform;
        this.passCount = passCount;
        this.failCount = failCount;
        this.unmarkedCount = unmarkedCount;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getUnmarkedCount() {
        return unmarkedCount;
    }

    public void setUnmarkedCount(int unmarkedCount) {
        this.unmarkedCount = unmarkedCount;
    }
}
