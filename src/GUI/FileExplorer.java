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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileExplorer extends JPanel {
    private JTree fe_tree;
    private FileManager fileManager;
    private JTextArea dTextArea;
    private JPopupMenu contextMenu;
    private JMenuItem renameItem;
    private JMenuItem deleteItem;

    public FileExplorer(String rootDir, JTextArea editorTextArea) {
        this.dTextArea = editorTextArea;
        initializeBackend(rootDir);
        initializeComponents();
        setupLayout();
        setupEventListeners();
        buildFileTree();
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
        contextMenu.add(renameItem);
        contextMenu.add(deleteItem);
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
                if (!Files.isDirectory(sfile.getPath())) {
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
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
                        if (node != null && node.getUserObject() instanceof SFile) {
                            contextMenu.show(fe_tree, e.getX(), e.getY());
                        }
                    }
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
                    buildFileTree();
                    dTextArea.setText("");
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
                        "Invalid Extension", JOptionPane.WARNING_MESSAGE);
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
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(fileManager.getRootdir().getFileName().toString());

        ArrayList<SFile> allFiles = fileManager.getFiles();

        for (SFile sfile : allFiles) {
            addFileNode(rootNode, sfile);
        }

        fe_tree.setModel(new DefaultTreeModel(rootNode));
        fe_tree.expandPath(new TreePath(rootNode));
    }

    private void addFileNode(DefaultMutableTreeNode root, SFile sfile) {
        Path filePath = sfile.getPath();
        Path relativePath = fileManager.getRootdir().relativize(filePath);
        String[] parts = relativePath.toString()
                .split(File.separator.equals("\\") ? "\\\\" : File.separator);

        DefaultMutableTreeNode current = root;

        for (int i = 0; i < parts.length; i++) {
            String partName = parts[i];
            DefaultMutableTreeNode child = findChild(current, partName);

            if (child == null) {
                Object userObj = (i == parts.length - 1) ? sfile : partName;
                child = new DefaultMutableTreeNode(userObj);
                current.add(child);

                List<DefaultMutableTreeNode> folders = new ArrayList<>();
                List<DefaultMutableTreeNode> files = new ArrayList<>();

                for (int j = 0; j < current.getChildCount(); j++) {
                    DefaultMutableTreeNode c = (DefaultMutableTreeNode) current.getChildAt(j);
                    Object obj = c.getUserObject();

                    boolean isFolder = false;
                    if (obj instanceof String) {
                        isFolder = true;
                    } else if (obj instanceof SFile sf) {
                        try {
                            isFolder = Files.isDirectory(sf.getPath());
                        } catch (Exception ex) {
                        }
                    }

                    if (isFolder) {
                        folders.add(c);
                    } else {
                        files.add(c);
                    }
                }

                current.removeAllChildren();
                folders.forEach(current::add);
                files.forEach(current::add);
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

    public Path resolveNodeToPath(DefaultMutableTreeNode node) {
        LinkedList<String> parts = new LinkedList<>();
        DefaultMutableTreeNode current = node;

        while (current != null && current.getParent() != null) {
            Object obj = current.getUserObject();
            if (obj instanceof String folderName) {
                parts.addFirst(folderName);
            } else if (obj instanceof SFile sfile && Files.isDirectory(sfile.getPath())) {
                parts.addFirst(sfile.getPath().getFileName().toString());
            }
            current = (DefaultMutableTreeNode) current.getParent();
        }

        Path path = fileManager.getRootdir();
        for (String part : parts) {
            path = path.resolve(part);
        }
        return path;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
    }
}