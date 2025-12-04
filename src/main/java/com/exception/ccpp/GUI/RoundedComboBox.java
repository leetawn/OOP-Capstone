package com.exception.ccpp.GUI;

import javax.swing.*;
import java.awt.*;

public class RoundedComboBox<E> extends JComboBox<E> {
    private int radius = 15; // Corner radius

    public RoundedComboBox(E[] items) {
        super(items);
        setupRoundedStyle();
    }

    public RoundedComboBox() {
        super();
        setupRoundedStyle();
    }

    private void setupRoundedStyle() {
        setOpaque(false);
        setBackground(Color.decode("#568afc"));
        setForeground(Color.WHITE);

        // Custom UI with rounded corners
        setUI(new RoundedComboBoxUI());

        // Custom renderer for rounded items
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setHorizontalAlignment(CENTER);
                setOpaque(false); // Make items transparent
                setBackground(Color.decode("#568afc"));
                setForeground(Color.WHITE);
                return this;
            }
        });
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Optional: Draw a border
        g2.setColor(getBackground().darker());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.dispose();
    }

    // Custom UI to style the arrow button
    private class RoundedComboBoxUI extends javax.swing.plaf.basic.BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton("▼") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Draw rounded background for arrow button
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius / 2, radius / 2);

                    // Draw arrow
                    g2.setColor(getForeground());
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth("▼")) / 2;
                    int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString("▼", textX, textY);

                    g2.dispose();
                }

                @Override
                protected void paintBorder(Graphics g) {
                    // No border
                }
            };

            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(false);
            button.setBackground(Color.decode("#568afc"));
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Custom background for selected item
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.decode("#568afc").darker());
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, radius / 2, radius / 2);

            g2.dispose();
        }
    }
}