package GUI;

import CustomExceptions.NotDirException;
import FileManagement.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class TextEditor extends JPanel {
    private JButton runCodeButton;
    private JButton addFileButton;
    private JButton createButton;
    private JButton openFolderButton;
    private JButton setEntryPointButton;
    private JTextArea dTextArea;
    private JComboBox<String> languageSelectDropdown;
    private FileExplorer fileExplorerPanel;
    private JTextArea actualOutputArea;
    private JTextArea expectedOutputArea;
    private SFile entryPointFile;

    public TextEditor() {
        initializeComponents();
        initializeBackend();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        runCodeButton = new JButton("Run Code");
        addFileButton = new JButton("Add File");
        createButton = new JButton("Add Folder"); // RENAMED: Initialize the folder button
        openFolderButton = new JButton("Open Folder");
        setEntryPointButton = new JButton("Set Entry Point");
        setEntryPointButton.setVisible(false); // Hidden by default

        dTextArea = new JTextArea();
        languageSelectDropdown = new JComboBox<>(new String[]{"C", "C++", "Java", "Python"});
        actualOutputArea = new JTextArea();
        expectedOutputArea = new JTextArea();

        fileExplorerPanel = new FileExplorer(".", dTextArea, this);
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        add(createLeftPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        JSeparator divider = new JSeparator(JSeparator.VERTICAL);
        divider.setPreferredSize(new Dimension(1, 0));
        add(divider, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        add(createRightPanel(), gbc);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // ROW 0: File Buttons (Open, Add File, Add Folder)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel fileButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fileButtonsPanel.add(openFolderButton);
        fileButtonsPanel.add(addFileButton);
        fileButtonsPanel.add(createButton); // New Folder Button
        panel.add(fileButtonsPanel, gbc);

        // Spacer
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(Box.createHorizontalGlue(), gbc);

        // Entry Point Button
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(setEntryPointButton, gbc);

        // Language Label
        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Language:"), gbc);

        // Language Dropdown
        gbc.gridx = 4;
        gbc.weightx = 0.0;
        languageSelectDropdown.setPreferredSize(new Dimension(120, 25));
        panel.add(languageSelectDropdown, gbc);

        // ROW 1: File Explorer and Editor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(fileExplorerPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4; // Spans the remaining columns
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        JScrollPane editorScroll = new JScrollPane(dTextArea);
        editorScroll.setBorder(BorderFactory.createTitledBorder("Editor"));
        panel.add(editorScroll, gbc);

        // ROW 2: Run Code Button
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Spans to the end
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(runCodeButton);
        panel.add(buttonPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(Box.createHorizontalGlue(), gbc);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        JScrollPane actualScroll = new JScrollPane(actualOutputArea);
        actualScroll.setBorder(BorderFactory.createTitledBorder("Actual Output"));
        panel.add(actualScroll, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane expectedScroll = new JScrollPane(expectedOutputArea);
        expectedScroll.setBorder(BorderFactory.createTitledBorder("Expected Output"));
        panel.add(expectedScroll, gbc);

        return panel;
    }

    private void initializeBackend() {

        dTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        dTextArea.setTabSize(4);

        actualOutputArea.setEditable(false);
        expectedOutputArea.setEditable(false);
        actualOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        expectedOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
    }

    public void saveCurrentFileContent() {
        FileManager fileManager = fileExplorerPanel.getFileManager();
        SFile currentFile = fileManager != null ? fileManager.getCurrentFile() : null;

        if (currentFile != null) {
            String content = dTextArea.getText();
            currentFile.setContent(content);
            currentFile.writeOut();
            System.out.println("File saved: " + currentFile.getStringPath());
        }
    }

    public SFile getEntryPointFile() {
        return entryPointFile;
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

        openFolderButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Project Root Folder");

            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDir = fileChooser.getSelectedFile();
                if (selectedDir != null && selectedDir.isDirectory()) {
                    try {
                        saveCurrentFileContent();
                        fileExplorerPanel.updateRootDirectory(selectedDir.getAbsolutePath());
                        dTextArea.setText("");
                        actualOutputArea.setText("Successfully loaded new project: " + selectedDir.getName());
                    } catch (NotDirException ex) {
                        JOptionPane.showMessageDialog(this, "Error loading directory: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        languageSelectDropdown.addActionListener(e -> {
            FileManager fileManager = fileExplorerPanel.getFileManager();
            String newLanguage = (String) languageSelectDropdown.getSelectedItem();

            if (newLanguage.equalsIgnoreCase("Java") || newLanguage.equalsIgnoreCase("Python")) {
                setEntryPointButton.setVisible(true);
            }

            fileManager.setLanguage(newLanguage);

            dTextArea.setText("");

            setEntryPointButton.setText("Set Entry Point");

            actualOutputArea.setText("");
            expectedOutputArea.setText("");

            System.out.println("Project language changed to: " + newLanguage + ". Entry point reset.");
        });

        setEntryPointButton.addActionListener(e -> {
            FileManager fileManager = fileExplorerPanel.getFileManager();
            DefaultMutableTreeNode node = fileExplorerPanel.getSelectedNode();

            if (node == null || !(node.getUserObject() instanceof SFile sfile)) {
                JOptionPane.showMessageDialog(null,
                        "Please select a file in the File Explorer to set as the entry point.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (Files.isDirectory(sfile.getPath())) {
                JOptionPane.showMessageDialog(null,
                        "Cannot set a folder as the entry point. Please select a file.",
                        "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Path entryPath = sfile.getPath();
            String language = fileManager.getLanguage();
            String fileExtension = "";

            String fileName = entryPath.getFileName().toString();
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0 && lastDot < fileName.length() - 1) {
                fileExtension = fileName.substring(lastDot + 1);
            }

            String fileLanguage = switch (fileExtension.toLowerCase()) {
                case "java" -> "java";
                case "c", "cpp", "h", "hpp" -> "c++";
                case "py" -> "python";
                default -> "unknown";
            };

            if (!fileLanguage.toLowerCase().equals(language.toLowerCase())) {
                JOptionPane.showMessageDialog(null,
                        "The file's language (" + fileLanguage + ") does not match the project's selected language (" + language + ").",
                        "Language Mismatch Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            fileManager.setCurrentFile(sfile);

            setEntryPointButton.setText("Entry: " + sfile.getPath().getFileName().toString());

            try {
                String content = Files.readString(sfile.getPath());
                dTextArea.setText(content);
            } catch (IOException ex) {
                dTextArea.setText("// Error loading entry point file: " + ex.getMessage());
            }

            JOptionPane.showMessageDialog(null,
                    sfile.getPath().getFileName().toString() + " set as the compilation entry point.");
        });
        createButton.addActionListener(e -> {
            fileExplorerPanel.handleCreateFolderAction();
        });


        addFileButton.addActionListener(e -> {
            handleAddFileAction();
        });

        runCodeButton.addActionListener(e -> {
            FileManager fileManager = fileExplorerPanel.getFileManager();
            saveCurrentFileContent();
            SFile entryFile = fileManager.getCurrentFile();

            if (entryFile == null) {
                JOptionPane.showMessageDialog(null,
                        "No file is currently set as the entry point (current file).",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            actualOutputArea.setText("Executing code...\nEntry Point: " + entryFile.getPath().getFileName() + "\n");
            expectedOutputArea.setText("Expected output will appear here");
        });
    }
    public void handleAddFileAction() {
        // NOTE: This method contains the exact logic that was previously inside addFileButton.addActionListener
        FileManager fileManager = fileExplorerPanel.getFileManager();
        if (fileManager == null) return;

        DefaultMutableTreeNode selectedNode = fileExplorerPanel.getSelectedNode();
        Path targetDir = fileManager.getRootdir();
        DefaultMutableTreeNode parentNodeInTree;

        // --- Logic to determine target directory and parent node ---
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
        // --- End logic to determine target directory and parent node ---

        String fileName = JOptionPane.showInputDialog(this, "Enter new file name (with extension):");
        if (fileName == null || fileName.isBlank()) return;

        if (!fileManager.isAllowedFile(fileName)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid file extension.\nAllowed: .c, .cpp, .h, .hpp, .java, .py",
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
            // newSFile.writeOut(); // Assumes SFile constructor handles file creation or we write out explicitly later
            newSFile.writeOut(); // Writing out now ensures the file exists before tracking

            fileManager.getFiles().add(newSFile);
            fileManager.setCurrentFile(newSFile);
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

    public String getSelectedLanguage() {
        return (String) languageSelectDropdown.getSelectedItem();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Text Editor with File Explorer");
            TextEditor editor = new TextEditor();
            frame.setContentPane(editor);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}