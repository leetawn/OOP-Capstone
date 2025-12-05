package com.exception.ccpp.GUI;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {
    private int radius;
    private int verticalOffset = 0;
    private Image iconImage; // Field to store the image

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    // New method to set the icon image (using Image instead of ImageIcon)
    public void setIconImage(Image image) {
        this.iconImage = image;

        // Use a MediaTracker to wait for the image to load completely
        if (iconImage != null) {
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(iconImage, 1);
            try {
                mt.waitForAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Image loading interrupted: " + e.getMessage());
            }
        }

        Dimension size = getPreferredSize();
        // Now that MediaTracker has run, image dimensions should be valid
        int imageWidth = iconImage != null ? iconImage.getWidth(this) : 0;
        int imageHeight = iconImage != null ? iconImage.getHeight(this) : 0;
        if (iconImage != null && imageWidth > 0) {
            size.height = Math.max(size.height, imageHeight + 10);
            // Give extra width for the icon + some padding
            size.width += imageWidth + 5;
        }
        setPreferredSize(size);

        // Request a layout refresh from the container
        if (getParent() != null) {
            getParent().revalidate();
        }

        repaint();
    }

    public void setVerticalOffset(int offset) {
        this.verticalOffset = offset;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // --- Draw Background ---
        if (getModel().isPressed()) {
            g2.setColor(getBackground().darker());
        } else if (getModel().isRollover()) {
            // Use a slight dimmer color for rollover since brighter might clash with some colors
            Color brighterColor = getBackground().brighter();
            int r = Math.min(255, brighterColor.getRed() + 10);
            int gr = Math.min(255, brighterColor.getGreen() + 10);
            int b = Math.min(255, brighterColor.getBlue() + 10);
            g2.setColor(new Color(r, gr, b));
        } else {
            g2.setColor(getBackground());
        }

        g2.fillRoundRect(0, verticalOffset, getWidth(), getHeight() - verticalOffset, radius, radius);

        // --- Calculate Positions ---
        g2.setColor(getForeground());
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();

        int contentWidth = fm.stringWidth(text);
        int iconWidth = 0;
        int padding = 0;

        if (iconImage != null) {
            iconWidth = iconImage.getWidth(this);

            // Logic: Only add padding (space between icon and text) if the text is NOT empty
            if (!text.isEmpty()) {
                padding = 5;
            } else {
                padding = 0;
            }

            // Combine the content width (Icon Width + Padding + Text Width)
            contentWidth = contentWidth + iconWidth + padding;
        }

        // Center the entire content block (icon + text)
        int startX = (getWidth() - contentWidth) / 2;
        int iconY = (getHeight() - (iconImage != null ? iconImage.getHeight(this) : 0)) / 2 + verticalOffset;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() + verticalOffset;

        // --- Draw Icon (if present) ---
        if (iconImage != null) {
            // Check if icon dimensions are valid before drawing
            if (iconImage.getWidth(this) > 0 && iconImage.getHeight(this) > 0) {
                g2.drawImage(iconImage, startX, iconY, this);
            }
            startX += iconWidth + padding; // Shift text starting point
        }

        // --- Draw Text ---
        g2.drawString(text, startX, textY);

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // No border
    }
}