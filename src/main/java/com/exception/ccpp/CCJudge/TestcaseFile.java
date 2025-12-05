package com.exception.ccpp.CCJudge;

import com.exception.ccpp.FileManagement.CCFile;

import java.nio.file.Path;

public class TestcaseFile extends CCFile {
    private String[][] inputs;
    private String[] expected_outputs;

    public TestcaseFile(String filepath) {
        super(filepath);
    }

    public TestcaseFile(Path path) {
        super(path);
    }

    @Override
    protected void load() {
        // TODO
    }

    @Override
    public void writeOut() {
        // TODO
    }

    public void use() {
        // TODO
    }
}
