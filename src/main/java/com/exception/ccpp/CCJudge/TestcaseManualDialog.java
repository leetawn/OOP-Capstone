package com.exception.ccpp.CCJudge;

import com.exception.ccpp.GUI.UpdateGUICallback;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class TestcaseManualDialog {
    public static final String COLOR_FOREGROUND = "#D4D4D4";
    public static final String COLOR_BACKGROUND = "#1E1E1E";
    public static final String COLOR_CURRENTLINE = "#2A2A2A";
    public static final String COLOR_SELECTED = "#264F78";

//    public static class Result {
//        public final String inputsText;
//        public final String expectedText;
//
//        public Result(String inputsText, String expectedText) {
//            this.inputsText = inputsText;
//            this.expectedText = expectedText;
//        }
//    }

    private static RSyntaxTextArea createTextArea()
    {
        RSyntaxTextArea textArea =  new RSyntaxTextArea(8, 40);;
        textArea.setForeground(Color.decode(COLOR_FOREGROUND));
        textArea.setBackground(Color.decode(COLOR_BACKGROUND));
        textArea.setCurrentLineHighlightColor(Color.decode(COLOR_CURRENTLINE));
        textArea.setSelectedTextColor(Color.decode(COLOR_SELECTED));
        textArea.setAntiAliasingEnabled(true);
        textArea.setFractionalFontMetricsEnabled(false);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        return textArea;
    }

    public static void show(TestcaseFile tf, UpdateGUICallback gui_cb) {
        JDialog dialog = new JDialog((Window)null, "Test Case Editor");
        dialog.setModalityType(Dialog.ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));

        // Text areas
        RSyntaxTextArea inputsArea = createTextArea();
        RSyntaxTextArea expectedArea = createTextArea();

        // Scroll panes
        RTextScrollPane inputsScroll = new RTextScrollPane(inputsArea);
        RTextScrollPane expectedScroll = new RTextScrollPane(expectedArea);

        // Labels
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.gridy = 0;
        center.add(new JLabel("Inputs (newline separated):"), gc);
        gc.gridy = 2;
        center.add(new JLabel("Expected Output:"), gc);

        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.gridy = 1;
        center.add(inputsScroll, gc);
        gc.gridy = 3;
        center.add(expectedScroll, gc);

        dialog.add(center, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel();
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);

        dialog.add(buttons, BorderLayout.SOUTH);

        // Result container
        ok.addActionListener(e -> {
            tf.manualAddTestcase(
                new Testcase(inputsArea.getText().trim().split("\\R"), expectedArea.getText().trim()),
                gui_cb
            );
            dialog.dispose();
        });

        cancel.addActionListener(e -> {
            dialog.dispose();
        });

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}

