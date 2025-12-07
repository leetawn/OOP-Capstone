package com.exception.ccpp.Gang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SlaveManager {

    // upto 5 code runner running in parallel
    public final static ExecutorService slaveWorkers =
            Executors.newFixedThreadPool(16);
}
