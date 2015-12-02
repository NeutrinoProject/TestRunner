package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestOutputParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Stream;

import static com.neutrinoproject.testrunner.ui.ProcessRunnerModel.*;

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

        loadTestBinaryButton.addActionListener(this::onLoadTestBinary);
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
            setLoadingProgressForButtons(true);
            statusLabel.setText("Loading binary...");

            model.startReadingBinary(path);
        }
    }

    private void onStop(final ActionEvent event) {
        model.stopAllProcesses();
        setLoadingProgressForButtons(false);
        statusLabel.setText("Stopped");
    }

    private void setLoadingProgressForButtons(boolean loading) {
        Stream.of(loadTestBinaryButton, runAllTestsButton, runSelectedButton, runFailedButton)
                .forEach(b -> b.setEnabled(!loading));
        stopButton.setEnabled(loading);
    }

    private void onTestCasesLoaded() {
        final Collection<TestOutputParser.TestCase> testCases = model.getTestCases();
        final String[] columnNames = {"R", "State", "Test Name"};
        final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        for (final TestOutputParser.TestCase testCase : testCases) {
            tableModel.addRow(new Object[]{">", "", testCase.name});
            for (final String testName : testCase.tests) {
                tableModel.addRow(new Object[]{">", "", "  " + testName});
            }
        }

        testOutputTable.setModel(tableModel);
        testOutputTable.getColumnModel().getColumn(2).setPreferredWidth(400);
        testOutputTable.getColumnModel().getColumn(2).setWidth(400);

        setLoadingProgressForButtons(false);
        statusLabel.setText("Binary loaded");
    }

    @Override
    public void update(final Observable o, final Object arg) {
        final Event event = (Event) arg;
        switch (event) {
            case TEST_CASES_LOADED:
                SwingUtilities.invokeLater(this::onTestCasesLoaded);
                break;
            case ERROR:
                break;
        }
    }
}
