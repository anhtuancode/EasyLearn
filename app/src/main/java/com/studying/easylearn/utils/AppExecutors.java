package com.studying.easylearn.utils;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static AppExecutors instance;
    private final ExecutorService diskIO;
    private final Handler mainThread;

    private AppExecutors() {
        this.diskIO = Executors.newSingleThreadExecutor();
        this.mainThread = new Handler(Looper.getMainLooper());
    }

    public static synchronized AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors();
        }
        return instance;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Handler mainThread() {
        return mainThread;
    }
}