package com.exception.ccpp.GUI;

import com.exception.ccpp.CCJudge.Judge;
import com.exception.ccpp.CustomExceptions.InvalidFileException;
import com.exception.ccpp.CustomExceptions.NotDirException;
import com.exception.ccpp.FileManagement.*;
import java.awt.event.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TextEditor extends JPanel {
    private JButton runCodeButton;
    private JButton addFileButton;
    private JButton createButton;
    private JButton openFolderButton;
    private JButton createFolderButton;
    private JButton setEntryPointButton;
    private JTextArea dTextArea;
    private JComboBox<String> languageSelectDropdown;
    private FileExplorer fileExplorerPanel;
    private JTextArea actualOutputArea;
    private JTextArea expectedOutputArea;
    private JButton importTestcaseButton;
    private JButton exportTestcaseButton;

    public TextEditor() {
        initializeComponents();
        initializeBackend();
        setupLayout();
        setupEventListeners();
        setupTabToSpaces();
    }

    public TextEditor(String folderPath, MainMenu mainMenu) {
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
    private void setupLayout() {
        setLayout(new GridBagLayout());
        setBackground(Color.decode("#28313b"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        add(createLeftPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        JSeparator divider = new JSeparator(JSeparator.VERTICAL);
        divider.setBackground(Color.decode("#28313b"));
        divider.setPreferredSize(new Dimension(1, 0));
        add(divider, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.BOTH;
        add(createRightPanel(), gbc);
    }


    private void setupEventListeners() {
        dTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_S) &&
                        ((e.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0)) {

                    e.consume();
                    saveCurrentFileContent();
                    actualOutputArea.setText("File saved successfully.");
                }
            }
        });

        openFolderButton.addActionListener(new OpenFolderButtonHandler(this));
        addFileButton.addActionListener(new AddFileButtonHandler(this));
        languageSelectDropdown.addActionListener(new LanguageSelectHandler(this));
        runCodeButton.addActionListener(new RunButtonHandler(this));
        createFolderButton.addActionListener(e -> {
            fileExplorerPanel.handleCreateFolderAction();
        });
        setEntryPointButton.addActionListener(new SetEntryPointButtonHandler(this));
        importTestcaseButton.addActionListener(new ImportTestcaseButtonHandler(this));
        exportTestcaseButton.addActionListener(new ExportTestcaseButtonHandler(this));
    }

    private void initializeComponents() {
        runCodeButton = new RoundedButton("Run Code", 30);
        runCodeButton.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        runCodeButton.setBackground(Color.decode("#568afc"));
        runCodeButton.setForeground(Color.WHITE);
        runCodeButton.setBorderPainted(false);
        runCodeButton.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

        createButton = new JButton("Add Folder");

        addFileButton = new RoundedButton("", 15);
        URL urlAddFileButton = TextEditor.class.getResource("/assets/add_file.png");
        if (urlAddFileButton != null) {
            ImageIcon iconAddFileButton = new ImageIcon(urlAddFileButton);
            // Scale the image down for a better fit inside the button (e.g., 20x20 pixels)
            Image originalImage = iconAddFileButton.getImage();
            Image scaledImage = originalImage.getScaledInstance(20, 20, Image.SCALE_SMOOTH);

            // Set the scaled image using the new setIconImage method
            ((RoundedButton)addFileButton).setIconImage(scaledImage);
        } else {
            System.err.println("Resource not found: /assets/add_file.png");
        }
        addFileButton.setBackground(Color.decode("#28313b"));
        addFileButton.setBorderPainted(false);
        addFileButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addFileButton.setToolTipText("Add File");
        ToolTipManager.sharedInstance().setInitialDelay(800);

        setEntryPointButton = new RoundedButton("Set Entry Point", 30);
        setEntryPointButton.setVisible(false);

        // Set Entry Point button styling to match Run Code
        setEntryPointButton.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        setEntryPointButton.setBackground(Color.decode("#568afc"));
        setEntryPointButton.setForeground(Color.WHITE);
        setEntryPointButton.setBorderPainted(false);
        setEntryPointButton.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

        openFolderButton = new RoundedButton("", 15);
        URL urlOpenFolderButton = TextEditor.class.getResource("/assets/open_folder.png");
        if (urlOpenFolderButton != null) {
            ImageIcon iconOpenFolderButton = new ImageIcon(urlOpenFolderButton);
            // Scale the image down for a better fit inside the button (e.g., 20x20 pixels)
            Image originalImage = iconOpenFolderButton.getImage();
            Image scaledImage = originalImage.getScaledInstance(20, 20, Image.SCALE_SMOOTH);

            // Set the scaled image using the new setIconImage method
            ((RoundedButton)openFolderButton).setIconImage(scaledImage);
        } else {
            System.err.println("Resource not found: /assets/open_folder.png");
        }
        openFolderButton.setBackground(Color.decode("#28313b"));
        openFolderButton.setBorderPainted(false);
        openFolderButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        openFolderButton.setToolTipText("Open Folder");
        ToolTipManager.sharedInstance().setInitialDelay(800);

        createFolderButton = new RoundedButton("", 15);
        URL urlCreateFolderButton = TextEditor.class.getResource("/assets/create_folder.png");
        if (urlOpenFolderButton != null) {
            ImageIcon iconCreateFolderButton = new ImageIcon(urlCreateFolderButton);
            // Scale the image down for a better fit inside the button (e.g., 20x20 pixels)
            Image originalImage = iconCreateFolderButton.getImage();
            Image scaledImage = originalImage.getScaledInstance(20, 20, Image.SCALE_SMOOTH);

            // Set the scaled image using the new setIconImage method
            ((RoundedButton)createFolderButton).setIconImage(scaledImage);
        } else {
            System.err.println("Resource not found: /assets/create_folder.png");
        }
        createFolderButton.setBackground(Color.decode("#28313b"));
        createFolderButton.setBorderPainted(false);
        createFolderButton.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        createFolderButton.setToolTipText("Create Folder");
        ToolTipManager.sharedInstance().setInitialDelay(800);

        // NEW: Import Testcase button
        importTestcaseButton = new RoundedButton("Import Testcase", 15);
        importTestcaseButton.setBackground(Color.decode("#568afc"));
        importTestcaseButton.setForeground(Color.WHITE);
        importTestcaseButton.setBorderPainted(false);
        importTestcaseButton.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        // NEW: Export Testcase button
        exportTestcaseButton = new RoundedButton("Export Testcase", 15);
        exportTestcaseButton.setBackground(Color.decode("#568afc"));
        exportTestcaseButton.setForeground(Color.WHITE);
        exportTestcaseButton.setBorderPainted(false);
        exportTestcaseButton.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        dTextArea = new JTextArea();
        dTextArea.setBackground(Color.decode("#1f2335"));
        dTextArea.setCaretColor(Color.WHITE);
        dTextArea.setForeground(Color.WHITE);

        languageSelectDropdown = new RoundedComboBox<>(new String[]{"C", "C++", "Java", "Python"});

        languageSelectDropdown.setBackground(Color.decode("#568afc"));
        languageSelectDropdown.setForeground(Color.WHITE);
        ((RoundedComboBox<String>) languageSelectDropdown).setRadius(20);


        actualOutputArea = new JTextArea();
        actualOutputArea.setBackground(Color.decode("#1f2335"));
        actualOutputArea.setCaretColor(Color.WHITE);
        actualOutputArea.setForeground(Color.WHITE);

        expectedOutputArea = new JTextArea();
        expectedOutputArea.setBackground(Color.decode("#1f2335"));
        expectedOutputArea.setCaretColor(Color.WHITE);
        expectedOutputArea.setForeground(Color.WHITE);

        fileExplorerPanel = new FileExplorer(".", dTextArea, this);
    }


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


    /* --------------- Setup --------------- */

    /* --------------- Util --------------- */
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

    /* --------------- Util --------------- */

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#28313b"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Top Panel (buttons + language)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(createTopPanel(), gbc);

        // Editors Panel (file explorer + text editor)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(createEditorsPanel(), gbc);

        // Bottom Panel (run code + set entry point)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(createBottomPanel(), gbc);

        return panel;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#28313b"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);

        // File buttons
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel fileButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fileButtonsPanel.setBackground(Color.decode("#28313b"));
        openFolderButton.setPreferredSize(new Dimension(30, 30)); // ang background sa buttons
        fileButtonsPanel.add(openFolderButton);
        addFileButton.setPreferredSize(new Dimension(30, 30)); // ang background sa buttons
        fileButtonsPanel.add(addFileButton);
        createFolderButton.setPreferredSize(new Dimension(30, 30)); // ang background sa buttons
        fileButtonsPanel.add(createFolderButton);
        fileButtonsPanel.add(importTestcaseButton);
        fileButtonsPanel.add(exportTestcaseButton);
        panel.add(fileButtonsPanel, gbc);

        // Spacer
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(Box.createHorizontalGlue(), gbc);

        // Language Label
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel languageLabel = new JLabel("Language:  ");
        languageLabel.setBackground(Color.decode("#28313b"));
        languageLabel.setForeground(Color.WHITE);
        languageLabel.setOpaque(true);
        panel.add(languageLabel, gbc);

        // Language Dropdown
        gbc.gridx = 3;
        gbc.weightx = 0.0;
        languageSelectDropdown.setPreferredSize(new Dimension(120, 25));
        panel.add(languageSelectDropdown, gbc);

        return panel;
    }

    private JPanel createEditorsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#28313b"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 0);

        // File Explorer
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 0.0;
        fileExplorerPanel.setPreferredSize(new Dimension(175, Integer.MAX_VALUE));
        panel.add(fileExplorerPanel, gbc);

        // Text Editor
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 1, 5, 0);
        JScrollPane editorScroll = new JScrollPane(dTextArea);
        editorScroll.setBorder(BorderFactory.createTitledBorder("Text Editor"));
        editorScroll.setBackground(Color.decode("#1f2335"));
        TitledBorder titledBorder = (TitledBorder) editorScroll.getBorder();
        titledBorder.setTitleColor(Color.WHITE);
        panel.add(editorScroll, gbc);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(Color.decode("#28313b"));
        panel.add(setEntryPointButton);
        panel.add(runCodeButton);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#28313b"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        JScrollPane actualScroll = new JScrollPane(actualOutputArea);
        actualScroll.setBackground(Color.decode("#1f2335"));
        actualScroll.setBorder(BorderFactory.createTitledBorder("Actual Output"));
        TitledBorder titledBorder3 = (TitledBorder) actualScroll.getBorder();
        titledBorder3.setTitleColor(Color.WHITE);
        panel.add(actualScroll, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JSeparator tempSeparator = new JSeparator(JSeparator.HORIZONTAL);
        panel.add(tempSeparator, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane expectedScroll = new JScrollPane(expectedOutputArea);
        expectedScroll.setBackground(Color.decode("#1f2335"));
        expectedScroll.setBorder(BorderFactory.createTitledBorder("Expected Output"));
        TitledBorder titledBorder2 = (TitledBorder) expectedScroll.getBorder();
        titledBorder2.setTitleColor(Color.WHITE);
        panel.add(expectedScroll, gbc);

        return panel;
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

        runCodeButton.addActionListener(e -> {
            saveCurrentFileContent();
            String out = Judge.judge(fm, new String[]{}).output();
            actualOutputArea.setText(out);
            expectedOutputArea.setText("Expected output will appear here");
        });
        createFolderButton.addActionListener(e -> {
            fileExplorerPanel.handleCreateFolderAction();
        });
        setEntryPointButton.addActionListener(e -> {
            FileManager fileManager = fileExplorerPanel.getFileManager();
            JTree fe_tree = fileExplorerPanel.getFeTree();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
            if (node == null || !(node.getUserObject() instanceof SFile sfile)) return;

            if (Files.isDirectory(sfile.getPath())) {
                JOptionPane.showMessageDialog(null,
                        "yo this is a folder gang you can't set folders as entry points",
                        "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (getCurrentSelectedLanguage().equalsIgnoreCase("Java")) {
                if (sfile.getStringPath().toLowerCase().endsWith(".java")) fileManager.setCurrentFile(sfile);
                else {
                    JOptionPane.showMessageDialog(null,
                            "i NEED JABAI ENTRY POINT",
                            "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
                    return;
                }

            }

            if (getCurrentSelectedLanguage().equalsIgnoreCase("Python"))  {
                if (sfile.getStringPath().toLowerCase().endsWith(".py")) fileManager.setCurrentFile(sfile);
                else {
                    JOptionPane.showMessageDialog(null,
                            "i NEED PYTHON ENTRY POINT",
                            "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            setEntryPointButton.setText(String.valueOf(sfile.getPath().getFileName()));
        });
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
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Project Root Folder");

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

    public static class RunButtonHandler extends ComponentHandler {
        public RunButtonHandler(TextEditor editor) {
            super(editor);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            getTextEditor().saveCurrentFileContent();
            FileManager fm = fe.getFileManager();
            String out = Judge.judge(fm, new String[]{}).output();
            getTextEditor().actualOutputArea.setText(out);
            getTextEditor().expectedOutputArea.setText("Expected output will appear here");
        }
    }

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
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Select Testcase File");

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
            System.out.println("Testcase Content: " + fe.getTestcaseFile().getContent());
        }
    }
    public static class ExportTestcaseButtonHandler extends ComponentHandler {
        public ExportTestcaseButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Export to");

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
            frame.setVisible(true);
        });
    }
}

