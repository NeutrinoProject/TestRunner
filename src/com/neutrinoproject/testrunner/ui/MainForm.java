package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestOutputParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
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
        Stream.of(loadTestBinaryButton, runAllTestsButton, runSelectedButton, runFailedButton)
                .forEach(b -> b.setEnabled(!loading));
        stopButton.setEnabled(loading);
        progressBar.setValue(loading ? progressBar.getMinimum() : progressBar.getMaximum());
    }

    private void onTestCasesLoaded() {
        final Collection<TestOutputParser.TestCase> testCases = model.getTestCases();
        final String[] columnNames = {"State", "Test Name"};
        final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        for (final TestOutputParser.TestCase testCase : testCases) {
            tableModel.addRow(new Object[]{"", testCase.name});
            for (final String testName : testCase.tests) {
                tableModel.addRow(new Object[]{"", "  " + testName});
            }
        }

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

    private void onTestRunFinished() {
        setLoadingProgress(false);
        statusLabel.setText("Done");
    }

    private void onTestStateChanged() {
        // TODO: Implement.
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
                SwingUtilities.invokeLater(this::onTestStateChanged);
                break;
            case TEST_RUN_FINISHED:
                SwingUtilities.invokeLater(this::onTestRunFinished);
                break;
            case ERROR:
                break;
        }
    }
}
