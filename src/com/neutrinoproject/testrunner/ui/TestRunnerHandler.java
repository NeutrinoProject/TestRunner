package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestRunState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by btv on 07.12.15.
 */
public interface TestRunnerHandler {

    /**
     * Callback method is called on finishing loading of a test binary.
     * @param success an indicator whether loading succeed or not
     * @param testNames a list of test names read from the test binary
     */
    void onTestsLoadingFinished(boolean success, final Collection<String> testNames);

    /**
     * Callback method is called on starting test run.
     */
    void onTestRunStart();

    /**
     * Callback method is called on a new line from out or err stream of a test binary.
     * @param testName a test the line belongs
     * @param overallLineIndex an index of the line of overall test run
     * @param testLineIndex an index of the line of particular test run
     * @param outputLine an output line
     */
    void onOutputLine(@Nullable String testName, int overallLineIndex, int testLineIndex, String outputLine);

    /**
     * Callback method is called on changing state of a test.
     * @param testName a test which state has been changed
     * @param newState new state of the test
     */
    void onTestStateChange(String testName, TestRunState newState);

    /**
     * Callback method is called on finishing test run.
     * @param success an indicator whether overall test run succeed or not
     */
    void onTestRunFinished(boolean success);

}
