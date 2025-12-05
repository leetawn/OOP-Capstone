package com.exception.ccpp.FileManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SFile extends CCFile {
    private String content = "";

    public SFile(String filepath) { super(filepath); }
    public SFile(Path path) { super(path); }

    /****************** INPUT/OUTPUT ******************/
    protected void load() {
        if (Files.exists(path)) {
            try {
                content = Files.readString(path);
            } catch (Exception ignored) {}
        }
    }
    public void writeOut() {
        try {
            Files.writeString(path, content);
        } catch (Exception ignored) {}
    }

    /****************** GETTERS ******************/
    public String getContent() { return content; }

    /****************** SETTERS ******************/
    public void setContent(String content) { this.content = content; }


    public static void main(String[] args) {
        SFile s = new SFile("TODO");
        System.out.println(s.getContent());
        s.writeOut();
    }

}
