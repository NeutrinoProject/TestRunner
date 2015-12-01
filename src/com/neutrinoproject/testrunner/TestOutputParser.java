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

    public Collection<TestCase> parseTestList(final Collection<String> lines) throws ParseException {
        final Collection<TestCase> result = new ArrayList<>();

        String testCaseName = null;
        Collection<String> testNames = new ArrayList<>();
        for (final String line : lines) {
            final Matcher testCaseMatcher = testListTestCasePattern.matcher(line);
            if (testCaseMatcher.find()) {
                // Add previous test case.
                if (testCaseName != null) {
                    result.add(createTestCase(testCaseName, testNames));
                }

                testCaseName = testCaseMatcher.group(1);
                testNames = new ArrayList<>();
            }

            final Matcher testNameMatcher = testListTestNamePattern.matcher(line);
            if (testNameMatcher.find()) {
                testNames.add(testNameMatcher.group(1));
            }
        }

        // Add the last test case.
        result.add(createTestCase(testCaseName, testNames));

        return result;
    }

    private TestCase createTestCase(final String name, final Collection<String> tests) throws ParseException {
        if (tests.isEmpty()) {
            throw new ParseException("Empty test case", 0);
        }
        return new TestCase(name, tests);
    }

    public static class TestCase {
        public final String name;
        public final Collection<String> tests;

        public TestCase(final String name, final Collection<String> tests) {
            this.name = name;
            this.tests = tests;
        }

        @Override
        public String toString() {
            return "TestCase{" + name + ": " + tests + '}';
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final TestCase testCase = (TestCase) o;

            if (name != null ? !name.equals(testCase.name) : testCase.name != null) return false;
            return !(tests != null ? !tests.equals(testCase.tests) : testCase.tests != null);

        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (tests != null ? tests.hashCode() : 0);
            return result;
        }
    }
}
