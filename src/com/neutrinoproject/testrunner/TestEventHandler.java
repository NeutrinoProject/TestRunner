package com.neutrinoproject.testrunner;

/**
 * Created by btv on 30.11.15.
 */
public interface TestEventHandler {
    void onOutLine(String line);

    void onErrLine(String line);

    void onTestState(TestRunState testState, String testCaseName, String testName);

}
