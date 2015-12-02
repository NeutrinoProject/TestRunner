package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.process.ProcessEventHandler;
import com.neutrinoproject.testrunner.process.ProcessRunner;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by btv on 02.12.15.
 */
public class ProcessRunnerModel {
    private final ExecutorService executorService;
    private String testBinaryPath;
    private ProcessRunner processRunner;

    public ProcessRunnerModel() {
        this.executorService = Executors.newFixedThreadPool(1);
    }

    public void startReadingBinary(final String testBinaryPath) {
        this.testBinaryPath = testBinaryPath;
        executorService.submit(this::readBinary);
    }

    public void stopAllProcesses() {
        processRunner.cancel();
    }

    private void readBinary() {
        try {
            processRunner = new ProcessRunner();
            processRunner.start(new String[]{testBinaryPath, "--gtest_list_tests"}, new ProcessEventHandler() {
//            processRunner.start(new String[]{"ping", "ya.ru"}, new ProcessEventHandler() {
                @Override
                public void onOutLine(final String line) {
                    System.out.println(line);
                    //                parser.parseString(line);
                }

                @Override
                public void onExitCode(final int exitCode) {
                    // TODO: Handle the exit code.
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
