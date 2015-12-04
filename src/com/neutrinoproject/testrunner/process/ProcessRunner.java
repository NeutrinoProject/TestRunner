package com.neutrinoproject.testrunner.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * Created by btv on 01.12.15.
 */
public class ProcessRunner {
    // FIXME: Make this reference atomic.
    private Process process;

    public int start(final String[] command, final Consumer<String> outLineConsumer) throws IOException {
        process = new ProcessBuilder(command).redirectErrorStream(true).start();

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                outLineConsumer.accept(line);
            }

            return process.waitFor();
        } catch (IOException e) {
            // TODO: Handle this exception.
            e.printStackTrace();
        } catch (InterruptedException ignore) {}
        return -1;
    }

    public void cancel() {
        process.destroy();
    }
}
