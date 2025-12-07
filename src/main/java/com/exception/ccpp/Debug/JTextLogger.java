package com.exception.ccpp.Debug;

import javax.swing.*;

public class JTextLogger implements CCLogger {
    JTextArea textArea;

    public JTextLogger(JTextArea textArea) {
        this.textArea = textArea;
    }


    @Override
    public void log(String msg) {
        SwingUtilities.invokeLater(() -> textArea.append(msg));
    }

    @Override
    public void logln(String msg) {
        SwingUtilities.invokeLater(() ->
                textArea.append(msg+"\n")
        );
    }

    @Override
    public void logf(String format, Object... args) {
        SwingUtilities.invokeLater(() ->
                textArea.append(String.format(format, args))
        );
    }

    @Override
    public void err(String msg) {
        log(msg);
    }

    @Override
    public void errln(String msg) {
        logln(msg);

    }

    @Override
    public void errf(String format, Object... args) {
        logf(format,args);
    }
}
