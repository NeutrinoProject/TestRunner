package com.neutrinoproject.testrunner;

import com.neutrinoproject.testrunner.ui.GTestRunnerModel;
import com.neutrinoproject.testrunner.ui.MainForm;

public class Main {

    public static void main(String[] args) {
        final GTestRunnerModel model = new GTestRunnerModel();
        final MainForm mainForm = new MainForm();

        mainForm.setTestRunnerModel(model);
        model.setTestRunnerHandler(mainForm);

        mainForm.initForm();
        mainForm.showForm();
    }
}
