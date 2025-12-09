package com.exception.ccpp;

import com.exception.ccpp.GUI.MainFrame;
import com.exception.ccpp.GUI.TextEditor;

import javax.swing.*;

// TODO: DO NOT REMOVE THIS CLASS THIS WILL BE OUR ENTRY POINT FOR THE APP
public class Main {

    public static void main(String[] args) {
        TextEditor.makeCoolAndNormal(); // IMPORTANT CODE, IT MAKES UI COOL AND NORMAL
        SwingUtilities.invokeLater(MainFrame::new); // We enter the main frame, sucks to be us
    }
}
