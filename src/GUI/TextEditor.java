package GUI;

import FileManagement.*;

import javax.swing.tree.*;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextEditor extends JPanel {
    private JButton runCodeButton;
    private JButton addFileButton;
    private JTextArea dTextArea;
    private JComboBox<String> languageSelectDropdown;
    // JTree and FileManager are now in FileExplorerPanel
    private FileExplorer fileExplorerPanel;
    private JTextArea actualOutputArea;
    private JTextArea expectedOutputArea;
    // JPopupMenu components are now in FileExplorerPanel
    // JPopupMenu contextMenu;
    // JMenuItem renameItem;
    // JMenuItem deleteItem;

    public TextEditor() {
        initializeComponents();
        initializeBackend(); // Call before setupLayout to ensure fileExplorerPanel exists
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        runCodeButton = new JButton("Run Code");
        addFileButton = new JButton("Add File");
        dTextArea = new JTextArea();
        languageSelectDropdown = new JComboBox<>(new String[]{"C", "C++", "Java", "Python"});
        actualOutputArea = new JTextArea();
        expectedOutputArea = new JTextArea();

        // Initialize the new panel, passing the root directory and the editor area
        fileExplorerPanel = new FileExplorer(".", dTextArea);

        // The original context menu items are now managed within FileExplorerPanel.
        // contextMenu = new JPopupMenu();
        // renameItem =  new JMenuItem("Rename");
        // deleteItem = new JMenuItem("Delete");
        // contextMenu.add(renameItem);
        // contextMenu.add(deleteItem);
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

        // File explorer panel (Now using the encapsulated class)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(fileExplorerPanel, gbc);

        // Text editor panel
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        JScrollPane editorScroll = new JScrollPane(dTextArea);
        editorScroll.setBorder(BorderFactory.createTitledBorder("Editor"));
        panel.add(editorScroll, gbc);

        // Run button (right side, bottom of Text Editor)
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(runCodeButton);
        panel.add(buttonPanel, gbc);

        // Empty space for alignment
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
        // fileManager is now accessed via fileExplorerPanel.getFileManager()

        // Setup text editor
        dTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        dTextArea.setTabSize(4);

        // Setup output areas
        actualOutputArea.setEditable(false);
        expectedOutputArea.setEditable(false);
        actualOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        expectedOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
    }

    private void setupEventListeners() {
        // File tree selection listener and context menu listeners are now in FileExplorerPanel.
        // The original logic is now handled there.

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

            // Determine the target directory based on selection
            if (node != null) {
                Object obj = node.getUserObject();
                if (obj instanceof SFile sfile) {
                    targetDir = sfile.getPath().getParent(); // selected file → parent folder
                } else {
                    // selected a folder → find path recursively
                    targetDir = fileExplorerPanel.resolveNodeToPath(node);
                }
            }

            String fileName = JOptionPane.showInputDialog(this, "Enter new file name (with extension):");
            if (fileName == null || fileName.isBlank()) return;

            // Validate against globally allowed extensions
            if (!fileManager.isAllowedFile(fileName)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid file extension.\nAllowed: .c, .cpp, .h, .hpp, .java, .py",
                        "Invalid Extension",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Path newFilePath = targetDir.resolve(fileName);

                // Avoid overwriting
                if (Files.exists(newFilePath)) {
                    JOptionPane.showMessageDialog(this, "File already exists in " + targetDir);
                    return;
                }

                // Use SFile to create and manage the new file
                SFile newSFile = new SFile(newFilePath);
                newSFile.writeOut();               // actually create the file
                fileManager.getFiles().add(newSFile);  // add to FileManager list
                fileManager.setCurrentFile(newSFile);

                // Refresh the tree to show the new file
                fileExplorerPanel.buildFileTree();

                JOptionPane.showMessageDialog(this, "File created: " + newFilePath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error creating file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Run button action
        runCodeButton.addActionListener(e -> {
            // TODO: Add code execution logic here
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