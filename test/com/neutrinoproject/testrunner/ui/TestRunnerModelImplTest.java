package com.neutrinoproject.testrunner.ui;

import org.junit.Test;

import java.util.Arrays;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by btv on 12.12.15.
 */
public class TestRunnerModelImplTest {

    @Test
    public void testOverallOutputSubscribe() {
        final TestRunnerModelImpl testRunnerModel = new TestRunnerModelImpl();
        testRunnerModel.onTestsLoadingFinished(true, Arrays.asList("test1", "test2"));

        final Consumer<String> consumer = mock(Consumer.class);

        testRunnerModel.onTestRunStart();

        // Overall output.
        testRunnerModel.onOutputLine(null, "overall test output1");
        testRunnerModel.subscribeOnTestOutput(TestRunnerModel.OVERALL_TEST_NAME, consumer);
        verify(consumer).accept("overall test output1");
        testRunnerModel.onOutputLine(null, "overall test output2");
        verify(consumer).accept("overall test output2");

        // Some tests output.
        testRunnerModel.onOutputLine("test1", "test1 output");
        verify(consumer).accept("test1 output");

        testRunnerModel.onOutputLine("test2", "test2 output");
        verify(consumer).accept("test2 output");

        testRunnerModel.onOutputLine(null, "overall test output3");
        verify(consumer).accept("overall test output3");

        verifyZeroInteractions(consumer);

        testRunnerModel.onTestRunFinished(true);
    }

    @Test
    public void testSeparateOutputSubscribe() {
        final TestRunnerModelImpl testRunnerModel = new TestRunnerModelImpl();
        testRunnerModel.onTestsLoadingFinished(true, Arrays.asList("test1", "test2"));

        final Consumer<String> consumer1 = mock(Consumer.class);
        final Consumer<String> consumer2 = mock(Consumer.class);

        // Consumer1 subscribes before test run.
        testRunnerModel.subscribeOnTestOutput("test1", consumer1);
        testRunnerModel.onTestRunStart();

        // Overall output.
        testRunnerModel.onOutputLine(null, "overall test output1");
        testRunnerModel.onOutputLine(null, "overall test output2");

        // Test 1 output.
        testRunnerModel.onOutputLine("test1", "test1 output1");
        verify(consumer1).accept("test1 output1");
        testRunnerModel.onOutputLine("test1", "test1 output2");
        verify(consumer1).accept("test1 output2");

        // Test 2 output.
        testRunnerModel.onOutputLine("test2", "test2 output1");
        testRunnerModel.onOutputLine("test2", "test2 output2");

        // Consumer2 subscribes after test run.
        testRunnerModel.subscribeOnTestOutput("test2", consumer2);
        // But it gets all previous output.
        verify(consumer2).accept("test2 output1");
        verify(consumer2).accept("test2 output2");

        testRunnerModel.onOutputLine("test2", "test2 output3");
        verify(consumer2).accept("test2 output3");

        verifyZeroInteractions(consumer1, consumer2);

        testRunnerModel.onTestRunFinished(true);
    }


    @Test
    public void testUnsubscribe() {
        final TestRunnerModelImpl testRunnerModel = new TestRunnerModelImpl();
        testRunnerModel.onTestsLoadingFinished(true, Arrays.asList("test1", "test2"));

        final Consumer<String> consumer = mock(Consumer.class);

        testRunnerModel.subscribeOnTestOutput("test1", consumer);
        testRunnerModel.onTestRunStart();

        testRunnerModel.onOutputLine("test1", "test1 output1");
        verify(consumer).accept("test1 output1");
        testRunnerModel.onOutputLine("test1", "test1 output2");
        verify(consumer).accept("test1 output2");

        testRunnerModel.unsubscribeAllOnTestOutput("test1");

        testRunnerModel.onOutputLine("test1", "test1 output3");

        verifyZeroInteractions(consumer);

        testRunnerModel.onTestRunFinished(true);
    }
}