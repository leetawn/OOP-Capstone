package com.exception.ccpp.CCJudge;
import com.exception.ccpp.CCJudge.Judge.JudgeVerdict;


// expected output can be null, when a testcase file is not used
public class SubmissionRecord {
    private JudgeVerdict v;
    private String o, e;
    public SubmissionRecord(JudgeVerdict verdict, String output, String expected_output) {
        v = verdict;
        o = output;
        e = expected_output;
    }

    public SubmissionRecord setExpectedOutput(String e) {
        this.e = e;
        return this;
    }

    public SubmissionRecord setOutput(String o) {
        this.o = o;
        return this;
    }

    public SubmissionRecord setVerdict(JudgeVerdict v) {
        this.v = v;
        return this;
    }

    public JudgeVerdict verdict() {
        return v;
    }
    public String output() {
        return o;
    }
    public String expected_output() {
        return e;
    }
}