package com.exception.ccpp.Gang;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class SlaveManager {

    // THE GOAT, without this UX would be a pain in the ass
    // thats why it has its own package and class
    public final static ExecutorService slaveWorkers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public final static ForkJoinPool romanArmy = ForkJoinPool.commonPool();

    public abstract class SlaveDispatcher
    {
        protected ConcurrentHashMap<String,Future<?>> runningThreads = new ConcurrentHashMap<>();

        abstract void submit(Runnable tasks);
        abstract <V> void submit(Callable<V> task);
        abstract <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;
    }

    public interface Slave {
    }
}

