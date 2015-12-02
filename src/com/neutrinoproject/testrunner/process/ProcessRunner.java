package com.neutrinoproject.testrunner.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

/**
 * Created by btv on 01.12.15.
 */
public class ProcessRunner {
    private Process process;
    private FutureTask<Void> processFuture;

    public void start(final String[] command, final ProcessEventHandler handler) throws IOException {
//        isProcessRunning.set(true);
        process = new ProcessBuilder(command).redirectErrorStream(true).start();

        processFuture = new FutureTask<>(() -> {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    handler.onOutLine(line);
                }

                final int exitCode = process.waitFor();
                handler.onExitCode(exitCode);
            } catch (IOException e) {
                // TODO: Handle this exception.
                e.printStackTrace();
            } catch (InterruptedException ignore) {}
            return null;
        });

        processFuture.run();
    }

    public void waitFor() throws InterruptedException, ExecutionException {
        processFuture.get();
    }

    public void cancel() {
        processFuture.cancel(true);
    }
}
