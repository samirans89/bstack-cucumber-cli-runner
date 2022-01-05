package com.browserstack.runner.listener;


import com.browserstack.runner.CucumberCLIRunner;
import com.browserstack.runner.utils.RunnerConstants;
import com.browserstack.runner.utils.RunnerUtils;
import com.browserstack.webdriver.config.DriverType;
import com.browserstack.webdriver.config.Platform;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TestEventListener implements ConcurrentEventListener {

    private static final Map<String, Stack<Platform>> scenarioIdPlatformStackMap = new ConcurrentHashMap<>();
    private static final Map<String, Stack<Platform>> rerunScenariosPlatformStackMap = new ConcurrentHashMap<>();
    public static final Map<Long, ThreadObjects> threadIdObjectsMap = new ConcurrentHashMap<>();
    public static boolean RERUN_MODE = false;

    private static String driverType;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::markAndCloseWebDriver);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::testCaseStarted);
    }

    private void testCaseStarted(TestCaseStarted testCaseStarted) {

        String scenarioId = testCaseStarted.getTestCase().getUri().toString() + ":" + testCaseStarted.getTestCase().getLocation().getLine();

        try {

            Stack<Platform> scenarioPlatformStack;
            synchronized (CucumberCLIRunner.threadLock) {

                if (!RERUN_MODE) {
                    scenarioPlatformStack = scenarioIdPlatformStackMap.get(scenarioId);
                    if (scenarioPlatformStack == null) {
                        List<Platform> shufflePlatforms = new ArrayList<>(CucumberCLIRunner.platformList);
                        Collections.shuffle(shufflePlatforms);

                        Stack<Platform> platformStack = new Stack<>();
                        platformStack.addAll(shufflePlatforms);

                        if (!RERUN_MODE) {
                            scenarioIdPlatformStackMap.put(scenarioId, platformStack);
                            scenarioPlatformStack = scenarioIdPlatformStackMap.get(scenarioId);
                        } else {
                            rerunScenariosPlatformStackMap.put(scenarioId, platformStack);
                            scenarioPlatformStack = rerunScenariosPlatformStackMap.get(scenarioId);
                        }
                    }
                } else {
                    scenarioPlatformStack = rerunScenariosPlatformStackMap.get(scenarioId);
                }
            }

            Platform platform = scenarioPlatformStack.pop();

            WebDriver webDriver = CucumberCLIRunner.webDriverFactory.createWebDriverForPlatform(platform, testCaseStarted.getTestCase().getName());
            synchronized (CucumberCLIRunner.threadLock) {
                threadIdObjectsMap.put(Thread.currentThread().getId(), new ThreadObjects(webDriver, platform));
            }

            driverType = CucumberCLIRunner.webDriverFactory.getDriverType().toString();

            if (platform.getRealMobile() != null && !platform.getRealMobile()) {
                webDriver.manage().window().maximize();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void markAndCloseWebDriver(TestCaseFinished testCaseFinished) {

        String scenarioId = testCaseFinished.getTestCase().getUri().toString() + ":" + testCaseFinished.getTestCase().getLocation().getLine();
        ThreadObjects threadObjects;
        synchronized (CucumberCLIRunner.threadLock) {
            threadObjects = threadIdObjectsMap.get(Thread.currentThread().getId());
        }

        WebDriver webDriver = threadObjects.getDriver();
        Platform platform = threadObjects.getPlatform();

        try {
            if (driverType.equalsIgnoreCase(DriverType.cloudDriver.toString())) {
                String status = "passed" ;
                String reason = "Test Passed" ;
                if (!testCaseFinished.getResult().getStatus().isOk()) {
                    status = "failed" ;
                    reason = testCaseFinished.getResult().getError().toString();
                }
                String script = createExecutorScript(status, reason);
                if (StringUtils.isNotEmpty(script)) {
                    ((JavascriptExecutor) webDriver).executeScript(script);
                }
            }
        } finally {
            if (webDriver != null) {
                webDriver.quit();
            }
        }

        if (!testCaseFinished.getResult().getStatus().isOk()) {
            String filePath = String.format("%s%s%s%s", RunnerConstants.RERUN_SCENARIOS_DIR, File.separator, platform.getName(), ".txt");
            RunnerUtils.writeToFile(filePath, scenarioId + System.lineSeparator(), true);

            synchronized (CucumberCLIRunner.threadLock) {
                Stack<Platform> platformStack = rerunScenariosPlatformStackMap.get(scenarioId);
                if(platformStack == null) {
                    platformStack = new Stack<>();
                    platformStack.push(platform);
                    rerunScenariosPlatformStackMap.put(scenarioId, platformStack);
                } else {
                    platformStack.push(platform);
                    rerunScenariosPlatformStackMap.put(scenarioId, platformStack);
                }


            }
        }
    }

    private String createExecutorScript(String status, String reason) {
        ObjectNode rootNode = objectMapper.createObjectNode();
        ObjectNode argumentsNode = objectMapper.createObjectNode();

        // Read only the first line of the error message
        reason = reason.split("\n")[0];
        // Limit the error message to only 255 characters
        if (reason.length() >= 255) {
            reason = reason.substring(0, 255);
        }
        // Replacing all the special characters with whitespace
        reason = reason.replaceAll("^[^a-zA-Z0-9]", " ");

        argumentsNode.put("status", status);
        argumentsNode.put("reason", reason);

        rootNode.put("action", "setSessionStatus");
        rootNode.set("arguments", argumentsNode);
        String executorStr;
        try {
            executorStr = objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new Error("Error creating JSON object for Marking tests", e);
        }
        return "browserstack_executor: " + executorStr;
    }

    public static Map<String, Stack<Platform>> getRerunScenariosPlatformStackMap() {
        return rerunScenariosPlatformStackMap;
    }
}