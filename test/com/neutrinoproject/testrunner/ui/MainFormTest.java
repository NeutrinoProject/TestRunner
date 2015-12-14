package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestExecutorService;
import com.neutrinoproject.testrunner.TestRunState;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JTextComponentMatcher;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.data.TableCellFinder;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.*;
import org.assertj.swing.timing.Timeout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.swing.timing.Pause.pause;

/**
 * Created by btv on 12.12.15.
 */
public class MainFormTest {
    private static final String HANG_ON_TEST_BINARY = "/home/btv/a";
    private static final String BAD_TEST_BINARY = "bad";
    private static final String OK_TEST_BINARY = "/home/btv/b";
    private static final String FAILED_TEST_BINARY = "/home/btv/f";
    private static final Collection<String> TEST_NAMES = Arrays.asList("test1", "test2", "test3");

    private FrameFixture window;
    private JButtonFixture stopButton;
    private JButtonFixture loadBinaryButton;
    private JButtonFixture runAllTestsButton;
    private JButtonFixture runFailedTestsButton;

    // A real file required to select for JFileChooser.
    private File resourceFile;
    private String currentTestBinaryPath;

    private class ManuelTestExecutorService implements TestExecutorService {
        private final Collection<TestRunnerHandler> testRunnerHandlers = new ArrayList<>();

        @Override
        public void submitTestRun(final String testBinaryPath, Collection<String> testNames) {
            if (testNames.isEmpty()) {
                testNames = TEST_NAMES;
            }

            testRunnerHandlers.stream().forEach(TestRunnerHandler::onTestRunStart);

            switch (currentTestBinaryPath) {
                case OK_TEST_BINARY:
                    testRunnerHandlers.stream().forEach(handler -> handler.onOutputLine(null, "Overall output begin"));
                    testNames.stream().forEach(testName -> {
                        testRunnerHandlers.stream().forEach(handler ->
                                handler.onTestStateChange(testName, TestRunState.RUNNING));
                        testRunnerHandlers.stream().forEach(handler ->
                                handler.onOutputLine(testName, "Test output1"));
                        testRunnerHandlers.stream().forEach(handler ->
                                handler.onOutputLine(testName, "Test output2"));
                        testRunnerHandlers.stream().forEach(handler ->
                                handler.onTestStateChange(testName, TestRunState.OK));
                    });
                    testRunnerHandlers.stream().forEach(handler -> handler.onOutputLine(null, "Overall output end"));
                    testRunnerHandlers.stream().forEach(handler -> handler.onTestRunFinished(true));
                    break;

                case FAILED_TEST_BINARY:
                    if (testNames.size() == 1) {
                        final String testName = testNames.iterator().next();
                        testRunnerHandlers.stream().forEach(handler ->
                                handler.onTestStateChange(testName, TestRunState.RUNNING));
                        testRunnerHandlers.stream().forEach(handler ->
                                handler.onOutputLine(testName, "Success"));
                        testRunnerHandlers.stream().forEach(handler ->
                                handler.onTestStateChange(testName, TestRunState.OK));
                        testRunnerHandlers.stream().forEach(handler -> handler.onTestRunFinished(true));
                    } else {
                        testRunnerHandlers.stream().forEach(handler -> handler.onOutputLine(null, "Overall output begin"));

                        testNames.stream().limit(2).forEach(testName -> {
                            testRunnerHandlers.stream().forEach(handler ->
                                    handler.onTestStateChange(testName, TestRunState.RUNNING));
                            testRunnerHandlers.stream().forEach(handler ->
                                    handler.onTestStateChange(testName, TestRunState.OK));
                        });
                        testNames.stream().skip(2).forEach(testName -> {
                            testRunnerHandlers.stream().forEach(handler ->
                                    handler.onTestStateChange(testName, TestRunState.RUNNING));
                            testRunnerHandlers.stream().forEach(handler ->
                                    handler.onOutputLine(testName, "ERROR"));
                            testRunnerHandlers.stream().forEach(handler ->
                                    handler.onTestStateChange(testName, TestRunState.FAILED));
                        });
                        testRunnerHandlers.stream().forEach(handler -> handler.onOutputLine(null, "Overall output end"));
                        testRunnerHandlers.stream().forEach(handler -> handler.onTestRunFinished(false));
                    }
                    break;
            }

        }

        @Override
        public void submitReadBinary(final String testBinaryPath) {
            switch (currentTestBinaryPath) {
                case BAD_TEST_BINARY:
                    testRunnerHandlers.stream().forEach(handler ->
                            handler.onTestsLoadingFinished(false, Collections.emptyList()));
                    break;
                case HANG_ON_TEST_BINARY:
                    break;
                case OK_TEST_BINARY:
                    // Fall through.
                case FAILED_TEST_BINARY:
                    testRunnerHandlers.stream().forEach(handler ->
                            handler.onTestsLoadingFinished(true, TEST_NAMES));
                    break;
            }
        }

        @Override
        public void stop() {
        }

        public void addTestRunnerHandler(final TestRunnerHandler testRunnerHandler) {
            this.testRunnerHandlers.add(testRunnerHandler);
        }
    }

    @Before
    public void setUp() {
        final TestRunnerModelImpl model = new TestRunnerModelImpl();
        final MainForm mainForm = new MainForm();
        final ManuelTestExecutorService testExecutorService = new ManuelTestExecutorService();

        testExecutorService.addTestRunnerHandler(model);
        testExecutorService.addTestRunnerHandler(mainForm);
        mainForm.setTestRunnerModel(model);
        mainForm.setTestExecutorService(testExecutorService);

        mainForm.initForm();

        final Frame frame = GuiActionRunner.execute(new GuiQuery<Frame>() {
            protected Frame executeInEDT() {
                return mainForm.getMainFrame();
            }
        });
        window = new FrameFixture(frame);
        window.show(); // shows the frame to test

        stopButton = window.button(JButtonMatcher.withText("Stop"));
        loadBinaryButton = window.button(JButtonMatcher.withText("Load Test Binary"));
        runAllTestsButton = window.button(JButtonMatcher.withText("Run All Tests"));
        runFailedTestsButton = window.button(JButtonMatcher.withText("Run Failed"));

        resourceFile = new File(Thread.currentThread().getContextClassLoader().getResource("neutrino").getFile());
    }

    @Test
    public void stopLoadingBinary() {
        loadBinaryButton.click();

        currentTestBinaryPath = HANG_ON_TEST_BINARY;
        window.fileChooser().selectFile(resourceFile).approve();

        loadBinaryButton.requireDisabled();
        runAllTestsButton.requireDisabled();
        runFailedTestsButton.requireDisabled();
        stopButton.requireEnabled();

        stopButton.click();
        stopButton.requireDisabled();
        loadBinaryButton.requireEnabled(Timeout.timeout(100));
    }

    @Test
    public void runOkTests() {
//        window.maximize();
        window.splitPane().moveDividerTo(100);

        loadBinaryButton.click();
        currentTestBinaryPath = OK_TEST_BINARY;
        window.fileChooser().selectFile(resourceFile).approve();
        runFailedTestsButton.requireDisabled();
        runAllTestsButton.requireEnabled(Timeout.timeout(100));
        window.textBox(JTextComponentMatcher.withText(resourceFile.getPath())).requireNotEditable();

        final JTextComponentFixture rawOutput = window.textBox(JTextComponentMatcher.withText(""));

        runAllTestsButton.click();
        runAllTestsButton.requireEnabled(Timeout.timeout(100));

        window.label().requireText("OK. 3 tests passed.");

        final String[][] expectedContent = {
                {"", "<Overall>"},
                {"OK", "test1"},
                {"OK", "test2"},
                {"OK", "test3"},
        };
        window.table().requireContents(expectedContent);

        final String testOutput = "Test output1\nTest output2\n";

        for (int i = 0; i < TEST_NAMES.size(); ++i) {
            window.table().cell(at(i + 1, 1)).click();
            rawOutput.requireText(testOutput);
        }

        window.table().cell(at(0, 1)).click();
        rawOutput.requireText(
                "Overall output begin\n" +
                        "Test output1\n" +
                        "Test output2\n" +
                        "Test output1\n" +
                        "Test output2\n" +
                        "Test output1\n" +
                        "Test output2\n" +
                        "Overall output end\n"
        );
    }

    @Test
    public void runFailedTests() {
//        window.maximize();
        window.splitPane().moveDividerTo(100);

        loadBinaryButton.click();
        currentTestBinaryPath = FAILED_TEST_BINARY;
        window.fileChooser().selectFile(resourceFile).approve();
        runFailedTestsButton.requireDisabled();
        runAllTestsButton.requireEnabled(Timeout.timeout(100));

        final JTextComponentFixture rawOutput = window.textBox(JTextComponentMatcher.withText(""));

        runAllTestsButton.click();
        runAllTestsButton.requireEnabled(Timeout.timeout(100));
        runFailedTestsButton.requireEnabled();

//        pause(100000);
        window.label().requireText("FAIL. 2 tests passed, 1 test failed.");

        final String[][] expectedContent = {
                {"", "<Overall>"},
                {"OK", "test1"},
                {"OK", "test2"},
                {"FAILED", "test3"},
        };
        window.table().requireContents(expectedContent);

        window.table().cell(at(0, 1)).click();
        rawOutput.requireText(
                "Overall output begin\n" +
                        "ERROR\n" +
                        "Overall output end\n"
        );
        window.table().cell(at(3, 1)).click();
        rawOutput.requireText("ERROR\n");

        // Run Failed test.
        runFailedTestsButton.click();
        runAllTestsButton.requireEnabled(Timeout.timeout(100));
        runFailedTestsButton.requireDisabled();

        window.label().requireText("OK. 1 test passed.");

        final String[][] expectedContentForFailedTestRun = {
                {"", "<Overall>"},
                {"", "test1"},
                {"", "test2"},
                {"OK", "test3"},
        };
        window.table().requireContents(expectedContentForFailedTestRun);

        window.table().cell(at(0, 1)).click();
        rawOutput.requireText("Success\n");
        window.table().cell(at(3, 1)).click();
        rawOutput.requireText("Success\n");
    }

    @After
    public void tearDown() {
        window.cleanUp();
    }

    private TableCellFinder at(final int row, final int column) {
        return (jTable, jTableCellReader) -> TableCell.row(row).column(column);
    }
}
