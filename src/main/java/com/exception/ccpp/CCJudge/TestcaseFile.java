package com.exception.ccpp.CCJudge;

import com.exception.ccpp.FileManagement.CCFile;
import com.exception.ccpp.FileManagement.CrypticWriter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestcaseFile extends CCFile {
    private String[][] inputs;
    private String[] expected_outputs;

    public TestcaseFile(String filepath) {
        super(filepath);
    }

    public TestcaseFile(Path path) {
        super(path);
    }

    public TestcaseFile(String filepath, String[][] inputs, String[] expected_outputs) {
        super(filepath);
        this.inputs = inputs;
        this.expected_outputs = expected_outputs;
        writeOut();
    }

    String[][] getInputs() {
        return inputs;
    }
    public String[] getExpectedOutputs() {
        return expected_outputs;
    }


    @Override
    protected void load() {
        try {
            List<Object> retrievedData = CrypticWriter.readEncryptedData(path);
            if (retrievedData.size() > 0) {
                inputs = (String[][]) retrievedData.get(1);
                expected_outputs = (String[]) retrievedData.get(0);
            }
        } catch (Exception e) {
            System.err.println("TestcaseFile.load() error loading " + path);
            e.printStackTrace();
        }
    }

    @Override
    public void writeOut() {
        {
            List<Object> data = new ArrayList<>();
            data.add(inputs);
            data.add(expected_outputs);
            try {
                CrypticWriter.writeEncryptedData(data, path);
            } catch (Exception e) {
                System.err.println("TestcaseFile.writeOut() error writing " + path);
                e.printStackTrace();
            }
        }
    }
}
