package CCJudge;

public enum JudgeVerdict {
    AC, // Accepted
    WA, // Wrong Answer
    CE, // Compile Error
    RE, // Runtime Error
    TLE, // Time Limit Exceeded
    MLE, // Memory Limit Exceeded

    UE, // Unknown Error. We the devs, idk and idc what the fuck you are facing rn
    ESF, // Execution System Failure, Not the Users Fault!
    JSF, // Judge System Failure
    NONE // USED FOR CREATION ONLY
}
