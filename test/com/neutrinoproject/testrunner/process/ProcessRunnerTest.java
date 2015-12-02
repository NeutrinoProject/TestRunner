package com.neutrinoproject.testrunner.process;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Created by btv on 01.12.15.
 */
public class ProcessRunnerTest {

    private final int timeoutMillis = 100;

    @Test
    public void testRunEcho() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final String message = "From another process with love";
        final ProcessEventHandler handler = mock(ProcessEventHandler.class);

        processRunner.start(new String[]{"echo", message}, handler);
        verify(handler, timeout(timeoutMillis)).onOutLine(message);
        verify(handler).onExitCode(0);

        processRunner.waitFor();
    }

    @Test
    public void testCancelProcess() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final ProcessEventHandler handler = mock(ProcessEventHandler.class);

        processRunner.start(new String[]{"sleep", "1"}, handler);

        processRunner.cancel();

        processRunner.waitFor();
    }

    @Test
    public void testNonZeroExitCode() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final ProcessEventHandler handler = mock(ProcessEventHandler.class);

        processRunner.start(new String[]{"sleep", "-1"}, handler);
        verify(handler, timeout(timeoutMillis)).onExitCode(1);

        processRunner.waitFor();
    }
}
