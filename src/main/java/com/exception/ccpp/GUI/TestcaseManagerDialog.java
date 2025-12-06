package com.exception.ccpp.GUI;

import com.exception.ccpp.CCJudge.TestcaseFile;

import javax.swing.*;
import java.awt.*;

public class TestcaseManagerDialog extends JDialog {
    private final TestcaseFile tf;
    private JList<String> testcaseList;
    private DefaultListModel<String> listModel;

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

    }
    private void handleAddTestcase() {
        // api call
    }

    private void handleDeleteTestcase() {
        // api call
    }

    private void handleSetPassword() {
        // api call
    }
}
