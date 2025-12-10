package com.exception.ccpp.GUI;

import com.exception.ccpp.CCJudge.*;
import com.exception.ccpp.CustomExceptions.InvalidFileException;
import com.exception.ccpp.CustomExceptions.NotDirException;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemFileChooser;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.exception.ccpp.FileManagement.SFile;
import com.exception.ccpp.FileManagement.FileManager;

import java.awt.event.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.exception.ccpp.Gang.SlaveManager.slaveWorkers;

public class TextEditor extends JPanel {
    public static final String CCPP_FILE_DESC = "CC++ File";
    public static final String CCPP_EXT = "ccpp";
    private JButton runCodeButton;
    private JButton addFileButton;
    private JButton createButton; // Appears unused, but keeping it
    private JButton openFolderButton;
    private JButton createFolderButton;
    private RoundedButton setEntryPointButton;
    private JButton submitCodeButton;
    private RSyntaxTextArea codeArea;
    StyledDocument document;
    private JComboBox<String> languageSelectDropdown;
    private FileExplorer fileExplorerPanel;
    private JTextPane actualOutputArea;
    private JTextPane expectedOutputArea;
    private JButton importTestcaseButton;
    private JButton manageTestcaseButton;
//    private JButton folderDropdownButton;

    private SimpleAttributeSet matchStyle;
    private SimpleAttributeSet mismatchStyle;
    private SimpleAttributeSet excessStyle;
    private SimpleAttributeSet defaultStyle;

    private static String oldLanguage;
    TestcasesPanel testcasesPanel;
    private JPanel diffMenu;
    private JPanel tcMenu;
    private boolean is_diff_open;
    private StyledDocument actual_null_doc, expected_null_doc;
    private static TextEditor instance;
    JSplitPane mainSplit;
    JSplitPane centerRightSplit;
    JLabel textEditorLabel;
    final int ICON_SIZE = 20;

    public static TextEditor getInstance()
    {
        if (instance == null) instance = new TextEditor();
        return instance;
    }

    private TextEditor() {
        initializeComponents();
        initializeBackend();
        initializeStyles();
        setupLayout();
        setupEventListeners();
    }

    public TextEditor setAll(String folderPath, MainMenu mainMenu) {
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
        return this;
    }

    /* --------------- Setup --------------- */

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
    private void setupLayout() {
        JPanel panel1 = create_first_panel();
        JPanel panel2 = create_second_panel();
        JPanel panel3 = create_third_panel_Container();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
//
//        // GLOBAL SETTINGS
        gbc.fill = GridBagConstraints.BOTH; // STRICTLY DO NOT EDIT THIS!
        gbc.insets = new Insets(0, 0, 0, 0); // STRICTLY DO NOT EDIT THIS!
        gbc.gridy = 1; // STRICTLY DO NOT EDIT THIS!
        gbc.weighty = 1; // STRICTLY DO NOT EDIT THIS!
        gbc.weightx = 1; // STRICTLY DO NOT EDIT THIS!

        centerRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel2, panel3);
        centerRightSplit.setDividerLocation(200);
        centerRightSplit.setContinuousLayout(true);
        centerRightSplit.setOneTouchExpandable(true);
        centerRightSplit.setResizeWeight(1.0); // center takes extra space when window resizes

        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1,centerRightSplit);
        mainSplit.setDividerLocation(800); // initial right panel width
        mainSplit.setContinuousLayout(true);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setResizeWeight(1.0); // center takes extra space when window resizes

        centerRightSplit.setDividerSize(5);     // optional: thin divider
        mainSplit.setDividerSize(5);

        add(mainSplit, gbc);
    }

    private JPanel create_first_panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#000000"));
//        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#000000"), 1));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_1_1_panel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_1_2_panel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_1_3_panel(), gbc);

        return panel;
    }

    private JPanel create_1_1_panel(){
        JPanel panel = new JPanel(); // Don't use FixedSizePanel here either!
        panel.setBackground(Color.decode("#191c2a"));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

        panel.setPreferredSize(new Dimension(0, 30));

        JLabel label = new JLabel("File Explorer");
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

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

    private JPanel create_1_2_panel(){
        JPanel panel = new JPanel(); // Don't use FixedSizePanel here!
        panel.setBackground(Color.decode("#191c2a"));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5)); // Add some vertical padding

        Class<?> contextClass = this.getClass();


        // Load URLs
        URL openFolderUrl = contextClass.getResource("/assets/open_folder.png");
        URL addFileUrl = contextClass.getResource("/assets/add_file.png");
        URL createFolderUrl = contextClass.getResource("/assets/create_folder.png");

        // Get the scaled icons
        ImageIcon openFolderIcon = getScaledIcon(openFolderUrl, ICON_SIZE, ICON_SIZE);
        ImageIcon addFileIcon = getScaledIcon(addFileUrl, ICON_SIZE, ICON_SIZE);
        ImageIcon createFolderIcon = getScaledIcon(createFolderUrl, ICON_SIZE, ICON_SIZE);

        // Create JButtons and set the icon
        openFolderButton = new JButton(openFolderIcon);
        addFileButton = new JButton(addFileIcon);
        createFolderButton = new JButton(createFolderIcon);

        openFolderButton.setToolTipText("Open Folder");
        addFileButton.setToolTipText("Add File");
        createFolderButton.setToolTipText("Create Folder");

        // Set tooltip delay ONCE for all components
        ToolTipManager.sharedInstance().setInitialDelay(500);

        openFolderButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addFileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createFolderButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Configure the Buttons to look like Icons
        configureToolbarButton(openFolderButton);
        configureToolbarButton(addFileButton);
        configureToolbarButton(createFolderButton);

        // Add JButtons to the Panel
        panel.add(openFolderButton);
        panel.add(addFileButton);
        panel.add(createFolderButton);

        // Set a minimum height for the panel
        panel.setPreferredSize(new Dimension(0, 30)); // Height of 30 pixels

        return panel;
    }

    private JPanel create_1_3_panel(){
        // Use GridBagLayout like your working branch
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#191c2a")); // Match FileExplorer background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Set preferred size for the FileExplorer like in working branch
        fileExplorerPanel.setPreferredSize(new Dimension(175, Integer.MAX_VALUE));
        panel.add(fileExplorerPanel, gbc);

        return panel;
    }

    private JPanel create_second_panel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#1f2335"));
        panel.setBorder(BorderFactory.createMatteBorder(
                1, 1, 1, 1, // top, left, bottom, right thickness
                Color.decode("#000000")
        ));

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
        gbc.weighty = 0.91;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_2_2_panel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 0.07;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_2_3_panel(), gbc);

        return panel;
    }

    private JPanel create_2_1_panel(){
        JPanel panel = new FixedSizePanel(1, 20);
        panel.setBackground(Color.decode("#191c2a"));
        panel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, // top, left, bottom, right thickness
                Color.decode("#000000")
        ));

        panel.setLayout(new BorderLayout());

        Color panelBgColor = Color.decode("#191c2a");
        Color foreColor = Color.WHITE;

        textEditorLabel = new JLabel();
        textEditorLabel.setText("Select a file!");
        textEditorLabel.setForeground(foreColor);
        textEditorLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        textEditorLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Add textEditorLabel to the WEST (left) region
        panel.add(textEditorLabel, BorderLayout.WEST);

        // Create an INNER panel to hold the two right-side components
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(panelBgColor); // Match parent background

        // Use FlowLayout.RIGHT to align components to the right edge. vgap=0 to keep it centered vertically.
        rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        // 1. Language Label
        JLabel label2 = new JLabel();
        label2.setText("Language: ");
        label2.setForeground(foreColor);
        label2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        // 2. Dropdown Initialization and Customization
        languageSelectDropdown = new RoundedComboBox<>(new String[]{"C", "C++", "Java", "Python"});

        // Set custom width (e.g., 120 wide). Height is determined by font/L&F.
        Dimension d = languageSelectDropdown.getPreferredSize();
        languageSelectDropdown.setPreferredSize(new Dimension(150, 20));

        // Apply colors and transparency (These settings rely on your RoundedComboBox class changes)
        languageSelectDropdown.setOpaque(false);
        languageSelectDropdown.setForeground(foreColor);
        languageSelectDropdown.setBackground(panelBgColor);

        // Ensure the internal editor component is also set correctly
        Component editor = languageSelectDropdown.getEditor().getEditorComponent();
        if (editor instanceof JTextField textField) {
            textField.setOpaque(false);
            textField.setForeground(foreColor);
            textField.setBackground(panelBgColor); // Ensure the background is dark
        }

        // Add components to the right panel
        rightPanel.add(label2);
        rightPanel.add(languageSelectDropdown);

        // Add margin/padding to the right panel (10px on the right side)
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        // Add the rightPanel to the EAST (right) region of the main panel
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel create_2_2_panel(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#1f2335"));

        // Add the text editor with scroll pane
        codeArea.setBackground(Color.decode("#1f2335"));
        codeArea.setForeground(Color.WHITE);
        RTextScrollPane editorScroll = new RTextScrollPane(codeArea);
        editorScroll.setBorder(null);
        editorScroll.setBackground(Color.decode("#1f2335"));
        panel.add(editorScroll, BorderLayout.CENTER);

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

    private JPanel create_2_3_panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#191c2a"));
        panel.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, // top, left, bottom, right thickness
                Color.decode("#000000")
        ));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weighty = 1.0;

        // --- LEFT PANEL ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.22;
        JPanel p1 = create_2_3_1_panel();
        p1.setMinimumSize(new Dimension(150, 60)); // Minimum width
//        p1.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        panel.add(p1, gbc);

        // --- MIDDLE PANEL  ---
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.37;
        JPanel p2 = create_2_3_2_panel();
        p2.setMinimumSize(new Dimension(80, 60)); // Minimum width
//        p2.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        panel.add(p2, gbc);

        // --- RIGHT PANEL ---
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.40;
        JPanel p3 = create_2_3_3_panel();
//        p3.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        panel.add(p3, gbc);

        return panel;
    }

    private JPanel create_2_3_1_panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#191c2a"));
        GridBagConstraints gbc = new GridBagConstraints();

        importTestcaseButton = new RoundedButton("Import Testcase", 15);
        importTestcaseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        importTestcaseButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        importTestcaseButton.setForeground(Color.WHITE);
        importTestcaseButton.setBackground(Color.decode("#568afc"));
        importTestcaseButton.setPreferredSize(new Dimension(145, 50));

        manageTestcaseButton = new RoundedButton("Manage Testcase", 15);
        manageTestcaseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        manageTestcaseButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        manageTestcaseButton.setForeground(Color.WHITE);
        manageTestcaseButton.setBackground(Color.decode("#568afc"));
        manageTestcaseButton.setPreferredSize(new Dimension(145, 50));

        // Import button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(importTestcaseButton, gbc);

        // Export button
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        panel.add(manageTestcaseButton, gbc);

        return panel;
    }

    private JPanel create_2_3_2_panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#191c2a"));
        GridBagConstraints gbc = new GridBagConstraints();

        setEntryPointButton = new RoundedButton("Set Entry Point", 15);
        setEntryPointButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setEntryPointButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        setEntryPointButton.setForeground(Color.WHITE);
        setEntryPointButton.setBackground(Color.decode("#568afc"));

        Dimension fixedSize = new Dimension(110, 40);
        setEntryPointButton.setMinimumSize(fixedSize);
        setEntryPointButton.setMaximumSize(fixedSize);
        setEntryPointButton.setPreferredSize(fixedSize);

        // Hide initially but keep space
        setEntryPointButton.setVisible(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, -25);
        panel.add(setEntryPointButton, gbc);

        // CRITICAL: Add an invisible placeholder that takes the same space
        JLabel placeholder = new JLabel("");
        placeholder.setPreferredSize(fixedSize);
        placeholder.setMinimumSize(fixedSize);
        placeholder.setMaximumSize(fixedSize);
        placeholder.setBackground(Color.decode("#191c2a"));
        placeholder.setOpaque(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, -25);
        panel.add(placeholder, gbc); // Add placeholder behind button

        // Use JLayeredPane or keep button on top
        panel.setComponentZOrder(setEntryPointButton, 0); // Button on top
        panel.setComponentZOrder(placeholder, 1); // Placeholder behind

        return panel;
    }

    private JPanel create_2_3_3_panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#191c2a"));
        GridBagConstraints gbc = new GridBagConstraints();

        // Smaller font sizes and minimal padding

        runCodeButton = new RoundedButton("Run Code", 15);
        runCodeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runCodeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        runCodeButton.setForeground(Color.WHITE);
        runCodeButton.setBackground(Color.decode("#568afc"));
        runCodeButton.setPreferredSize(new Dimension(145, 50));

        submitCodeButton = new RoundedButton("Submit Code", 15);
        submitCodeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitCodeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        submitCodeButton.setForeground(Color.WHITE);
        submitCodeButton.setBackground(Color.decode("#39ca79"));
        submitCodeButton.setPreferredSize(new Dimension(145, 50));

        // Run code button
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.36;
        gbc.insets = new Insets(0, 0, 0, -10);
        panel.add(runCodeButton, gbc);

        // Submit code button
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.31;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(submitCodeButton, gbc);

        return panel;
    }

    // Container for both TestcasePanel and DiffPanel
    private JPanel create_third_panel_Container(){
        JPanel cardPanel = new JPanel(new CardLayout());
        cardPanel.setBorder(BorderFactory.createMatteBorder(
                0, 1, 0, 0, // top, left, bottom, right thickness
                Color.decode("#000000")
        ));

        // DONE@GLENSH: add hide fucntionality
        //  TODO: himo pa oga action listener, show / hide which one
        tcMenu = create_third_panel_Testcase();
        diffMenu = create_third_panel_Diff();
        is_diff_open = false;
        cardPanel.add(tcMenu, "TESTCASE_VIEW");
        cardPanel.add(diffMenu, "DIFF_VIEW");

        return cardPanel;
    }

    // For TestcaseListPanel ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private JPanel create_third_panel_Testcase(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#1a1c2a"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        panel.add(create_third_panel_Testcase_content(), gbc);

        return panel;
    }

    private JPanel create_third_panel_Testcase_content(){
        RoundedPanel panel = new RoundedPanel(new GridBagLayout(), 40, "#1f2335");
        panel.setBorderThickness(1);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;

        // TITLE SECTION
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(create_testcases_title(), gbc);

        // Scrollable content section
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0,0,0,0);
        gbc.fill = GridBagConstraints.BOTH;
        testcasesPanel = create_testcases_panel();
        panel.add(testcasesPanel, gbc);

        return panel;
    }

    private JPanel create_testcases_title(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#1f2335"));

        JLabel label = new JLabel("Testcases");
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.LEFT);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(12,12,12,12);
        panel.add(label, gbc);


        return panel;
    }

    private TestcasesPanel create_testcases_panel(){
        return TestcasesPanel.getInstance();
    }

    private JPanel create_third_panel_Diff() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#1f2335"));

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_3_1_panel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(create_3_2_panel(), gbc);

        return panel;
    }

    private JPanel create_3_1_panel(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 0, // top, left, bottom, right thickness
                Color.decode("#000000")
        ));
        panel.setBackground(Color.decode("#1f2335"));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#1f2335"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

        JButton backButton = new JButton("X");
        backButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(Color.decode("#1f2335"));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> closeDiffMenu());
        headerPanel.add(backButton, BorderLayout.WEST);

        JLabel label = new JLabel();
        label.setText("Actual Output");
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(-5, -50, 0, 0));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(label, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);

        actualOutputArea.setBackground(Color.decode("#1f2335"));
        actualOutputArea.setForeground(Color.WHITE);
        JScrollPane actualScroll = new JScrollPane(actualOutputArea);
        actualScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        actualScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        actualScroll.setBackground(Color.decode("#1f2335"));
        actualScroll.setBorder(null);
        panel.add(actualScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel create_3_2_panel(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, // top, left, bottom, right thickness
                Color.decode("#000000")
        ));
        panel.setBackground(Color.decode("#1f2335"));

        JLabel label = new JLabel();
        label.setText("Expected Output");
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(15, 5, 0, 0));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(label, BorderLayout.NORTH);

        // Add the expected output area
        expectedOutputArea.setBackground(Color.decode("#1f2335"));
        expectedOutputArea.setForeground(Color.WHITE);
        JScrollPane expectedScroll = new JScrollPane(expectedOutputArea);
        expectedScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        expectedScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        expectedScroll.setBackground(Color.decode("#1f2335"));
        expectedScroll.setBorder(null);
        panel.add(expectedScroll, BorderLayout.CENTER);

        return panel;
    }

    public void setDiffMenuDoc(StyledDocument actual, StyledDocument expected) {
        if (actual == null || null == expected) {
            actual = actual_null_doc;
            expected = expected_null_doc;
        };

        actualOutputArea.setDocument(actual);
        expectedOutputArea.setDocument(expected);
        actualOutputArea.revalidate();
        expectedOutputArea.revalidate();
//        SwingUtilities.invokeLater(() -> {
//
//        });
    }

    public void openDiffMenu() {
        is_diff_open = true;
        diffMenu.setVisible(is_diff_open);
        tcMenu.setVisible(!is_diff_open);
    }
    public void closeDiffMenu() {
        is_diff_open = false;
        diffMenu.setVisible(is_diff_open);
        tcMenu.setVisible(!is_diff_open);
    }

    // For DiffPanel ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // END OF FRONT END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // START OF BACK END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void setupEventListeners() {
        codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0)) {
                    e.consume();
                    saveCurrentFileContent();
                    actualOutputArea.setText("File saved successfully.");
                    return;
                }
                super.keyPressed(e);
            }
        });

        openFolderButton.addActionListener(new OpenFolderButtonHandler(this));
        addFileButton.addActionListener(new AddFileButtonHandler(this));
        languageSelectDropdown.addActionListener(new LanguageSelectHandler(this));
        runCodeButton.addActionListener(new RunButtonHandler(this));
        submitCodeButton.addActionListener(new SubmitButtonHandler(this));
        createFolderButton.addActionListener(e -> {
            fileExplorerPanel.handleCreateFolderAction();
        });
        setEntryPointButton.addActionListener(new SetEntryPointButtonHandler(this));
        importTestcaseButton.addActionListener(new ImportTestcaseButtonHandler(this));
        manageTestcaseButton.addActionListener(new ManageTestcaseButtonHandler(this));

    }

    private void initializeComponents() {
        runCodeButton = new RoundedButton("Run Code", 30);
        runCodeButton.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        runCodeButton.setBackground(Color.decode("#568afc"));
        runCodeButton.setForeground(Color.WHITE);
        runCodeButton.setBorderPainted(false);
        runCodeButton.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        submitCodeButton = new RoundedButton("Submit Code", 15);
        submitCodeButton.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        submitCodeButton.setBackground(Color.decode("#00FF00"));
        submitCodeButton.setForeground(Color.WHITE);
        submitCodeButton.setBorderPainted(false);
        submitCodeButton.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

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

        importTestcaseButton = new RoundedButton("Import Testcase", 15);
        importTestcaseButton.setBackground(Color.decode("#568afc"));
        importTestcaseButton.setForeground(Color.WHITE);
        importTestcaseButton.setBorderPainted(false);
        importTestcaseButton.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        codeArea = new RSyntaxTextArea(20, 60);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setFractionalFontMetricsEnabled(false);
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        codeArea.getDocument().addDocumentListener(new DocumentListener() {

            private void updateModifiedState() {
                SFile currentFile = fileExplorerPanel.getSelectedFile();
                if (currentFile != null) {
                    if (!currentFile.isDirty()) {
                        currentFile.setModified(true);
                    }

                    SwingUtilities.invokeLater(() -> {
                        updateUnsavedIndicator(true);
                    });
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { updateModifiedState(); }
            @Override public void removeUpdate(DocumentEvent e) { updateModifiedState(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        InputStream in = getClass().getClassLoader()
                .getResourceAsStream("org/fife/ui/rsyntaxtextarea/themes/monokai.xml");
        try {
            // Keep the text area's font since it has our e.g. ligature hints
            Theme theme = Theme.load(in, codeArea.getFont());
            theme.apply(codeArea);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


        languageSelectDropdown = new RoundedComboBox<>(new String[]{"C", "C++", "Java", "Python"});

        languageSelectDropdown.setBackground(Color.decode("#568afc"));
        languageSelectDropdown.setForeground(Color.WHITE);
        ((RoundedComboBox<String>) languageSelectDropdown).setRadius(20);


        actualOutputArea = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width < getParent().getWidth();
            }
        };
        actualOutputArea.setBackground(Color.decode("#191c2a"));
        actualOutputArea.setCaretColor(Color.WHITE);
        actualOutputArea.setForeground(Color.WHITE);

        expectedOutputArea = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width < getParent().getWidth();
            }
        };
        expectedOutputArea.setBackground(Color.decode("#191c2a")) ;
        expectedOutputArea.setCaretColor(Color.WHITE);
        expectedOutputArea.setForeground(Color.WHITE);

//        DefaultCaret caret = (DefaultCaret) actualOutputArea.getCaret();
//        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
//        caret = (DefaultCaret) expectedOutputArea.getCaret();
//        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);


        fileExplorerPanel = new FileExplorer(".", codeArea, this);
    }

    private void initializeBackend() {

        Font font = codeArea.getFont();
        codeArea.setFont(codeArea.getFont().deriveFont(font.getStyle(),16));
        codeArea.setText("No selected file. Select a file to start editing.");
        codeArea.setEditable(false);

        oldLanguage = getCurrentSelectedLanguage();
        actualOutputArea.setEditable(false);
        actualOutputArea.setCaretColor(Color.decode("#1f2335"));
        expectedOutputArea.setEditable(false);
        expectedOutputArea.setCaretColor(Color.decode("#1f2335"));
        actualOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        expectedOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        actual_null_doc = actualOutputArea.getStyledDocument();
        expected_null_doc = expectedOutputArea.getStyledDocument();
    }


    /* --------------- Setup --------------- */

    /* --------------- Util --------------- */



    private int BUFFER_MAX_CHARS = 10000;
    private void displayActualDiff(String[] actualLines, String[] expectedLines, StyledDocument doc) {
        if (doc == null) return;

        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ignored) {}

        int maxLines = Math.max(actualLines.length, expectedLines.length);

        AttributeSet prevStyle;
        AttributeSet styleToApply = null;
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < maxLines; i++) {
            String actualLine = (i < actualLines.length) ? actualLines[i] : "";
            String expectedLine = (i < expectedLines.length) ? expectedLines[i] : "";

            int maxLength = Math.max(actualLine.length(), expectedLine.length());

            // full line, char by char
            for (int j = 0; j < maxLength; j++) {
                char actualChar = (j < actualLine.length()) ? actualLine.charAt(j) : 0;
                char expectedChar = (j < expectedLine.length()) ? expectedLine.charAt(j) : 0;

                char charToProcess = actualChar; // Start with the actual character

                prevStyle = styleToApply;
                if (expectedChar == 0 && actualChar != 0) {
                    styleToApply = excessStyle;
                }
                else if (expectedChar != 0 && actualChar == expectedChar) {
                    styleToApply = matchStyle; // Green
                }
                else {
                    styleToApply = mismatchStyle;
                    if (actualChar == 0) charToProcess = ' ';
                }

                // flush first
                if (prevStyle != null && !buffer.isEmpty())
                    if (prevStyle != styleToApply || buffer.length() > BUFFER_MAX_CHARS) {
                        final AttributeSet final_style = prevStyle;
                        final String final_str = buffer.toString();
                        SwingUtilities.invokeLater(() ->
                        {
                            try {
                                // TODO OPTIMIZATION: setLargeText and doc.setCharacterAttributes();
                                doc.insertString(doc.getLength(), final_str, final_style);
                            } catch (BadLocationException ignored) {}
                        });
                        buffer.delete(0, buffer.length());
                    }

                if (charToProcess == '\t') buffer.append("    "); // 4 spaces for Tab
                else if (charToProcess == '\r' || charToProcess == '\n') buffer.append("\u2424"); // Symbol for Newline
                else if (charToProcess == 0 || charToProcess == ' ') buffer.append(" ");
                else buffer.append(charToProcess);
            }

            // newline if not the last line
            if (i < maxLines - 1) {
                buffer.append("\n");
            }
        }
        if (!buffer.isEmpty()) {
            final AttributeSet final_style = styleToApply;
            final String final_str = buffer.toString();
            SwingUtilities.invokeLater( () ->
            {
                try {
                    doc.insertString(doc.getLength(), final_str, final_style);
                } catch (BadLocationException ignored) {}
            });
        }

//        actualOutputArea.getParent().getParent().setIgnoreRepaint(true);
//        actualOutputArea.getParent().getParent().setIgnoreRepaint(false);
    }
    private void displayExpectedDiff(String[] actualLines, String[] expectedLines, StyledDocument doc) {
        // Note: We are using expectedOutputArea for this.
//        DefaultCaret caret = (DefaultCaret) expectedOutputArea.getCaret();
//        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        if (doc == null) return;

        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ignored) {}

        int maxLines = Math.max(actualLines.length, expectedLines.length);

        AttributeSet prevStyle;
        AttributeSet styleToApply = null;
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < maxLines; i++) {
            String actualLine = (i < actualLines.length) ? actualLines[i] : "";
            String expectedLine = (i < expectedLines.length) ? expectedLines[i] : "";

            for (int j = 0; j < expectedLine.length(); j++) {
                char actualChar = (j < actualLine.length()) ? actualLine.charAt(j) : 0;
                char expectedChar = (j < expectedLine.length()) ? expectedLine.charAt(j) : 0;

                char charToProcess = expectedChar; // Focus on the expected character

                if (expectedChar == 0) {
                    continue;
                }

                prevStyle = styleToApply;
                if (actualChar == expectedChar) {
                    styleToApply = matchStyle; // Green
                } else {
                    styleToApply = mismatchStyle; // Red
                }

                if (prevStyle != null && !buffer.isEmpty())
                    if (prevStyle != styleToApply || buffer.length() > BUFFER_MAX_CHARS) {
                        final AttributeSet final_style = prevStyle;
                        final String final_str = buffer.toString();
                        SwingUtilities.invokeLater(() ->
                        {
                            try {
                                doc.insertString(doc.getLength(), final_str, final_style);
                            } catch (BadLocationException ignored) {}
                        });
                        buffer.delete(0, buffer.length());
                    }

                if (charToProcess == '\t') {
                    buffer.append("    "); // 4 spaces for Tab
                } else if (charToProcess == '\r' || charToProcess == '\n') {
                    buffer.append("\u2424"); // Symbol for Newline
                } else {
                    buffer.append(charToProcess);
                }
            }

            if (i < maxLines - 1 && i < expectedLines.length) { // Only add newline if Expected has more lines
                buffer.append("\n");
            }
        }
        if (!buffer.isEmpty()) {
            final AttributeSet final_style = styleToApply;
            final String final_str = buffer.toString();
            SwingUtilities.invokeLater( () ->
            {
                try {
                    doc.insertString(doc.getLength(), final_str, final_style);
                } catch (BadLocationException ignored) {}
            });
        }

//        expectedOutputArea.getParent().getParent().setIgnoreRepaint(true);
//        expectedOutputArea.getParent().getParent().setIgnoreRepaint(false);
    }
    public void saveCurrentFileContent() {
        SFile currentFile = fileExplorerPanel.getSelectedFile(); // <-- Use the new source of truth

        String placeholderText = "No file selected. Select a file to start editing.";
        String openFolderPlaceholderText = "No file selected. Please open a project or select a file to begin editing.";

        String content = codeArea.getText();

        if (currentFile != null && !content.equals(placeholderText) && !content.equals(openFolderPlaceholderText)) {
            currentFile.setContent(content);
            currentFile.write();
            updateUnsavedIndicator(false);
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

            SFile newSFile = SFile.open(newFilePath);
            newSFile.write();

            fm.getFiles().add(newSFile);
            fm.setCurrentFile(newSFile);
            codeArea.setText(newSFile.getContent());
            JOptionPane.showMessageDialog(this, "File created: " + newFilePath.getFileName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error creating file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
    /* --------------- Util --------------- */
    /* --------------- Getters & Setters --------------- */
    public void updateUnsavedIndicator(boolean isDirty) {
        String currentText = textEditorLabel.getText();

        final String ASTERISK_INDICATOR = " *";

        String cleanText = currentText;
        if (currentText.endsWith(ASTERISK_INDICATOR)) {
            cleanText = currentText.substring(0, currentText.length() - ASTERISK_INDICATOR.length());
        }

        if (isDirty) {
            if (!currentText.endsWith(ASTERISK_INDICATOR)) {
                textEditorLabel.setText(cleanText + ASTERISK_INDICATOR);
                System.out.println("LABEL: " + textEditorLabel.getText());
            }
        } else {
            textEditorLabel.setText(cleanText);
        }
    }

    public String getCurrentSelectedLanguage() {
        if (languageSelectDropdown.getSelectedItem().equals("C++")) return "cpp";
        if (languageSelectDropdown.getSelectedItem().equals("Python")) return "py";
        return ((String) languageSelectDropdown.getSelectedItem()).toLowerCase();
    }

    public void setTextArea(boolean ok) {
        this.codeArea.setEditable(ok);
        if (!ok) {
            this.codeArea.setText("No file selected. Please open a project or select a file to begin editing.");
        }
    }
    public static void makeCoolAndNormal() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
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
            SystemFileChooser fileChooser = new SystemFileChooser();
            fileChooser.setFileSelectionMode(SystemFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Project Root Folder");
            fileChooser.setCurrentDirectory(fm.getRootdir().toFile());

            int result = fileChooser.showOpenDialog(getTextEditor());

            if (result == SystemFileChooser.APPROVE_OPTION) {
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

                SFile newSFile = SFile.open(newFilePath);
                newSFile.write();
                fileManager.getFiles().add(newSFile);
                fileManager.setCurrentFile(newSFile);
                getTextEditor().codeArea.setText(newSFile.getContent());

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
            FileManager fileManager = FileManager.getInstance();
            String newLanguage = (String) getTextEditor().languageSelectDropdown.getSelectedItem();
            if (oldLanguage.equals(newLanguage)) return; // no need to reset
            if (newLanguage == null) return;

            boolean showEntryButton = newLanguage.equalsIgnoreCase("Java") ||
                    newLanguage.equalsIgnoreCase("Python");

            // DON'T touch opaque or contentAreaFilled - just use visibility
            getTextEditor().setEntryPointButton.setVisible(showEntryButton);

            fileManager.setLanguage(newLanguage);
            oldLanguage = newLanguage;

            fileManager.setCurrentFile(null);

            getTextEditor().setEntryPointButton.setText("Set Entry Point");
            getTextEditor().revalidate();
            getTextEditor().repaint();
            System.out.println("Project language changed to: " + newLanguage + ". Entry point reset.");
        }
    }

    public static class RunButtonHandler extends ComponentHandler {
        public RunButtonHandler(TextEditor editor) {
            super(editor);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            getTextEditor().saveCurrentFileContent();
            if (!canProceedRunCode()) return;
            FileManager fm = FileManager.getInstance();
            SwingUtilities.invokeLater(() -> {
                TerminalApp.getInstance().stopSetAll(fm, null, null).start();
//                new TerminalApp(fm, null, null);
            });
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

            if (getTextEditor().getCurrentSelectedLanguage().equalsIgnoreCase("java")) {
                if (sfile.getStringPath().toLowerCase().endsWith(".java")) fileManager.setCurrentFile(sfile);
                else {
                    JOptionPane.showMessageDialog(null,
                            "i NEED JABAI ENTRY POINT",
                            "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (getTextEditor().getCurrentSelectedLanguage().equalsIgnoreCase("py"))  {
                if (sfile.getStringPath().toLowerCase().endsWith(".py")) fileManager.setCurrentFile(sfile);
                else {
                    JOptionPane.showMessageDialog(null,
                            "i NEED PYTHON ENTRY POINT",
                            "Invalid Entry Point", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            getTextEditor().setEntryPointButton.setText(String.valueOf(sfile.getPath().getFileName()));
        }
    }

    public static class ImportTestcaseButtonHandler extends ComponentHandler {
        public ImportTestcaseButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = FileExplorer.getInstance();
            FileManager fm =  FileManager.getInstance();
            SystemFileChooser fileChooser = new SystemFileChooser();
            fileChooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Load CC++ Testcase File");
            fileChooser.setCurrentDirectory(fm.getRootdir().toFile());
            fileChooser.addChoosableFileFilter(new SystemFileChooser.FileNameExtensionFilter(CCPP_FILE_DESC,CCPP_EXT));

            int result = fileChooser.showOpenDialog(getTextEditor());
// TODO@GLENSH import testcase

            if (result == SystemFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null && selectedFile.isFile()) {
                    try {
                        if (selectedFile.getPath().endsWith(".ccpp")) {
                            TestcaseFile tf = TestcaseFile.open(selectedFile.getPath());
                            fe.setTestcaseFile(tf);
                            TextEditor.getInstance().testcasesPanel.setTestcaseFile(tf);
                        } else {
                            throw new InvalidFileException("Invalid file! Please select .ccpp files for testcases.");
                        }

                   } catch (InvalidFileException ex) {
                        JOptionPane.showMessageDialog(getTextEditor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            if (fe.getTestcaseFile() != null) {
                System.out.println("Testcases has " + fe.getTestcaseFile().getTestcases().size() + " testcases");
            } else {
                System.out.println("No testcase file selected.");
            }
        }
    }
    public static class ManageTestcaseButtonHandler extends ComponentHandler {
        public ManageTestcaseButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            FileManager fm =  fe.getFileManager();
            SystemFileChooser fileChooser = new SystemFileChooser();
            fileChooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Load/Create CC++ Testcase File");
            fileChooser.setCurrentDirectory(fm.getRootdir().toFile());
            fileChooser.addChoosableFileFilter(new  SystemFileChooser.FileNameExtensionFilter(CCPP_FILE_DESC, CCPP_EXT));

            int result = fileChooser.showDialog(getTextEditor(), "Open");

            if (result == SystemFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile.getName().toLowerCase().endsWith(".ccpp")) {
                    try {
                        Path selectedPath = selectedFile.toPath();
                        TestcaseFile tf = TestcaseFile.open(selectedPath);
                        TestcaseManagerDialog tf_dialog = new TestcaseManagerDialog(SwingUtilities.getWindowAncestor(getTextEditor()), tf);
                        tf_dialog.setVisible(true);
                    } catch (Exception ex) { // we don't have to catch NotDir because we only display directories anyway
                        JOptionPane.showMessageDialog(getTextEditor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            else {
                System.err.println("HELLO2");
            }
        }
    }


    public static class SubmitButtonHandler extends ComponentHandler {
        public SubmitButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileManager fm = FileManager.getInstance();
            if (!canProceedRunCode()) return;

            fm.saveAll(); // ADDED SAVING ALL FILES

            TestcaseFile tf = getTextEditor().fileExplorerPanel.getTestcaseFile();
            if (tf == null) {
                JOptionPane.showMessageDialog(getTextEditor(), "IMPORT A TESTCASE FILE OR ELSE...", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // TODO@GLENSH return to testcases
            System.out.println("TF has " + tf.getTestcases().size() + " testcases");
            Judge.judge(FileManager.getInstance(), tf, results -> {

                // QUEUE TESTCASES
                if (results.length > 0) {
                    Map<Testcase, TestcasesPanel.TCEntry> activeTC =
                            TestcasesPanel.getInstance().getActiveTestcases();
                    for (SubmissionRecord s : results)
                    {
                        TestcasesPanel.TCEntry entry = activeTC.get(s.testcase());
                        System.out.println("[TextEditor] SENDING SLAVES");
                        slaveWorkers.submit(new DiffSlave(s, entry.actualDoc, entry.expectedDoc));
                    }
                }
            });
        }
    }

    private static boolean canProceedRunCode() {
        TextEditor tEditor = TextEditor.getInstance();
        FileExplorer fExplorer = FileExplorer.getInstance();
        String sel_lang = (String) tEditor.languageSelectDropdown.getSelectedItem();
        SFile sel_file = fExplorer.getSelectedFile();
        if (sel_lang == null) return false; // idk how we got in this point;
        sel_lang = sel_lang.toLowerCase();

        boolean is_java_python = switch (sel_lang) { case "python", "java" -> true; default -> false; };
        if (is_java_python) {
            if (sel_file == null)
            {
                JOptionPane.showMessageDialog(tEditor, "Yo, compilers aren't smart enough to run null files, so select one.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            else if (!(fExplorer.getFileExtension(sel_file.getPath().toString()).equals(tEditor.getCurrentSelectedLanguage())))
            {
                JOptionPane.showMessageDialog(tEditor, "can you please select the correct compiler for your language please user please please please", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    public static class DiffSlave implements Runnable {
        final String actual;
        final String expected;
        final StyledDocument actualDoc;
        final StyledDocument expectedDoc;
        public DiffSlave(SubmissionRecord rec, StyledDocument actualDoc, StyledDocument expectedDoc) {
            this.actual = rec.output();
            this.expected = rec.expected_output();
            this.actualDoc = actualDoc;
            this.expectedDoc = expectedDoc;
        }

        @Override
        public void run() {
            Future<String[]> future_actual = slaveWorkers.submit(() -> actual.split("\\R", -1));
            String[] expectedLines = expected.split("\\R", -1);

            try {
                final String[] actualLines = future_actual.get();
                slaveWorkers.submit(() -> TextEditor.getInstance().displayActualDiff(actualLines, expectedLines, actualDoc));
                TextEditor.getInstance().displayExpectedDiff(actualLines, expectedLines, expectedDoc);
            }  catch (InterruptedException ex) {}
            catch (ExecutionException ex) {}
        }

    }
    /* --------------- Button Handlers --------------- */
}