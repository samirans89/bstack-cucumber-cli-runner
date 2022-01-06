package com.browserstack.runner;

import com.browserstack.runner.listener.TestEventListener;
import com.browserstack.runner.reporter.utils.ReportUtil;
import com.browserstack.runner.reporter.utils.ReporterConstants;
import com.browserstack.runner.utils.RunnerUtils;
import com.browserstack.webdriver.config.Platform;
import com.browserstack.webdriver.core.WebDriverFactory;
import io.cucumber.core.cli.CommandlineOptions;
import io.cucumber.core.cli.Main;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.browserstack.runner.utils.RunnerConstants.RERUN_SCENARIOS_DIR;

public class CucumberCLIRunner extends Runner {

    public static List<Platform> platformList = new ArrayList<>();
    public static final WebDriverFactory webDriverFactory = WebDriverFactory.getInstance();

    private final Instant sessionStartTime;

    private final String runnerTestListenerPlugin = "com.browserstack.runner.listener.TestEventListener";

    private final int cliRunnerExecutorPools = Integer.parseInt(System.getProperty("cli-runner.executor.pools", "2"));

    private final int cliRunnerCucumberThreads = Integer.parseInt(System.getProperty("cli-runner.cucumber.threads", "2"));


    public final Class<RunCucumberTest> runnerClass;
    public static final Object threadLock = new Object();

    public CucumberCLIRunner(Class<RunCucumberTest> testClass) {
        this.runnerClass = testClass;
        sessionStartTime = Instant.now();
    }

    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(CucumberCLIRunner.class);
    }

    @Override
    public void run(RunNotifier runNotifier) {

        try {

            CucumberCLIRunner customParallelRunnerObj = new CucumberCLIRunner(RunCucumberTest.class);
            customParallelRunnerObj.prepareForRun();
            customParallelRunnerObj.createExecutorPools();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void prepareForRun() {
        try {
            RunnerUtils.createDirectories(RERUN_SCENARIOS_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createExecutorPools() throws IOException {

        ExecutorService pool = Executors.newFixedThreadPool(cliRunnerExecutorPools);

        String absolutePath = Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString();
        String featuresParentDirectory = absolutePath + File.separator + System.getProperty("cucumber.features", "");
        List<String> files = RunnerUtils.findFiles(Paths.get(featuresParentDirectory), "feature");

        webDriverFactory.getPlatforms().forEach(platform -> {
            platformList.add(platform);

            for (String featureFile : files) {
                final String jsonReportFile = String.format("json:%s/%s/%s.json"
                        , ReporterConstants.CUCUMBER_JSON_REPORTS_DIR
                        , platform.getName().toUpperCase()
                        , featureFile.substring(featureFile.lastIndexOf(File.separator) + 1, featureFile.lastIndexOf("."))
                );

                String[] cucumberRunParams = new String[]{
                        featureFile
                        , CommandlineOptions.PLUGIN
                        , jsonReportFile
                        , CommandlineOptions.PLUGIN
                        , runnerTestListenerPlugin
                        , CommandlineOptions.THREADS
                        , String.valueOf(cliRunnerCucumberThreads)
                };
                pool.submit(new Task(cucumberRunParams));
            }

        });

        pool.shutdown();
        boolean isTerminated = false;
        try {
            isTerminated = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (isTerminated) {
            TestEventListener.RERUN_MODE = true;
            System.out.println(TestEventListener.getRerunScenariosPlatformStackMap());
            createRerunFailedExecutorPools();

        }
    }

    private void createRerunFailedExecutorPools() throws IOException {

        ExecutorService pool = Executors.newFixedThreadPool(cliRunnerExecutorPools);
        List<String> files = RunnerUtils.findFiles(Paths.get(RERUN_SCENARIOS_DIR), "txt");
        files.stream().map(File::new).forEach(rerunTxtFile -> {
            Scanner sc;
            try {
                sc = new Scanner(rerunTxtFile);
                while (sc.hasNextLine()) {
                    String scenarioId = sc.nextLine();
                    String jsonFileName = scenarioId.replaceAll(":", "-").replaceAll("\\.", "-");
                    String rerunPlatformFolderName = rerunTxtFile.getName().substring(0, rerunTxtFile.getName().lastIndexOf(".")).toUpperCase();
                    final String jsonReportFile = String.format("json:%s/%s/%s.json"
                            , ReporterConstants.CUCUMBER_RERUN_JSON_REPORTS_DIR
                            , rerunPlatformFolderName
                            , jsonFileName.substring(jsonFileName.lastIndexOf("/") + 1));

                    String[] cucumberRunParams = new String[]{
                             scenarioId
                            , CommandlineOptions.PLUGIN
                            , jsonReportFile
                            , CommandlineOptions.PLUGIN
                            , runnerTestListenerPlugin
                            , CommandlineOptions.THREADS
                            , String.valueOf(cliRunnerCucumberThreads)
                    };
                    pool.submit(new Task(cucumberRunParams));
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        pool.shutdown();
        boolean isTerminated = false;
        try {
            isTerminated = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (isTerminated) {
            generateReports();
        }
    }

    private void generateReports() {
        new ReportUtil().create(this);
    }


    public int getCliRunnerExecutorPools() {
        return cliRunnerExecutorPools;
    }

    public int getCliRunnerCucumberThreads() {
        return cliRunnerCucumberThreads;
    }

    public Instant getSessionStartTime() {
        return this.sessionStartTime;
    }

    private static class Task extends CucumberCLIRunner implements Runnable {

        private final String[] cucumberRunParams;

        public Task(String[] cucumberRunParams) {
            super(RunCucumberTest.class);
            this.cucumberRunParams = cucumberRunParams;
        }

        @Override
        public void run() {


            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Main.run(cucumberRunParams, contextClassLoader);
            // pool-1
            // pool-2
            // pool-3

            // platform - X

            /// cucumber-runner-1-thread-1 (scenario_1 - X)
            /// cucumber-runner-1-thread-2 (scenario_1 - Y)
            /// cucumber-runner-1-thread-3 (scenario_1 - Z)
            /// cucumber-runner-2-thread-1 (scenario_2 - X)
            /// cucumber-runner-2-thread-2 (scenario_2 - Y)
            /// cucumber-runner-3-thread-3 (scenario_2 - Z)
        }
    }
}
