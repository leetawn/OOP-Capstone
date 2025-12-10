package com.exception.ccpp.CCJudge;
import com.exception.ccpp.CCJudge.Judge.JudgeVerdict;


// expected output can be null, when a testcase file is not used
public class SubmissionRecord {
    private int v;
    private String o;
    Testcase testcase;
    public SubmissionRecord(int verdict, String output, Testcase testcase) {
        v = verdict;
        o = output;
        this.testcase = testcase;
    }

    public SubmissionRecord setTestcase(Testcase testcase) {
        this.testcase = testcase;
        return this;
    }

    public SubmissionRecord setOutput(String o) {
        this.o = o;
        return this;
    }

    public SubmissionRecord setVerdict(int v) {
        this.v = v;
        return this;
    }

    public int verdict() {
        return v;
    }
    public String verdictName() {
        return JudgeVerdict.getName(v);
    }

    public String output() {
        return o;
    }
    public Testcase testcase() {
        return testcase;
    }
    public String expected_output() {
        if (testcase == null) return "";
        return testcase.getExpectedOutput();
    }
}