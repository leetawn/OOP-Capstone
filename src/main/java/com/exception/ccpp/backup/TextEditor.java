//package com.exception.ccpp.backup;
//
//import com.exception.ccpp.CCJudge.Judge;
//import com.exception.ccpp.CustomExceptions.InvalidFileException;
//import com.exception.ccpp.CustomExceptions.NotDirException;
//import com.exception.ccpp.FileManagement.FileManager;
//import com.exception.ccpp.FileManagement.SFile;
//import com.exception.ccpp.GUI.ComponentHandler;
//import com.exception.ccpp.GUI.FileExplorer;
//import com.exception.ccpp.GUI.MainMenu;
//
//import javax.swing.*;
//import javax.swing.text.*;
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.DefaultTreeModel;
//import javax.swing.tree.TreePath;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardOpenOption;
//
//public class TextEditor extends JPanel {
//    private JButton runCodeButton;
//    private JButton addFileButton;
//    private JButton createButton;
//    private JButton openFolderButton;
//    private JButton createFolderButton;
//    private JButton setEntryPointButton;
//    private JTextArea dTextArea;
//    private JComboBox<String> languageSelectDropdown;
//    private FileExplorer fileExplorerPanel;
//    private JTextPane actualOutputArea;
//    private JTextPane expectedOutputArea;
//    private JButton importTestcaseButton;
//    private JButton exportTestcaseButton;
//
//    private SimpleAttributeSet matchStyle;
//    private SimpleAttributeSet mismatchStyle;
//    private SimpleAttributeSet excessStyle;
//    private SimpleAttributeSet defaultStyle;
//
//    public TextEditor() {
//        initializeComponents();
//        initializeBackend();
//        initializeStyles();
//        setupLayout();
//        setupEventListeners();
//        setupTabToSpaces();
//
//    }
//
//    public TextEditor(String folderPath, MainMenu mainMenu) {
//        this(); // call the original no-arg constructor that builds your UI
//
//        // Close the MainMenu immediately when TextEditor starts
//        if (mainMenu != null) {
//            mainMenu.setVisible(false);
//        }
//
//        try {
//            fileExplorerPanel.updateRootDirectory(folderPath);
//            setTextArea(false); // disable editing since no file is selected yet
//        } catch (NotDirException ex) {
//            JOptionPane.showMessageDialog(this,
//                    "Failed to load project folder:\n" + ex.getMessage(),
//                    "Invalid Folder",
//                    JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    /* --------------- Setup --------------- */
//
//    private void setupTabToSpaces() {
//        final String fourSpaces = "    ";
//
//        Action insertSpacesAction = new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                dTextArea.replaceSelection(fourSpaces);
//            }
//        };
//
//        KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
//
//        Object actionKey = "insert-four-spaces";
//
//        InputMap inputMap = dTextArea.getInputMap(JComponent.WHEN_FOCUSED);
//        inputMap.put(tabKey, actionKey);
//
//        dTextArea.getActionMap().put(actionKey, insertSpacesAction);
//    }
//    private void initializeStyles() {
//        defaultStyle = new SimpleAttributeSet();
//        StyleConstants.setForeground(defaultStyle, Color.BLACK);
//        StyleConstants.setBackground(defaultStyle, Color.decode("#1f2335"));
//
//        matchStyle = new SimpleAttributeSet();
//        StyleConstants.setForeground(matchStyle, Color.BLACK);
//        StyleConstants.setBackground(matchStyle, new Color(176, 237, 184)); // Light Green (Match)
//
//        mismatchStyle = new SimpleAttributeSet();
//        StyleConstants.setForeground(mismatchStyle, Color.BLACK);
//        StyleConstants.setBackground(mismatchStyle, new Color(228, 163, 159)); // Lacking
//
//        excessStyle = new SimpleAttributeSet();
//        StyleConstants.setForeground(excessStyle, Color.BLACK); // Use Black FG for visibility on Yellow
//        StyleConstants.setBackground(excessStyle, new Color(245, 224, 59)); // Yellow (Excess)
//    }
//
//    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//    private void setupLayout() {
//
//
//        // use gribaglayouts for all
//        // call the create panels
//        // make sure each border color lines are different for all
//    }
//
//    private void initializeComponents() {
//        // Keep it blank for now
//    }
//
//
//    private JPanel create_first_panel() {
//
//    }
//
//    private JPanel create_second_panel() {
//
//    }
//
//    private JPanel create_third_panel() {
//
//    }
//
//    private JPanel create_fourth_panel() {
//
//    }
//
//    private JPanel create_fifth_panel() {
//
//    }
//
//    // END OF FRONT END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//    // START OF BACK END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
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
//
//    private void initializeBackend() {
//
//        dTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
//        dTextArea.setText("No selected file. Select a file to start editing.");
//        dTextArea.setEditable(false);
//
//        actualOutputArea.setEditable(false);
//        actualOutputArea.setCaretColor(Color.decode("#1f2335"));
//        expectedOutputArea.setEditable(false);
//        expectedOutputArea.setCaretColor(Color.decode("#1f2335"));
//        actualOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//        expectedOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//    }
//
//    private void displayActualDiff(String actualText, String expectedText) {
//        StyledDocument doc = actualOutputArea.getStyledDocument();
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
//                char expectedChar = (j < expectedLine.length()) ? expectedLine.charAt(j) : 0;
//
//                AttributeSet styleToApply;
//                char charToProcess = actualChar; // Start with the actual character
//
//                // --- 1. Comparison Logic (Inverted: Focus on Expected Status) ---
//
//                if (expectedChar == 0 && actualChar != 0) {
//                    // 1. EXCESS (Yellow): Actual output continues beyond Expected length
//                    styleToApply = excessStyle; // Yellow
//
//                } else if (expectedChar != 0 && actualChar == expectedChar) {
//                    // 2. MATCH (Green): Expected character was found exactly
//                    styleToApply = matchStyle; // Green
//
//                } else {
//                    // 3. LACKING / MISMATCH (Red):
//                    //    - Expected char exists, but Actual is different, OR
//                    //    - Expected char exists, but Actual is missing (actualChar == 0).
//                    styleToApply = mismatchStyle; // Red
//
//                    // If Actual is missing, insert a space placeholder to show the red box
//                    if (actualChar == 0) {
//                        charToProcess = ' ';
//                    }
//                }
//
//                // --- 2. Visualization Logic (Based on the character being displayed) ---
//                String charToDisplay;
//
//                if (charToProcess == '\t') {
//                    charToDisplay = "    "; // 4 spaces for Tab
//                } else if (charToProcess == '\r' || charToProcess == '\n') {
//                    charToDisplay = "\u2424"; // Symbol for Newline
//                } else if (charToProcess == 0 || charToProcess == ' ') {
//                    // Handles the space placeholder for LACKING or actual space characters
//                    charToDisplay = " ";
//                } else {
//                    charToDisplay = String.valueOf(charToProcess);
//                }
//
//                try {
//                    // Insert the visual representation of the character with the determined style
//                    doc.insertString(doc.getLength(), charToDisplay, styleToApply);
//                } catch (BadLocationException ignored) {}
//            }
//
//            // Add a literal newline to advance the cursor in the JTextPane
//            if (i < maxLines - 1) {
//                try {
//                    doc.insertString(doc.getLength(), "\n", defaultStyle);
//                } catch (BadLocationException ignored) {}
//            }
//        }
//    }
//
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
//                char expectedChar = (j < expectedLine.length()) ? expectedLine.charAt(j) : 0;
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
//
//    public void saveCurrentFileContent() {
//        SFile currentFile = fileExplorerPanel.getSelectedFile(); // <-- Use the new source of truth
//
//        String placeholderText = "No file selected. Select a file to start editing.";
//        String content = dTextArea.getText();
//
//        if (currentFile != null && !content.equals(placeholderText)) {
//            currentFile.setContent(content);
//            currentFile.writeOut();
//            System.out.println("File saved: " + currentFile.getStringPath());
//        }
//    }
//
//    public void handleAddFileAction() {
//        FileManager fm = fileExplorerPanel.getFileManager();
//        if (fm == null) return;
//
//        DefaultMutableTreeNode selectedNode = fileExplorerPanel.getSelectedNode();
//        Path targetDir = fm.getRootdir();
//        DefaultMutableTreeNode parentNodeInTree;
//
//        // Logic to determine target directory and parent node (for insertion in JTree)
//        if (selectedNode != null) {
//            Object obj = selectedNode.getUserObject();
//
//            if (obj instanceof SFile sfile) {
//                if (Files.isDirectory(sfile.getPath())) {
//                    targetDir = sfile.getPath();
//                    parentNodeInTree = selectedNode;
//                } else {
//                    targetDir = sfile.getPath().getParent();
//                    parentNodeInTree = (DefaultMutableTreeNode) selectedNode.getParent();
//                }
//            } else {
//                targetDir = fileExplorerPanel.resolveNodeToPath(selectedNode);
//                parentNodeInTree = selectedNode;
//            }
//        } else {
//            parentNodeInTree = (DefaultMutableTreeNode) fileExplorerPanel.getFeTree().getModel().getRoot();
//        }
//
//        String fileName = JOptionPane.showInputDialog(this, "Enter new file name (with extension):");
//        if (fileName == null || fileName.isBlank()) return;
//
//        // Use the FileManager's language-based validation for new files
//        if (!fm.isAllowedFile(fileName)) {
//            JOptionPane.showMessageDialog(this,
//                    "Invalid file extension for the current project language (" + fm.getLanguage() + ").",
//                    "Invalid Extension",
//                    JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        try {
//            Path newFilePath = targetDir.resolve(fileName);
//
//            if (Files.exists(newFilePath)) {
//                JOptionPane.showMessageDialog(this, "File already exists in " + targetDir);
//                return;
//            }
//
//            saveCurrentFileContent();
//
//            SFile newSFile = new SFile(newFilePath);
//            newSFile.writeOut();
//
//            fm.getFiles().add(newSFile);
//            fm.setCurrentFile(newSFile);
//            dTextArea.setText(newSFile.getContent());
//
//            DefaultMutableTreeNode newFileNode = new DefaultMutableTreeNode(newSFile);
//            DefaultTreeModel model = (DefaultTreeModel) fileExplorerPanel.getFeTree().getModel();
//
//            model.insertNodeInto(newFileNode, parentNodeInTree, parentNodeInTree.getChildCount());
//
//            fileExplorerPanel.getFeTree().expandPath(new TreePath(parentNodeInTree.getPath()));
//            fileExplorerPanel.getFeTree().setSelectionPath(new TreePath(newFileNode.getPath()));
//
//            JOptionPane.showMessageDialog(this, "File created: " + newFilePath.getFileName());
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this,
//                    "Error creating file: " + ex.getMessage(),
//                    "Error", JOptionPane.ERROR_MESSAGE);
//        }
//
//    }
//    /* --------------- Util --------------- */
//    /* --------------- Getters & Setters --------------- */
//
//    public String getCurrentSelectedLanguage() {
//        return (String) languageSelectDropdown.getSelectedItem();
//    }
//
//    public void setTextArea(boolean ok) {
//        this.dTextArea.setEditable(ok);
//        if (!ok) {
//            this.dTextArea.setText("No file selected. Please open a project or select a file to begin editing.");
//
//            fileExplorerPanel.setSelectedFile(null);
//        }
//    }
//    public static void setNimbusLaf() {
//        try {
//            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("nooooooo garbage ui for now");
//        }
//    }
//
//    public JButton getSetEntryPointButton() {
//        return setEntryPointButton;
//    }
//
//    /* --------------- Getters & Setters --------------- */
//    /* --------------- Button Handlers --------------- */
//
//    public static class OpenFolderButtonHandler extends ComponentHandler {
//
//        public OpenFolderButtonHandler(TextEditor editor) {
//            super(editor);
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            FileExplorer fe = getTextEditor().fileExplorerPanel;
//            FileManager fm = fe.getFileManager();
//            JFileChooser fileChooser = new JFileChooser();
//            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//            fileChooser.setDialogTitle("Select Project Root Folder");
//            fileChooser.setCurrentDirectory(fm.getRootdir().toFile());
//
//            int result = fileChooser.showOpenDialog(getTextEditor());
//
//            if (result == JFileChooser.APPROVE_OPTION) {
//                File selectedDir = fileChooser.getSelectedFile();
//                if (selectedDir != null && selectedDir.isDirectory()) {
//                    try {
//                        getTextEditor().saveCurrentFileContent();
//                        getTextEditor().fileExplorerPanel.updateRootDirectory(selectedDir.getAbsolutePath());
//                        getTextEditor().setTextArea(false);
//                        getTextEditor().actualOutputArea.setText("Successfully loaded new project: " + selectedDir.getName());
//                    } catch (NotDirException ex) {
//                        JOptionPane.showMessageDialog(getTextEditor(), "Error loading directory: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//                    }
//                }
//            }
//        }
//    };
//
//    public static class AddFileButtonHandler extends ComponentHandler {
//        public AddFileButtonHandler(TextEditor editor) {
//            super(editor);
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            FileExplorer fe = getTextEditor().fileExplorerPanel;
//            FileManager fileManager = fe.getFileManager();
//            if (fileManager == null) return;
//
//            DefaultMutableTreeNode selectedNode = fe.getSelectedNode();
//            Path targetDir = fileManager.getRootdir();
//            DefaultMutableTreeNode parentNodeInTree;
//
//            if (selectedNode != null) {
//                Object obj = selectedNode.getUserObject();
//
//                if (obj instanceof SFile sfile) {
//                    if (Files.isDirectory(sfile.getPath())) {
//                        targetDir = sfile.getPath();
//                        parentNodeInTree = selectedNode;
//                    } else {
//                        targetDir = sfile.getPath().getParent();
//                        parentNodeInTree = (DefaultMutableTreeNode) selectedNode.getParent();
//                    }
//                } else {
//                    targetDir = fe.resolveNodeToPath(selectedNode);
//                    parentNodeInTree = selectedNode;
//                }
//            } else {
//                parentNodeInTree = (DefaultMutableTreeNode) fe.getFeTree().getModel().getRoot();
//            }
//
//
//            String fileName = JOptionPane.showInputDialog(getTextEditor(), "Enter new file name (with extension):");
//            if (fileName == null || fileName.isBlank()) return;
//
//            if (!fileManager.isAllowedFile(fileName)) {
//                JOptionPane.showMessageDialog(getTextEditor(),
//                        "Invalid file extension.\nAllowed: .c, .cpp, .h, .hpp, .java, .py",
//                        "Invalid Extension",
//                        JOptionPane.WARNING_MESSAGE);
//                return;
//            }
//
//            try {
//                Path newFilePath = targetDir.resolve(fileName);
//
//                if (Files.exists(newFilePath)) {
//                    JOptionPane.showMessageDialog(getTextEditor(), "File already exists in " + targetDir);
//                    return;
//                }
//
//                getTextEditor().saveCurrentFileContent();
//
//                SFile newSFile = new SFile(newFilePath);
//                newSFile.writeOut();
//                fileManager.getFiles().add(newSFile);
//                fileManager.setCurrentFile(newSFile);
//                getTextEditor().dTextArea.setText(newSFile.getContent());
//
//                DefaultMutableTreeNode newFileNode = new DefaultMutableTreeNode(newSFile);
//                DefaultTreeModel model = (DefaultTreeModel) fe.getFeTree().getModel();
//
//                model.insertNodeInto(newFileNode, parentNodeInTree, parentNodeInTree.getChildCount());
//
//                fe.getFeTree().expandPath(new TreePath(parentNodeInTree.getPath()));
//                fe.getFeTree().setSelectionPath(new TreePath(newFileNode.getPath()));
//
//                JOptionPane.showMessageDialog(getTextEditor(), "File created: " + newFilePath);
//            } catch (Exception ex) {
//                JOptionPane.showMessageDialog(getTextEditor(),
//                        "Error creating file: " + ex.getMessage(),
//                        "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }
//
//    public static class LanguageSelectHandler extends ComponentHandler {
//        public LanguageSelectHandler(TextEditor editor) {
//            super(editor);
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            FileExplorer fe = getTextEditor().fileExplorerPanel;
//            FileManager fileManager = fe.getFileManager();
//            String newLanguage = (String) getTextEditor().languageSelectDropdown.getSelectedItem();
//
//            if (newLanguage.equalsIgnoreCase("Java") || newLanguage.equalsIgnoreCase("Python")) {
//                getTextEditor().setEntryPointButton.setVisible(true);
//            } else {
//                getTextEditor().setEntryPointButton.setVisible(false);
//            }
//            fileManager.setLanguage(newLanguage);
//
//            fileManager.setCurrentFile(null);
//
//            getTextEditor().setEntryPointButton.setText("Set Entry Point");
//
//            getTextEditor().actualOutputArea.setText("");
//            getTextEditor().expectedOutputArea.setText("");
//
//            System.out.println("Project language changed to: " + newLanguage + ". Entry point reset.");
//        }
//    }
//
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
//
//    public static class SetEntryPointButtonHandler extends ComponentHandler {
//        public SetEntryPointButtonHandler(TextEditor editor) {
//           super(editor);
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            FileExplorer fe = getTextEditor().fileExplorerPanel;
//            FileManager fileManager = fe.getFileManager();
//            JTree fe_tree = fe.getFeTree();
//            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
//            if (node == null || !(node.getUserObject() instanceof SFile sfile)) return;
//
//            if (Files.isDirectory(sfile.getPath())) {
//                JOptionPane.showMessageDialog(null,
//                        "yo this is a folder gang you can't set folders as entry points",
//                        "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
//                return;
//            }
//
//            if (getTextEditor().getCurrentSelectedLanguage().equalsIgnoreCase("Java")) {
//                if (sfile.getStringPath().toLowerCase().endsWith(".java")) fileManager.setCurrentFile(sfile);
//                else {
//                    JOptionPane.showMessageDialog(null,
//                            "i NEED JABAI ENTRY POINT",
//                            "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
//
//            }
//
//            if (getTextEditor().getCurrentSelectedLanguage().equalsIgnoreCase("Python"))  {
//                if (sfile.getStringPath().toLowerCase().endsWith(".py")) fileManager.setCurrentFile(sfile);
//                else {
//                    JOptionPane.showMessageDialog(null,
//                            "i NEED PYTHON ENTRY POINT",
//                            "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
//            }
//            getTextEditor().setEntryPointButton.setText(String.valueOf(sfile.getPath().getFileName()));
//            // DEBUG SHIT
//            // System.out.println("Language: " + fileManager.getLanguage());
//            // System.out.println("Entry point file set to: " + fileManager.getCurrentFileStringPath());
//        }
//    }
//
//    public static class ImportTestcaseButtonHandler extends ComponentHandler {
//        public ImportTestcaseButtonHandler(TextEditor editor) {
//            super(editor);
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            FileExplorer fe = getTextEditor().fileExplorerPanel;
//            FileManager fm =  fe.getFileManager();
//            JFileChooser fileChooser = new JFileChooser();
//            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//            fileChooser.setDialogTitle("Select Testcase File");
//            fileChooser.setCurrentDirectory(fm.getRootdir().toFile());
//
//            int result = fileChooser.showOpenDialog(getTextEditor());
//
//            if (result == JFileChooser.APPROVE_OPTION) {
//                File selectedFile = fileChooser.getSelectedFile();
//                if (selectedFile != null && selectedFile.isFile()) {
//                    try {
//                        if (selectedFile.getPath().endsWith(".ccpp")) {
//                            fe.setTestcaseFile(new SFile(selectedFile.getPath()));
//                        } else {
//                            throw new InvalidFileException("Invalid file! Please select .ccpp files for testcases.");
//                        }
//
//                   } catch (InvalidFileException ex) {
//                        JOptionPane.showMessageDialog(getTextEditor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//                    }
//                }
//            }
//            System.out.println("Testcase Content: " + fe.getTestcaseFile().getContent());
//        }
//    }
//    public static class ExportTestcaseButtonHandler extends ComponentHandler {
//        public ExportTestcaseButtonHandler(TextEditor editor) {
//            super(editor);
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            FileExplorer fe = getTextEditor().fileExplorerPanel;
//            FileManager fm =  fe.getFileManager();
//            JFileChooser fileChooser = new JFileChooser();
//            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//            fileChooser.setDialogTitle("Export to");
//            fileChooser.setCurrentDirectory(fm.getRootdir().toFile());
//
//            int result = fileChooser.showOpenDialog(getTextEditor());
//
//            if (result == JFileChooser.APPROVE_OPTION) {
//                File selectedDirectory = fileChooser.getSelectedFile();
//                SFile toSave = fe.getDummyExportFile(); // this will be changed once testcase generation is possible
//                if (selectedDirectory != null && selectedDirectory.isDirectory()) {
//                    try {
//                        Path dest = selectedDirectory.toPath();
//                        Path fileName = toSave.getPath().getFileName();
//                        Path finalDest = dest.resolve(fileName);
//
//                        String content = Files.readString(toSave.getPath());
//                        Files.writeString(finalDest, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//                        System.out.println("Exported to: " + finalDest);
//
//                    } catch (IOException ex) { // we don't have to catch NotDir because we only display directories anyway
//                        JOptionPane.showMessageDialog(getTextEditor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//                    }
//                }
//            }
//        }
//    }
//    /* --------------- Button Handlers --------------- */
//
//    public static void main(String[] args) {
//        setNimbusLaf();
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("CodeChum++");
//            TextEditor editor = new TextEditor();
//            frame.setContentPane(editor);
//
//            // Set window icon (for taskbar/dock)
//            URL url = TextEditor.class.getResource("/assets/logo2.png");
//            if (url != null) {
//                ImageIcon icon = new ImageIcon(url);
//                Image image = icon.getImage();
//                frame.setIconImage(image);
//
//                // For macOS Dock icon specifically
//                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
//                    try {
//                        // Use Apple's Taskbar API
//                        Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
//                        java.lang.reflect.Method getTaskbar = taskbarClass.getMethod("getTaskbar");
//                        Object taskbar = getTaskbar.invoke(null);
//                        java.lang.reflect.Method setIconImage = taskbarClass.getMethod("setIconImage", Image.class);
//                        setIconImage.invoke(taskbar, image);
//                    } catch (Exception e) {
//                        // Fallback to window icon
//                        frame.setIconImage(image);
//                    }
//                }
//            }
//
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(1400, 800);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
//    }
//}
//
