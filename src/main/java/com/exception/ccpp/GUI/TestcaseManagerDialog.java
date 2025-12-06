package com.exception.ccpp.GUI;

import com.exception.ccpp.CCJudge.Testcase;
import com.exception.ccpp.CCJudge.TestcaseFile;
import com.exception.ccpp.FileManagement.FileManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class TestcaseManagerDialog extends JDialog {
    private final TestcaseFile tf;
    private JList<String> testcaseList;
    private DefaultListModel<String> listModel;
    private List<Testcase> tList;

    public TestcaseManagerDialog(Window owner, TestcaseFile tf) {
        super(owner instanceof Dialog ? (Dialog) owner : (Frame) owner);
        this.tf = tf;
        initializeUI();
        loadTestcases();
        setSize(600, 450);
        setLocationRelativeTo(owner);
    }
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        // 1. Testcase List Panel
        listModel = new DefaultListModel<>();
        testcaseList = new JList<>(listModel);
        listModel.addElement("Loading testcases...");
        testcaseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Existing Testcases"));
        listPanel.add(new JScrollPane(testcaseList), BorderLayout.CENTER);
        add(listPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        JButton addButton = new JButton("Add Testcase");
        JButton deleteButton = new JButton("Delete Selected");
        JButton passwordButton = new JButton("Set Password");

        controlPanel.add(addButton);
        controlPanel.add(deleteButton);
        controlPanel.add(passwordButton);

        add(controlPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> handleAddTestcase());
        deleteButton.addActionListener(e -> handleDeleteTestcase());
        passwordButton.addActionListener(e -> handleSetPassword());
    }
    private void loadTestcases() {
        listModel.clear();
        Map<Testcase, String> testcases = tf.getTestcases();

        if (testcases.isEmpty()) {
            listModel.addElement("No testcases found.");
            return;
        }

        tList = new ArrayList<>(testcases.keySet());
        int i=0;
        for (Testcase tc : tList) {
            // Display a summary of the input and expected output
            String inputSummary = tc.getInputs().length > 0 ?
                    String.join(", ", tc.getInputs()) : "[]";

            String output = tc.getExpectedOutput();
            String outputSummary = (output != null && !output.isBlank()) ?
                    output.substring(0, Math.min(output.length(), 30)).replace("\n", " ") + "..." : "[No Expected Output]";

            listModel.addElement(String.format("TC %d: Input: %s | Output: %s", ++i, inputSummary, outputSummary));
        }
    }
    private void handleAddTestcase() {
        // api call
        tf.addTestcase(FileManager.getInstance());
        loadTestcases();
    }

    private void handleDeleteTestcase() {
        // api call
        int selectedIndex = testcaseList.getSelectedIndex();

        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a testcase to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tList != null && selectedIndex < tList.size()) {
            Testcase tcToDelete = tList.get(selectedIndex);

            tf.deleteTestcase(tcToDelete);

            loadTestcases();
        } else {
            JOptionPane.showMessageDialog(this, "Error finding the selected testcase.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSetPassword() {
        // api call
    }
}
