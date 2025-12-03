package GUI;

import CustomExceptions.NotDirException;
import FileManagement.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TextEditor extends JPanel {
    private JButton runCodeButton;
    private JButton addFileButton;
    private JTextArea dTextArea;
    private JComboBox<String> languageSelectDropdown;
    private JTree fe_tree;
    private JPanel fileExplorer;
    private FileManager fileManager;
    private JTextArea actualOutputArea;
    private JTextArea expectedOutputArea;

    public TextEditor() {
        initializeComponents();
        setupLayout();
        initializeBackend();
        setupEventListeners();
    }

    private void initializeComponents() {
        runCodeButton = new JButton("Run Code");
        addFileButton = new JButton("Add File");
        dTextArea = new JTextArea();
        languageSelectDropdown = new JComboBox<>(new String[]{"C", "C++", "Java", "Python"});
        fe_tree = new JTree();
        fileExplorer = new JPanel();
        actualOutputArea = new JTextArea();
        expectedOutputArea = new JTextArea();
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);

        // Left panel - File explorer and editor
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        add(createLeftPanel(), gbc);

        // Divider
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        JSeparator divider = new JSeparator(JSeparator.VERTICAL);
        divider.setPreferredSize(new Dimension(1, 0));
        add(divider, gbc);

        // Right panel - Output areas
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

        // Top row: Add File button (left) and Language combo (right)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(addFileButton, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(Box.createHorizontalGlue(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Language:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.0;
        languageSelectDropdown.setPreferredSize(new Dimension(120, 25));
        panel.add(languageSelectDropdown, gbc);

        // File explorer panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane treeScroll = new JScrollPane(fe_tree);
        treeScroll.setBorder(BorderFactory.createTitledBorder("File Explorer"));
        panel.add(treeScroll, gbc);

        // Text editor panel
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 0.6;
        JScrollPane editorScroll = new JScrollPane(dTextArea);
        editorScroll.setBorder(BorderFactory.createTitledBorder("Editor"));
        panel.add(editorScroll, gbc);

        // Empty space for alignment
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(Box.createHorizontalGlue(), gbc);

        // Run button (right side, bottom of Text Editor)
        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(runCodeButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Actual Output
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        JScrollPane actualScroll = new JScrollPane(actualOutputArea);
        actualScroll.setBorder(BorderFactory.createTitledBorder("Actual Output"));
        panel.add(actualScroll, gbc);

        // Divider
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gbc);

        // Expected Output
        gbc.gridy = 2;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane expectedScroll = new JScrollPane(expectedOutputArea);
        expectedScroll.setBorder(BorderFactory.createTitledBorder("Expected Output"));
        panel.add(expectedScroll, gbc);

        return panel;
    }

    private void initializeBackend() {
        try {
            fileManager = new FileManager(".", "java"); // project root
            buildFileTree();

            // Setup text editor
            dTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
            dTextArea.setTabSize(4);

            // Setup output areas
            actualOutputArea.setEditable(false);
            expectedOutputArea.setEditable(false);
            actualOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            expectedOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        } catch (NotDirException e) {
            JOptionPane.showMessageDialog(this, "Invalid directory: " + e.getMessage());
        }
    }

    private void setupEventListeners() {
        // File tree selection listener
        fe_tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
            if (node == null) return;

            Object obj = node.getUserObject();
            if (obj instanceof SFile sfile) {
                try {
                    fileManager.setCurrentFile(sfile);
                    Path filePath = sfile.getPath();
                    String content = Files.readString(filePath);
                    dTextArea.setText(content);
                    System.out.println("Current file set to: " + fileManager.getCurrentFileStringPath());
                } catch (Exception ex) {
                    dTextArea.setText("// Error loading file: " + ex.getMessage());
                }
            }
        });

        // Custom tree cell renderer
        fe_tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean sel, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {

                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObj = node.getUserObject();

                if (userObj instanceof SFile sfile) {
                    setText(sfile.getPath().getFileName().toString());
                    setIcon(UIManager.getIcon("FileView.fileIcon"));
                } else {
                    setText(userObj.toString());
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                }

                return this;
            }
        });

        languageSelectDropdown.addActionListener(e -> {
            String selectedLang = (String) languageSelectDropdown.getSelectedItem();
            if (selectedLang != null && fileManager != null) {
                fileManager.setLanguage(selectedLang.toLowerCase());
                System.out.println("Language changed to: " + selectedLang);
            }
        });


        // Add File button action
        addFileButton.addActionListener(e -> {
            try {
                // Ask for file name
                String fileName = JOptionPane.showInputDialog(
                        this,
                        "Enter new file name (with extension):",
                        "Add New File",
                        JOptionPane.PLAIN_MESSAGE
                );

                if (fileName == null || fileName.isBlank()) return; // cancelled or empty input

                // Determine target directory
                Path targetDir;
                DefaultMutableTreeNode selectedNode =
                        (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();

                if (selectedNode != null && selectedNode.getUserObject() instanceof SFile sfile) {
                    Path selectedPath = sfile.getPath();
                    targetDir = Files.isDirectory(selectedPath)
                            ? selectedPath
                            : selectedPath.getParent();
                } else {
                    targetDir = fileManager.getRootdir();
                }

                // --- Validate file extension according to FileManager's language ---
                if (!fileManager.isAllowedFile(fileName)) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid file extension for current language (" + fileManager.getLanguage() + ").\n" +
                                    "Allowed: " + fileManager.isAllowedFile(fileName),
                            "Invalid Extension",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // --- Use FileManager + SFile to create file ---
                Path newFilePath = targetDir.resolve(fileName);

                if (Files.exists(newFilePath)) {
                    JOptionPane.showMessageDialog(this,
                            "File already exists: " + newFilePath.getFileName(),
                            "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Create new SFile through standard class logic
                Files.createFile(newFilePath);
                SFile newFile = new SFile(newFilePath);

                // Register it into the FileManager list for consistency
                fileManager.getFiles().add(newFile);

                // Refresh the file tree to reflect the change
                buildFileTree();

                // Optional: Select or load the new file
                dTextArea.setText("");
                fileManager.setCurrentFile(newFile);

                JOptionPane.showMessageDialog(this,
                        "File created successfully: " + newFile.getPath().getFileName(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to create file:\n" + ex.getMessage(),
                        "File Creation Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });



        // Run button action
        runCodeButton.addActionListener(e -> {
            // TODO: Add code execution logic here
            actualOutputArea.setText("Executing code...\n");
            expectedOutputArea.setText("Expected output will appear here");
        });
    }

    private void buildFileTree() {
        DefaultMutableTreeNode rootNode =
                new DefaultMutableTreeNode(fileManager.getRootdir().getFileName().toString());

        for (SFile sfile : fileManager.getFiles()) {
            addFileNode(rootNode, sfile);
        }

        fe_tree.setModel(new DefaultTreeModel(rootNode));
    }

    private void addFileNode(DefaultMutableTreeNode root, SFile sfile) {
        Path filePath = sfile.getPath();
        Path relativePath = fileManager.getRootdir().relativize(filePath);
        String[] parts = relativePath.toString()
                .split(File.separator.equals("\\") ? "\\\\" : File.separator);

        DefaultMutableTreeNode current = root;
        for (int i = 0; i < parts.length; i++) {
            DefaultMutableTreeNode child = findChild(current, parts[i]);
            if (child == null) {
                Object userObj = (i == parts.length - 1) ? sfile : parts[i];
                child = new DefaultMutableTreeNode(userObj);
                current.add(child);
            }
            current = child;
        }
    }

    private DefaultMutableTreeNode findChild(DefaultMutableTreeNode parent, String name) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            Object obj = child.getUserObject();

            if (obj instanceof SFile sfile) {
                if (sfile.getPath().getFileName().toString().equals(name)) {
                    return child;
                }
            } else if (obj.equals(name)) {
                return child;
            }
        }
        return null;
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