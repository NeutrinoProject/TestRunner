package com.neutrinoproject.testrunner.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.stream.Stream;

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

//    private final Model model = new Model();
    private final ProcessRunnerModel model = new ProcessRunnerModel();

    public void initForm() {
        mainFrame = new JFrame();

        mainFrame.setContentPane(mainPanel);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mainFrame.setTitle("TestRunner");
        mainFrame.setSize(800, 600);
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
}
