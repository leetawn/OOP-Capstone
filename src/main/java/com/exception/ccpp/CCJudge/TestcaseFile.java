package com.exception.ccpp.CCJudge;

import com.exception.ccpp.FileManagement.CCFile;
import com.exception.ccpp.FileManagement.CrypticWriter;
import com.exception.ccpp.FileManagement.FileManager;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TestcaseFile extends CCFile implements TerminalCallback{
    private ArrayList<String[]> inputs = new ArrayList<>();
    private ArrayList<String> expected_outputs = new ArrayList<>();
    Map<String, Testcase> testcases = new ConcurrentHashMap<>();

    public TestcaseFile(String filepath) {
        super(filepath);
        load();
    }
    public TestcaseFile(Path path) {
        super(path);
        load();
    }
    // FOR TerminalApp Usage ONLY
    TestcaseFile(String[] input) {
        super((Path) null);
        inputs.add(input);
        expected_outputs.add(null);
    }

    public ArrayList<String[]> getInputs() {
        return this.inputs;
    }
    public ArrayList<String> getExpectedOutputs() {
        return this.expected_outputs;
    }

    public void deleteTestcase() {

    }

    public void addTestcase(FileManager fm) {

    }

    @Override
    protected void load() {
        if (path == null) return;

        try {
            List<Object> retrievedData = CrypticWriter.readEncryptedData(path);
            if (retrievedData.size() > 0) {
                String[][] input_arr = (String[][]) retrievedData.get(0);
                String[] exout_arr = (String[]) retrievedData.get(1);

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
        if (path == null) return;

        List<Object> data = new ArrayList<>();
        String[][] input_arr = inputs.toArray(new String[inputs.size()][]);
        String[] ex_out =  expected_outputs.toArray(new String[0]);
        data.add(input_arr);
        data.add(ex_out);
        try {
            CrypticWriter.writeEncryptedData(data, path);
        } catch (Exception e) {
            System.err.println("TestcaseFile.writeOut() error writing " + path);
            e.printStackTrace();
        }
    }

    @Override
    public void onTerminalExit(String[] inputs, String expected) {
        this.inputs.add(inputs);
        this.expected_outputs.add(expected);
    }
}
