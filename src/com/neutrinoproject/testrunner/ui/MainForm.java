package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestEventHandler;
import com.neutrinoproject.testrunner.TestOutputParser;
import com.neutrinoproject.testrunner.TestRunState;
import com.neutrinoproject.testrunner.process.ProcessEventHandler;
import com.neutrinoproject.testrunner.process.ProcessRunner;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Created by btv on 02.12.15.
 */
public class MainForm {
    private JFrame mainFrame;
    private JPanel mainPanel;

    private JButton loadTestBinaryButton;
    private JTextField testBinaryPathField;

    private JButton runAllTestsButton;
    private JButton runSelectedButton;
    private JButton runFailedButton;
    private JButton stopButton;

    private JTable testOutputTable;
    private JTextArea rawOutputArea;

    private JLabel statusLabel;

    private JProgressBar progressBar;

    private final Model model = new Model();

    public void initForm() {
        mainFrame = new JFrame();

        mainFrame.setContentPane(mainPanel);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mainFrame.setTitle("TestRunner");
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);
//        mainFrame.pack();

        loadTestBinaryButton.addActionListener(this::onLoadTestBinary);
    }

    public void showForm() {
        SwingUtilities.invokeLater(() -> mainFrame.setVisible(true));
    }

    private void onLoadTestBinary(final ActionEvent event) {
        final JFileChooser fileChooser = new JFileChooser();
        final int returnVal = fileChooser.showOpenDialog(mainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final String path = fileChooser.getSelectedFile().getPath();
            testBinaryPathField.setText(path);
            model.testBinaryPath = path;
            setLoadingProgressForButtons(true);
            statusLabel.setText("Loading binary...");
            model.startReadingBinary();
        }
    }

    private void setLoadingProgressForButtons(boolean loading) {
        loadTestBinaryButton.setEnabled(!loading);
        runAllTestsButton.setEnabled(!loading);
        runSelectedButton.setEnabled(!loading);
        runFailedButton.setEnabled(!loading);
        stopButton.setEnabled(loading);
    }

    private class Model {
        private String testBinaryPath;
        private ProcessRunner readingBinaryProcess;

        private void startReadingBinary() {
            readingBinaryProcess = new ProcessRunner();
            try {
                final TestOutputParser parser = new TestOutputParser(new TestEventHandler() {
                    @Override
                    public void onOutLine(final String line) {
                        SwingUtilities.invokeLater(() -> {
                            rawOutputArea.append(line);
                            rawOutputArea.append("\n");
                            rawOutputArea.setCaretPosition(rawOutputArea.getDocument().getLength());
                        });
                    }

                    @Override
                    public void onErrLine(final String line) {

                    }

                    @Override
                    public void onTestState(final TestRunState testState, final String testCaseName, final String testName) {

                    }

                    @Override
                    public void onExitCode(final int exitCode) {

                    }
                });

                readingBinaryProcess.start(new String[]{testBinaryPath, "--gtest_list_tests"}, new ProcessEventHandler() {
//                readingBinaryProcess.start(new String[]{"ping", "ya.ru"}, new ProcessEventHandler() {
                    @Override
                    public void onOutLine(final String line) {
                        parser.parseString(line);
                    }

                    @Override
                    public void onExitCode(final int exitCode) {
                        // TODO: Handle the exit code.
                    }
                });
            } catch (IOException e) {
                // TODO: Handle the exception.
                e.printStackTrace();
            }
        }
    }
}
