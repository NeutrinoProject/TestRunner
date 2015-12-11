package com.neutrinoproject.testrunner;

import com.neutrinoproject.testrunner.ui.TestRunnerModelImpl;
import com.neutrinoproject.testrunner.ui.MainForm;

public class Main {

    public static void main(String[] args) {
        final TestRunnerModelImpl model = new TestRunnerModelImpl();
        final MainForm mainForm = new MainForm();
        final TestExecutorServiceImpl testExecutorService = new TestExecutorServiceImpl();

        testExecutorService.addTestRunnerHandler(model);
        testExecutorService.addTestRunnerHandler(mainForm);
        mainForm.setTestRunnerModel(model);
        mainForm.setTestExecutorService(testExecutorService);

        mainForm.initForm();
        mainForm.showForm();
    }
}
