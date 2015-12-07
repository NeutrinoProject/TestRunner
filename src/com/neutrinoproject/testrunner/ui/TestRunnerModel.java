package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestRunState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by btv on 07.12.15.
 */
public interface TestRunnerModel {

    /**
     * Non blocking call starts reading a test binary.
     * {@code TestRunnerHandler.onTestsLoadingFinished} will be called after loading the test binary.
     * @param testBinaryPath a path to a test binary to read
     */
    void startReadingBinary(String testBinaryPath);

    /**
     * Non blocking call starts all test from the loaded test binary.
     * {@code TestRunnerHandler.onOutputLine}, {@code TestRunnerHandler.onTestStateChange},
     * {@code TestRunnerHandler.onTestRunFinished} will be called during it's execution.
     */
    void startAllTests();

    /**
     * Non blocking call starts tests specified in {@code testNames} from the loaded test binary.
     * {@code TestRunnerHandler.onOutputLine}, {@code TestRunnerHandler.onTestStateChange},
     * {@code TestRunnerHandler.onTestRunFinished} will be called during it's execution.
     * @param testNames a list of test to run
     */
    void startTests(Collection<String> testNames);

    /**
     * Stops all processes run from {@code startReadingBinary}, {@code startAllTests}, and {@code startTests}.
     */
    void stopAllProcesses();

    /**
     * Returns a list of test names existent in the loaded test binary.
     * @return a list of test names
     */
    @NotNull
    Collection<String> getTestNames();

    /**
     * Returns a list of the overall test run output.
     * @return out and err output
     */
    @NotNull
    Collection<String> getOverallTestOutput();

    /**
     * Returns state of particular test.
     * @param testName a name of test
     * @return test state
     */
    @NotNull
    Optional<TestRunState> getTestState(String testName);

    /**
     * Returns a list of specified test run output.
     * @param testName a name of test
     * @return out and err output
     */
    @NotNull
    Collection<String> getTestOutput(String testName);

}
