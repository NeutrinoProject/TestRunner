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
