package com.neutrinoproject.testrunner.process;

/**
 * Created by btv on 01.12.15.
 */
public interface ProcessEventHandler {
    void onOutLine(String line);

    void onExitCode(int exitCode);
}
