package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestOutputParser;
import com.neutrinoproject.testrunner.TestRunState;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by btv on 02.12.15.
 */
public class MainForm implements Observer {
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

    private final ProcessRunnerModel model = new ProcessRunnerModel();

    public void initForm() {
        model.addObserver(this);

        mainFrame = new JFrame();

        mainFrame.setContentPane(mainPanel);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mainFrame.setTitle("TestRunner");
        mainFrame.setSize(600, 600);
        mainFrame.setLocationRelativeTo(null);
//        mainFrame.pack();

        rawOutputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, rawOutputArea.getFont().getSize()));
        runSelectedButton.setEnabled(false);

        loadTestBinaryButton.addActionListener(this::onLoadTestBinary);
        runAllTestsButton.addActionListener(this::onRunAllTests);
        stopButton.addActionListener(this::onStop);
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
            setLoadingProgress(true);
            statusLabel.setText("Loading binary...");
            rawOutputArea.setText(null);
            testOutputTable.setModel(new DefaultTableModel());;

            model.startReadingBinary(path);
        }
    }

    private void onRunAllTests(final ActionEvent event) {
        setLoadingProgress(true);
        statusLabel.setText("Running tests...");
        rawOutputArea.setText(null);
        for (int i = 0; i < testOutputTable.getModel().getRowCount(); i++) {
            testOutputTable.getModel().setValueAt("Queued", i, 0);
        }

        model.startAllTests();
    }

    private void onStop(final ActionEvent event) {
        model.stopAllProcesses();
        setLoadingProgress(false);
        statusLabel.setText("Stopped");
    }

    private void setLoadingProgress(boolean loading) {
        Stream.of(loadTestBinaryButton, runAllTestsButton, runFailedButton)
                .forEach(b -> b.setEnabled(!loading));
        stopButton.setEnabled(loading);
//        progressBar.setValue(loading ? progressBar.getMinimum() : progressBar.getMaximum());
    }

    private void onTestCasesLoaded() {
        final Collection<String> testCases = model.getTestCases();
        final String[] columnNames = {"State", "Test Name"};
        final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        testCases.stream().forEach(testName -> tableModel.addRow(new Object[]{"", testName}));

        testOutputTable.setModel(tableModel);
        testOutputTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        testOutputTable.getColumnModel().getColumn(1).setWidth(400);

        setLoadingProgress(false);
        statusLabel.setText("Binary loaded");
    }

    private void onOutLine(final String line) {
        rawOutputArea.append(line);
        rawOutputArea.append("\n");
        rawOutputArea.setCaretPosition(rawOutputArea.getDocument().getLength());
    }

    private void onTestStateChanged(final String fullName) {
        for (int i = 0; i < testOutputTable.getModel().getRowCount(); i++) {
            if (testOutputTable.getModel().getValueAt(i, 1).equals(fullName)) {
                final Optional<ProcessRunnerModel.TestState> testState = model.getTestState(fullName);
                if (testState.isPresent()) {
                    testOutputTable.getModel().setValueAt(testState.get().getState(), i, 0);
                }
                return;
            }
        }
    }

    private void onTestRunFinished(final int exitCode) {
        setLoadingProgress(false);
        statusLabel.setText(exitCode == 0 ? "Ok" : "Fail");
    }

    @Override
    public void update(final Observable o, final Object arg) {
        final ProcessRunnerModel.Event event = (ProcessRunnerModel.Event) arg;
        switch (event.type) {
            case TEST_CASES_LOADED:
                SwingUtilities.invokeLater(this::onTestCasesLoaded);
                break;
            case OUT_LINE:
                SwingUtilities.invokeLater(() -> onOutLine((String) event.data));
                break;
            case TEST_STATE_CHANGED:
                SwingUtilities.invokeLater(() -> onTestStateChanged((String) event.data));
                break;
            case TEST_RUN_FINISHED:
                SwingUtilities.invokeLater(() -> onTestRunFinished((Integer) event.data));
                break;
            case ERROR:
                break;
        }
    }
}
