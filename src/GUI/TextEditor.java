package GUI;

import FileManagement.*;
import CustomExceptions.*;
import FileManagement.NotDirException;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class TextEditor {
    private JButton runCodeButton;
    private JTextArea dTextArea;
    private JComboBox comboBox1;
    private JTree fe_tree;
    private JPanel fileExplorer;
    private JPanel mainPanel;

    private FileManager fileManager;

    public TextEditor() throws FileListingException, TreePopulationException {
        try {
            fileManager = new FileManager(".", "java"); // project root
        } catch (NotDirException e) {
            throw new FileListingException("Invalid directory: " + e.getMessage(), e);
        }

        buildFileTree(); // can throw TreePopulationException

        dTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        dTextArea.setTabSize(4);

        fe_tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
            if (node == null) return;

            Object obj = node.getUserObject();
            if (obj instanceof SFile sfile) {
                try {
                    openFile(sfile); // throws FileReadException
                } catch (FileReadException ex) {
                    dTextArea.setText("// Error loading file: " + ex.getMessage());
                }
            }
        });

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

        fileExplorer.setLayout(new BorderLayout());
        fileExplorer.add(new JScrollPane(fe_tree), BorderLayout.CENTER);
    }

    private void buildFileTree() throws TreePopulationException {
        ArrayList<SFile> files;
        try {
            files = fileManager.getFiles();
        } catch (Exception e) {
            throw new TreePopulationException("Failed to list files from FileManager", e);
        }

        DefaultMutableTreeNode rootNode =
                new DefaultMutableTreeNode(fileManager.getRootdir().getFileName().toString());

        try {
            for (SFile sfile : files) {
                addFileNode(rootNode, sfile);
            }
        } catch (Exception e) {
            throw new TreePopulationException("Error while populating file tree", e);
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

    private void openFile(SFile sfile) throws FileReadException {
        try {
            String content = Files.readString(sfile.getPath());
            dTextArea.setText(content);
        } catch (Exception e) {
            throw new FileReadException("Failed to read file: " + sfile.getPath().toString(), e);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                TextEditor editor = new TextEditor();
                JFrame frame = new JFrame("Text Editor with File Explorer");
                frame.setContentPane(editor.getMainPanel());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 700);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (FileListingException | TreePopulationException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
