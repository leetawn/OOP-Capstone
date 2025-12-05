package com.exception.ccpp.CCJudge;

import com.exception.ccpp.FileManagement.CCFile;
import com.exception.ccpp.FileManagement.CrypticWriter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestcaseFile extends CCFile implements TerminalCallback{
    private ArrayList<String[]> inputs;
    private ArrayList<String> expected_outputs;

    public TestcaseFile(String filepath) {
        super(filepath);
        inputs = new ArrayList<>();
        expected_outputs = new ArrayList<>();
    }
    public TestcaseFile(Path path) {
        super(path);
        inputs = new ArrayList<>();
        expected_outputs = new ArrayList<>();
    }

    public ArrayList<String[]>  getInputs() {
        return this.inputs;
    }
    public ArrayList<String> getExpectedOutputs() {
        return this.expected_outputs;
    }


    @Override
    protected void load() {
        try {
            List<Object> retrievedData = CrypticWriter.readEncryptedData(path);
            if (retrievedData.size() > 0) {
                String[][] input_arr = (String[][]) retrievedData.get(1);
                String[] exout_arr = (String[]) retrievedData.get(0);
                inputs = new ArrayList<>(Arrays.asList(input_arr));

                expected_outputs = new ArrayList<>(Arrays.asList(exout_arr));
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
            data.add(inputs.toArray(new String[inputs.size()][]));
            data.add(expected_outputs.toArray(new String[0]));
            try {
                CrypticWriter.writeEncryptedData(data, path);
            } catch (Exception e) {
                System.err.println("TestcaseFile.writeOut() error writing " + path);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTerminalExit(String[] inputs, String expected) {
        this.inputs.add(inputs);
        this.expected_outputs.add(expected);
    }
}
