package com.exception.ccpp.Gang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SlaveManager {

    // THE GOAT, without this UX would be a pain in the ass
    // thats why it has its own package and class
    public final static ExecutorService slaveWorkers = Executors.newFixedThreadPool(16);
}
