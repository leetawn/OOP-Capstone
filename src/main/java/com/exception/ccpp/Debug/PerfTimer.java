package com.exception.ccpp.Debug;

public class PerfTimer {
    private final String name;
    private long start;
    private double durationMs = 0;

    public PerfTimer(String name) {
        this.name = name;
    }
    public PerfTimer start () {
        start = System.nanoTime();  // start time
        return this;
    }

    public PerfTimer stop() {
        long end = System.nanoTime();    // end time
        long durationNs = end - start;   // duration in nanoseconds
        double durationMs = durationNs / 1_000_000.0; // convert to milliseconds
        System.err.printf("[%s] Done in %.2f ms\n", name, durationMs);
        return this;
    }

    public double getDurationMs() {
        return durationMs;
    }
}
