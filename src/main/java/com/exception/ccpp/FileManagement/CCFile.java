package com.exception.ccpp.FileManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CCFile {
    // testing
    protected Path path = null;
    private static Map<Path, CCFile> loadedFiles = new ConcurrentHashMap<>();

    /****************** LOADS ******************/
    protected CCFile(String filepath) {
        this(Paths.get(filepath));
    }
    protected CCFile(Path path) {
        this.path = path;
        loadedFiles.put(this.path, this);
    }

    /****************** INPUT/OUTPUT ******************/
    public static CCFile getCached(String path) { return getCached(Paths.get(path)); }
    public static CCFile getCached(Path path) {
        if (loadedFiles.containsKey(path)) {
            System.err.println("File " + path + " is already cached");
            return loadedFiles.get(path);
        }
        System.out.println("File " + path + " is not cached");
        return null;
    }

    public void delete() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            System.out.printf("SFile.deleteDenied: %s\n", path);
        }
    }
    protected abstract void read();
    public abstract void write();


    /****************** GETTERS ******************/
    public Path getPath() { return path; }
    public String getStringPath() { return path.toString(); }

    /****************** SETTERS ******************/
    public void setPath(Path newPath) { this.path = newPath; }


}
