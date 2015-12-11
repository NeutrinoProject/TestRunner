package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestExecutorService;
import com.neutrinoproject.testrunner.TestRunState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by btv on 02.12.15.
 */
public class TestRunnerModelImpl implements TestRunnerModel, TestRunnerHandler {
    private final AtomicReference<Map<String, TestState>> testStateMap = new AtomicReference<>(new LinkedHashMap<>());
    private final AtomicReference<List<String>> overallOutLines =
            new AtomicReference<>(Collections.synchronizedList(new ArrayList<>()));

//    testStateMap.get().clear();
//    overallOutLines.get().clear();

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
        return Optional.ofNullable(testState).map(TestState::getOutLines).orElse(Collections.emptyList());
    }

    @Override
    public void onTestsLoadingFinished(final boolean success, final Collection<String> testNames) {
        if (success) {
            final Map<String, TestState> localTestStateMap = new LinkedHashMap<>(testNames.size());
            testNames.forEach(testName -> localTestStateMap.put(testName, new TestState()));
            testStateMap.set(localTestStateMap);
        }
    }

    @Override
    public void onTestRunStart() {
        overallOutLines.get().clear();
        // TODO: Clean test outputs, test states in testStateMap.
    }

    @Override
    public void onOutputLine(@Nullable final String testName, final int overallLineIndex, final int testLineIndex,
                             final String outputLine) {
        overallOutLines.get().add(outputLine);
        final TestState testState = testStateMap.get().get(testName);
        if (testState != null) {
            testState.appendOutLine(outputLine);
        }
    }

    @Override
    public void onTestStateChange(final String testName, final TestRunState newState) {
        final TestState testState = testStateMap.get().get(testName);
        if (testState != null) {
            testState.setState(newState);
        }
    }

    @Override
    public void onTestRunFinished(final boolean success) {
    }
}
