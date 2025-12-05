package com.exception.ccpp.CCJudge;

import java.util.ArrayList;

public interface TerminalCallback {
    void onTerminalExit(String[] inputs, String expected);
}
