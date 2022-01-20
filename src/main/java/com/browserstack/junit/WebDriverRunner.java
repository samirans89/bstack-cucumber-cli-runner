package com.browserstack.junit;

import com.browserstack.WebDriverTestRunner;
import com.browserstack.webdriver.WebDriverManager;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.openqa.selenium.WebDriver;

import java.util.Objects;

public class WebDriverRunner extends Runner {

    private final Class testClass;

    public WebDriverRunner(Class testClass) {
        this.testClass = testClass;
    }

    @Override
    public Description getDescription() {
        return Description.createTestDescription(testClass,"WebDriverRunner Tests");
    }

    @Override
    public void run(RunNotifier runNotifier) {
        WebDriverOptions webDriverOptions =
                (WebDriverOptions) testClass.getAnnotation(WebDriverOptions.class);
        Objects.requireNonNull(webDriverOptions,"WebDriverOptions Undefined");
        runNotifier.fireTestRunStarted(getDescription());
        RuntimeOptions runtimeOptions = new CucumberOptionsAnnotationParser()
                .withOptionsProvider(new WebDriverCucumberOptionsProvider())
                .parse(testClass).build();
        WebDriverTestRunner.run(webDriverOptions.rerun(), String.valueOf(webDriverOptions.thread()),runtimeOptions);
        runNotifier.fireTestFinished(getDescription());
    }

    public static WebDriver getWebDriver() {
        return WebDriverManager.getWebDriver();
    }
}
