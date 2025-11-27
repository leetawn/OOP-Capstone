package CompilePackage;

import javax.swing.*;
import java.awt.*;

public class TextEditor extends JFrame {
    public TextEditor(){
        setTitle("CodeChum++");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Get screen size and set frame to maximum
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        System.out.println("Screen: " + screenSize.width + " x " + screenSize.height);

        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());

        // ============ LEFT PANEL ============
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // ROW 1: Top buttons (Add File, Language)
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        row1.setBackground(Color.WHITE);
        row1.setPreferredSize(new Dimension(465, 69));
        row1.setMinimumSize(new Dimension(465, 69));
        row1.setMaximumSize(new Dimension(465, 69));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addFileBtn = new JButton("Add File");
        addFileBtn.setPreferredSize(new Dimension(140, 40));
        addFileBtn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

        String[] languages = {"C", "C++", "Java", "Python"};
        JComboBox<String> languageCombo = new JComboBox<>(languages);
        languageCombo.setPreferredSize(new Dimension(140, 40));
        languageCombo.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

        row1.add(addFileBtn);
        JPanel spacer1 = new JPanel();
        spacer1.setOpaque(false);
        spacer1.setPreferredSize(new Dimension(140, 1));
        row1.add(spacer1);
        row1.add(languageCombo);

        // ROW 2: Text Area for code
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setBackground(Color.WHITE);
        row2.setPreferredSize(new Dimension(465, 750));
        row2.setMinimumSize(new Dimension(465, 750));
        row2.setMaximumSize(new Dimension(465, 750));
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea codeArea = new JTextArea();
        codeArea.setTabSize(1);
        codeArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        codeArea.setText("#include <stdio.h>\n\nint main() {\n\n   return 0;\n}");
        codeArea.setMargin(new Insets(10, 10, 10, 10));
        codeArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JScrollPane codeScroll = new JScrollPane(codeArea);
        row2.add(codeScroll, BorderLayout.CENTER);

        // ROW 3: Bottom buttons (Reuse Testcase, Submit)
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 25));
        row3.setBackground(Color.WHITE);
        row3.setPreferredSize(new Dimension(465, 108));
        row3.setMinimumSize(new Dimension(465, 108));
        row3.setMaximumSize(new Dimension(465, 108));
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton reuseBtn = new JButton("Reuse Testcase");
        reuseBtn.setPreferredSize(new Dimension(160, 50));
        reuseBtn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

        JButton submitBtn = new JButton("Submit");
        submitBtn.setPreferredSize(new Dimension(140, 50));
        submitBtn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

        row3.add(reuseBtn);
        row3.add(submitBtn);

        // Add rows to left panel
        leftPanel.add(row1);
        leftPanel.add(row2);
        leftPanel.add(row3);

        // ============ RIGHT PANEL ============
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // Title panel for "Actual Output" and "Expected Output"
        JPanel titlePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setPreferredSize(new Dimension(800, 40));

        JLabel actualLabel = new JLabel("Actual Output", SwingConstants.CENTER);
        actualLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));

        JLabel expectedLabel = new JLabel("Expected Output", SwingConstants.CENTER);
        expectedLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));

        titlePanel.add(actualLabel);
        titlePanel.add(expectedLabel);

        // Output text areas panel
        JPanel outputPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        outputPanel.setBackground(Color.WHITE);

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

        JScrollPane actualScroll = new JScrollPane(actualOutput);
        JScrollPane expectedScroll = new JScrollPane(expectedOutput);

        outputPanel.add(actualScroll);
        outputPanel.add(expectedScroll);

        // Add components to right panel
        GridBagConstraints rightGbc = new GridBagConstraints();

        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightGbc.weightx = 1.0;
        rightGbc.weighty = 0;
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.insets = new Insets(10, 10, 10, 10);
        rightPanel.add(titlePanel, rightGbc);

        rightGbc.gridy = 1;
        rightGbc.weighty = 1.0;
        rightGbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(outputPanel, rightGbc);

        // ============ ADD TO MAIN PANEL ============
        GridBagConstraints gbc = new GridBagConstraints();

        // Left panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(leftPanel, gbc);

        // Right panel
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(rightPanel, gbc);

        // Add main panel to frame
        add(mainPanel);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}

// THIS ONE
// package CompilePackage;

// import javax.swing.*;
// import java.awt.*;

// public class TextEditor extends JFrame {
//     public TextEditor(){
//         setTitle("CodeChum++");
//         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//         // Make the window fullscreen on any OS
//         setExtendedState(JFrame.MAXIMIZED_BOTH);

//         // Main panel (GridBag for left + right)
//         JPanel mainPanel = new JPanel(new GridBagLayout());

//         // ============ LEFT PANEL ============
//         JPanel leftPanel = new JPanel();
//         leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
//         leftPanel.setBackground(Color.WHITE);
//         leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

//         // ROW 1: Buttons row
//         JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
//         row1.setBackground(Color.WHITE);
//         row1.setAlignmentX(Component.LEFT_ALIGNMENT);

//         JButton addFileBtn = new JButton("Add File");
//         addFileBtn.setPreferredSize(new Dimension(140, 40));
//         addFileBtn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

//         String[] languages = {"C", "C++", "Java", "Python"};
//         JComboBox<String> languageCombo = new JComboBox<>(languages);
//         languageCombo.setPreferredSize(new Dimension(140, 40));
//         languageCombo.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

//         row1.add(addFileBtn);
//         row1.add(Box.createHorizontalStrut(20));
//         row1.add(languageCombo);

//         // ROW 2: Text area
//         JPanel row2 = new JPanel(new BorderLayout());
//         row2.setBackground(Color.WHITE);
//         row2.setAlignmentX(Component.LEFT_ALIGNMENT);

//         JTextArea codeArea = new JTextArea();
//         codeArea.setTabSize(1);
//         codeArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
//         codeArea.setText("#include <stdio.h>\n\nint main() {\n\n   return 0;\n}");
//         codeArea.setMargin(new Insets(10, 10, 10, 10));
//         codeArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

//         JScrollPane codeScroll = new JScrollPane(codeArea);
//         row2.add(codeScroll, BorderLayout.CENTER);

//         // ROW 3: Buttons bottom
//         JPanel row3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 25));
//         row3.setBackground(Color.WHITE);
//         row3.setAlignmentX(Component.LEFT_ALIGNMENT);

//         JButton reuseBtn = new JButton("Reuse Testcase");
//         reuseBtn.setPreferredSize(new Dimension(160, 50));
//         reuseBtn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

//         JButton submitBtn = new JButton("Submit");
//         submitBtn.setPreferredSize(new Dimension(140, 50));
//         submitBtn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));

//         row3.add(reuseBtn);
//         row3.add(submitBtn);

//         // Add rows to left panel (allowing them to stretch)
//         leftPanel.add(row1);
//         leftPanel.add(Box.createVerticalStrut(10));
//         leftPanel.add(row2);
//         leftPanel.add(Box.createVerticalStrut(10));
//         leftPanel.add(row3);


//         // ============ RIGHT PANEL ============
//         JPanel rightPanel = new JPanel(new GridBagLayout());
//         rightPanel.setBackground(Color.WHITE);
//         rightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

//         // Title panel
//         JPanel titlePanel = new JPanel(new GridLayout(1, 2, 10, 0));
//         titlePanel.setBackground(Color.WHITE);

//         JLabel actualLabel = new JLabel("Actual Output", SwingConstants.CENTER);
//         actualLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));

//         JLabel expectedLabel = new JLabel("Expected Output", SwingConstants.CENTER);
//         expectedLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));

//         titlePanel.add(actualLabel);
//         titlePanel.add(expectedLabel);

//         // Output areas
//         JPanel outputPanel = new JPanel(new GridLayout(1, 2, 10, 0));
//         outputPanel.setBackground(Color.WHITE);

//         JTextArea actualOutput = new JTextArea();
//         actualOutput.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
//         actualOutput.setEditable(false);
//         actualOutput.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
//         actualOutput.setMargin(new Insets(10, 10, 10, 10));

//         JTextArea expectedOutput = new JTextArea();
//         expectedOutput.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
//         expectedOutput.setEditable(false);
//         expectedOutput.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
//         expectedOutput.setMargin(new Insets(10, 10, 10, 10));

//         JScrollPane actualScroll = new JScrollPane(actualOutput);
//         JScrollPane expectedScroll = new JScrollPane(expectedOutput);

//         outputPanel.add(actualScroll);
//         outputPanel.add(expectedScroll);

//         // Add right components with GridBag
//         GridBagConstraints rightGbc = new GridBagConstraints();

//         rightGbc.gridx = 0;
//         rightGbc.gridy = 0;
//         rightGbc.weightx = 1;
//         rightGbc.weighty = 0;
//         rightGbc.fill = GridBagConstraints.HORIZONTAL;
//         rightGbc.insets = new Insets(10, 10, 10, 10);
//         rightPanel.add(titlePanel, rightGbc);

//         rightGbc.gridy = 1;
//         rightGbc.weighty = 1;
//         rightGbc.fill = GridBagConstraints.BOTH;
//         rightPanel.add(outputPanel, rightGbc);


//         // ============ ADD TO MAIN PANEL ============
//         GridBagConstraints gbc = new GridBagConstraints();

//         // LEFT
//         gbc.gridx = 0;
//         gbc.gridy = 0;
//         gbc.weightx = 0.25;    // 25% width
//         gbc.weighty = 1.0;
//         gbc.fill = GridBagConstraints.BOTH;
//         mainPanel.add(leftPanel, gbc);

//         // RIGHT
//         gbc.gridx = 1;
//         gbc.weightx = 0.75;    // 75% width
//         gbc.fill = GridBagConstraints.BOTH;
//         mainPanel.add(rightPanel, gbc);

//         add(mainPanel);
//         setLocationRelativeTo(null);
//         setVisible(true);
//     }
// }
