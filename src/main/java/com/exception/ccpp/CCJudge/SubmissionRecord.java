package com.exception.ccpp.CCJudge;
import com.exception.ccpp.CCJudge.Judge.JudgeVerdict;


// expected output can be null, when a testcase file is not used
public record SubmissionRecord (JudgeVerdict verdict, String output, String expected_output) {

    public SubmissionRecord (JudgeVerdict verdict, String output) {
        this(verdict, output, null);
    }
}