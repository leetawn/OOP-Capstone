package GUI;

import CustomExceptions.NotDirException;
import FileManagement.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class TextEditor extends JPanel {
    private JButton runCodeButton;
    private JButton addFileButton;
    private JButton openFolderButton;
    private JTextArea dTextArea;
    private JComboBox<String> languageSelectDropdown;
    private FileExplorer fileExplorerPanel;
    private JTextArea actualOutputArea;
    private JTextArea expectedOutputArea;

    public TextEditor() {
        initializeComponents();
        initializeBackend();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        runCodeButton = new JButton("Run Code");
        addFileButton = new JButton("Add File");
        openFolderButton = new JButton("Open Folder");
        dTextArea = new JTextArea();
        languageSelectDropdown = new JComboBox<>(new String[]{"C", "C++", "Java", "Python"});
        actualOutputArea = new JTextArea();
        expectedOutputArea = new JTextArea();

        // Pass 'this' TextEditor instance to the FileExplorer
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
        panel.add(fileButtonsPanel, gbc);

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

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(fileExplorerPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        JScrollPane editorScroll = new JScrollPane(dTextArea);
        editorScroll.setBorder(BorderFactory.createTitledBorder("Editor"));
        panel.add(editorScroll, gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
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
            // Crucial step: Update the SFile object's internal content field
            currentFile.setContent(content);
            // Write the SFile content (which now holds the latest JTextArea content) to the disk
            currentFile.writeOut();
            System.out.println("File saved: " + currentFile.getStringPath());
        }
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
            String selectedLang = (String) languageSelectDropdown.getSelectedItem();
            FileManager fileManager = fileExplorerPanel.getFileManager();
            if (selectedLang != null && fileManager != null) {
                fileManager.setLanguage(selectedLang.toLowerCase());
                System.out.println("Language changed to: " + selectedLang);
            }
        });

        addFileButton.addActionListener(e -> {
            FileManager fileManager = fileExplorerPanel.getFileManager();
            if (fileManager == null) return;

            DefaultMutableTreeNode node = fileExplorerPanel.getSelectedNode();
            Path targetDir = fileManager.getRootdir();

            if (node != null) {
                Object obj = node.getUserObject();
                if (obj instanceof SFile sfile) {
                    targetDir = sfile.getPath().getParent();
                } else {
                    targetDir = fileExplorerPanel.resolveNodeToPath(node);
                }
            }

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
                newSFile.writeOut();
                fileManager.getFiles().add(newSFile);
                fileManager.setCurrentFile(newSFile);
                dTextArea.setText(newSFile.getContent());

                fileExplorerPanel.buildFileTree();

                JOptionPane.showMessageDialog(this, "File created: " + newFilePath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error creating file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        runCodeButton.addActionListener(e -> {
            saveCurrentFileContent();
            actualOutputArea.setText("Executing code...\n");
            expectedOutputArea.setText("Expected output will appear here");
        });
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