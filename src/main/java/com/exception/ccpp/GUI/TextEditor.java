package com.exception.ccpp.GUI;

import com.exception.ccpp.CCJudge.Judge;
import com.exception.ccpp.CustomExceptions.InvalidFileException;
import com.exception.ccpp.CustomExceptions.NotDirException;
import com.exception.ccpp.FileManagement.SFile;
import com.exception.ccpp.FileManagement.FileManager;
import java.awt.event.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import com.exception.ccpp.GUI.RoundedButton; // Assuming this class is available
import com.exception.ccpp.GUI.RoundedComboBox; // Assuming this class is available

public class TextEditor extends JPanel {
    private JButton runCodeButton;
    private JButton addFileButton;
    private JButton createButton; // Appears unused, but keeping it
    private JButton openFolderButton;
    private JButton createFolderButton;
    private JButton setEntryPointButton;
    private JTextArea dTextArea;
    private JComboBox<String> languageSelectDropdown;
    private com.exception.ccpp.GUI.FileExplorer fileExplorerPanel;
    private JTextPane actualOutputArea;
    private JTextPane expectedOutputArea;
    private JButton importTestcaseButton;
    private JButton exportTestcaseButton;
    private JButton submitCodeButton;

    private SimpleAttributeSet matchStyle;
    private SimpleAttributeSet mismatchStyle;
    private SimpleAttributeSet excessStyle;
    private SimpleAttributeSet defaultStyle;

    public TextEditor() {
//        initializeComponents();
        // Initialize the essential components that are used elsewhere
        dTextArea = new JTextArea();
        actualOutputArea = new JTextPane();
        expectedOutputArea = new JTextPane();
        languageSelectDropdown = new JComboBox<>(new String[]{"C", "C++", "Java", "Python"});

        // Initialize file explorer with a dummy text area if needed
        fileExplorerPanel = new com.exception.ccpp.GUI.FileExplorer(".", dTextArea, this);

        initializeBackend();
        initializeStyles();
        setupLayout();
//        setupEventListeners();
        setupTabToSpaces();
        setVisible(true);
    }

    public TextEditor(String folderPath, com.exception.ccpp.GUI.MainMenu mainMenu) {
        this(); // call the original no-arg constructor that builds your UI

        // Close the MainMenu immediately when TextEditor starts
        if (mainMenu != null) {
            mainMenu.setVisible(false);
        }

        try {
            fileExplorerPanel.updateRootDirectory(folderPath);
            setTextArea(false); // disable editing since no file is selected yet
        } catch (NotDirException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load project folder:\n" + ex.getMessage(),
                    "Invalid Folder",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* --------------- Setup --------------- */

    private void setupTabToSpaces() {
        final String fourSpaces = "    ";

        Action insertSpacesAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dTextArea.replaceSelection(fourSpaces);
            }
        };

        KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);

        Object actionKey = "insert-four-spaces";

        InputMap inputMap = dTextArea.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(tabKey, actionKey);

        dTextArea.getActionMap().put(actionKey, insertSpacesAction);
    }
    private void initializeStyles() {
        defaultStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(defaultStyle, Color.BLACK);
        StyleConstants.setBackground(defaultStyle, Color.decode("#1f2335"));

        matchStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(matchStyle, Color.BLACK);
        StyleConstants.setBackground(matchStyle, new Color(176, 237, 184)); // Light Green (Match)

        mismatchStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(mismatchStyle, Color.BLACK);
        StyleConstants.setBackground(mismatchStyle, new Color(228, 163, 159)); // Lacking

        excessStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(excessStyle, Color.BLACK); // Use Black FG for visibility on Yellow
        StyleConstants.setBackground(excessStyle, new Color(245, 224, 59)); // Yellow (Excess)
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // GLOBAL SETTINGS
        gbc.fill = GridBagConstraints.BOTH; // STRICTLY DO NOT EDIT THIS!
        gbc.insets = new Insets(0, 0, 0, 0); // STRICTLY DO NOT EDIT THIS!
        gbc.gridy = 0; // STRICTLY DO NOT EDIT THIS!
        gbc.weighty = 1.0; // STRICTLY DO NOT EDIT THIS!

        // --- PANEL 1: Left Sidebar (Purple) ---
        gbc.gridx = 0;  // STRICTLY DO NOT EDIT THIS!
        gbc.weightx = 0.11; // STRICTLY DO NOT EDIT THIS!
        add(create_first_panel(), gbc);

        // --- PANEL 2: Center (Green) ---
        gbc.gridx = 1; // STRICTLY DO NOT EDIT THIS!
        gbc.weightx = 0.59; // STRICTLY DO NOT EDIT THIS!
        add(create_second_panel(), gbc);

        // --- PANEL 3: Right Sidebar (Red) ---
        gbc.gridx = 2; // STRICTLY DO NOT EDIT THIS!
        gbc.weightx = 0.30; // STRICTLY DO NOT EDIT THIS!
        add(create_third_panel(), gbc);
    }

// --- PANEL CREATION METHODS ---

    private JPanel create_first_panel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#ffffff"));
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 2));

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.0125;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_1_1_panel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.0325;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_1_2_panel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 0.955;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_1_3_panel(), gbc);

        return panel;
    }

    private JPanel create_1_1_panel(){
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 2));
        panel.setBackground(Color.decode("#191c2a"));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel();
        label.setText("File Explorer");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Font.SANS_SERIF", Font.BOLD, 11));

        panel.add(label);

        return panel;
    }

    private ImageIcon getScaledIcon(URL imageUrl, int width, int height) {
        if (imageUrl == null) {
            System.err.println("Image URL is null. Resource not found.");
            return new ImageIcon(); // Return an empty icon to prevent null pointer exceptions
        }

        // 1. Get the original image
        ImageIcon originalIcon = new ImageIcon(imageUrl);
        Image originalImage = originalIcon.getImage();

        // 2. Scale the image
        // Uses SCALE_SMOOTH for better visual quality when scaling down.
        Image scaledImage = originalImage.getScaledInstance(
                width,
                height,
                Image.SCALE_SMOOTH
        );

        // 3. Create and return the new scaled icon
        return new ImageIcon(scaledImage);
    }

    // Helper method to make the buttons look like toolbar icons
    private void configureToolbarButton(JButton button) {
        // 1. Remove the text, even if none is explicitly set
        button.setText(null);

        // 2. Remove the standard border and background painting
        button.setBorder(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        // 3. Remove the box drawn around the button when it has focus
        button.setFocusPainted(false);

        // 4. Force the button size to be exactly the icon size plus a small margin
        // This is optional but ensures a tight fit
        Dimension fixedSize = new Dimension(22, 22); // Slightly larger than 20 to allow for padding/hover effects
        button.setPreferredSize(fixedSize);
        button.setMinimumSize(fixedSize);
        button.setMaximumSize(fixedSize);
    }

    // Helper method definition (place this as a private method in your class)
    private JPanel create_1_2_panel(){
        JPanel panel = new FixedSizePanel(1, 1);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 2));
        panel.setBackground(Color.decode("#191c2a"));
        // Keep FlowLayout.LEFT and hgap/vgap settings
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        Class<?> contextClass = this.getClass();

        final int ICON_SIZE = 20;

        // Load URLs
        URL openFolderUrl = contextClass.getResource("/assets/open_folder.png");
        URL addFileUrl = contextClass.getResource("/assets/add_file.png");
        URL createFolderUrl = contextClass.getResource("/assets/create_folder.png");

        // 1. Create the JButtons using the scaled icons

        // Get the scaled icons
        ImageIcon openFolderIcon = getScaledIcon(openFolderUrl, ICON_SIZE, ICON_SIZE);
        ImageIcon addFileIcon = getScaledIcon(addFileUrl, ICON_SIZE, ICON_SIZE);
        ImageIcon createFolderIcon = getScaledIcon(createFolderUrl, ICON_SIZE, ICON_SIZE);

        // Create JButtons and set the icon
        openFolderButton = new JButton(openFolderIcon);
        addFileButton = new JButton(addFileIcon);
        createFolderButton = new JButton(createFolderIcon);

        openFolderButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addFileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createFolderButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 2. Configure the Buttons to look like Icons (Crucial for a toolbar)
        configureToolbarButton(openFolderButton);
        configureToolbarButton(addFileButton);
        configureToolbarButton(createFolderButton);

        // 3. Add JButtons to the Panel
        panel.add(openFolderButton);
        panel.add(addFileButton);
        panel.add(createFolderButton);

        return panel;
    }

    private JPanel create_1_3_panel(){
        JPanel panel = new FixedSizePanel(1, 1);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 2));
        panel.setBackground(Color.decode("#191c2a"));

        return panel;
    }

    private JPanel create_second_panel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#32CD32"), 2));

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.02;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_2_1_panel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.92;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_2_2_panel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 0.06;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_2_3_panel(), gbc);

        return panel;
    }

    private JPanel create_2_1_panel(){
        JPanel panel = new FixedSizePanel(1, 1);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 2));
        panel.setBackground(Color.decode("#1f2335"));

        return panel;
    }

    private JPanel create_2_2_panel(){
        JPanel panel = new FixedSizePanel(1, 1);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 2));
        panel.setBackground(Color.decode("#1f2335"));

        return panel;
    }

    public class FixedSizePanel extends JPanel {
        private final Dimension fixedSize;

        public FixedSizePanel(int width, int height) {
            this.fixedSize = new Dimension(width, height);
        }

        @Override
        public Dimension getPreferredSize() {
            return fixedSize;
        }

        // It's good practice to also fix minimum and maximum sizes
        @Override
        public Dimension getMinimumSize() {
            return fixedSize;
        }

        @Override
        public Dimension getMaximumSize() {
            return fixedSize;
        }
    }

    private JPanel create_2_3_panel(){
        // 1. USE FixedSizePanel and specify the exact size (e.g., 600 wide, 40 high)
        // You MUST choose the correct width and height for your layout.
        JPanel panel = new FixedSizePanel(1, 1);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 2));
        panel.setBackground(Color.decode("#1f2335"));
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        importTestcaseButton = new JButton("ImportTestcase");
        importTestcaseButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        importTestcaseButton.setForeground(Color.WHITE);
        importTestcaseButton.setBackground(Color.decode("#568afc"));

        exportTestcaseButton = new JButton("Export Testcase");
        exportTestcaseButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        exportTestcaseButton.setForeground(Color.WHITE);
        exportTestcaseButton.setBackground(Color.decode("#568afc"));

        setEntryPointButton = new RoundedButton("Set Entry Point", 30);
        setEntryPointButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        setEntryPointButton.setForeground(Color.WHITE);
        setEntryPointButton.setBackground(Color.decode("#568afc"));

        runCodeButton = new RoundedButton("Run Code", 30);
        runCodeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        runCodeButton.setForeground(Color.WHITE);
        runCodeButton.setBackground(Color.decode("#568afc"));

        submitCodeButton = new RoundedButton("Submit Code", 30);
        submitCodeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        submitCodeButton.setForeground(Color.WHITE);
        submitCodeButton.setBackground(Color.decode("#0cf500"));

        panel.add(importTestcaseButton);
        panel.add(exportTestcaseButton);
        panel.add(setEntryPointButton);
        panel.add(runCodeButton);
        panel.add(submitCodeButton);

        return panel;
    }

    private JPanel create_third_panel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_3_1_panel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_3_2_panel(), gbc);

        return panel;
    }

    private JPanel create_3_1_panel(){
        JPanel panel = new FixedSizePanel(1, 1);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 2));
        panel.setBackground(Color.decode("#1f2335"));

        // 1. Set the panel to use BorderLayout for anchoring
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel();
        label.setText("Actual Output");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Font.SANS_SERIF", Font.BOLD, 11));

        // Optional: Add a small margin/padding around the text
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

        // Crucial: Set the label's content alignment to left (it will stretch in NORTH)
        label.setHorizontalAlignment(SwingConstants.LEFT);

        // 2. Add the label to the top (NORTH) region
        panel.add(label, BorderLayout.NORTH);

        return panel;
    }

    private JPanel create_3_2_panel(){
        JPanel panel = new FixedSizePanel(1, 1);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 2));
        panel.setBackground(Color.decode("#1f2335"));

        // 1. Set the panel to use BorderLayout for anchoring
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel();
        label.setText("Expected Output");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Font.SANS_SERIF", Font.BOLD, 11));

        // Optional: Add a small margin/padding around the text
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

        // Crucial: Set the label's content alignment to left (it will stretch in NORTH)
        label.setHorizontalAlignment(SwingConstants.LEFT);

        // 2. Add the label to the top (NORTH) region
        panel.add(label, BorderLayout.NORTH);

        return panel;
    }

//    private void initializeComponents() {
//        // Initialize Buttons
//        runCodeButton = new RoundedButton("Run Code", 30);
//        setEntryPointButton = new RoundedButton("Set Entry Point", 30);
//        addFileButton = new RoundedButton("Add File", 15);
//        openFolderButton = new RoundedButton("", 15); // Icon only
//        createFolderButton = new RoundedButton("Create Folder", 15);
//        importTestcaseButton = new RoundedButton("Import Testcase", 15);
//        exportTestcaseButton = new RoundedButton("Export Testcase", 15);
//
//        // Initialize Text Areas
//        dTextArea = new JTextArea();
//        actualOutputArea = new JTextPane();
//        expectedOutputArea = new JTextPane();
//
//        // Initialize Language Dropdown
//        languageSelectDropdown = new RoundedComboBox<>(new String[]{"C", "C++", "Java", "Python"});
//        ((RoundedComboBox<String>) languageSelectDropdown).setRadius(20);
//
//        // Initialize File Explorer
//        fileExplorerPanel = new com.exception.ccpp.GUI.FileExplorer(".", dTextArea, this);
//
//        // Set up Open Folder Button Icon
//        URL urlOpenFolderButton = TextEditor.class.getResource("/assets/open_folder.png");
//        if (urlOpenFolderButton != null) {
//            ImageIcon iconOpenFolderButton = new ImageIcon(urlOpenFolderButton);
//            Image originalImage = iconOpenFolderButton.getImage();
//            Image scaledImage = originalImage.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
//            ((RoundedButton)openFolderButton).setIconImage(scaledImage);
//        } else {
//            System.err.println("Resource not found: /assets/open_folder.png");
//        }
//
//        // Apply Styling
//        runCodeButton.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
//        setEntryPointButton.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
//
//        Color buttonColor = Color.decode("#568afc");
//        Color bgColor = Color.decode("#28313b");
//
//        // Shared Button Styling
//        JButton[] buttons = {runCodeButton, setEntryPointButton, addFileButton, createFolderButton, importTestcaseButton, exportTestcaseButton};
//        for (JButton btn : buttons) {
//            btn.setBackground(buttonColor);
//            btn.setForeground(Color.WHITE);
//            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
//        }
//
//        // Specific Button Styling
//        openFolderButton.setBackground(bgColor);
//        openFolderButton.setForeground(Color.WHITE);
//        openFolderButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//        openFolderButton.setToolTipText("Open Project Folder");
//
//        setEntryPointButton.setVisible(false);
//        setEntryPointButton.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
//        runCodeButton.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
//    }

    // END OF FRONT END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // START OF BACK END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

//    private void setupEventListeners() {
//        dTextArea.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if ((e.getKeyCode() == KeyEvent.VK_S) &&
//                        ((e.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0)) {
//
//                    e.consume();
//                    saveCurrentFileContent();
//                    actualOutputArea.setText("File saved successfully.");
//                }
//            }
//        });
//
//        openFolderButton.addActionListener((ActionListener) new OpenFolderButtonHandler(this));
//        addFileButton.addActionListener(new AddFileButtonHandler(this));
//        languageSelectDropdown.addActionListener(new LanguageSelectHandler(this));
//        runCodeButton.addActionListener(new RunButtonHandler(this));
//        createFolderButton.addActionListener(e -> {
//            fileExplorerPanel.handleCreateFolderAction();
//        });
//        setEntryPointButton.addActionListener(new SetEntryPointButtonHandler(this));
//        importTestcaseButton.addActionListener(new ImportTestcaseButtonHandler(this));
//        exportTestcaseButton.addActionListener(new ExportTestcaseButtonHandler(this));
//    }

    private void initializeBackend() {

        dTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        dTextArea.setText("No selected file. Select a file to start editing.");
        dTextArea.setEditable(false);

        actualOutputArea.setEditable(false);
        actualOutputArea.setCaretColor(Color.decode("#1f2335"));
        expectedOutputArea.setEditable(false);
        expectedOutputArea.setCaretColor(Color.decode("#1f2335"));
        actualOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        expectedOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
    }

    private void displayActualDiff(String actualText, String expectedText) {
        StyledDocument doc = actualOutputArea.getStyledDocument();

        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ignored) {}

        String[] actualLines = actualText.split("\\R", -1);
        String[] expectedLines = expectedText.split("\\R", -1);

        int maxLines = Math.max(actualLines.length, expectedLines.length);

        for (int i = 0; i < maxLines; i++) {
            String actualLine = (i < actualLines.length) ? actualLines[i] : "";
            String expectedLine = (i < expectedLines.length) ? expectedLines[i] : "";

            int maxLength = Math.max(actualLine.length(), expectedLine.length());

            for (int j = 0; j < maxLength; j++) {
                char actualChar = (j < actualLine.length()) ? actualLine.charAt(j) : 0;
                char expectedChar = (j < expectedLine.length()) ? expectedLine.charAt(j) : 0;

                AttributeSet styleToApply;
                char charToProcess = actualChar; // Start with the actual character

                // --- 1. Comparison Logic (Inverted: Focus on Expected Status) ---

                if (expectedChar == 0 && actualChar != 0) {
                    // 1. EXCESS (Yellow): Actual output continues beyond Expected length
                    styleToApply = excessStyle; // Yellow

                } else if (expectedChar != 0 && actualChar == expectedChar) {
                    // 2. MATCH (Green): Expected character was found exactly
                    styleToApply = matchStyle; // Green

                } else {
                    // 3. LACKING / MISMATCH (Red):
                    //    - Expected char exists, but Actual is different, OR
                    //    - Expected char exists, but Actual is missing (actualChar == 0).
                    styleToApply = mismatchStyle; // Red

                    // If Actual is missing, insert a space placeholder to show the red box
                    if (actualChar == 0) {
                        charToProcess = ' ';
                    }
                }

                // --- 2. Visualization Logic (Based on the character being displayed) ---
                String charToDisplay;

                if (charToProcess == '\t') {
                    charToDisplay = "    "; // 4 spaces for Tab
                } else if (charToProcess == '\r' || charToProcess == '\n') {
                    charToDisplay = "\u2424"; // Symbol for Newline
                } else if (charToProcess == 0 || charToProcess == ' ') {
                    // Handles the space placeholder for LACKING or actual space characters
                    charToDisplay = " ";
                } else {
                    charToDisplay = String.valueOf(charToProcess);
                }

                try {
                    // Insert the visual representation of the character with the determined style
                    doc.insertString(doc.getLength(), charToDisplay, styleToApply);
                } catch (BadLocationException ignored) {}
            }

            // Add a literal newline to advance the cursor in the JTextPane
            if (i < maxLines - 1) {
                try {
                    doc.insertString(doc.getLength(), "\n", defaultStyle);
                } catch (BadLocationException ignored) {}
            }
        }
    }

//    private void displayExpectedDiff(String actualText, String expectedText) {
//        // Note: We are using expectedOutputArea for this.
//        StyledDocument doc = expectedOutputArea.getStyledDocument();
//
//        try {
//            doc.remove(0, doc.getLength());
//        } catch (BadLocationException ignored) {}
//
//        String[] actualLines = actualText.split("\\R", -1);
//        String[] expectedLines = expectedText.split("\\R", -1);
//
//        int maxLines = Math.max(actualLines.length, expectedLines.length);
//
//        for (int i = 0; i < maxLines; i++) {
//            String actualLine = (i < actualLines.length) ? actualLines[i] : "";
//            String expectedLine = (i < expectedLines.length) ? expectedLines[i] : "";
//
//            int maxLength = Math.max(actualLine.length(), expectedLine.length());
//
//            for (int j = 0; j < maxLength; j++) {
//                char actualChar = (j < actualLine.length()) ? actualLine.charAt(j) : 0;
//                char expectedChar = (j < expectedLines.length) ? expectedLines[j] : 0;
//
//                AttributeSet styleToApply;
//                char charToProcess = expectedChar; // Focus on the expected character
//
//                // If Expected output ran out, stop processing this line in the Expected pane
//                if (expectedChar == 0) {
//                    continue;
//                }
//
//                // --- Comparison Logic (Focus on Expected) ---
//                if (actualChar == expectedChar) {
//                    // Match
//                    styleToApply = matchStyle; // Green
//                } else {
//                    // Lacking or Mismatched Character
//                    styleToApply = mismatchStyle; // Red
//                }
//
//                // --- Visualization Logic for Expected ---
//                String charToDisplay;
//                if (charToProcess == '\t') {
//                    charToDisplay = "    "; // 4 spaces for Tab
//                } else if (charToProcess == '\r' || charToProcess == '\n') {
//                    charToDisplay = "\u2424"; // Symbol for Newline
//                } else {
//                    charToDisplay = String.valueOf(charToProcess);
//                }
//
//                try {
//                    doc.insertString(doc.getLength(), charToDisplay, styleToApply);
//                } catch (BadLocationException ignored) {}
//            }
//
//            // Add a literal newline to advance the cursor in the JTextPane
//            if (i < maxLines - 1 && i < expectedLines.length) { // Only add newline if Expected has more lines
//                try {
//                    doc.insertString(doc.getLength(), "\n", defaultStyle);
//                } catch (BadLocationException ignored) {}
//            }
//        }
//    }

    public void saveCurrentFileContent() {
        SFile currentFile = fileExplorerPanel.getSelectedFile(); // <-- Use the new source of truth

        String placeholderText = "No file selected. Select a file to start editing.";
        String content = dTextArea.getText();

        if (currentFile != null && !content.equals(placeholderText)) {
            currentFile.setContent(content);
            currentFile.writeOut();
            System.out.println("File saved: " + currentFile.getStringPath());
        }
    }

    public void handleAddFileAction() {
        FileManager fm = fileExplorerPanel.getFileManager();
        if (fm == null) return;

        DefaultMutableTreeNode selectedNode = fileExplorerPanel.getSelectedNode();
        Path targetDir = fm.getRootdir();
        DefaultMutableTreeNode parentNodeInTree;

        // Logic to determine target directory and parent node (for insertion in JTree)
        if (selectedNode != null) {
            Object obj = selectedNode.getUserObject();

            if (obj instanceof SFile sfile) {
                if (Files.isDirectory(sfile.getPath())) {
                    targetDir = sfile.getPath();
                    parentNodeInTree = selectedNode;
                } else {
                    targetDir = sfile.getPath().getParent();
                    parentNodeInTree = (DefaultMutableTreeNode) selectedNode.getParent();
                }
            } else {
                targetDir = fileExplorerPanel.resolveNodeToPath(selectedNode);
                parentNodeInTree = selectedNode;
            }
        } else {
            parentNodeInTree = (DefaultMutableTreeNode) fileExplorerPanel.getFeTree().getModel().getRoot();
        }

        String fileName = JOptionPane.showInputDialog(this, "Enter new file name (with extension):");
        if (fileName == null || fileName.isBlank()) return;

        // Use the FileManager's language-based validation for new files
        if (!fm.isAllowedFile(fileName)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid file extension for the current project language (" + fm.getLanguage() + ").",
                    "Invalid Extension",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Path newFilePath = targetDir.resolve(fileName);

            if (Files.exists(newFilePath)) {
                JOptionPane.showMessageDialog(this, "File already exists in " + targetDir);
                return;
            }

            saveCurrentFileContent();

            SFile newSFile = new SFile(newFilePath);
            newSFile.writeOut();

            fm.getFiles().add(newSFile);
            fm.setCurrentFile(newSFile);
            dTextArea.setText(newSFile.getContent());

            DefaultMutableTreeNode newFileNode = new DefaultMutableTreeNode(newSFile);
            DefaultTreeModel model = (DefaultTreeModel) fileExplorerPanel.getFeTree().getModel();

            model.insertNodeInto(newFileNode, parentNodeInTree, parentNodeInTree.getChildCount());

            fileExplorerPanel.getFeTree().expandPath(new TreePath(parentNodeInTree.getPath()));
            fileExplorerPanel.getFeTree().setSelectionPath(new TreePath(newFileNode.getPath()));

            JOptionPane.showMessageDialog(this, "File created: " + newFilePath.getFileName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error creating file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
    /* --------------- Util --------------- */
    /* --------------- Getters & Setters --------------- */

    public String getCurrentSelectedLanguage() {
        return (String) languageSelectDropdown.getSelectedItem();
    }

    public void setTextArea(boolean ok) {
        this.dTextArea.setEditable(ok);
        if (!ok) {
            this.dTextArea.setText("No file selected. Please open a project or select a file to begin editing.");

            fileExplorerPanel.setSelectedFile(null);
        }
    }
    public static void setNimbusLaf() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("nooooooo garbage ui for now");
        }
    }

    public JButton getSetEntryPointButton() {
        return setEntryPointButton;
    }

    /* --------------- Getters & Setters --------------- */
    /* --------------- Button Handlers --------------- */

    public static class OpenFolderButtonHandler extends ComponentHandler {

        public OpenFolderButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            FileManager fm = fe.getFileManager();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Project Root Folder");
            fileChooser.setCurrentDirectory(fm != null ? fm.getRootdir().toFile() : null);

            int result = fileChooser.showOpenDialog(getTextEditor());

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDir = fileChooser.getSelectedFile();
                if (selectedDir != null && selectedDir.isDirectory()) {
                    try {
                        getTextEditor().saveCurrentFileContent();
                        getTextEditor().fileExplorerPanel.updateRootDirectory(selectedDir.getAbsolutePath());
                        getTextEditor().setTextArea(false);
                        getTextEditor().actualOutputArea.setText("Successfully loaded new project: " + selectedDir.getName());
                    } catch (NotDirException ex) {
                        JOptionPane.showMessageDialog(getTextEditor(), "Error loading directory: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    };

    public static class AddFileButtonHandler extends ComponentHandler {
        public AddFileButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            FileManager fileManager = fe.getFileManager();
            if (fileManager == null) return;

            DefaultMutableTreeNode selectedNode = fe.getSelectedNode();
            Path targetDir = fileManager.getRootdir();
            DefaultMutableTreeNode parentNodeInTree;

            if (selectedNode != null) {
                Object obj = selectedNode.getUserObject();

                if (obj instanceof SFile sfile) {
                    if (Files.isDirectory(sfile.getPath())) {
                        targetDir = sfile.getPath();
                        parentNodeInTree = selectedNode;
                    } else {
                        targetDir = sfile.getPath().getParent();
                        parentNodeInTree = (DefaultMutableTreeNode) selectedNode.getParent();
                    }
                } else {
                    targetDir = fe.resolveNodeToPath(selectedNode);
                    parentNodeInTree = selectedNode;
                }
            } else {
                parentNodeInTree = (DefaultMutableTreeNode) fe.getFeTree().getModel().getRoot();
            }


            String fileName = JOptionPane.showInputDialog(getTextEditor(), "Enter new file name (with extension):");
            if (fileName == null || fileName.isBlank()) return;

            if (!fileManager.isAllowedFile(fileName)) {
                JOptionPane.showMessageDialog(getTextEditor(),
                        "Invalid file extension.\nAllowed: .c, .cpp, .h, .hpp, .java, .py",
                        "Invalid Extension",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Path newFilePath = targetDir.resolve(fileName);

                if (Files.exists(newFilePath)) {
                    JOptionPane.showMessageDialog(getTextEditor(), "File already exists in " + targetDir);
                    return;
                }

                getTextEditor().saveCurrentFileContent();

                SFile newSFile = new SFile(newFilePath);
                newSFile.writeOut();
                fileManager.getFiles().add(newSFile);
                fileManager.setCurrentFile(newSFile);
                getTextEditor().dTextArea.setText(newSFile.getContent());

                DefaultMutableTreeNode newFileNode = new DefaultMutableTreeNode(newSFile);
                DefaultTreeModel model = (DefaultTreeModel) fe.getFeTree().getModel();

                model.insertNodeInto(newFileNode, parentNodeInTree, parentNodeInTree.getChildCount());

                fe.getFeTree().expandPath(new TreePath(parentNodeInTree.getPath()));
                fe.getFeTree().setSelectionPath(new TreePath(newFileNode.getPath()));

                JOptionPane.showMessageDialog(getTextEditor(), "File created: " + newFilePath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(getTextEditor(),
                        "Error creating file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static class LanguageSelectHandler extends ComponentHandler {
        public LanguageSelectHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            FileManager fileManager = fe.getFileManager();
            String newLanguage = (String) getTextEditor().languageSelectDropdown.getSelectedItem();

            if (newLanguage.equalsIgnoreCase("Java") || newLanguage.equalsIgnoreCase("Python")) {
                getTextEditor().setEntryPointButton.setVisible(true);
            } else {
                getTextEditor().setEntryPointButton.setVisible(false);
            }
            fileManager.setLanguage(newLanguage);

            fileManager.setCurrentFile(null);

            getTextEditor().setEntryPointButton.setText("Set Entry Point");

            getTextEditor().actualOutputArea.setText("");
            getTextEditor().expectedOutputArea.setText("");

            System.out.println("Project language changed to: " + newLanguage + ". Entry point reset.");
        }
    }

//    public static class RunButtonHandler extends ComponentHandler {
//        public RunButtonHandler(TextEditor editor) {
//            super(editor);
//        }
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            FileExplorer fe = getTextEditor().fileExplorerPanel;
//            getTextEditor().saveCurrentFileContent();
//            FileManager fm = fe.getFileManager();
//            String out = Judge.judge(fm, new String[]{}).output();
//            String dummyActual = "Hello World\nThis is line 2\twith a tab.\nExtra line.";
//            String dummyExpected = "Hella World\nThis is line 2\rwith a tab.\n";
//
//            // Call the diff checker method
//            getTextEditor().displayActualDiff(dummyActual, dummyExpected);
//            getTextEditor().displayExpectedDiff(dummyActual, dummyExpected);
//        }
//    }

    public static class SetEntryPointButtonHandler extends ComponentHandler {
        public SetEntryPointButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            FileManager fileManager = fe.getFileManager();
            JTree fe_tree = fe.getFeTree();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
            if (node == null || !(node.getUserObject() instanceof SFile sfile)) return;

            if (Files.isDirectory(sfile.getPath())) {
                JOptionPane.showMessageDialog(null,
                        "yo this is a folder gang you can't set folders as entry points",
                        "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (getTextEditor().getCurrentSelectedLanguage().equalsIgnoreCase("Java")) {
                if (sfile.getStringPath().toLowerCase().endsWith(".java")) fileManager.setCurrentFile(sfile);
                else {
                    JOptionPane.showMessageDialog(null,
                            "i NEED JABAI ENTRY POINT",
                            "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
                    return;
                }

            }

            if (getTextEditor().getCurrentSelectedLanguage().equalsIgnoreCase("Python"))  {
                if (sfile.getStringPath().toLowerCase().endsWith(".py")) fileManager.setCurrentFile(sfile);
                else {
                    JOptionPane.showMessageDialog(null,
                            "i NEED PYTHON ENTRY POINT",
                            "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            getTextEditor().setEntryPointButton.setText(String.valueOf(sfile.getPath().getFileName()));
            // DEBUG SHIT
            // System.out.println("Language: " + fileManager.getLanguage());
            // System.out.println("Entry point file set to: " + fileManager.getCurrentFileStringPath());
        }
    }

    public static class ImportTestcaseButtonHandler extends ComponentHandler {
        public ImportTestcaseButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            FileManager fm =  fe.getFileManager();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Select Testcase File");
            fileChooser.setCurrentDirectory(fm != null ? fm.getRootdir().toFile() : null);

            int result = fileChooser.showOpenDialog(getTextEditor());

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null && selectedFile.isFile()) {
                    try {
                        if (selectedFile.getPath().endsWith(".ccpp")) {
                            fe.setTestcaseFile(new SFile(selectedFile.getPath()));
                        } else {
                            throw new InvalidFileException("Invalid file! Please select .ccpp files for testcases.");
                        }

                    } catch (InvalidFileException ex) {
                        JOptionPane.showMessageDialog(getTextEditor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            if (fe.getTestcaseFile() != null) {
                System.out.println("Testcase Content: " + fe.getTestcaseFile().getContent());
            } else {
                System.out.println("No testcase file selected.");
            }
        }
    }
    public static class ExportTestcaseButtonHandler extends ComponentHandler {
        public ExportTestcaseButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            FileManager fm =  fe.getFileManager();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Export to");
            fileChooser.setCurrentDirectory(fm != null ? fm.getRootdir().toFile() : null);

            int result = fileChooser.showOpenDialog(getTextEditor());

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                SFile toSave = fe.getDummyExportFile(); // this will be changed once testcase generation is possible
                if (selectedDirectory != null && selectedDirectory.isDirectory()) {
                    try {
                        Path dest = selectedDirectory.toPath();
                        Path fileName = toSave.getPath().getFileName();
                        Path finalDest = dest.resolve(fileName);

                        String content = Files.readString(toSave.getPath());
                        Files.writeString(finalDest, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        System.out.println("Exported to: " + finalDest);

                    } catch (IOException ex) { // we don't have to catch NotDir because we only display directories anyway
                        JOptionPane.showMessageDialog(getTextEditor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
    /* --------------- Button Handlers --------------- */

    public static void main(String[] args) {
        setNimbusLaf();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CodeChum++");
            TextEditor editor = new TextEditor();
            frame.setContentPane(editor);

            // Set window icon (for taskbar/dock)
            URL url = TextEditor.class.getResource("/assets/logo2.png");
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image image = icon.getImage();
                frame.setIconImage(image);

                // For macOS Dock icon specifically
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    try {
                        // Use Apple's Taskbar API
                        Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
                        java.lang.reflect.Method getTaskbar = taskbarClass.getMethod("getTaskbar");
                        Object taskbar = getTaskbar.invoke(null);
                        java.lang.reflect.Method setIconImage = taskbarClass.getMethod("setIconImage", Image.class);
                        setIconImage.invoke(taskbar, image);
                    } catch (Exception e) {
                        // Fallback to window icon
                        frame.setIconImage(image);
                    }
                }
            }

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 800);
            frame.setLocationRelativeTo(null);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        });
    }
}