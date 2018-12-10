package com.test.gausslist;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiejq on 2018/7/4.
 */
public class ThreadManager {

    private volatile static ThreadManager ourInstance;
    private static ThreadPoolExecutor sFixThreadPoolExecutor;
    private static ThreadPoolExecutor sSingleThreadPoolExecutor;
    private static ThreadPoolExecutor sItemBgThreadPoolExecutor;


    public static ThreadManager getInstance() {
        if (ourInstance == null) {
            synchronized (ThreadManager.class) {
                if (ourInstance == null) {
                    ourInstance = new ThreadManager();
                }
            }
        }
        return ourInstance;
    }

    private ThreadManager() {
    }


    public ExecutorService getFixThreadPool() {
        if (sFixThreadPoolExecutor == null) {
            sFixThreadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        }
        return sFixThreadPoolExecutor;
    }

    public ExecutorService getSingleThreadPool() {
        if (sSingleThreadPoolExecutor == null) {
            sSingleThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        }
        return sSingleThreadPoolExecutor;
    }


    public ExecutorService getItemBgSingleThreadPool() {
        if (sItemBgThreadPoolExecutor == null) {
            sItemBgThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        }
        return sItemBgThreadPoolExecutor;
    }
}
