package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestExecutorService;
import com.neutrinoproject.testrunner.TestRunState;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by btv on 02.12.15.
 */
public class MainForm implements TestRunnerHandler {
    public static final String OVERALL_RESULT_ROW_NAME = "<Overall>";
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

    private TestRunnerModel testRunnerModel;
    private TestExecutorService testExecutorService;
    private String selectedTestName;

    public void setTestRunnerModel(final TestRunnerModel testRunnerModel) {
        this.testRunnerModel = testRunnerModel;
    }

    public void setTestExecutorService(final TestExecutorService testExecutorService) {
        this.testExecutorService = testExecutorService;
    }

    public void initForm() {
        mainFrame = new JFrame();

        mainFrame.setContentPane(mainPanel);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mainFrame.setTitle("TestRunner");
        mainFrame.setSize(600, 600);
        mainFrame.setLocationRelativeTo(null);
//        mainFrame.pack();

        rawOutputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, rawOutputArea.getFont().getSize()));
        runAllTestsButton.setEnabled(false);
        runSelectedButton.setEnabled(false);
        runFailedButton.setEnabled(false);

        loadTestBinaryButton.addActionListener(this::onLoadTestBinary);
        runAllTestsButton.addActionListener(this::onRunAllTests);
        runFailedButton.addActionListener(this::onRunFailedTests);
        stopButton.addActionListener(this::onStop);

        testOutputTable.getSelectionModel().addListSelectionListener(this::onRowSelected);
    }

    public void showForm() {
        SwingUtilities.invokeLater(() -> mainFrame.setVisible(true));
    }

    @Override
    public void onTestsLoadingFinished(final boolean success, final Collection<String> testNames) {
        SwingUtilities.invokeLater(() -> {
            final String[] columnNames = {"State", "Test Name"};
            final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(final int row, final int column) {
                    return false;
                }
            };

            testOutputTable.setModel(tableModel);
            testOutputTable.getColumnModel().getColumn(1).setPreferredWidth(400);
            testOutputTable.getColumnModel().getColumn(1).setWidth(400);
            testOutputTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            if (success) {
                selectedTestName = OVERALL_RESULT_ROW_NAME;

                tableModel.addRow(new Object[]{"", OVERALL_RESULT_ROW_NAME});
                testNames.stream().forEach(testName -> tableModel.addRow(new Object[]{"", testName}));
                selectedTestName = null;
                testOutputTable.getSelectionModel().setSelectionInterval(0, 0);
                onRowSelected(null);
                runAllTestsButton.setEnabled(true);
                statusLabel.setText("Binary loaded");
            } else {
                statusLabel.setText("Error");
            }
            setLoadingProgress(false);
        });
    }

    @Override
    public void onTestStateChange(final String testName, final TestRunState newState) {
        SwingUtilities.invokeLater(() ->
                getStreamOfTestResultTableRows()
                        .filter(tableRow -> tableRow.testName.equals(testName))
                        .forEach(tableRow -> tableRow.setTestRunState(newState.toString())));
    }

    @Override
    public void onTestRunFinished(final boolean success) {
        SwingUtilities.invokeLater(() -> {
            getStreamOfTestResultTableRows().forEach(tableRow -> {
                if (tableRow.testRunState.equals(TestRunState.RUNNING.toString()) || tableRow.testRunState.equals("Queued")) {
                    tableRow.setTestRunState("Stopped");
                }
            });

            setLoadingProgress(false);
            statusLabel.setText(success ? "Ok" : "Fail");
            runAllTestsButton.setEnabled(true);

            final boolean hasFailedTests = getStreamOfTestResultTableRows()
                    .anyMatch(tableRow -> tableRow.testRunState.equals(TestRunState.FAILED.toString()));
            runFailedButton.setEnabled(hasFailedTests);
        });
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
            testOutputTable.setModel(new DefaultTableModel());

            testExecutorService.submitReadBinary(path);
        }
    }

    private void onRunAllTests(final ActionEvent event) {
        setLoadingProgress(true);
        statusLabel.setText("Running tests...");
        rawOutputArea.setText(null);
        getStreamOfTestResultTableRows()
                .skip(1) // Skip the row for overall result
                .forEach(tableRow -> tableRow.setTestRunState("Queued"));

        testExecutorService.submitTestRun(testBinaryPathField.getText(), Collections.emptyList());
    }

    private void onRunFailedTests(final ActionEvent event) {
        setLoadingProgress(true);
        statusLabel.setText("Running tests...");
        rawOutputArea.setText(null);

        final java.util.List<String> failedTestNames = getStreamOfTestResultTableRows()
                .peek(tableRow -> tableRow.setTestRunState(""))
                .filter(tableRow -> tableRow.testRunState.equals(TestRunState.FAILED.toString()))
                .peek(tableRow -> tableRow.setTestRunState("Queued"))
                .map(tableRow -> tableRow.testName).collect(toList());

        testExecutorService.submitTestRun(testBinaryPathField.getText(), failedTestNames);
    }

    private void onStop(final ActionEvent event) {
        testExecutorService.stop();
        setLoadingProgress(false);
        statusLabel.setText("Stopped");
    }

    private void setLoadingProgress(boolean loading) {
        if (loading) {
            runAllTestsButton.setEnabled(false);
            runFailedButton.setEnabled(false);
        }
        loadTestBinaryButton.setEnabled(!loading);
        stopButton.setEnabled(loading);
//        progressBar.setValue(loading ? progressBar.getMinimum() : progressBar.getMaximum());
    }

    private void onRowSelected(ListSelectionEvent event) {
        if (testOutputTable.getSelectedRow() < 0) {
            this.selectedTestName = null;
            return;
        }
        final String selectedTestName = (String) testOutputTable.getValueAt(testOutputTable.getSelectedRow(), 1);
        if (!Objects.equals(selectedTestName, this.selectedTestName)) {
            testRunnerModel.unsubscribeAllOnTestOutput(this.selectedTestName);
            this.selectedTestName = selectedTestName.equals(OVERALL_RESULT_ROW_NAME)
                    ? TestRunnerModel.OVERALL_TEST_NAME
                    : selectedTestName;
            rawOutputArea.setText(null);
            testRunnerModel.subscribeOnTestOutput(this.selectedTestName, line ->
                SwingUtilities.invokeLater(() -> {
                    rawOutputArea.append(line);
                    rawOutputArea.append("\n");
//                    rawOutputArea.setCaretPosition(rawOutputArea.getDocument().getLength());
                })
            );
        }
    }

    private Stream<TableRow> getStreamOfTestResultTableRows() {
        final AtomicInteger index = new AtomicInteger(0);
        final TableModel tableModel = testOutputTable.getModel();
        Supplier<TableRow> supplier = () -> {
            final int localIndex = index.getAndIncrement();
            return new TableRow((String) tableModel.getValueAt(localIndex, 0),
                    (String) tableModel.getValueAt(localIndex, 1),
                    localIndex);
        };
        return Stream.generate(supplier).limit(tableModel.getRowCount());
    }

    private class TableRow {
        public String testRunState;
        public String testName;
        public int index;

        public TableRow(final String testRunState, final String testName, final int index) {
            this.testRunState = testRunState;
            this.testName = testName;
            this.index = index;
        }

        public void setTestRunState(final String state) {
            testOutputTable.getModel().setValueAt(state, index, 0);
        }
    }
}
