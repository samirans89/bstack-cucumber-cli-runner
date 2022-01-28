package com.browserstack.runner;

import com.browserstack.webdriver.WebDriverManager;
import io.cucumber.core.cli.CommandlineOptions;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.plugin.Plugin;

import java.util.Optional;

public final class WebDriverTestRunner {

    private WebDriverTestRunner() {
    }

    public static void run(boolean isRerunEnabled,String threads, RuntimeOptions annotationOptions) {
        run(isRerunEnabled, threads,annotationOptions, Thread.currentThread().getContextClassLoader(), new WebDriverManager());
    }


    private static void run(boolean isRerunEnabled, String threads, RuntimeOptions annotationOptions, ClassLoader classLoader, Plugin... additionalPlugins) {
        RuntimeOptions propertiesFileOptions = (new CucumberPropertiesParser()).parse(CucumberProperties.fromPropertiesFile()).build(annotationOptions);
        RuntimeOptions environmentOptions = (new CucumberPropertiesParser()).parse(CucumberProperties.fromEnvironment()).build(propertiesFileOptions);
        RuntimeOptions systemOptions = (new CucumberPropertiesParser()).parse(CucumberProperties.fromSystemProperties()).build(environmentOptions);
        CommandlineOptionsParser commandlineOptionsParser = new CommandlineOptionsParser(System.out);
        RuntimeOptions runtimeOptions = commandlineOptionsParser.parse(CommandlineOptions.THREADS, threads).addDefaultGlueIfAbsent().addDefaultFeaturePathIfAbsent().addDefaultSummaryPrinterIfNotDisabled().enablePublishPlugin().build(systemOptions);
        Optional<Byte> exitStatus = commandlineOptionsParser.exitStatus();
         if (!exitStatus.isPresent()) {
            WebDriverRuntime.Builder builder = WebDriverRuntime.builder();
            if (isRerunEnabled) {
                builder.withRerunEnabled();
            }
            WebDriverRuntime runtime =
                    builder.withRuntimeOptions(runtimeOptions).withClassLoader(() -> classLoader)
                            .withAdditionalPlugins(additionalPlugins)
                            .build();
            runtime.run();
        }
    }
}
