package com.exception.ccpp.GUI;

import java.awt.event.*;
import java.io.File;

/*
    This class handles Button actions which shall be implemented by static classes under TextEditor.java
*/
public abstract class ComponentHandler implements ActionListener {

    private static TextEditor textEditor = null;

    public ComponentHandler(TextEditor textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public abstract void actionPerformed(ActionEvent e);

    public static TextEditor getTextEditor() {
        return textEditor;
    }

}
