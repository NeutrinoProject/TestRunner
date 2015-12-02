package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestEventHandler;
import com.neutrinoproject.testrunner.TestOutputParser;
import com.neutrinoproject.testrunner.TestRunState;
import com.neutrinoproject.testrunner.process.ProcessEventHandler;
import com.neutrinoproject.testrunner.process.ProcessRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by btv on 02.12.15.
 */
public class ProcessRunnerModel extends Observable {
    private final ExecutorService executorService;
    private final AtomicReference<Collection<TestOutputParser.TestCase>> testCases;

    private String testBinaryPath;
    private ProcessRunner processRunner;


    public enum Event {
        TEST_CASES_LOADED,
        ERROR,
    }

    public ProcessRunnerModel() {
        this.executorService = Executors.newFixedThreadPool(1);
        testCases = new AtomicReference<>();
    }

    public Collection<TestOutputParser.TestCase> getTestCases() {
        return testCases.get();
    }

    public void startReadingBinary(final String testBinaryPath) {
        this.testBinaryPath = testBinaryPath;
        executorService.submit(this::readBinary);
    }

    public void stopAllProcesses() {
        processRunner.cancel();
    }

    private void readBinary() {
        final Collection<String> lines = new ArrayList<>();
        final TestOutputParser testOutputParser = new TestOutputParser(null);

        processRunner = new ProcessRunner();
        try {
            processRunner.start(new String[]{testBinaryPath, "--gtest_list_tests"}, new ProcessEventHandler() {
                //            processRunner.start(new String[]{"ping", "ya.ru"}, new ProcessEventHandler() {
                @Override
                public void onOutLine(final String line) {
                    lines.add(line);
                    System.out.println(line);
                }

                @Override
                public void onExitCode(final int exitCode) {
                    if (exitCode == 0) {
                        try {
                            testCases.set(testOutputParser.parseTestList(lines));
                            System.out.println(testCases.get());
                            setChanged();
                            notifyObservers(Event.TEST_CASES_LOADED);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // TODO: Handle the exit code.
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
