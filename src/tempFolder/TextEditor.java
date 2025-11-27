package CompilePackage;

import javax.swing.*;
import java.awt.*;

public class TextEditor extends JFrame {
    public TextEditor() {
        setTitle("CodeChum++");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Main panel (GridBag for left + right)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // ============ LEFT PANEL ============
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.insets = new Insets(10, 10, 10, 10);
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;

        // -------- ROW 1: Buttons --------
        JButton addFileBtn = new JButton("Add File");
        addFileBtn.setPreferredSize(new Dimension(140, 40));
        addFileBtn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

        String[] languages = {"C", "C++", "Java", "Python"};
        JComboBox<String> languageCombo = new JComboBox<>(languages);
        languageCombo.setPreferredSize(new Dimension(140, 40));
        languageCombo.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.weightx = 0;
        gbcLeft.anchor = GridBagConstraints.WEST;
        leftPanel.add(addFileBtn, gbcLeft);

        gbcLeft.gridx = 1;
        gbcLeft.weightx = 1;
        leftPanel.add(Box.createHorizontalGlue(), gbcLeft);

        gbcLeft.gridx = 2;
        gbcLeft.weightx = 0;
        gbcLeft.anchor = GridBagConstraints.EAST;
        leftPanel.add(languageCombo, gbcLeft);

        // -------- ROW 2: Code area --------
        JTextArea codeArea = new JTextArea();
        codeArea.setTabSize(1);
        codeArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        codeArea.setText("#include <stdio.h>\n\nint main() {\n\n   return 0;\n}");
        codeArea.setMargin(new Insets(10, 10, 10, 10));
        codeArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JScrollPane codeScroll = new JScrollPane(codeArea);

        gbcLeft.gridx = 0;
        gbcLeft.gridy = 1;
        gbcLeft.gridwidth = 3;
        gbcLeft.weightx = 1;
        gbcLeft.weighty = 1;
        gbcLeft.fill = GridBagConstraints.BOTH;
        leftPanel.add(codeScroll, gbcLeft);

        // -------- ROW 3: Bottom buttons --------
        JButton reuseBtn = new JButton("Reuse Testcase");
        reuseBtn.setPreferredSize(new Dimension(160, 50));
        reuseBtn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

        JButton submitBtn = new JButton("Submit");
        submitBtn.setPreferredSize(new Dimension(140, 50));
        submitBtn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcBottom = new GridBagConstraints();
        gbcBottom.insets = new Insets(0, 10, 0, 10);
        gbcBottom.gridx = 0;
        gbcBottom.gridy = 0;
        gbcBottom.weightx = 1;
        gbcBottom.anchor = GridBagConstraints.CENTER;
        bottomPanel.add(reuseBtn, gbcBottom);

        gbcBottom.gridx = 1;
        bottomPanel.add(submitBtn, gbcBottom);

        gbcLeft.gridx = 0;
        gbcLeft.gridy = 2;
        gbcLeft.gridwidth = 3;
        gbcLeft.weightx = 1;
        gbcLeft.weighty = 0;
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        leftPanel.add(bottomPanel, gbcLeft);

        // ============ RIGHT PANEL ============
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.insets = new Insets(10, 10, 10, 10);
        gbcRight.fill = GridBagConstraints.BOTH;

        // Title labels
        JLabel actualLabel = new JLabel("Actual Output", SwingConstants.CENTER);
        actualLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
        JLabel expectedLabel = new JLabel("Expected Output", SwingConstants.CENTER);
        expectedLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));

        JPanel titlePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(actualLabel);
        titlePanel.add(expectedLabel);

        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.weightx = 1;
        gbcRight.weighty = 0;
        rightPanel.add(titlePanel, gbcRight);

        // Output areas
        JTextArea actualOutput = new JTextArea();
        actualOutput.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        actualOutput.setEditable(false);
        actualOutput.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        actualOutput.setMargin(new Insets(10, 10, 10, 10));

        JTextArea expectedOutput = new JTextArea();
        expectedOutput.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        expectedOutput.setEditable(false);
        expectedOutput.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        expectedOutput.setMargin(new Insets(10, 10, 10, 10));

        JPanel outputPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        outputPanel.setBackground(Color.WHITE);
        outputPanel.add(new JScrollPane(actualOutput));
        outputPanel.add(new JScrollPane(expectedOutput));

        gbcRight.gridx = 0;
        gbcRight.gridy = 1;
        gbcRight.weightx = 1;
        gbcRight.weighty = 1;
        rightPanel.add(outputPanel, gbcRight);

        // ============ ADD PANELS TO MAIN ============
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        mainPanel.add(rightPanel, gbc);

        add(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
