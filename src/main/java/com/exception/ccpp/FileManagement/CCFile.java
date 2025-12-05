package com.exception.ccpp.FileManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class CCFile {
    // testing
    protected Path path = null;

    /****************** LOADS ******************/
    public CCFile(String filepath) {
        path = Paths.get(filepath);
    }


    public CCFile(Path path) {
        this.path = path;
    }

    /****************** INPUT/OUTPUT ******************/
    public void delete() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            System.out.printf("SFile.deleteDenied: %s\n", path);
        }
    }
    protected abstract void load();
    public abstract void writeOut();


    /****************** GETTERS ******************/
    public Path getPath() { return path; }
    public String getStringPath() { return path.toString(); }

    /****************** SETTERS ******************/
    public void setPath(Path newPath) { this.path = newPath; }


}
