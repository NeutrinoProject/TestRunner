package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestRunState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

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
     * Returns state of particular test.
     * @param testName a name of test
     * @return test state
     */
    @NotNull
    Optional<TestRunState> getTestState(String testName);

    /**
     * An artificial name of a test representing overall test run output results.
     */
    String OVERALL_TEST_NAME = "";

    /**
     * Subscribe to get output of a test. {@code consumer} will get all previous output of the test and will be given
     * all subsequent output of the test.
     * @param testName a name of the test
     * @param consumer a consumer of the test output
     */
    void subscribeOnTestOutput(String testName, Consumer<String> consumer);

    /**
     * Unsubscribe all subscriber of a test output.
     * @param testName a name of the test
     */
    void unsubscribeAllOnTestOutput(String testName);

}
