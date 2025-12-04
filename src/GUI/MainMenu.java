package GUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainMenu extends JPanel {
    private JLabel title = new JLabel();
    private JLabel description = new JLabel();
    private JLabel start = new JLabel();
    private JButton openFolder = new JButton();

    public MainMenu(MainWindow window) {
        setBackground(new Color(40, 48, 59));
        setLayout(new GridBagLayout());

        // Title label setup
        title.setText("CodeChum++");
        title.setFont(new Font("Consolas", Font.BOLD, 64));
        title.setForeground(Color.WHITE);

        // Description label setup
        description.setText("Fastest and Reliable CodeJudger!");
        description.setFont(new Font("Consolas", Font.PLAIN, 24));
        description.setForeground(new Color(200, 200, 200));

        // Start label setup
        start.setText("Start Coding");
        start.setFont(new Font("Consolas", Font.PLAIN, 20));
        start.setForeground(Color.WHITE);

        // Open Folder button setup
        openFolder.setText("Open Folder");
        openFolder.setFont(new Font("Consolas", Font.BOLD, 20));
        openFolder.setForeground(Color.WHITE);
        openFolder.setBackground(new Color(90, 130, 255));
        openFolder.setFocusPainted(false);
        openFolder.setBorderPainted(false);
        openFolder.setPreferredSize(new Dimension(200, 45));
        openFolder.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Layout constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 0, 15, 0);

        // Add components to panel
        gbc.gridy = 0;
        add(title, gbc);
        gbc.gridy = 1;
        add(description, gbc);
        gbc.gridy = 2;
        add(start, gbc);
        gbc.gridy = 3;
        add(openFolder, gbc);

        // 🔵 BUTTON ACTION: OPEN FOLDER + REDIRECT TO EDITOR PAGE
        openFolder.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Project Folder");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = chooser.getSelectedFile();
                // TODO: Pass selected folder to EditorPage
                System.out.println("Selected folder: " + selectedFolder.getAbsolutePath());
                // Go to editor screen
                window.showPage("editor");
            }
        });
    }
}
