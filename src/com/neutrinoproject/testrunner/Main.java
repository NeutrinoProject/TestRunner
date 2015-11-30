package com.neutrinoproject.testrunner;

public class Main {

    public static void main(String[] args) {
        final TestEventHandler handler = new TestEventHandler() {
            @Override
            public void onOutLine(final String line) {
                System.out.println(" == OUT === " + line);
            }

            @Override
            public void onErrLine(final String line) {
                System.out.println(" == ERR === " + line);
            }

            @Override
            public void onTestState(final TestRunState testState, final String testCaseName, final String testName) {
                System.out.println(testState + ": " + testCaseName + " " + testName);
            }

            @Override
            public void onExitCode(final int exitCode) {
                System.out.println("EXIT CODE: " + exitCode);
            }
        };

    }
}
