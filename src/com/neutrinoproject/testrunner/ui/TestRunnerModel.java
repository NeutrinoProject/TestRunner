package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestRunState;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by btv on 07.12.15.
 */
public interface TestRunnerModel {

    void startReadingBinary(String testBinaryPath);

    void startAllTests();

    void startTests(Collection<String> testNames);

    void stopAllProcesses();

    Collection<String> getTestNames();

    Collection<String> getOverallTestOutput();

    Optional<TestRunState> getTestState(String testName);

    Collection<String> getTestOutput(String testName);

}
