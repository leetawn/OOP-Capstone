package com.exception.ccpp.Gang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class SlaveManager {

    // THE GOAT, without this UX would be a pain in the ass
    // thats why it has its own package and class
    public final static ExecutorService slaveWorkers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public final static ForkJoinPool romanArmy = ForkJoinPool.commonPool();
}
