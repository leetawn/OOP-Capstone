package com.exception.ccpp.GUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainMenu extends JPanel {

    private JLabel title = new JLabel("CodeChum++");
    private JLabel description = new JLabel("The Fastest and Reliable CodeJudger!");
    private JLabel start = new JLabel("Start Coding");
    private JButton openFolder = new JButton("Open Folder");

    public MainMenu() {
        setLayout(new GridBagLayout());
        setBackground(new Color(33, 41, 52));

        title.setFont(new Font("Consolas", Font.BOLD, 64));
        title.setForeground(Color.WHITE);

        description.setFont(new Font("Consolas", Font.PLAIN, 24));
        description.setForeground(new Color(200, 200, 200));

        start.setFont(new Font("Consolas", Font.PLAIN, 20));
        start.setForeground(Color.WHITE);
        start.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

        openFolder.setFont(new Font("Consolas", Font.PLAIN, 20));
        openFolder.setForeground(Color.BLACK);
        openFolder.setBackground(Color.RED);
        openFolder.setBorderPainted(false);
        openFolder.setContentAreaFilled(true);
        openFolder.setOpaque(true);
        openFolder.setFocusPainted(false);
        openFolder.setPreferredSize(new Dimension(200, 45));

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridy = 0;
        add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 20, 0);
        add(description, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(30, 0, 10, 0);
        add(start, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(5, 0, 0, 0);
        add(openFolder, gbc);

        // ---------- ACTION ----------
        openFolder.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {

                File folder = chooser.getSelectedFile();
                if (folder != null && folder.isDirectory()) {

                    SwingUtilities.invokeLater(() -> {
                        JFrame editorFrame = new JFrame("CodeChum++ Editor");

                        TextEditor editorPanel =
                                new TextEditor(folder.getAbsolutePath(), this);

                        editorFrame.setContentPane(editorPanel);
                        editorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        editorFrame.setSize(1400, 800);
                        editorFrame.setLocationRelativeTo(null);
                        editorFrame.setVisible(true);
                    });

                    // Close MainMenu
                    Window w = SwingUtilities.getWindowAncestor(this);
                    if (w != null) w.dispose();
                }
            }
        });

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("com.exception.ccpp.Main Menu");
            frame.setContentPane(new MainMenu());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
