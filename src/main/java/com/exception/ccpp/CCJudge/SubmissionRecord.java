package com.exception.ccpp.CCJudge;
import com.exception.ccpp.CCJudge.Judge.JudgeVerdict;

public record SubmissionRecord (JudgeVerdict verdict, String output) {}