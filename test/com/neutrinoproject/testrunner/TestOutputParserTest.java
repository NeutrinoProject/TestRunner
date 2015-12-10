package com.neutrinoproject.testrunner;

import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by btv on 30.11.15.
 */
public class TestOutputParserTest {

    @Test
    public void testParseOutputLine() throws Exception {
        final TestOutputParser parser = new TestOutputParser();

        assertNull(parser.parseOutputLine("some text"));
        assertNull(parser.parseOutputLine("some text"));

        assertEquals(parser.parseOutputLine("[ RUN      ] Neutrino.HasMass"),
                buildResult("Neutrino.HasMass", TestRunState.RUNNING));

        // Starting a new test before finishing the previous one has no effect.
        assertNull(parser.parseOutputLine("[ RUN      ] Neutrino.HasMass"));

        assertNull(parser.parseOutputLine("another text"));

        assertEquals(parser.parseOutputLine("[       OK ] Neutrino.HasMass (0 ms)"),
                buildResult("Neutrino.HasMass", TestRunState.OK));

        assertEquals(parser.parseOutputLine("[ RUN      ] Neutrino.IsStable"),
                buildResult("Neutrino.IsStable", TestRunState.RUNNING));

        assertEquals(parser.parseOutputLine("[  FAILED  ] Neutrino.IsStable (0 ms)"),
                buildResult("Neutrino.IsStable", TestRunState.FAILED));
    }

    private TestOutputParser.Result buildResult(final String testName, final TestRunState testState) {
        return new TestOutputParser.Result(testName, testState);
    }

    @Test
    public void testParseTestList() throws Exception {
        final TestOutputParser parser = new TestOutputParser();

        final String neutrinoTestListString =
                "Running main() from gtest_main.cc\n" +
                        "Neutrino.\n" +
                        "  HasMass\n" +
                        "  IsStable\n" +
                        "UrcaProcess.\n" +
                        "  InvolvesNeutrino\n";

        final Collection<String> neutrinoActual =
                parser.parseTestList(Arrays.asList(neutrinoTestListString.split("\n")));

        final Collection<String> neutrinoExpected =
                Arrays.asList("Neutrino.HasMass", "Neutrino.IsStable", "UrcaProcess.InvolvesNeutrino");
        assertEquals(neutrinoExpected, neutrinoActual);

        final String neutralinoTestListString =
                "Running main() from gtest_main.cc\n" +
                        "Neutralino.\n" +
                        "  Exists\n";
        final Collection<String> neutralinoActual =
                parser.parseTestList(Arrays.asList(neutralinoTestListString.split("\n")));
        final Collection<String> neutralinoExpected = Arrays.asList("Neutralino.Exists");
        assertEquals(neutralinoExpected, neutralinoActual);
    }

    @Test(expected = ParseException.class)
    public void testParseException() throws ParseException {
        final String garbage =
                "Running main() from gtest_main.cc\n" +
                        "Neutralino\n" +
                        " --a\n";

        final TestOutputParser parser = new TestOutputParser();
        parser.parseTestList(Arrays.asList(garbage.split("\n")));
    }
}