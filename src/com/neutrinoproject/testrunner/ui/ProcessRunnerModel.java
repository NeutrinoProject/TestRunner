package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestEventHandler;
import com.neutrinoproject.testrunner.TestOutputParser;
import com.neutrinoproject.testrunner.TestRunState;
import com.neutrinoproject.testrunner.process.ProcessRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Created by btv on 02.12.15.
 */
public class ProcessRunnerModel extends Observable {
    private final ExecutorService executorService;
    private final AtomicReference<Collection<String>> testCases;
    // TODO: Use more efficient data structure.
    private final List<TestState> testStates = Collections.synchronizedList(new ArrayList<>());

    private String testBinaryPath;
    private ProcessRunner processRunner;

    public static class TestState {
        private final String fullName;
        private final List<String> outLines = Collections.synchronizedList(new ArrayList<>());
        private TestRunState state;

        public TestState(final String fullName) {
            this.fullName = fullName;
        }

        public TestRunState getState() {
            return state;
        }

        public void setState(final TestRunState state) {
            this.state = state;
        }

        public List<String> getOutLines() {
            return outLines;
        }

        public void appendOutLine(final String line) {
            outLines.add(line);
        }

        public String getFullName() {
            return fullName;
        }
    }

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

    public ProcessRunnerModel() {
        this.executorService = Executors.newFixedThreadPool(1);
        testCases = new AtomicReference<>();
    }

    public Collection<String> getTestCases() {
        return testCases.get();
    }

    public Optional<TestState> getTestState(final String fullName) {
        return testStates.stream()
                .filter(testState -> testState.getFullName().equals(fullName))
                .findFirst();
    }

    public void startReadingBinary(final String testBinaryPath) {
        this.testBinaryPath = testBinaryPath;
        executorService.submit(this::readBinary);
    }

    public void startAllTests() {
        executorService.submit(this::runAllTests);
    }

    public void stopAllProcesses() {
        // FIXME: processRunner might be null if readBinary has not started yet.
        processRunner.cancel();
    }

    private void readBinary() {
        final Collection<String> lines = new ArrayList<>();
        final TestOutputParser testOutputParser = new TestOutputParser(null);

        processRunner = new ProcessRunner();
        try {
            final int exitCode = processRunner.start(new String[]{testBinaryPath, "--gtest_list_tests"}, lines::add);
            if (exitCode == 0) {
                testCases.set(testOutputParser.parseTestList(lines));
                System.out.println(testCases.get());

                testStates.clear();
                testCases.get().stream().map(TestState::new).forEach(testStates::add);

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
            @Override
            public void onOutLine(final String line) {
                setChanged();
                notifyObservers(new Event(Event.Type.OUT_LINE, line));
            }

            @Override
            public void onErrLine(final String line) {
                System.out.println(line);
            }

            @Override
            public void onTestState(final TestRunState state, final String testCaseName, final String testName) {
                final String fullName = testCaseName + "." + testName;
                testStates.stream()
                        .filter(testState -> testState.getFullName().equals(fullName))
                        .limit(1)
                        .forEach(testState -> testState.setState(state));
                setChanged();
                notifyObservers(new Event(Event.Type.TEST_STATE_CHANGED, fullName));
            }
        });

        processRunner = new ProcessRunner();
        try {
            // TODO: Handle the exit code.
            processRunner.start(new String[]{testBinaryPath}, testOutputParser::parseString);
            setChanged();
            notifyObservers(new Event(Event.Type.TEST_RUN_FINISHED));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
