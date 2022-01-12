#About the Repository

The repository is a web driver manager framework for java-cucumber based tests.
It contains APIs for running your tests, injecting web driver and plugin for reporting and rerun.
The framework uses the WebDriverFramework Core to manage the creation of web drivers using a configuration file.
The framework executes the scenarios selected by the given conditions across all the platforms mentioned in the configuration file in a concurrent faction.

Events
=========

The framework exposes a number of events, so tha you can build any extension on top of the framework for logging, reporting, monitoring etc.

| Event | Detail |
|--- | --- | 
|BuildStarted| Returns the **Instant** at which the build is started.
|RuntimeCreated| Returns the **Instant** at which the runtime options are parsed and the **RuntimeOptions** object.
|BatchExecutionStarted| Returns the **Instant** at which the current **Batch** is started.
|ExecutionStarted| Returns the **Instant** at which the current **Execution** is started.
|WebDriverCreated| Returns the **WebDriver** and the **Instant** of creation.
|ExecutionCompleted| Returns the **Instant**, **Execution** and **WebDriver** after an execution is completed.
|BatchExecutionCompleted| Returns the **Instant** and **Batch** after its completion.
|BuildCompleted| Marks the completion of the build.

Plugins
=======
The  repository also comes with a number of basic plugins as described below.

**WebDriverManager**
======================
This plugin the enabled by default. It is used for closing the driver gracefully after the completion of a test.

| Events | Purpose |
|--- | --- | 
|WebDriverCreated| Copies and delivers web driver to the tests
|TestCaseFinished|MarkAndCloseWebDriver

**RerunExecutionManager**
======================

This plugin can we enabled with the number of repetitions you need to perform on a test failure.

| Events | Purpose |
|--- | --- | 
|ExecutionStarted|Copies execution for rerun
|TestCaseFinished|Push execution for rerun in next batch
|BatchExecutionCompleted|Fires next batch failed tests for run

Note : This plugin takes a parameter for maximum reruns


**CustomReportListener**
======================

This plugin collects metadata about the tests after the build completion. It generates a mustache based HTML report after the completion of the build.

| Plugin | Purpose |
|--- | --- | 
|BuildStarted|Record build start time
|RuntimeCreated|Record runtime options
|BatchExecutionStarted|Records the current batch
|ExecutionStarted|Record the features executed
|TestStepFinished|Record the step
|TestCaseFinished|Record the test case
|BuildCompleted|Record build end time & Generate reports

Note : This plugin takes a parameter for report path.
Ensure you have the mustache templates, js and css ready in `src/test/resources/reporter` directory.


**Other Classes**

| Plugin | Purpose |
|--- | --- |
BatchExecutionRunner|Executes tests on provided list of platforms
Execution|Current combination of pickle & Platform
WebDriverRuntime|Entry point for running tests
WebDriverTestRunner|External Facing API


*How to run a test with this library?*

1.Clone this repository

``
git clone git@github.com:BrowserStackCE/bstack-cucumber-cli-runner.git
``

2. Install it in your local repository
```sh
mvn clean install
```

3. Use within your Java project
```xml
<dependency>
    <groupId>com.browserstack</groupId>
    <artifactId>bstack-cucumber-cli-runner</artifactId>
    <version>0.0.1</version>
</dependency>
```

4. Run the tests within your framework with ``WebDriverTestRunner`` and required ``CucumberOptions``

```java
public class Test{
    public void testMethod(){
        String[] argv = new String[]{
                CommandlineOptions.GLUE, ""};
        WebDriverTestRunner.run(true,argv);
    }
}
```

5. Enable required plugins and options you need

```java
public class Test{
    public void testMethod(){
        String[] argv = new String[]{
                CommandlineOptions.THREADS,"25",
                CommandlineOptions.PLUGIN,"com.browserstack.rerun.RerunExecutionManager:2",
                CommandlineOptions.PLUGIN,"com.browserstack.report.CustomReportListener:target/reports",
                CommandlineOptions.NAME,"End to End Scenario",
                CommandlineOptions.GLUE, ""};
        WebDriverTestRunner.run(true,argv);
    }
}
```

6. Within your test steps are a base class  use ``WebDriverTestRunner`` to get the web driver
```java
public class BaseStep {
    public WebDriver getWebDriver(){
        return WebDriverTestRunner.getWebDriver();
    }
    
}

```
