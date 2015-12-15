package com.neutrinoproject.testrunner;

import com.neutrinoproject.testrunner.ui.TestRunnerModelImpl;
import com.neutrinoproject.testrunner.ui.MainForm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.*;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        setupLogging();

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

    private static void setupLogging() {
        final URL logConfigFilePath = Main.class.getResource("logging.properties");
        if (logConfigFilePath != null) {
            try (final InputStream inputStream = new FileInputStream(logConfigFilePath.getPath())) {
                LogManager.getLogManager().readConfiguration(inputStream);
            } catch (final IOException e) {
                System.err.println("Could no read log configuration from " + logConfigFilePath);
                e.printStackTrace();
            }
        } else {
            Stream.of(Logger.getLogger("").getHandlers()).forEach(handler -> handler.setLevel(Level.ALL));
            Logger.getLogger("com.neutrinoproject.testrunner").setLevel(Level.WARNING);
        }
    }
}
