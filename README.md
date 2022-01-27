<h1 align="center">   :zap: <img src="https://avatars.githubusercontent.com/u/1119453?s=200&v=4" width="60" height="60" > <a href="https://github.com/browserstack/browserstack-examples-junit5">BrowserStack Cucumber CLI Runner</a>  <img src="https://avatars.githubusercontent.com/u/320565?s=200&v=4" width="60" height="60" >
 :zap:</h1>
 
 Welcome to BrowserStack Cucumber CLI Runner, a sample UI testing framework empowered with **[Selenium](https://www.selenium.dev/)** and **[Cucumber-JVM](https://cucumber.io/docs/installation/java/)**. It is based on the **[WebDriver Framework](https://github.com/browserstack/webdriver-framework)**.
 
 ## :pushpin: Key Features
 
:globe_with_meridians: Empowered to run on various platforms including **on-premise browsers**, browsers running on a remote selenium grid such as 
 **[BrowserStack Automate](https://www.browserstack.com/automate)** or in a **[Docker container](https://github.com/SeleniumHQ/docker-selenium)**.

:rocket: Enables concurrent execution of cucumber scenarios across different platforms.

:recycle: Effectively manages WebDrivers by abstracting the logic such driver creation, driver closure and marking test status etc.

:arrow_down: Provides direct APIs for fetching WebDrivers into the step definitions.

:zap: Single Runner class required to configure all parameters and plugins.

:hammer_and_wrench: Easy configuration for managing the level of concurrency.

:bulb:A number of light weight plugins available for various purposes such as debugging, custom report generation and rerunning failed scenarios.

:test_tube: Cucumber events pusblished at various stages, which are useful to build new extesions for custom needs.

 
  ## :pushpin: Prerequisites
 Ensure you have the following dependencies installed on the machine
 
 1. Java Development Kit (8 or above)
 2. Maven (3 or above)
 3. [Chrome Driver](https://chromedriver.chromium.org/) and [Chrome Browser](https://www.google.com/chrome/)   [![OnPrem](https://img.shields.io/badge/For-OnPrem-green)]()
 5. [Docker](https://www.docker.com/) and [Docker Selenium Grid](https://github.com/SeleniumHQ/docker-selenium).  [![OnDocker](https://img.shields.io/badge/For-OnDocker-blue)]()
 6. [BrowserStack Automate Account](https://www.browserstack.com/automate). [![BrowserStack](https://img.shields.io/badge/For-BrowserStackAutomate-orange)]()



  ## :pushpin: Usage

:arrow_down: Clone this repository

```git
git clone git@github.com:BrowserStackCE/bstack-cucumber-cli-runner.git
```
<br>

:package: Install the JAR in your local maven repository
```sh
mvn clean install
```

<br>

:card_file_box: Import the package into your project with the following maven coordinates
```xml
<dependency>
    <groupId>com.browserstack</groupId>
    <artifactId>bstack-cucumber-cli-runner</artifactId>
    <version>0.0.1</version>
</dependency>
```

<br>

:white_check_mark: Run your cucumber tests with ``WebDriverRunner`` and required ``WebDriverOptions``

```java
@RunWith(WebDriverRunner.class)
@WebDriverOptions(
        thread = 5,
        cucumberOptions = @CucumberOptions(
                features = "src/test/resources/features/com/browserstack",
                name = "End to End Scenario"
        )
)
public class RunCucumberTest {

}
```

<br>

:dizzy: Enable required plugins and options you need

```java
@RunWith(WebDriverRunner.class)
@WebDriverOptions(
        thread = 5,
        rerun = true,
        cucumberOptions = @CucumberOptions(
                features = "src/test/resources/features/com/browserstack",
                name = "End to End Scenario",
                plugin = {"pretty",
                        "com.browserstack.report.CustomReportListener:custom/reports",
                        "com.browserstack.rerun.RerunExecutionManager:2"},
                monochrome = true
        )
)
public class RunCucumberTest {

}
```

<br>

:rocket: Implement the following method in your step definition classes, page object classes or a base class and use within test steps.
```java
public class BaseStep {
    public WebDriver getWebDriver(){
        return WebDriverRunner.getWebDriver();
    }
    
}

```

  ## :pushpin: WebDriverRunner Parameters
  
  The ``WebDriverRunner`` class expects ``WebDriverOptions`` annotaion for all the runtime parameters. Following are the parameters, their usage and default values.
  
  | Parameter | Usage | Default Value |
|--- | --- | --- | 
|thread| The number of concurrent tests to be executed |1|
|rerun|The flag for enabling rerun plugins. (if any)|false|
|cucumberOptions|Cucumber options to be enabled such as features, plugins etc. |None|
  

  ## :pushpin: Events

The framework exposes a number of events, so that you can build any extension on top of the framework for logging, reporting, monitoring etc.

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

  ## :pushpin: Available Plugins
The  repository also comes with a number of basic plugins as described below along with events used.

### WebDriverManager
This plugin is enabled by default and cannot be disabled. It is used for closing the driver gracefully after the completion of a test.

| Events | Purpose |
|--- | --- | 
|WebDriverCreated| Copies and delivers web driver to the tests.
|TestCaseFinished|Marks and closes the webDriver.

###  RerunExecutionManager

This plugin can we enabled to rerun your failed tests. It takes a parameter, which is the maxium number of repitiotions in case of constant failures.

| Events | Purpose |
|--- | --- | 
|ExecutionStarted|Copies execution for rerun.
|TestCaseFinished|Push execution for rerun in next batch.
|BatchExecutionCompleted|Fires next batch failed tests for run.

Note : This plugin takes a parameter for maximum reruns


### CustomReportListener

This plugin collects metadata about the tests after the build completion. It generates a mustache based HTML report after the completion of the build. It takes a parameter, which is the path for report generation.

| Plugin | Purpose |
|--- | --- | 
|BuildStarted|Record build start time.
|RuntimeCreated|Record runtime options.
|BatchExecutionStarted|Records the current batch.
|ExecutionStarted|Record the features executed.
|TestStepFinished|Record the step.
|TestCaseFinished|Record the test case.
|BuildCompleted|Record build end time & Generate reports.
