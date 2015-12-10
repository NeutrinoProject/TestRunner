package com.neutrinoproject.testrunner;

import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by btv on 30.11.15.
 */
public class TestOutputParser {
    private final Pattern testRunOutputPattern = Pattern.compile("^\\[(.{10})\\] (\\w+\\.\\w+).*");
    private final Pattern testListTestCasePattern = Pattern.compile("^(\\w+)\\.$");
    private final Pattern testListTestNamePattern = Pattern.compile("^  (\\w+)$");
    private TestRunState testRunState;

    public static class Result {
        public final String testName;
        public final TestRunState testState;

        public Result(final String testName, final TestRunState testState) {
            this.testName = testName;
            this.testState = testState;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Result result = (Result) o;

            if (!testName.equals(result.testName)) return false;
            return testState == result.testState;
        }

        @Override
        public int hashCode() {
            int result = testName.hashCode();
            result = 31 * result + testState.hashCode();
            return result;
        }
    }

    @Nullable
    public Result parseOutputLine(final String line) {
        if (line.startsWith("[ ")) {
            final Matcher matcher = testRunOutputPattern.matcher(line);
            if (matcher.find()) {
                final String outputType = matcher.group(1);
                final String testName = matcher.group(2);

                switch (outputType) {
                    case " RUN      ":
                        if (testRunState != TestRunState.RUNNING) {
                            testRunState = TestRunState.RUNNING;
                            return new Result(testName, testRunState);
                        }
                        break;
                    case "       OK ":
                        if (testRunState == TestRunState.RUNNING) {
                            testRunState = TestRunState.OK;
                            return new Result(testName, testRunState);
                        }
                        break;
                    case "  FAILED  ":
                        if (testRunState == TestRunState.RUNNING) {
                            testRunState = TestRunState.FAILED;
                            return new Result(testName, testRunState);
                        }
                        break;
                }
            }
        }
        return null;
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
}
