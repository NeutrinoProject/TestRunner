package com.neutrinoproject.testrunner.process;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

/**
 * Created by btv on 01.12.15.
 */
public class ProcessRunnerTest {

    private final int timeoutMillis = 100;

    @Test
    public void testRunEcho() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final String message = "From another process with love";
        final Consumer<String> consumer = mock(Consumer.class);

        final int exitCode = processRunner.start(new String[]{"echo", message}, consumer);
        verify(consumer, timeout(timeoutMillis)).accept(message);
        // TODO: Check the exit code.
    }

    @Test
    public void testCancelProcess() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final Consumer<String> consumer = mock(Consumer.class);

//        processRunner.start(new String[]{"sleep", "5"}, handler);
//        processRunner.cancel();
        // TODO: What does this test check?
    }

    @Test
    public void testNonZeroExitCode() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final Consumer<String> consumer = mock(Consumer.class);

        final int exitCode = processRunner.start(new String[]{"sleep", "-1"}, consumer);
        // TODO: Check the exit code.
    }
}
