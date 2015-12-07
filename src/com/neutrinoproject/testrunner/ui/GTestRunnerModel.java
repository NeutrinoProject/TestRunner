package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestEventHandler;
import com.neutrinoproject.testrunner.TestOutputParser;
import com.neutrinoproject.testrunner.TestRunState;
import com.neutrinoproject.testrunner.process.ProcessRunner;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by btv on 02.12.15.
 */
public class GTestRunnerModel implements TestRunnerModel {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final AtomicReference<Map<String, TestState>> testStateMap = new AtomicReference<>(new LinkedHashMap<>());
    private final AtomicReference<List<String>> overallOutLines =
            new AtomicReference<>(Collections.synchronizedList(new ArrayList<>()));
    private final TestRunnerHandler testRunnerHandler;

    private String testBinaryPath;
    private ProcessRunner processRunner;

    public GTestRunnerModel(final TestRunnerHandler testRunnerHandler) {
        this.testRunnerHandler = testRunnerHandler;
    }

    @Override
    public void startReadingBinary(final String testBinaryPath) {
        this.testBinaryPath = testBinaryPath;
        executorService.submit(this::readBinary);
    }

    @Override
    public void startAllTests() {
        executorService.submit(this::runAllTests);
    }

    @Override
    public void startTests(final Collection<String> testNames) {
        throw new NotImplementedException();
    }

    @Override
    public void stopAllProcesses() {
        // FIXME: processRunner might be null if readBinary has not started yet.
        processRunner.cancel();
    }

    @NotNull
    @Override
    public Collection<String> getTestNames() {
        return testStateMap.get().keySet();
    }

    @NotNull
    @Override
    public Collection<String> getOverallTestOutput() {
        return overallOutLines.get();
    }

    @NotNull
    @Override
    public Optional<TestRunState> getTestState(final String testName) {
        return Optional.ofNullable(testStateMap.get().get(testName)).map(TestState::getState);
    }

    @NotNull
    @Override
    public Collection<String> getTestOutput(final String testName) {
        final TestState testState = testStateMap.get().get(testName);
        return Optional.ofNullable(testState).map(TestState::getOutLines).orElse(null);
    }

    private void readBinary() {
        final Collection<String> lines = new ArrayList<>();
        final TestOutputParser testOutputParser = new TestOutputParser(null);

        boolean hasSucceed = false;
        processRunner = new ProcessRunner();
        try {
            final int exitCode = processRunner.start(new String[]{testBinaryPath, "--gtest_list_tests"}, lines::add);
            if (exitCode == 0) {
                final Collection<String> testNames = testOutputParser.parseTestList(lines);
                final Map<String, TestState> localTestStateMap = new LinkedHashMap<>(testNames.size());
                testNames.forEach(testName -> localTestStateMap.put(testName, new TestState()));
                testStateMap.set(localTestStateMap);

                overallOutLines.set(Collections.synchronizedList(new ArrayList<>()));

                hasSucceed = true;
            } else {
                // TODO: Log this problem.
            }
        } catch (IOException | ParseException e) {
            // TODO: Log this problem.
            e.printStackTrace();
        } finally {
            testRunnerHandler.onTestsLoadingFinished(hasSucceed);
        }
    }

    private void runAllTests() {
        final TestOutputParser testOutputParser = new TestOutputParser(new TestEventHandler() {
            private TestState currentTestState;
            private final List<String> overallOutLinesLocal = overallOutLines.get();

            @Override
            public void onOutLine(final String line) {
                overallOutLinesLocal.add(line);
                if (currentTestState != null) {
                    currentTestState.appendOutLine(line);
                }
                // TODO: Implement passing test name and indices.
                testRunnerHandler.onOutputLine(null, 0, 0, line);
            }

            @Override
            public void onErrLine(final String line) {
                System.out.println(line);
            }

            @Override
            public void onTestState(final TestRunState state, final String testName) {
                final TestState testState = testStateMap.get().get(testName);
                if (testState != null) {
                    testState.setState(state);
                    if (state == TestRunState.RUNNING) {
                        currentTestState = testState;
                    }
                    testRunnerHandler.onTestStateChange(testName, state);
                }
            }
        });

        boolean hasSucceed = false;
        processRunner = new ProcessRunner();
        try {
            final int exitCode = processRunner.start(new String[]{testBinaryPath}, testOutputParser::parseString);
            if (exitCode == 0) {
                hasSucceed = true;
            } else {
                // TODO: Log the problem somehow.
            }
        } catch (IOException e) {
            // TODO: Log the problem somehow.
            e.printStackTrace();
        } finally {
            testRunnerHandler.onTestRunFinished(hasSucceed);
        }
    }
}
