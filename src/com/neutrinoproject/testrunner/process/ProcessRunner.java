package com.neutrinoproject.testrunner.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Created by btv on 01.12.15.
 */
public class ProcessRunner {
    private AtomicReference<Process> process = new AtomicReference<>();

    /**
     * Starts executing command in a separate process.
     * @param command a command to execute
     * @param outLineConsumer a consumer of standard output and standard error process's streams
     * @return exit code of the process
     * @throws IOException
     */
    public int start(final String[] command, final Consumer<String> outLineConsumer) throws IOException {
        final Process localProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
        process.set(localProcess);

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                outLineConsumer.accept(line);
            }

            return localProcess.waitFor();
        } catch (IOException e) {
            // Handle the exception only if `process` is not null. Otherwise it means that the process has been killed.
            if (process.get() != null) {
                throw new IOException(e);
            }
        } catch (InterruptedException ignore) {}
        return -1;
    }

    /**
     * Cancel the process. This is a thread-safe call.
     */
    public void cancel() {
        final Process localProcess = process.getAndSet(null);
        if (localProcess != null) {
            localProcess.destroy();
        }
    }
}
