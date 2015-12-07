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
public class GTestRunnerModel extends Observable implements TestRunnerModel {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final AtomicReference<Map<String, TestState>> testStateMap = new AtomicReference<>(new LinkedHashMap<>());
    private final AtomicReference<List<String>> overallOutLines =
            new AtomicReference<>(Collections.synchronizedList(new ArrayList<>()));

    private String testBinaryPath;
    private ProcessRunner processRunner;

    public static class Event {
        public enum Type {
            TEST_CASES_LOADED,

            OUT_LINE,
            TEST_STATE_CHANGED,
            TEST_RUN_FINISHED,

            ERROR,
        }

        public final Type type;
        public final Object data;

        public Event(final Type type) {
            this(type, null);
        }

        public Event(final Type type, final Object data) {
            this.type = type;
            this.data = data;
        }
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

        processRunner = new ProcessRunner();
        try {
            final int exitCode = processRunner.start(new String[]{testBinaryPath, "--gtest_list_tests"}, lines::add);
            if (exitCode == 0) {
                final Collection<String> testNames = testOutputParser.parseTestList(lines);
                final Map<String, TestState> localTestStateMap = new LinkedHashMap<>(testNames.size());
                testNames.forEach(testName -> localTestStateMap.put(testName, new TestState()));
                testStateMap.set(localTestStateMap);

                overallOutLines.set(Collections.synchronizedList(new ArrayList<>()));

                setChanged();
                notifyObservers(new Event(Event.Type.TEST_CASES_LOADED));
            } else {
                // TODO: Handle the exit code.
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
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
                setChanged();
                notifyObservers(new Event(Event.Type.OUT_LINE, line));
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
                    setChanged();
                    notifyObservers(new Event(Event.Type.TEST_STATE_CHANGED, testName));
                }
            }
        });

        processRunner = new ProcessRunner();
        int exitCode = -1;
        try {
            exitCode = processRunner.start(new String[]{testBinaryPath}, testOutputParser::parseString);
            setChanged();
        } catch (IOException e) {
            // TODO: Log the problem somehow.
            e.printStackTrace();
        }
        notifyObservers(new Event(Event.Type.TEST_RUN_FINISHED, exitCode));
    }
}
