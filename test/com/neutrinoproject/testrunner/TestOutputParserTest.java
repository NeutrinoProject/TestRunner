package com.neutrinoproject.testrunner;

import org.junit.Test;

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
}