package com.zebra.shutdownservice;

import android.content.Context;
import android.util.Log;

import com.zebra.emdkprofilemanagerhelper.IResultCallbacks;

import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ShutdownWorker extends DailyWorker {

    static String TAG = "ShutDownWorker";
    static String message = null;
    static String resultXML = null;
    static boolean succeeded = false;

    public ShutdownWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    protected Result executeTask() {
        CountDownLatch latch = new CountDownLatch(1);
        ShutdownCommand.shutdown(getApplicationContext(), new IResultCallbacks() {
            @Override
            public void onSuccess(String message, String resultXML) {
                // We will never reach this part of the code
                ShutdownWorker.message = message;
                succeeded = true;
                latch.countDown();
            }

            @Override
            public void onError(String message, String resultXML) {
                ShutdownWorker.message = message;
                ShutdownWorker.resultXML = resultXML;
                succeeded = false;
                latch.countDown();
            }

            @Override
            public void onDebugStatus(String message) {
                Log.v(TAG, message);
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            message = e.getMessage();
            return Result.failure();
        }
        return succeeded ? Result.success() : Result.failure();    }
}
