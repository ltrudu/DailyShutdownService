package com.zebra.shutdownservice;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public abstract class DailyWorker extends Worker {
    private static int hour, minute, seconds;
    private static String workerTag = "DailyWorker";

    public DailyWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    protected abstract Result executeTask();

    public static Operation removeWorker(Context context)
    {
        return WorkManager.getInstance(context).cancelAllWorkByTag(workerTag);
    }

    public static Operation intializeWorker(Context context, Class worker, int hours, int minutes, int seconds)
    {
        DailyWorker.hour = hours;
        DailyWorker.minute = minutes;
        DailyWorker.seconds = seconds;
        DailyWorker.workerTag = workerTag;

        final Calendar currentDate = Calendar.getInstance();
        final Calendar dueDate = Calendar.getInstance();
        // Set Execution around 05:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, hour);
        dueDate.set(Calendar.MINUTE, minute);
        dueDate.set(Calendar.SECOND, seconds);
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }
        final long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        Constraints constraints = new Constraints(Constraints.NONE);
        final OneTimeWorkRequest dailyWorkRequest = new OneTimeWorkRequest.Builder(worker)
                .setConstraints(constraints)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(workerTag)
                .build();
        return WorkManager.getInstance(context).enqueue(dailyWorkRequest);
    }

    @NonNull
    @Override
    public Result doWork() {
        // We prepare the next event before executing the real task
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        // Set Execution
        dueDate.set(Calendar.HOUR_OF_DAY, hour);
        dueDate.set(Calendar.MINUTE, minute);
        dueDate.set(Calendar.SECOND, seconds);
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }
        Constraints constraints = new Constraints(Constraints.NONE);
        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        OneTimeWorkRequest dailyWorkRequest = new OneTimeWorkRequest.Builder(DailyWorker.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag(workerTag)
                .build();
        WorkManager.getInstance(getApplicationContext())
                .enqueue(dailyWorkRequest);
        // finally we execute the task
        return executeTask();
    }
}
