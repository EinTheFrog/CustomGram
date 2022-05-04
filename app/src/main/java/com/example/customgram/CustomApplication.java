package com.example.customgram;

import android.app.Application;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CustomApplication extends Application {
    public ExecutorService executor = Executors.newFixedThreadPool(1);
}
