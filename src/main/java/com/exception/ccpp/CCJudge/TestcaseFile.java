package com.exception.ccpp.CCJudge;

import com.exception.ccpp.FileManagement.CCFile;
import com.exception.ccpp.FileManagement.CrypticWriter;
import com.exception.ccpp.FileManagement.FileManager;
import com.exception.ccpp.GUI.UpdateGUICallback;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TestcaseFile extends CCFile implements TerminalCallback{
    Map<Testcase, String> testcases = new LinkedHashMap<>();
    TestcasesPanel testcasesPanel = null;
    private TestcaseFile(Path path) {
        super(path);
        read();
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
        if (testcasesPanel != null) {testcasesPanel.removeTestcaseCallback(tc); }
    }
    public void addTestcase(FileManager fm, UpdateGUICallback gui_cb) {
        TerminalApp.getInstance().setAll(fm,this, gui_cb).start();
    }

    /************ I/O *******************/

    public static TestcaseFile open(String path) {
        return open(Paths.get(path));
    };
    public static TestcaseFile open(Path path) {
        CCFile f = getCached(path);
        if (f == null) return new TestcaseFile(path);
        assert f instanceof TestcaseFile;
        return (TestcaseFile) f;
    }



    @Override
    protected void read() {
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
    public void write() {
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
        Testcase nTC = new Testcase(inputs, expected);
        testcases.put(nTC, UUID.randomUUID().toString());
        if (testcasesPanel != null) testcasesPanel.addTestcaseCallback(nTC);
    }
}
