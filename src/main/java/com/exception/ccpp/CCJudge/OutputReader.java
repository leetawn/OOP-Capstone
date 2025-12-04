package com.exception.ccpp.CCJudge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class OutputReader implements Callable<Void> {
    private final BufferedReader reader;
    private final StringBuilder transcript;

    public OutputReader(InputStream is, StringBuilder transcript) {
        this.reader = new BufferedReader(new InputStreamReader(is));
        this.transcript = transcript;
    }

    @Override
    public Void call() {
        try {
            int data;
            while ((data = reader.read()) != -1) {
                transcript.append((char) data);
            }
        } catch (IOException e) {}
        return null;
    }
}