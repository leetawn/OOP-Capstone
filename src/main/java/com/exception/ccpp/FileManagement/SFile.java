package com.exception.ccpp.FileManagement;

import com.exception.ccpp.Common.Helpers;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RUndoManager;

import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.exception.ccpp.Gang.SlaveManager.slaveWorkers;

public final class SFile extends CCFile {
    private RSyntaxDocument doc;
    private boolean isModified = false;

    private SFile(Path path) {
        super(path);
        doc = new RSyntaxDocument(Helpers.getSyntaxHightlighting(path.toString()));
        read();
    }

    public static SFile open(String path) {
        return open(Paths.get(path));
    };
    public static SFile open(Path path) {
        CCFile f = getCached(path);
        if (f == null) return new SFile(path);
        assert f instanceof SFile; // FIXME: Remove later, this only for detection
        return (SFile) f;
    }

    /****************** INPUT/OUTPUT ******************/
    public void read() {
        try {
            if (Files.exists(path)) doc.insertString(0, Files.readString(path), null);
            else Files.createFile(path);
        } catch (Exception ignored) {}
        this.isModified = false;
    }
    public void write() {
        try {
            String content = doc.getText(0, doc.getLength());
            Files.writeString(path, content);
        } catch (Exception ignored) {}
        this.isModified = false;
    }

    /****************** GETTERS ******************/
    public RSyntaxDocument getDoc() { return doc; }
    public boolean isDirty() { return this.isModified; }

    /****************** SETTERS ******************/
    public void setModified(boolean modified) {
        this.isModified = modified;
    }
    public void attachTo(RSyntaxTextArea textArea) {
        textArea.setDocument(doc);
    }


}
