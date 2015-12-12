package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestRunState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Created by btv on 02.12.15.
 */
public class TestRunnerModelImpl implements TestRunnerModel, TestRunnerHandler {
    private final AtomicReference<Map<String, TestState>> testStateMap = new AtomicReference<>(new LinkedHashMap<>());
    private final List<String> overallOutLines = Collections.synchronizedList(new ArrayList<>());
    private final HashMap<String, List<Consumer<String>>> subscribers = new HashMap<>();

    @NotNull
    @Override
    public Collection<String> getTestNames() {
        return testStateMap.get().keySet();
    }

    @NotNull
    @Override
    public Optional<TestRunState> getTestState(final String testName) {
        return Optional.ofNullable(testStateMap.get().get(testName)).map(TestState::getState);
    }

    @Override
    public void subscribeOnTestOutput(final String testName, final Consumer<String> consumer) {
        Collection<String> testOutput;
        synchronized (this) {
            subscribers.computeIfAbsent(testName, k -> new ArrayList<>()).add(consumer);
            testOutput = new ArrayList<>(getTestOutput(testName));
        }
        testOutput.stream().forEach(consumer::accept);
    }

    @Override
    public void unsubscribeAllOnTestOutput(final String testName) {
        synchronized (this) {
            subscribers.remove(testName);
        }
    }

    // Methods for TestRunnerHandler.
    @Override
    public void onTestsLoadingFinished(final boolean success, final Collection<String> testNames) {
        final Map<String, TestState> localTestStateMap = new LinkedHashMap<>(testNames.size());
        testNames.forEach(testName -> localTestStateMap.put(testName, new TestState()));
        testStateMap.set(localTestStateMap);
        overallOutLines.clear();
    }

    @Override
    public void onTestRunStart() {
        overallOutLines.clear();
        testStateMap.get().values().stream().forEach(TestState::clear);
    }

    @Override
    public void onOutputLine(@Nullable final String testName, final String outputLine) {
        List<Consumer<String>> forThisTestSubscribersLocal = Collections.emptyList();
        List<Consumer<String>> forOverallTestSubscribersLocal = Collections.emptyList();
        synchronized (this) {
            overallOutLines.add(outputLine);
            final TestState testState = testStateMap.get().get(testName);
            if (testState != null) {
                testState.appendOutLine(outputLine);
            }

            if (subscribers.containsKey(testName)) {
                forThisTestSubscribersLocal = new ArrayList<>(subscribers.get(testName));
            }
            if (subscribers.containsKey(OVERALL_TEST_NAME)) {
                forOverallTestSubscribersLocal = new ArrayList<>(subscribers.get(OVERALL_TEST_NAME));
            }
        }
        forThisTestSubscribersLocal.stream().forEach(consumer -> consumer.accept(outputLine));
        forOverallTestSubscribersLocal.stream().forEach(consumer -> consumer.accept(outputLine));
    }

    @Override
    public void onTestStateChange(final String testName, final TestRunState newState) {
        final TestState testState = testStateMap.get().get(testName);
        if (testState != null) {
            testState.setState(newState);
        }
    }

    @NotNull
    private Collection<String> getTestOutput(final String testName) {
        if (OVERALL_TEST_NAME.equals(testName)) {
            return overallOutLines;
        }
        final TestState testState = testStateMap.get().get(testName);
        return Optional.ofNullable(testState).map(TestState::getOutLines).orElse(Collections.emptyList());
    }
}
