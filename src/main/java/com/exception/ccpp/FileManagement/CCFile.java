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
    private static Map<Path, CCFile> cachedFiles = new ConcurrentHashMap<>();
    private static Map<Path, CCFile> tempCache = null;

    /****************** LOADS ******************/
    protected CCFile(String filepath) {
        this(Paths.get(filepath));
    }
    protected CCFile(Path path) {
        this.path = path;
        cachedFiles.put(this.path, this);
        if (tempCache != null) { tempCache.put(this.path, this); }
    }

    /****************** INPUT/OUTPUT ******************/
    public static CCFile getCached(String path) { return getCached(Paths.get(path)); }
    public static CCFile getCached(Path path) {
        path = path.toAbsolutePath().normalize();
        if (cachedFiles.containsKey(path)) {
            System.err.println("[CCFile.cache] " + path + " is already cached");
            CCFile cached = cachedFiles.get(path);
            if (tempCache != null) { tempCache.put(path, cached); }
            return cached;
        }
        System.out.println("[CCFile.cache] " + path + " is not cached");
        return null;
    }
    public static void clearOldCache() {
        cachedFiles.clear();
        if (tempCache != null) {
            cachedFiles = tempCache;
            tempCache = null;
        }
    }
    public static void newCache() {
        tempCache = new ConcurrentHashMap<>();
    }

    public void delete() {
        try {
            if (Files.deleteIfExists(path))
            {
                cachedFiles.remove(path);
                System.out.println("CCFile.rmCachedFile " + path);
            }
        } catch (IOException ignored) {
            System.out.printf("CCFile.rmDenied: %s\n", path);
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
