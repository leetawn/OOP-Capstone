package com.exception.ccpp.CCJudge;

import com.exception.ccpp.GUI.RoundedButton;
import com.exception.ccpp.GUI.RoundedPanel;

import javax.swing.*;
import java.awt.*;

public class TestcaseButton  extends RoundedButton {
    JLabel textLabel;
    int testcaseNumber;
    public void setTestcaseNumber(int num)
    {
        testcaseNumber = num;
        textLabel.setText("Test case " + num);
    }
    public int getTestcaseNumber() {
        return  testcaseNumber;
    }

    public TestcaseButton(int testcaseNumber) {
        super("", 30);
        this.testcaseNumber = testcaseNumber;
        setBackground(Color.decode("#1a1c2a"));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new GridBagLayout());

        GridBagConstraints gbcInternal = new GridBagConstraints();
        gbcInternal.insets = new Insets(10, 15, 10, 15);

        // TODO@GLENSH INSERT TESTCASE LOGO/ICON HERE
        RoundedPanel imagePanel = new RoundedPanel(new GridBagLayout(), 30, "#1a1c2a");
        imagePanel.setBackground(Color.decode("#1a1c2a")); // default color
        imagePanel.setBorderColor(Color.WHITE);
        imagePanel.setBorderThickness(1);
        imagePanel.setPreferredSize(new Dimension(20, 20));
        imagePanel.setMinimumSize(new Dimension(20, 20));

        gbcInternal.gridx = 0;
        gbcInternal.gridy = 0;
        gbcInternal.weightx = 0.0;
        gbcInternal.anchor = GridBagConstraints.WEST;
        add(imagePanel, gbcInternal);

        // --- 2. Text Label ---
        textLabel = new JLabel("Test case " + testcaseNumber);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        gbcInternal.gridx = 1;
        gbcInternal.weightx = 1.0;
        gbcInternal.anchor = GridBagConstraints.WEST;
        gbcInternal.insets = new Insets(10, 5, 10, 15);
        add(textLabel, gbcInternal);

        // Set size based on monitor (ADD THESE LINES)
        int monitorWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int monitorHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int tempW = (int) (monitorWidth * 0.12);
        int tempH = (int) (monitorHeight * 0.05);
        setPreferredSize(new Dimension(tempW, tempH));

        setOpaque(false);
    }
}