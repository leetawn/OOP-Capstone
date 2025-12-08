package com.exception.ccpp.FileManagement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.exception.ccpp.Gang.SlaveManager.slaveWorkers;

public final class SFile extends CCFile {
    private String content = "";

    private SFile(Path path) {
        super(path);
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
    protected void read() {
        if (Files.exists(path)) {
            try {
                content = Files.readString(path);
            } catch (Exception ignored) {}
        }
    }
    public void write() {
        try {
            Files.writeString(path, content);
        } catch (Exception ignored) {}
    }

    /****************** GETTERS ******************/
    public String getContent() { return content; }

    /****************** SETTERS ******************/
    public void setContent(String content) { this.content = content; }

}
