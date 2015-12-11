package com.neutrinoproject.testrunner;

import java.util.Collection;

/**
 * Created by btv on 11.12.15.
 */
public interface TestExecutorService {

    /**
     * Non blocking call starts tests specified in {@code testNames} from the loaded test binary.
     * {@code TestRunnerHandler.onOutputLine}, {@code TestRunnerHandler.onTestStateChange},
     * {@code TestRunnerHandler.onTestRunFinished} will be called during it's execution.
     * @param testNames a list of test to run
     */
    void submitTestRun(String testBinaryPath, Collection<String> testNames);

    /**
     * Non blocking call starts reading a test binary.
     * {@code TestRunnerHandler.onTestsLoadingFinished} will be called after loading the test binary.
     * @param testBinaryPath a path to a test binary to read
     */
    void submitReadBinary(String testBinaryPath);

    /**
     * Stops all processes run from {@code submitReadBinary} or {@code submitTestRun}.
     */
    void stop();

}

