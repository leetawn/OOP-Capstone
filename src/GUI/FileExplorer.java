package GUI;

import CustomExceptions.NotDirException;
import FileManagement.FileManager;
import FileManagement.SFile;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class FileExplorer extends JPanel {
    private JTree fe_tree;
    private FileManager fileManager;
    private JTextArea dTextArea;
    private JPopupMenu contextMenu;
    private JMenuItem renameItem;
    private JMenuItem createFolderItem;
    private JMenuItem deleteItem;
    private JMenuItem addFileItem;
    private TextEditor textEditor;
    private SFile selectedFile;

    // Updated constructor signature: No longer accepts language
    public FileExplorer(String rootDir, JTextArea editorTextArea, TextEditor textEditor) {
        this.dTextArea = editorTextArea;
        this.textEditor = textEditor;
        initializeBackend(rootDir); // Calls initializeBackend without language argument
        initializeComponents();
        setupLayout();
        setupEventListeners();
        buildFileTree();
    }

    // Updated updateRootDirectory to fetch language from TextEditor
    public void updateRootDirectory(String newRootDir) throws NotDirException {
        String currentLang = fileManager != null ? fileManager.getLanguage() : null;
        this.fileManager = fileManager.setAll(newRootDir, currentLang);
        this.dTextArea.setText("");
        this.buildFileTree();
    }

    // Updated initializeBackend to fetch language from TextEditor
    private void initializeBackend(String rootDir) {
        try {
            fileManager = FileManager.getInstance().setAll(rootDir, textEditor.getCurrentSelectedLanguage());
            selectedFile = null;
        } catch (NotDirException e) {
            JOptionPane.showMessageDialog(this, "Invalid directory: " + e.getMessage());
        }
    }

    private void initializeComponents() {
        fe_tree = new JTree();
        fe_tree.setBackground(Color.decode("#1f2335"));
        fe_tree.setForeground(Color.WHITE);

        UIManager.put("Tree.selectionBackground", Color.decode("#568afc"));
        UIManager.put("Tree.selectionForeground", Color.WHITE);

        fe_tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean sel, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {

                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                if (sel) {
                    setBackground(Color.decode("#568afc"));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.decode("#1f2335"));  // ADD THIS
                    setForeground(Color.WHITE);
                }

                setOpaque(true);  // ADD THIS - makes background visible

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObj = node.getUserObject();

                if (userObj instanceof SFile sfile) {
                    setText(sfile.getPath().getFileName().toString());
                    if (Files.isDirectory(sfile.getPath())) {
                        setIcon(UIManager.getIcon("FileView.directoryIcon"));
                    } else {
                        setIcon(UIManager.getIcon("FileView.fileIcon"));
                    }

                } else {
                    setText(userObj.toString());
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                }

                return this;
            }
        });

        contextMenu = new JPopupMenu();
        renameItem =  new JMenuItem("Rename");
        deleteItem = new JMenuItem("Delete");
        createFolderItem = new JMenuItem("Create folder");
        addFileItem = new JMenuItem("Add file");

        contextMenu.addSeparator();
        contextMenu.add(renameItem);
        contextMenu.add(deleteItem);
        contextMenu.add(createFolderItem);
        contextMenu.add(addFileItem);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        JScrollPane treeScroll = new JScrollPane(fe_tree);

        // ---- DARK COLORS ----
        Color BG = Color.decode("#1f2335");
        Color TITLE = Color.decode("#ffffff");
        Color SCROLL_TRACK = Color.decode("#2a2f45");
        Color SCROLL_THUMB = Color.decode("#3b425c");

        // ---- BORDER WITH DARK THEME ----
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BG),
                "File Explorer",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("JetBrains Mono", Font.PLAIN, 12),
                TITLE
        );
        treeScroll.setBorder(border);

        // ---- FIX WHITE BACKGROUNDS ----
        treeScroll.setBackground(BG);                   // JScrollPane background
        treeScroll.getViewport().setBackground(BG);    // Viewport background
        this.setBackground(BG);                        // Panel background

        // ---- CUSTOM DARK SCROLLBAR ----
        JScrollBar vBar = treeScroll.getVerticalScrollBar();
        JScrollBar hBar = treeScroll.getHorizontalScrollBar();

        vBar.setBackground(SCROLL_TRACK);
        hBar.setBackground(SCROLL_TRACK);

        vBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = SCROLL_THUMB;
                this.trackColor = SCROLL_TRACK;
            }
        });

        hBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = SCROLL_THUMB;
                this.trackColor = SCROLL_TRACK;
            }
        });

        add(treeScroll, BorderLayout.CENTER);
        setPreferredSize(new Dimension(250, 0));
    }

    private void setupEventListeners() {
        fe_tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
            if (node == null) return;

            Object obj = node.getUserObject();
            if (obj instanceof SFile sfile) {

                if (Files.isDirectory(sfile.getPath())) {
                    System.out.println("Selected folder: " + sfile.getPath().getFileName());
                    return;
                }

                try {
                    textEditor.saveCurrentFileContent();

                    setSelectedFile(sfile);
                    textEditor.setTextArea(true);

                    Path filePath = sfile.getPath();
                    String content = Files.readString(filePath);
                    dTextArea.setText(content);
                    System.out.println("Current file: ");
                } catch (Exception ex) {
                    // some error here
                }
            }
        });

        fe_tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();

                    if (node != null && node.getUserObject() instanceof SFile) {
                        renameItem.setVisible(true);
                        deleteItem.setVisible(true);
                        addFileItem.setVisible(true);
                    } else {
                        renameItem.setVisible(false);
                        deleteItem.setVisible(false);
                        addFileItem.setVisible(true);
                    }

                    contextMenu.show(fe_tree, e.getX(), e.getY());
                }
            }
        });
        addFileItem.addActionListener(e -> {
            // Reuses the main handler logic in TextEditor
            textEditor.handleAddFileAction();
        });

        renameItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
            if (node == null || !(node.getUserObject() instanceof SFile sfile)) return;

            boolean isDirectory = Files.isDirectory(sfile.getPath());
            String itemType = isDirectory ? "folder" : "file";


            String currentName = sfile.getPath().getFileName().toString();
            String newName = JOptionPane.showInputDialog(null,
                    "Enter new name for " + itemType + ":", currentName);

            if (newName == null || newName.isBlank() || newName.equals(currentName)) return;

            // Validation for renaming: Check if new name is allowed for current language
            if (!isDirectory && !fileManager.isAllowedFile(newName)) {
                JOptionPane.showMessageDialog(null,
                        "Invalid file extension for the current project language (" + fileManager.getLanguage() + ").",
                        "Invalid Extension", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = fileManager.renameFile(sfile, newName);

            if (success) {
                DefaultTreeModel model = (DefaultTreeModel) fe_tree.getModel();
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();

                model.nodeChanged(node);

                if (parentNode != null) {
                    model.reload(parentNode);
                } else {
                    model.reload();
                }

                fe_tree.setSelectionPath(new TreePath(node.getPath()));
                fe_tree.repaint();

                JOptionPane.showMessageDialog(null, itemType + " renamed successfully to " + newName);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to rename " + itemType + ".", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        createFolderItem.addActionListener(e -> {
            handleCreateFolderAction();
        });
        deleteItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();

            if (node == null || !(node.getUserObject() instanceof SFile sfile)) return;

            boolean isDirectory = Files.isDirectory(sfile.getPath());
            String itemType = isDirectory ? "folder" : "file";

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Delete the " + itemType + ": " + sfile.getPath().getFileName() + "?\n(This action cannot be undone.)",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {

                boolean success;

                if (isDirectory) {
                    success = fileManager.deleteFolder(sfile.getPath());
                } else {
                    success = fileManager.deleteFile(sfile);
                }

                if (success) {
                    DefaultTreeModel model = (DefaultTreeModel) fe_tree.getModel();
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();

                    if (parentNode != null) {
                        model.removeNodeFromParent(node);

                        if (sfile.equals(fileManager.getCurrentFile()) || isDirectory) {
                            dTextArea.setText("");
                            fileManager.setCurrentFile(null);
                            // If a deleted file was the entry point, reset the entry point button label
                            textEditor.getSetEntryPointButton().setText("Set Entry Point");
                        }
                    }

                    JOptionPane.showMessageDialog(null, itemType + " deleted successfully.");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to delete " + itemType + ". Check permissions or if the folder is in use.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public void handleCreateFolderAction() {
        FileManager fm = getFileManager();
        if (fm == null) return;

        DefaultMutableTreeNode selectedTreePathNode = getSelectedNode();
        Path directoryToCreateIn = fileManager.getRootdir();
        DefaultMutableTreeNode parentNodeInTree;

        if (selectedTreePathNode != null) {
            Object obj = selectedTreePathNode.getUserObject();
            if (obj instanceof SFile sfile) {
                // If a file is selected, create folder in its parent directory
                directoryToCreateIn = Files.isDirectory(sfile.getPath()) ? sfile.getPath() : sfile.getPath().getParent();
                // Parent node is the directory itself, or the selected node's parent
                parentNodeInTree = Files.isDirectory(sfile.getPath()) ? selectedTreePathNode : (DefaultMutableTreeNode) selectedTreePathNode.getParent();
            } else {
                directoryToCreateIn = fileManager.getRootdir();
                parentNodeInTree = selectedTreePathNode;
            }
        } else {
            parentNodeInTree = (DefaultMutableTreeNode) fe_tree.getModel().getRoot();
        }

        String newFolderName = JOptionPane.showInputDialog(null, "Enter new folder name:");

        if (newFolderName != null && !newFolderName.isBlank()) {
            if (newFolderName.contains(File.separator) || newFolderName.startsWith(".")) {
                JOptionPane.showMessageDialog(null, "Invalid folder name. Cannot contain path separators or start with '.'", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = fm.createFolder(directoryToCreateIn, newFolderName);

            if (success) {
                Path newFolderPath = directoryToCreateIn.resolve(newFolderName);
                SFile newDirSFile = new SFile(newFolderPath);

                DefaultMutableTreeNode newFolderTreeNode = new DefaultMutableTreeNode(newDirSFile);

                DefaultTreeModel model = (DefaultTreeModel) fe_tree.getModel();

                // Insert into the determined parent node
                model.insertNodeInto(newFolderTreeNode, parentNodeInTree, parentNodeInTree.getChildCount());

                fe_tree.expandPath(new TreePath(parentNodeInTree.getPath()));

                JOptionPane.showMessageDialog(null, "Folder created successfully in " + directoryToCreateIn.getFileName());
            } else {
                JOptionPane.showMessageDialog(null, "Failed to create folder.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    public void buildFileTree() {
        if (fileManager == null) return;

        Path rootPath = fileManager.getRootdir();

        if (rootPath == null || !Files.exists(rootPath)) {
            fe_tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Project Not Found")));
            return;
        }

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootPath.getFileName().toString());

        recursivelyAddNodes(rootNode, rootPath);

        fe_tree.setModel(new DefaultTreeModel(rootNode));
        // Expand the root node by default
        fe_tree.expandPath(new TreePath(rootNode));
    }


    private void recursivelyAddNodes(DefaultMutableTreeNode parentNode, Path parentPath) {
        try {
            List<Path> contents = new ArrayList<>();
            try (Stream<Path> stream = Files.list(parentPath)) {
                stream.forEach(contents::add);
            }

            // Sort directories first, then files alphabetically
            contents.sort((p1, p2) -> {
                boolean isDir1 = Files.isDirectory(p1);
                boolean isDir2 = Files.isDirectory(p2);

                if (isDir1 && !isDir2) return -1;
                if (!isDir1 && isDir2) return 1;

                return p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString());
            });

            ArrayList<SFile> sFiles = fileManager.getFiles();

            for (Path childPath : contents) {
                String fileName = childPath.getFileName().toString();

                // Skip hidden files/folders and those starting with '.'
                if (Files.isHidden(childPath) || fileName.startsWith(".")) {
                    continue;
                }

                if (Files.isDirectory(childPath)) {
                    SFile dirSFile = new SFile(childPath);
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(dirSFile);

                    parentNode.add(childNode);

                    recursivelyAddNodes(childNode, childPath);
                } else {
                    // Check if the file is tracked by the FileManager
                    // NOTE: Since FileManager tracks ALL files now, this will include them all.
                    SFile targetSFile = sFiles.stream()
                            .filter(sf -> sf.getPath().equals(childPath))
                            .findFirst()
                            .orElse(null);

                    if (targetSFile != null) {
                        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(targetSFile);
                        parentNode.add(childNode);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading directory for tree build: " + e.getMessage());
        }
    }


    public Path resolveNodeToPath(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();

        if (userObject instanceof SFile sfile) {
            return sfile.getPath();
        }
        if (userObject instanceof String) {
            return fileManager.getRootdir();
        }
        return fileManager.getRootdir();
    }

    public FileManager getFileManager() {
        return fileManager;
    }
    public void setSelectedFile(SFile newFile) {
        this.selectedFile = newFile;
    }

    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
    }

    public JTree getFeTree() {
        return fe_tree;
    }
}