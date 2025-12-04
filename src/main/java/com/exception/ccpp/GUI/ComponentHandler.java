package com.exception.ccpp.GUI;

import java.awt.event.*;
import java.io.File;

/*
    This class handles Button actions which shall be implemented by static classes under TextEditor.java
*/
public abstract class ComponentHandler implements ActionListener {

    private final TextEditor textEditor;

    public ComponentHandler(TextEditor textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public abstract void actionPerformed(ActionEvent e);

    public TextEditor getTextEditor() {
        return this.textEditor;
    }

}
