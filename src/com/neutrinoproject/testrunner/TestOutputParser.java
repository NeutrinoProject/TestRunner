package com.neutrinoproject.testrunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by btv on 30.11.15.
 */
public class TestOutputParser {
    private final Pattern testRunOutputPattern = Pattern.compile("^\\[(.{10})\\] (\\w+)\\.(\\w+).*");
    private final Pattern testListTestCasePattern = Pattern.compile("^(\\w+)\\.$");
    private final Pattern testListTestNamePattern = Pattern.compile("^  (\\w+)$");
    private final TestEventHandler testEventHandler;
    private TestRunState testRunState;

    public TestOutputParser(final TestEventHandler testEventHandler) {
        this.testEventHandler = testEventHandler;
    }

    public void parseString(final String line) {
        if (line.startsWith("[ ")) {
            final Matcher matcher = testRunOutputPattern.matcher(line);
            if (matcher.find()) {
                final String outputType = matcher.group(1);
                final String testName = matcher.group(2) + "." + matcher.group(3);

                switch (outputType) {
                    case " RUN      ":
                        if (testRunState != TestRunState.RUNNING) {
                            testRunState = TestRunState.RUNNING;
                            testEventHandler.onTestState(TestRunState.RUNNING, testName);
                            testEventHandler.onOutLine(line);
                        }
                        return;
                    case "       OK ":
                        if (testRunState == TestRunState.RUNNING) {
                            testRunState = TestRunState.OK;
                            testEventHandler.onOutLine(line);
                            testEventHandler.onTestState(TestRunState.OK, testName);
                        }
                        return;
                    case "  FAILED  ":
                        if (testRunState == TestRunState.RUNNING) {
                            testRunState = TestRunState.FAILED;
                            testEventHandler.onOutLine(line);
                            testEventHandler.onTestState(TestRunState.FAILED, testName);
                        }
                        return;
                }
            }
        }
        testEventHandler.onOutLine(line);
    }

    public Collection<String> parseTestList(final Collection<String> lines) throws ParseException {
        final Collection<String> result = new ArrayList<>();

        String testCaseName = null;
        for (final String line : lines) {
            final Matcher testCaseMatcher = testListTestCasePattern.matcher(line);
            if (testCaseMatcher.find()) {
                testCaseName = testCaseMatcher.group(1) + ".";
            }

            final Matcher testNameMatcher = testListTestNamePattern.matcher(line);
            if (testNameMatcher.find()) {
                if (testCaseName == null) {
                    throw new ParseException("Empty test case", 0);
                }
                result.add(testCaseName + testNameMatcher.group(1));
            }
        }

        if (result.isEmpty()) {
            throw new ParseException("Empty test case", 0);
        }

        return result;
    }

    private String buildFullTestName(final String testCaseName, final String testName) {
        return testCaseName + "." + testName;
    }
}
