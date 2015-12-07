package com.neutrinoproject.testrunner.process;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * Created by btv on 01.12.15.
 */
public class ProcessRunnerTest {
    @Test
    public void testRunEcho() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final String message = "From another process with love";
        final Consumer<String> consumer = mock(Consumer.class);

        final int exitCode = processRunner.start(new String[]{"echo", message}, consumer);
        verify(consumer).accept(message);
        assertEquals(0, exitCode);
    }

    @Test
    public void testCancelProcess() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final CompletableFuture<Void> processStarted = new CompletableFuture<>();
        final Consumer<String> notifierConsumer = s -> processStarted.complete(null);

        Callable<Integer> runnerCallable = () -> processRunner.start(new String[]{"yes"}, notifierConsumer);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Future<Integer> exitCode = executorService.submit(runnerCallable);
        executorService.shutdown();

        // Wait until the consumer gets some call, that means that the process has been started.
        processStarted.join();
        processRunner.cancel();

        assertEquals(-1, exitCode.get().intValue());
    }

    @Test
    public void testNonZeroExitCode() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final Consumer<String> consumer = mock(Consumer.class);

        final int exitCode = processRunner.start(new String[]{"sleep", "-1"}, consumer);
        assertNotEquals(0, exitCode);
    }
}
