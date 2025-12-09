package com.exception.ccpp.GUI;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

import static com.exception.ccpp.Gang.SlaveManager.slaveWorkers;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("CodeChum++");
        TextEditor editor = TextEditor.getInstance();
        setContentPane(editor);

        URL url = TextEditor.class.getResource("/assets/logo2.png");
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image image = icon.getImage();
            setIconImage(image);

            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                try {
                    Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
                    java.lang.reflect.Method getTaskbar = taskbarClass.getMethod("getTaskbar");
                    Object taskbar = getTaskbar.invoke(null);
                    java.lang.reflect.Method setIconImage = taskbarClass.getMethod("setIconImage", Image.class);
                    setIconImage.invoke(taskbar, image);
                } catch (Exception e) {
                    setIconImage(image);
                }
            }
        }

        createMenu();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                slaveWorkers.shutdown();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                int sideWidth = 200;

                TextEditor.getInstance().mainSplit.setDividerLocation(sideWidth);
                TextEditor.getInstance().centerRightSplit.setDividerLocation(width - sideWidth - sideWidth);
            }
        });
        setVisible(true);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        JMenu themesMenu = new JMenu("Themes");
        viewMenu.add(themesMenu);

        JRadioButtonMenuItem lightItem = new JRadioButtonMenuItem("Light");
        JRadioButtonMenuItem darkItem = new JRadioButtonMenuItem("Dark");

        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightItem);
        themeGroup.add(darkItem);

        themesMenu.add(lightItem);
        themesMenu.add(darkItem);

        darkItem.addActionListener(e -> {
            System.out.println("Switch to dark mode");
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                SwingUtilities.updateComponentTreeUI(this);
            } catch (UnsupportedLookAndFeelException ex) {
                System.err.println("Sucks to be you youll be drench in light");
            }
        });

        lightItem.addActionListener(e -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
                SwingUtilities.updateComponentTreeUI(this);
            } catch (UnsupportedLookAndFeelException ex) {
                System.err.println("Welp deal with it");
            }
        });

        JMenuItem fullscreenItem = new JMenuItem("Fullscreen");
        fullscreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        viewMenu.add(fullscreenItem);


        fullscreenItem.addActionListener(new ActionListener() {
            private boolean fullscreen = false;
            private Rectangle lastBounds;

            @Override
            public void actionPerformed(ActionEvent e) {

                fullscreen = !fullscreen;

                if (fullscreen) {
                    lastBounds = getBounds();

                    dispose();
                    setUndecorated(true);

                    GraphicsDevice gd =
                            GraphicsEnvironment.getLocalGraphicsEnvironment()
                                    .getDefaultScreenDevice();

                    gd.setFullScreenWindow(MainFrame.this);
                } else {
                    GraphicsDevice gd =
                            GraphicsEnvironment.getLocalGraphicsEnvironment()
                                    .getDefaultScreenDevice();

                    gd.setFullScreenWindow(null);
                    dispose();
                    setUndecorated(false);

                    setBounds(lastBounds);
                    setVisible(true);
                }
            }
        });
    }
}
