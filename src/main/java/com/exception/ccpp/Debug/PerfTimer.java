package com.exception.ccpp.Debug;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class PerfTimer {
    private final String name;
    private long start_elapsed;
    private double durationMs = 0;
    private final DoubleAdder accumulatedMs = new DoubleAdder();
    AtomicInteger lap_id = new AtomicInteger();
    Map<Integer, Long> times = new ConcurrentHashMap<>();

    public PerfTimer(String name) {
        this.name = name;
    }
    public PerfTimer start () {
        start_elapsed = System.nanoTime();  // start time
        return this;
    }
    public int startLap()
    {
        int id = lap_id.getAndIncrement();
        long time = System.nanoTime();
        times.put(id, time);
        return id;
    }

    public void endLap(int id)
    {
        long end = System.nanoTime();
        if (times.containsKey(id)) {
            long start = times.get(id);
            long durationNs = end - start;
            double durationMs = durationNs /  1_000_000.0;
            accumulatedMs.add(durationMs);
            System.err.printf("[%s_%d] Lapped in %.2f ms\n", name, id, durationMs);
            times.remove(id);
        }
    }

    public void showAccumulated() {
        System.err.printf("[%s] Accumulated %.2f ms\n", name, accumulatedMs.sum());
    }

    public void stop() {
        long end = System.nanoTime();    // end time
        long durationNs = end - start_elapsed;   // duration in nanoseconds
        double durationMs = durationNs / 1_000_000.0; // convert to milliseconds
        System.err.printf("[%s] Done in %.2f ms\n", name, durationMs);
        showAccumulated();
    }
}
