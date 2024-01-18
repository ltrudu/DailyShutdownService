package com.zebra.shutdownservice;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import android.util.Log;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class ForegroundService extends Service {
    private static final int SERVICE_ID = 1;

    private NotificationManager mNotificationManager;
    private Notification mNotification;

    protected static int sdHours, sdMinutes, sdSeconds;
    protected static boolean[] sdShutdowndays;


    public ForegroundService() {
    }

    public IBinder onBind(Intent paramIntent)
    {
        return null;
    }

    public void onCreate()
    {
        logD("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logD("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        startService();
        return Service.START_STICKY;
    }

    public void onDestroy()
    {
        logD("onDestroy");
        stopService();
    }

    protected static void getSettings(Context context)
    {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        sdHours = sharedpreferences.getInt(Constants.SHARED_PREFERENCES_SHUTDOWN_HOURS, 18);
        sdMinutes = sharedpreferences.getInt(Constants.SHARED_PREFERENCES_SHUTDOWN_MINUTES, 0);
        sdSeconds = sharedpreferences.getInt(Constants.SHARED_PREFERENCES_SHUTDOWN_SECONDS, 0);
        String shutdown_days_string = sharedpreferences.getString(Constants.SHARED_PREFERENCES_SHUTDOWN_DAYS,"0;0;0;0;0;0;0");
        sdShutdowndays = PreferencesHelper.stringToBooleanArray(shutdown_days_string,';');
    }

    @SuppressLint({"Wakelock"})
    private void startService()
    {
        logD("startService");
        try
        {
            getSettings(this);

            if(mNotificationManager == null)
                mNotificationManager = ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));

            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = null;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        mainActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }
            else {
                pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        mainActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            // Create the Foreground Service
            String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(mNotificationManager) : "";


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
            mNotification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.shutdown_service_notification_title))
                    .setContentText(getString(R.string.shutdown_service_notification_text))
                    .setTicker(getString(R.string.shutdown_service_notification_tickle))
                    .setPriority(PRIORITY_MIN)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .build();

            TaskStackBuilder localTaskStackBuilder = TaskStackBuilder.create(this);
            localTaskStackBuilder.addParentStack(MainActivity.class);
            localTaskStackBuilder.addNextIntent(mainActivityIntent);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                notificationBuilder.setContentIntent(localTaskStackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE));
            }
            else {
                notificationBuilder.setContentIntent(localTaskStackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT));
            }

            // Start foreground service
            startForeground(SERVICE_ID, mNotification);

            // TODO: Add your service code here
            DailyWorker.intializeWorker(getApplicationContext(), ShutdownWorker.class, sdHours,sdMinutes,sdSeconds, sdShutdowndays);

            logD("startService:Service started without error.");
        }
        catch(Exception e)
        {
            logD("startService:Error while starting service.");
            e.printStackTrace();
        }


    }

    protected static void restartWorker(Context context)
    {
        DailyWorker.removeWorker(context.getApplicationContext());
        DailyWorker.intializeWorker(context.getApplicationContext(), ShutdownWorker.class, sdHours,sdMinutes,sdSeconds, sdShutdowndays);
    }

    private void stopService()
    {
        try
        {
            logD("stopService.");

            // TODO: Release your stuffs here
            ShutdownWorker.removeWorker(getApplicationContext());
            if(mNotificationManager != null)
            {
                mNotificationManager.cancelAll();
                mNotificationManager = null;
            }


            stopForeground(true);
            logD("stopService:Service stopped without error.");
        }
        catch(Exception e)
        {
            logD("Error while stopping service.");
            e.printStackTrace();

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        NotificationChannel channel = new NotificationChannel(getString(R.string.shutdownservice_channel_id), getString(R.string.shutdownservice_channel_name), NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return getString(R.string.shutdownservice_channel_id);
    }

    private void logD(String message)
    {
        Log.d(Constants.TAG, message);
    }

    public static void startService(Context context)
    {
        Intent myIntent = new Intent(context.getApplicationContext(), ForegroundService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // Use start foreground service to prevent the runtime error:
            // "not allowed to start service intent app is in background"
            // to happen when running on OS >= Oreo
            context.startForegroundService(myIntent);
        }
        else
        {
            context.startService(myIntent);
        }
    }

    public static void stopService(Context context)
    {
        Intent myIntent = new Intent(context, ForegroundService.class);
        context.stopService(myIntent);
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
