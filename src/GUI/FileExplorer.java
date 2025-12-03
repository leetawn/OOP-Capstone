package GUI;

import CustomExceptions.NotDirException;
import FileManagement.FileManager;
import FileManagement.SFile;

import javax.swing.*;
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
    private TextEditor textEditor;

    public FileExplorer(String rootDir, JTextArea editorTextArea, TextEditor textEditor) {
        this.dTextArea = editorTextArea;
        this.textEditor = textEditor;
        initializeBackend(rootDir);
        initializeComponents();
        setupLayout();
        setupEventListeners();
        buildFileTree();
    }

    public void updateRootDirectory(String newRootDir) throws NotDirException {
        String currentLang = fileManager != null ? fileManager.getLanguage() : null;
        this.fileManager = new FileManager(newRootDir, currentLang);
        this.dTextArea.setText("");
        this.buildFileTree();
    }


    private void initializeBackend(String rootDir) {
        try {
            fileManager = new FileManager(rootDir, null);
        } catch (NotDirException e) {
            JOptionPane.showMessageDialog(this, "Invalid directory: " + e.getMessage());
        }
    }

    private void initializeComponents() {
        fe_tree = new JTree();
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

        contextMenu.addSeparator();
        contextMenu.add(renameItem);
        contextMenu.add(deleteItem);
        contextMenu.add(createFolderItem);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        JScrollPane treeScroll = new JScrollPane(fe_tree);
        treeScroll.setBorder(BorderFactory.createTitledBorder("File Explorer"));
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

        fe_tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int selRow = fe_tree.getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = fe_tree.getPathForLocation(e.getX(), e.getY());

                    if (selRow != -1) {
                        fe_tree.setSelectionPath(selPath);
                    } else {
                        fe_tree.setSelectionRow(0);
                    }

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();

                    if (node != null && node.getUserObject() instanceof SFile) {
                        renameItem.setVisible(true);
                        deleteItem.setVisible(true);
                    } else {
                        renameItem.setVisible(false);
                        deleteItem.setVisible(false);
                    }

                    contextMenu.show(fe_tree, e.getX(), e.getY());
                }
            }
        });

        createFolderItem.addActionListener(e -> {
            FileManager fm = getFileManager();
            if (fm == null) return;

            DefaultMutableTreeNode selectedTreePathNode = getSelectedNode();
            Path directoryToCreateIn = fileManager.getRootdir();
            DefaultMutableTreeNode parentNodeInTree;

            if (selectedTreePathNode != null) {
                Object obj = selectedTreePathNode.getUserObject();
                if (obj instanceof SFile sfile) {
                    directoryToCreateIn = Files.isDirectory(sfile.getPath()) ? sfile.getPath() : sfile.getPath().getParent();
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

                    model.insertNodeInto(newFolderTreeNode, parentNodeInTree, parentNodeInTree.getChildCount());

                    fe_tree.expandPath(new TreePath(parentNodeInTree.getPath()));

                    JOptionPane.showMessageDialog(null, "Folder created successfully in " + directoryToCreateIn.getFileName());
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to create folder.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();

            if (node == null || !(node.getUserObject() instanceof SFile sfile)) return;

            if (Files.isDirectory(sfile.getPath())) {
                JOptionPane.showMessageDialog(null, "Cannot delete a folder.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Delete file: " + sfile.getPath().getFileName() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = fileManager.deleteFile(sfile);

                if (success) {

                    DefaultTreeModel model = (DefaultTreeModel) fe_tree.getModel();
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();

                    if (parentNode != null) {
                        model.removeNodeFromParent(node);

                        if (sfile.equals(fileManager.getCurrentFile())) {
                            dTextArea.setText("");
                            fileManager.setCurrentFile(null);
                        }
                    } else {
                        buildFileTree();
                    }


                    JOptionPane.showMessageDialog(null, "File deleted successfully.");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to delete file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        renameItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
            if (node == null || !(node.getUserObject() instanceof SFile sfile)) return;

            if (Files.isDirectory(sfile.getPath())) {
                JOptionPane.showMessageDialog(null, "Cannot rename a folder.");
                return;
            }

            String newName = JOptionPane.showInputDialog(null,
                    "Enter new name for file:", sfile.getPath().getFileName().toString());

            if (newName == null || newName.isBlank()) return;

            if (!fileManager.isAllowedFile(newName)) {
                JOptionPane.showMessageDialog(null,
                        "Invalid file extension.\nAllowed: .c, .cpp, .h, .hpp, .java, .py",
                        "Invalid Extension",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = fileManager.renameFile(sfile, newName);
            if (success) {
                buildFileTree();
                JOptionPane.showMessageDialog(null, "File renamed successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to rename file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
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
        fe_tree.expandPath(new TreePath(rootNode));
    }


    private void recursivelyAddNodes(DefaultMutableTreeNode parentNode, Path parentPath) {
        try {
            List<Path> contents = new ArrayList<>();
            try (Stream<Path> stream = Files.list(parentPath)) {
                stream.forEach(contents::add);
            }

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

                if (Files.isHidden(childPath) || fileName.startsWith(".")) {
                    continue;
                }

                if (Files.isDirectory(childPath)) {
                    SFile dirSFile = new SFile(childPath);
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(dirSFile);

                    parentNode.add(childNode);

                    recursivelyAddNodes(childNode, childPath);
                } else {
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

    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
    }

    public JTree getFeTree() {
        return fe_tree;
    }
}