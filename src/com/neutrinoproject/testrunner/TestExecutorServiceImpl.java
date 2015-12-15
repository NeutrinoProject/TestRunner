package com.neutrinoproject.testrunner;

import com.neutrinoproject.testrunner.process.ProcessRunner;
import com.neutrinoproject.testrunner.ui.TestRunnerHandler;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by btv on 11.12.15.
 */
public class TestExecutorServiceImpl implements TestExecutorService {
    private final static Logger LOGGER = Logger.getLogger(TestExecutorServiceImpl.class.getName());
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final TestOutputParser parser = new TestOutputParser();
    private final Collection<TestRunnerHandler> testRunnerHandlers = new ArrayList<>();
    private ProcessRunner processRunner;

    public void addTestRunnerHandler(final TestRunnerHandler testRunnerHandler) {
        LOGGER.fine(() -> "Adding handler: " + testRunnerHandler);
        testRunnerHandlers.add(testRunnerHandler);
    }

    public void submitTestRun(final String testBinaryPath, final Collection<String> testNames) {
        final String testFilterFlag = testNames.isEmpty() ? "" : "--gtest_filter=" + String.join(":", testNames);
        final String[] command = new String[]{testBinaryPath, testFilterFlag};
        parser.clearState();
        processRunner = new ProcessRunner();
        executorService.submit(() -> runTests(command));
    }

    public void submitReadBinary(final String testBinaryPath) {
        final String[] command = new String[]{testBinaryPath, "--gtest_list_tests"};
        parser.clearState();
        processRunner = new ProcessRunner();
        executorService.submit(() -> readBinary(command));
    }

    public void stop() {
        if (processRunner != null) {
            LOGGER.info("Stopping process");
            processRunner.cancel();
        }
    }

    private void readBinary(final String[] command) {
        LOGGER.fine(() -> "Starting reading binary with command: " + String.join(" ", Arrays.asList(command)));
        final Collection<String> lines = new ArrayList<>();

        boolean hasSucceed = false;
        Collection<String> testNames = Collections.emptyList();
        try {
            final int exitCode = processRunner.start(command, lines::add);
            if (exitCode == 0) {
                testNames = parser.parseTestList(lines);
                hasSucceed = true;
                LOGGER.fine("Reading binary process exits with normal code");
            } else {
                LOGGER.warning("Reading binary process exits with " + exitCode + " code");
            }
        } catch (IOException | ParseException e) {
            LOGGER.warning("Reading binary process throws exception: " + e.getMessage());
        } finally {
            for (TestRunnerHandler handler : testRunnerHandlers) {
                handler.onTestsLoadingFinished(hasSucceed, testNames);
            }
        }
    }

    private void runTests(final String[] command) {
        LOGGER.fine(() -> "Starting execute tests with command: " + String.join(" ", Arrays.asList(command)));
        final Consumer<String> consumer = new Consumer<String>() {
            private String currentTestName;

            @Override
            public void accept(final String line) {
                final TestOutputParser.Result result = parser.parseOutputLine(line);
                if (result != null) {
                    if (result.testState == TestRunState.RUNNING) {
                        currentTestName = result.testName;
                    }
                    testRunnerHandlers.stream().forEach(handler ->
                            handler.onTestStateChange(result.testName, result.testState));
                }
                testRunnerHandlers.stream().forEach(handler ->
                        handler.onOutputLine(currentTestName, line));

                if (result != null && result.testState != TestRunState.RUNNING) {
                    currentTestName = null;
                }
            }
        };

        testRunnerHandlers.stream().forEach(TestRunnerHandler::onTestRunStart);
        boolean hasSucceed = false;
        try {
            final int exitCode = processRunner.start(command, consumer);
            if (exitCode == 0) {
                hasSucceed = true;
                LOGGER.fine("Test run exits with normal code");
            } else {
                LOGGER.info("Test run exits with " + exitCode + " code");
            }
        } catch (IOException e) {
            LOGGER.warning("Test run process throws exception: " + e.getMessage());
        } finally {
            for (TestRunnerHandler handler : testRunnerHandlers) {
                handler.onTestRunFinished(hasSucceed);
            }
        }
    }
}
