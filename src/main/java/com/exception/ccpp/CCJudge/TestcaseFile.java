package com.exception.ccpp.CCJudge;

import com.exception.ccpp.FileManagement.CCFile;
import com.exception.ccpp.FileManagement.CrypticWriter;
import com.exception.ccpp.FileManagement.FileManager;
import com.exception.ccpp.GUI.UpdateGUICallback;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;

public class TestcaseFile extends CCFile implements TerminalCallback{
    Map<Testcase, String> testcases = new LinkedHashMap<>();

    public TestcaseFile(String filepath) {
        super(filepath);
        load();
    }
    public TestcaseFile(Path path) {
        super(path);
        load();
    }
    // TerminalApp USAGE ONLY
    TestcaseFile(String[] inputs) {
        super((Path)null);
        testcases = new LinkedHashMap<>();
        testcases.put(new Testcase(inputs,""), "SINGLE_INSTANCE");
    }

    /************ GETTERS *******************/
    // TODO: USE THIS INSTEAD
    public Map<Testcase, String>  getTestcases() { return testcases; }

    /************ BASIC OPS *******************/
    public void deleteTestcase(Testcase tc) {
        testcases.remove(tc);
    }
    public void addTestcase(FileManager fm, UpdateGUICallback gui_cb) {
        new TerminalApp(fm,this, gui_cb);
    }

    /************ I/O *******************/
    @Override
    protected void load() {
        if (path == null) return;

        try {
            List<Object> retrievedData = CrypticWriter.readEncryptedData(path);
            if (retrievedData.size() > 0) {
                Object rawMap = retrievedData.get(0);

                testcases = (LinkedHashMap<Testcase, String>) rawMap;

            }
        } catch (Exception e) {
            System.err.println("TestcaseFile.load() error loading " + path);
            e.printStackTrace();
        }
        if (testcases.size() <= 0) testcases.put(new Testcase(new String[]{},""), "FIRST_TESTCASE");
    }

    @Override
    public void writeOut() {
        if (path == null) return;

        List<Object> data = new ArrayList<>();
        data.add(testcases);
        try {
            CrypticWriter.writeEncryptedData(data, path);
        } catch (Exception e) {
            System.err.println("TestcaseFile.writeOut() error writing " + path);
            e.printStackTrace();
        }
    }

    /************ CALLBACKS *******************/
    @Override
    public void onTerminalExit(String[] inputs, String expected) {
        testcases.put(new Testcase(inputs, expected), UUID.randomUUID().toString());
    }
}
