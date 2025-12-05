package com.exception.ccpp.GUI;

import com.exception.ccpp.CCJudge.Judge;
import com.exception.ccpp.CCJudge.SubmissionRecord;
import com.exception.ccpp.CCJudge.TerminalApp;
import com.exception.ccpp.CCJudge.TestcaseFile;
import com.exception.ccpp.CustomExceptions.InvalidFileException;
import com.exception.ccpp.CustomExceptions.NotDirException;
import com.exception.ccpp.FileManagement.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.FoldIndicatorStyle;
import org.fife.ui.rtextarea.LineNumberList;
import org.fife.ui.rtextarea.RTextScrollPane;

import java.awt.event.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.exception.ccpp.Gang.SlaveManager.slaveWorkers;
import java.nio.file.StandardOpenOption;
import com.exception.ccpp.GUI.RoundedButton; // Assuming this class is available
import com.exception.ccpp.GUI.RoundedComboBox; // Assuming this class is available

public class TextEditor extends JPanel {
    private JButton runCodeButton;
    private JButton addFileButton;
    private JButton createButton; // Appears unused, but keeping it
    private JButton openFolderButton;
    private JButton createFolderButton;
    private JButton submitCodeButton;
    private JButton setEntryPointButton;
    private RSyntaxTextArea codeArea;
    private JComboBox<String> languageSelectDropdown;
    private FileExplorer fileExplorerPanel;
    private JTextPane actualOutputArea;
    private JTextPane expectedOutputArea;
    private JButton importTestcaseButton;
    private JButton manageTestcaseButton;
    private JButton exportTestcaseButton;
    private JButton submitCodeButton;

    private SimpleAttributeSet matchStyle;
    private SimpleAttributeSet mismatchStyle;
    private SimpleAttributeSet excessStyle;
    private SimpleAttributeSet defaultStyle;

    private static String oldLanguage;

    public TextEditor() {
        initializeComponents();
        initializeBackend();
        initializeStyles();
        setupLayout();
        setupEventListeners();
//        setupTabToSpaces();

    }

    public TextEditor(String folderPath, MainMenu mainMenu) {
        this();

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

//    private void setupTabToSpaces() {
//        final String fourSpaces = "    ";
//
//        Action insertSpacesAction = new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                codeArea.replaceSelection(fourSpaces);
//            }
//        };
//
//        KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
//
//        Object actionKey = "insert-four-spaces";
//
//        InputMap inputMap = codeArea.getInputMap(JComponent.WHEN_FOCUSED);
//        inputMap.put(tabKey, actionKey);
//
//        codeArea.getActionMap().put(actionKey, insertSpacesAction);
//    }

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

        manageTestcaseButton = new RoundedButton("Manage Testcases", 15);
        manageTestcaseButton.setBackground(Color.decode("#568afc"));
        manageTestcaseButton.setForeground(Color.WHITE);
        manageTestcaseButton.setBorderPainted(false);
        manageTestcaseButton.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        codeArea = new RSyntaxTextArea(20, 60);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setFractionalFontMetricsEnabled(false);
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);

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


        actualOutputArea = new JTextPane();
        actualOutputArea.setBackground(Color.decode("#1f2335"));
        actualOutputArea.setCaretColor(Color.WHITE);
        actualOutputArea.setForeground(Color.WHITE);

        expectedOutputArea = new JTextPane();
        expectedOutputArea.setBackground(Color.decode("#1f2335"));
        expectedOutputArea.setCaretColor(Color.WHITE);
        expectedOutputArea.setForeground(Color.WHITE);

        DefaultCaret caret = (DefaultCaret) actualOutputArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret = (DefaultCaret) expectedOutputArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);


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
    }


    /* --------------- Setup --------------- */

    /* --------------- Util --------------- */
    private int BUFFER_MAX_CHARS = 10000;
    private void displayActualDiff(String[] actualLines, String[] expectedLines) {
        StyledDocument doc = actualOutputArea.getStyledDocument();
        actualOutputArea.getParent().getParent().setIgnoreRepaint(true);
//        DefaultCaret caret = (DefaultCaret) actualOutputArea.getCaret();
//        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

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

//        caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
        actualOutputArea.getParent().getParent().setIgnoreRepaint(false);
        actualOutputArea.setCaretPosition(doc.getLength());
    }
    private void displayExpectedDiff(String[] actualLines, String[] expectedLines) {
        // Note: We are using expectedOutputArea for this.
        StyledDocument doc = expectedOutputArea.getStyledDocument();
        expectedOutputArea.getParent().getParent().setIgnoreRepaint(true);
//        DefaultCaret caret = (DefaultCaret) expectedOutputArea.getCaret();
//        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

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

//        caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
        expectedOutputArea.setCaretPosition(doc.getLength());
        expectedOutputArea.getParent().getParent().setIgnoreRepaint(false);
    }
    public void saveCurrentFileContent() {
        SFile currentFile = fileExplorerPanel.getSelectedFile(); // <-- Use the new source of truth

        String placeholderText = "No file selected. Select a file to start editing.";
        String openFolderPlaceholderText = "No file selected. Please open a project or select a file to begin editing.";

        String content = codeArea.getText();

        if (currentFile != null && !content.equals(placeholderText) && !content.equals(openFolderPlaceholderText)) {
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
        fileButtonsPanel.add(manageTestcaseButton);
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
        JScrollPane editorScroll = new RTextScrollPane(codeArea);
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
        panel.add(submitCodeButton);
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
            codeArea.setText(newSFile.getContent());

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
            fileChooser.setCurrentDirectory(fm.getRootdir().toFile());

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
            if (newLanguage.equalsIgnoreCase("Java") || newLanguage.equalsIgnoreCase("Python")) {
                getTextEditor().setEntryPointButton.setVisible(true);
            } else {
                getTextEditor().setEntryPointButton.setVisible(false);
            }
            fileManager.setLanguage(newLanguage);
            oldLanguage = newLanguage;

            fileManager.setCurrentFile(null);

            getTextEditor().setEntryPointButton.setText("Set Entry Point");

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
            SwingUtilities.invokeLater(() -> {
                TerminalApp.getInstance().setAll(fm, null, null).start();
//                new TerminalApp(fm, null, null);
            });
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
            fileChooser.setCurrentDirectory(fm.getRootdir().toFile());
            fileChooser.setFileFilter(new  FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) return true;
                    return f.getName().toLowerCase().endsWith(".ccpp");
                }

                @Override
                public String getDescription() {
                    return "CC++ Testcase File";
                }
            });

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
    public static class ManageTestcaseButtonHandler extends ComponentHandler {
        public ManageTestcaseButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileExplorer fe = getTextEditor().fileExplorerPanel;
            FileManager fm =  fe.getFileManager();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Manage Testcase");
            fileChooser.setCurrentDirectory(fm.getRootdir().toFile());
            fileChooser.setFileFilter(new  FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) return true;
                    return f.getName().toLowerCase().endsWith(".ccpp");
                }

                @Override
                public String getDescription() {
                    return "CC++ Testcase File";
                }
            });

            int result = fileChooser.showOpenDialog(getTextEditor());

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                // SFile toSave = fe.getDummyExportFile();
                if (selectedFile.getName().toLowerCase().endsWith(".ccpp")) {
                    try {
                        Path selectedPath = selectedFile.toPath();
                        TestcaseFile tf = new TestcaseFile(selectedPath);
                        TestcaseManagerDialog tf_dialog = new TestcaseManagerDialog(SwingUtilities.getWindowAncestor(getTextEditor()), tf);
                        tf_dialog.setVisible(true);
                    } catch (Exception ex) { // we don't have to catch NotDir because we only display directories anyway
                        JOptionPane.showMessageDialog(getTextEditor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    public static class SubmitButtonHandler extends ComponentHandler {
        public SubmitButtonHandler(TextEditor editor) {
            super(editor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean is_java_python = false;
            FileManager fm = FileManager.getInstance();
            String sel_lang = (String) getTextEditor().languageSelectDropdown.getSelectedItem();
            SFile sel_file = FileExplorer.getInstance().getSelectedFile();
            if (sel_lang != null) is_java_python = sel_lang.equalsIgnoreCase("python") || sel_lang.equalsIgnoreCase("java");
            if (is_java_python) {

                if (sel_file == null)
                {
                    JOptionPane.showMessageDialog(getTextEditor(), "Yo, compilers aren't smart enough to run null files, so select one.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                else if (!(FileExplorer.getInstance().getFileExtension(FileExplorer.getInstance().getSelectedFile().getPath().toString()).equals(getTextEditor().getCurrentSelectedLanguage())))
                {
                    JOptionPane.showMessageDialog(getTextEditor(), "can you please select the correct compiler for your language please user please please please", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (FileExplorer.getInstance().getSelectedFile() != null && (getTextEditor().languageSelectDropdown.getSelectedItem().equals("Java") || getTextEditor().languageSelectDropdown.getSelectedItem().equals("Python"))) {
                getTextEditor().saveCurrentFileContent();
            }

            TestcaseFile tf = new TestcaseFile("datafile3.ccpp");
            System.out.println("TF has " + tf.getTestcases().size() + " testcases");
            Judge.judge(FileManager.getInstance(), tf, results -> {
                System.out.println("[TextEditor] RECEIVED");
                if (results.length > 0) {
                    System.out.println("[TextEditor] RESULTS HAS LENGTH");
                    SubmissionRecord rec = results[0];

                    final String actual = rec.output();
                    final String expected = rec.expected_output();
                    System.out.println("Exit code : " + rec.verdict());


                    Future<String[]> future_actual = slaveWorkers.submit(() -> actual.split("\\R", -1));
                    String[] expectedLines = expected.split("\\R", -1);

                    try {
                        final String[] actualLines = future_actual.get();
                        slaveWorkers.submit(() -> getTextEditor().displayActualDiff(actualLines, expectedLines));
                        getTextEditor().displayExpectedDiff(actualLines, expectedLines);

                    }
                    catch (InterruptedException ex) {}
                    catch (ExecutionException ex) {}
                }
            });

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

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    slaveWorkers.shutdown();
                }
            });

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 800);
            frame.setLocationRelativeTo(null);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        });
    }
}