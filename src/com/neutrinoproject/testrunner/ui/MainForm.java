package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestRunState;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.stream.Stream;

/**
 * Created by btv on 02.12.15.
 */
public class MainForm implements TestRunnerHandler {
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

    public void initForm() {
        testRunnerModel = new GTestRunnerModel(this);

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

            testRunnerModel.startReadingBinary(path);
        }
    }

    private void onRunAllTests(final ActionEvent event) {
        setLoadingProgress(true);
        statusLabel.setText("Running tests...");
        rawOutputArea.setText(null);
        for (int i = 0; i < testOutputTable.getModel().getRowCount(); i++) {
            testOutputTable.getModel().setValueAt("Queued", i, 0);
        }

        testRunnerModel.startAllTests();
    }

    private void onStop(final ActionEvent event) {
        testRunnerModel.stopAllProcesses();
        setLoadingProgress(false);
        statusLabel.setText("Stopped");
    }

    private void setLoadingProgress(boolean loading) {
        Stream.of(loadTestBinaryButton, runAllTestsButton, runFailedButton)
                .forEach(b -> b.setEnabled(!loading));
        stopButton.setEnabled(loading);
//        progressBar.setValue(loading ? progressBar.getMinimum() : progressBar.getMaximum());
    }

    @Override
    public void onTestsLoadingFinished(final boolean success) {
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
                final Stream<String> testNameStream = testRunnerModel.getTestNames().stream();
                testNameStream.forEach(testName -> tableModel.addRow(new Object[]{"", testName}));
                statusLabel.setText("Binary loaded");
            } else {
                statusLabel.setText("Error");
            }
            setLoadingProgress(false);
        });
    }

    @Override
    public void onOutputLine(@Nullable final String testName, final int overallLineIndex, final int testLineIndex, final String outputLine) {
        SwingUtilities.invokeLater(() -> {
            rawOutputArea.append(outputLine);
            rawOutputArea.append("\n");
            rawOutputArea.setCaretPosition(rawOutputArea.getDocument().getLength());
        });
    }

    @Override
    public void onTestStateChange(final String testName, final TestRunState newState) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < testOutputTable.getModel().getRowCount(); i++) {
                if (testOutputTable.getModel().getValueAt(i, 1).equals(testName)) {
                    testOutputTable.getModel().setValueAt(newState, i, 0);
                    return;
                }
            }
        });
    }

    @Override
    public void onTestRunFinished(final boolean success) {
        SwingUtilities.invokeLater(() -> {
            // TODO: Clean up test state. Set Running to Fail, Queued to Skipped.
            setLoadingProgress(false);
            statusLabel.setText(success ? "Ok" : "Fail");
        });
    }
}
