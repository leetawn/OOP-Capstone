package GUI;

import CustomExceptions.NotDirException;
import FileManagement.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;

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

    public TextEditor() {
        initializeComponents();
        initializeBackend();
        setupLayout();
        //setupEventListeners();
    }

    // Initialize UI components
    private void initializeComponents() {
        runCodeButton = new JButton("Run Code");
        runCodeButton.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        runCodeButton.setBackground(Color.decode("#568afc"));
        runCodeButton.setForeground(Color.WHITE);
        runCodeButton.setOpaque(true);
        runCodeButton.setBorderPainted(false);
        runCodeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        addFileButton = new JButton("Add File");
        createButton = new JButton("Add Folder");
        openFolderButton = new JButton("Open Folder");
        createFolderButton = new JButton("Create Folder");
        setEntryPointButton = new JButton("Set Entry Point");
        setEntryPointButton.setVisible(false);

        dTextArea = new JTextArea();
        dTextArea.setBackground(Color.decode("#1f2335"));
        dTextArea.setCaretColor(Color.WHITE);
        dTextArea.setForeground(Color.WHITE);

        languageSelectDropdown = new JComboBox<>(new String[]{"C", "C++", "Java", "Python"});
        languageSelectDropdown.setBackground(Color.decode("#568afc"));
        languageSelectDropdown.setForeground(Color.WHITE);
        languageSelectDropdown.setOpaque(true);

        actualOutputArea = new JTextArea();
        actualOutputArea.setBackground(Color.decode("#1f2335"));
        actualOutputArea.setForeground(Color.WHITE);

        expectedOutputArea = new JTextArea();
        expectedOutputArea.setBackground(Color.decode("#1f2335"));
        expectedOutputArea.setForeground(Color.WHITE);

        fileExplorerPanel = new FileExplorer(".", dTextArea, this);
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        setBackground(Color.decode("#28313b"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // Left Panel: File Explorer + Editor
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

        // Right Panel: Output
        gbc.gridx = 2;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        add(createRightPanel(), gbc);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#28313b"));
        GridBagConstraints gbc = new GridBagConstraints();

        // File buttons
        JPanel fileButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fileButtonsPanel.setBackground(Color.decode("#28313b"));
        fileButtonsPanel.add(openFolderButton);
        fileButtonsPanel.add(addFileButton);
        fileButtonsPanel.add(createFolderButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(fileButtonsPanel, gbc);

        // Editor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane editorScroll = new JScrollPane(dTextArea);
        editorScroll.setBorder(BorderFactory.createTitledBorder("Text Editor"));
        panel.add(editorScroll, gbc);

        // Run button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.decode("#28313b"));
        buttonPanel.add(runCodeButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#28313b"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // Actual Output
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        JScrollPane actualScroll = new JScrollPane(actualOutputArea);
        actualScroll.setBorder(BorderFactory.createTitledBorder("Actual Output"));
        panel.add(actualScroll, gbc);

        // Separator
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        panel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);

        // Expected Output
        gbc.gridy = 2;
        gbc.weighty = 0.5;
        JScrollPane expectedScroll = new JScrollPane(expectedOutputArea);
        expectedScroll.setBorder(BorderFactory.createTitledBorder("Expected Output"));
        panel.add(expectedScroll, gbc);

        return panel;
    }

    private void initializeBackend() {
        dTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        actualOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        expectedOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        actualOutputArea.setEditable(false);
        expectedOutputArea.setEditable(false);
    }

    public void resetEditorState() {
        dTextArea.setText("");
        actualOutputArea.setText("");
        expectedOutputArea.setText("");
        setEntryPointButton.setVisible(false);
    }

    public FileExplorer getFileExplorerPanel() {
        return fileExplorerPanel;
    }
}
