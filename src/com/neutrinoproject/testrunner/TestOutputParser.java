package com.neutrinoproject.testrunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by btv on 30.11.15.
 */
public class TestOutputParser {
    private final Pattern pattern = Pattern.compile("\\[(.{10})\\] (\\w+)\\.(\\w+).*");
    private final TestEventHandler testEventHandler;
    private TestRunState testRunState;

    public TestOutputParser(final TestEventHandler testEventHandler) {
        this.testEventHandler = testEventHandler;
    }

    public void parseString(final String line) {
        if (line.startsWith("[ ")) {
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                final String outputType = matcher.group(1);
                final String testCaseName = matcher.group(2);
                final String testName = matcher.group(3);

                switch (outputType) {
                    case " RUN      ":
                        if (testRunState != TestRunState.STARTED) {
                            testRunState = TestRunState.STARTED;
                            testEventHandler.onTestState(TestRunState.STARTED, testCaseName, testName);
                            testEventHandler.onOutLine(line);
                        }
                        return;
                    case "       OK ":
                        if (testRunState == TestRunState.STARTED) {
                            testRunState = TestRunState.OK;
                            testEventHandler.onOutLine(line);
                            testEventHandler.onTestState(TestRunState.OK, testCaseName, testName);
                        }
                        return;
                    case "  FAILED  ":
                        if (testRunState == TestRunState.STARTED) {
                            testRunState = TestRunState.FAILED;
                            testEventHandler.onOutLine(line);
                            testEventHandler.onTestState(TestRunState.FAILED, testCaseName, testName);
                        }
                        return;
                }
            }
        }
        testEventHandler.onOutLine(line);
    }
}
