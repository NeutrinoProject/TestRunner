package com.neutrinoproject.testrunner;

import com.neutrinoproject.testrunner.process.ProcessRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by btv on 01.12.15.
 */
public class RunBinariesTest {
    @Test
    public void testRunNeutrinoBinary() throws IOException, ExecutionException, InterruptedException {
        final ProcessRunner processRunner = new ProcessRunner();
        processRunner.start(new String[]{getPathToResource("neutrino"), "--gtest_list_tests"}, System.out::println);

        processRunner.waitFor();
    }

    @Test
    public void testRunNeutralinoBinary() throws IOException, ExecutionException, InterruptedException {
        final ProcessRunner processRunner = new ProcessRunner();
        processRunner.start(new String[]{getPathToResource("neutralino")}, System.out::println);

        processRunner.waitFor();
    }

    private String getPathToResource(final String resourceName) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (url == null) {
            throw new RuntimeException("Resource '" + resourceName + "' not found");
        }
        return url.getPath();
    }
}
