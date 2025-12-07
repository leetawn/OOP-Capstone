package com.exception.ccpp.GUI;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private int radius;
    private Color borderColor = null;
    private int borderThickness = 1;

    public RoundedPanel(int radius) {
        super();
        this.radius = radius;
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout, int radius) {
        super(layout);
        this.radius = radius;
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout, int radius, String hexColor) {
        super(layout);
        this.radius = radius;
        setBackground(Color.decode(hexColor));
        setOpaque(false);
        super.setOpaque(false);
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    public void setBorderThickness(int thickness) {
        this.borderThickness = thickness;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // MOVE THIS TO TOP

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill rounded background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        super.paintBorder(g);

        if (borderColor != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw rounded border
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderThickness));
            g2.drawRoundRect(borderThickness/2, borderThickness/2,
                    getWidth() - borderThickness, getHeight() - borderThickness,
                    radius, radius);

            g2.dispose();
        }
    }
}