package com.browserstack.runner;

import com.browserstack.webdriver.core.WebDriverFactory;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.ExceptionUtils;
import io.cucumber.core.exception.UnrecoverableExceptions;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.CucumberExecutionContext;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchExecutionRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchExecutionRunner.class);
    private static volatile BatchExecutionRunner instance;
    private final EventBus eventBus;
    private final CucumberExecutionContext context;
    private final RuntimeOptions runtimeOptions;
    private final WebDriverFactory webDriverFactory = WebDriverFactory.getInstance();

    private BatchExecutionRunner(EventBus eventBus, CucumberExecutionContext context, RuntimeOptions runtimeOptions) {
        this.eventBus = eventBus;
        this.context = context;
        this.runtimeOptions = runtimeOptions;
    }

    public static BatchExecutionRunner createBatchExecutionRunner(EventBus eventBus, CucumberExecutionContext context, RuntimeOptions runtimeOptions) {
        if (instance == null) {
            synchronized (BatchExecutionRunner.class) {
                if (instance == null) {
                    instance = new BatchExecutionRunner(eventBus, context, runtimeOptions);
                }
            }
        }
        return instance;
    }

    public static BatchExecutionRunner getInstance() {
        if (instance == null) {
            throw new RuntimeException("Batch Executor initialisation failed");
        }
        return instance;
    }

    public void submitExecutions(int batch, List<Execution> executions) {
        eventBus.send(new BatchExecutionStarted(Instant.now(),batch));
        ExecutorService executor = this.runtimeOptions.isMultiThreaded() ? Executors.newFixedThreadPool(this.runtimeOptions.getThreads(), new WebDriverThreadFactory()) : new SameThreadExecutorService();
        List<Future<?>> executingPickles = new ArrayList<>();
        executions.forEach(execution -> executingPickles.add(executor.submit(executeForPlatform(execution))));

        executor.shutdown();

        for (Future<?> executingPickle : executingPickles) {
            try {
                executingPickle.get();
            } catch (java.util.concurrent.ExecutionException executionException) {
                LOGGER.error("Exception while executing pickle", executionException);
            } catch (InterruptedException interruptedException) {
                executor.shutdownNow();
                LOGGER.debug("Interrupted while executing pickle", interruptedException);
            }
        }
        eventBus.send(new BatchExecutionCompleted(Instant.now(), batch));
    }

    private Runnable executeForPlatform(Execution execution) {
        return () -> this.context.runTestCase((runner) -> {
            eventBus.send(new ExecutionStarted(Instant.now(), execution));
            WebDriver webDriver = webDriverFactory.createWebDriverForPlatform(execution.getPlatform(), execution.getPickle().getName());
            eventBus.send(new WebDriverCreated(Instant.now(), webDriver));
            runner.runPickle(execution.getPickle());
            eventBus.send(new ExecutionCompleted(Instant.now(), execution, webDriver));
        });
    }

    public void closeExecutionContext() {
        CucumberExecutionContext executionContext = this.context;
        Objects.requireNonNull(executionContext);
        this.execute(executionContext::runAfterAllHooks);
        Objects.requireNonNull(executionContext);
        this.execute(executionContext::finishTestRun);
        Throwable exception = this.context.getThrowable();
        if (exception != null) {
            ExceptionUtils.throwAsUncheckedException(exception);
        }else {
            LOGGER.debug("Cucumber feature execution completed");
        }
        eventBus.send(new BuildCompleted(Instant.now()));
    }

    private void execute(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            UnrecoverableExceptions.rethrowIfUnrecoverable(throwable);
        }
    }

    private static final class SameThreadExecutorService extends AbstractExecutorService {
        private SameThreadExecutorService() {
        }

        public void execute(Runnable command) {
            command.run();
        }

        public void shutdown() {
        }

        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        public boolean isShutdown() {
            return true;
        }

        public boolean isTerminated() {
            return true;
        }

        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }
    }

    private static final class WebDriverThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        WebDriverThreadFactory() {
            this.namePrefix = "web-driver-runner-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, this.namePrefix + this.threadNumber.getAndIncrement());
        }
    }

}
