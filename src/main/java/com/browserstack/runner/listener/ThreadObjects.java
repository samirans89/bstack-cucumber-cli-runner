package com.browserstack.runner.listener;

import com.browserstack.webdriver.config.Platform;
import org.openqa.selenium.WebDriver;

public class ThreadObjects {

    WebDriver driver;
    Platform platform;

    public ThreadObjects(WebDriver driver, Platform platform) {
        this.driver = driver;
        this.platform = platform;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public Platform getPlatform() {
        return platform;
    }
}
