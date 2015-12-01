package com.neutrinoproject.testrunner;

import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

/**
 * Created by btv on 30.11.15.
 */
public class TestOutputParserTest {

    @Test
    public void testParseString() throws Exception {
        final TestEventHandler handler = mock(TestEventHandler.class);
        final TestOutputParser parser = new TestOutputParser(handler);

        final String someText = "some text";
        parser.parseString(someText);
        verify(handler).onOutLine(someText);

        parser.parseString(someText);
        verify(handler, times(2)).onOutLine(someText);

        parser.parseString("[ RUN      ] Neutrino.HasMass");
        verify(handler).onTestState(TestRunState.STARTED, "Neutrino", "HasMass");

        // Starting a new test before finishing the previous one has no effect.
        parser.parseString("[ RUN      ] Neutrino.HasMass");
        verify(handler, times(1)).onTestState(TestRunState.STARTED, "Neutrino", "HasMass");

        parser.parseString("[       OK ] Neutrino.HasMass (0 ms)");
        verify(handler).onTestState(TestRunState.OK, "Neutrino", "HasMass");

        parser.parseString("[ RUN      ] Neutrino.IsStable");
        verify(handler).onTestState(TestRunState.STARTED, "Neutrino", "IsStable");

        parser.parseString("[  FAILED  ] Neutrino.IsStable (0 ms)");
        verify(handler).onTestState(TestRunState.FAILED, "Neutrino", "IsStable");
    }

    @Test
    public void testParseTestList() throws Exception {
        final TestEventHandler handler = mock(TestEventHandler.class);
        final TestOutputParser parser = new TestOutputParser(handler);

        final String neutrinoTestListString =
                "Running main() from gtest_main.cc\n" +
                        "Neutrino.\n" +
                        "  HasMass\n" +
                        "  IsStable\n" +
                        "UrcaProcess.\n" +
                        "  InvolvesNeutrino\n";

        final Collection<TestOutputParser.TestCase> neutrinoActual =
                parser.parseTestList(Arrays.asList(neutrinoTestListString.split("\n")));
        final Collection<TestOutputParser.TestCase> neutrinoExpected = Arrays.asList(
                new TestOutputParser.TestCase("Neutrino", Arrays.asList("HasMass", "IsStable")),
                new TestOutputParser.TestCase("UrcaProcess", Arrays.asList("InvolvesNeutrino"))
        );
        assertEquals(neutrinoExpected, neutrinoActual);

        final String neutralinoTestListString =
                "Running main() from gtest_main.cc\n" +
                        "Neutralino.\n" +
                        "  Exists\n";

        final Collection<TestOutputParser.TestCase> neutralinoActual =
                parser.parseTestList(Arrays.asList(neutralinoTestListString.split("\n")));
        final Collection<TestOutputParser.TestCase> neutralinoExpected = Arrays.asList(
                new TestOutputParser.TestCase("Neutralino", Arrays.asList("Exists"))
        );
        assertEquals(neutralinoExpected, neutralinoActual);
    }

    @Test(expected=ParseException.class)
    public void testIndexOutOfBoundsException() throws ParseException {
        final String garbage =
                "Running main() from gtest_main.cc\n" +
                        "Neutralino\n" +
                        " --a\n";

        final TestEventHandler handler = mock(TestEventHandler.class);
        final TestOutputParser parser = new TestOutputParser(handler);
        parser.parseTestList(Arrays.asList(garbage.split("\n")));
    }
}