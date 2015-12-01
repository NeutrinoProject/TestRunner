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

    @Test
    public void testRunEcho() throws IOException, InterruptedException, ExecutionException {
        final ProcessRunner processRunner = new ProcessRunner();
        final String message = "From another process with love";
        final Consumer<String> consumer = mock(Consumer.class);

        processRunner.start(new String[]{"echo", message}, consumer);
        verify(consumer, timeout(100)).accept(message);

        processRunner.waitFor();
    }
}
