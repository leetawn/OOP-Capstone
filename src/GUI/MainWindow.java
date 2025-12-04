package GUI;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel container = new JPanel(cardLayout);

    public MainWindow() {
        setTitle("CodeChum++");
        setSize(1400, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Screens
        MainMenu menu = new MainMenu(this);
        TextEditor editor = new TextEditor();
        container.add(menu, "menu");
        container.add(editor, "editor");

        setContentPane(container);
    }

    public void showPage(String name) {
        cardLayout.show(container, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
