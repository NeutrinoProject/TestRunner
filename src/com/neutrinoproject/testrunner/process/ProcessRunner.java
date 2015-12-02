package com.neutrinoproject.testrunner.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by btv on 01.12.15.
 */
public class ProcessRunner {
    private Process process;

    public void start(final String[] command, final ProcessEventHandler handler) throws IOException {
        process = new ProcessBuilder(command).redirectErrorStream(true).start();

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
    }

//    public void waitFor() throws InterruptedException, ExecutionException {
//        processFuture.get();
//    }

    public void cancel() {
        process.destroy();
    }
}
