package GUI;

import CustomExceptions.NotDirException;
import FileManagement.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextEditor {
    private JButton runCodeButton;
    private JTextArea dTextArea;
    private JComboBox comboBox1;
    private JTree fe_tree;
    private JPanel fileExplorer;
    private JPanel mainPanel;

    private FileManager fileManager;

    public TextEditor() {
        try {
            fileManager = new FileManager(".","java"); // project root
        } catch (NotDirException e) {
            JOptionPane.showMessageDialog(null, "Invalid directory: " + e.getMessage());
            return;
        }

        buildFileTree();

        dTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 16));
        dTextArea.setTabSize(4);

        fe_tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fe_tree.getLastSelectedPathComponent();
            if (node == null) return;

            Object obj = node.getUserObject();
            if (obj instanceof SFile sfile) {
                Path filePath = sfile.getPath();
                try {
                    String content = Files.readString(filePath);
                    dTextArea.setText(content);
                } catch (Exception ex) {
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

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TextEditor editor = new TextEditor();
            JFrame frame = new JFrame("Text Editor with File Explorer");
            frame.setContentPane(editor.getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
