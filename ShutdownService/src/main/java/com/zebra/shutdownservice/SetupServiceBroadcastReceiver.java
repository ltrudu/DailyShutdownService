package com.zebra.shutdownservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetupServiceBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive");
        String sStartOnBoot = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_START_ON_BOOT, null);
        if(sStartOnBoot != null)
        {
            Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:Start on boot extra found with value:" + sStartOnBoot);
            boolean bStartOnBoot = sStartOnBoot.equalsIgnoreCase("true") || sStartOnBoot.equalsIgnoreCase("1");
            setSharedPreference(context, Constants.SHARED_PREFERENCES_START_SERVICE_ON_BOOT, bStartOnBoot);
            // Update GUI if necessary
            MainActivity.updateGUISwitchesIfNecessary();
        }
        else
        {
            Log.v(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:No start on boot extra found.");
        }

        String sStartOnCharging = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_START_ON_CHARGING, null);
        if(sStartOnCharging != null)
        {
            Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:Start on charging extra found with value:" + sStartOnCharging);
            boolean bStartOnCharging = sStartOnCharging.equalsIgnoreCase("true") || sStartOnBoot.equalsIgnoreCase("1");
            setSharedPreference(context, Constants.SHARED_PREFERENCES_START_SERVICE_ON_CHARGING, bStartOnCharging);
            // Launch service if necessary
            if(bStartOnCharging)
            {
                if(!PowerEventsWatcherService.isRunning(context))
                    PowerEventsWatcherService.startService(context);

                // Let's check if we are already connected on power to launch ForegroundService if necessary
                BatteryManager myBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                if(myBatteryManager.isCharging() && !ForegroundService.isRunning(context))
                    ForegroundService.startService(context);
            }
            // Update GUI if necessary
            MainActivity.updateGUISwitchesIfNecessary();
        }
        else
        {
            Log.v(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:No start on charging extra found.");
        }

        Integer iHours = intent.getExtras().getInt(Constants.EXTRA_CONFIGURATION_HOURS, -1);
        Integer iMinutes = intent.getExtras().getInt(Constants.EXTRA_CONFIGURATION_MINUTES, 0);
        Integer iSeconds = intent.getExtras().getInt(Constants.EXTRA_CONFIGURATION_SECONDS, 0);
        if(iHours != -1)
        {
            SharedPreferences sharedpreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(Constants.SHARED_PREFERENCES_SHUTDOWN_HOURS, iHours);
            editor.putInt(Constants.SHARED_PREFERENCES_SHUTDOWN_MINUTES, iMinutes);
            editor.putInt(Constants.SHARED_PREFERENCES_SHUTDOWN_SECONDS, iSeconds);
            editor.commit();
            MainActivity.updateTimeIfNecessary();
            if(ForegroundService.isRunning(context))
            {
                ForegroundService.getSettings(context);
                ForegroundService.restartWorker(context);
            }
        }
        else
        {
            Log.v(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:No shutdown hour extra found.");
        }

        String shutdown_days = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_DAYS_OF_WEEK, "false,false,false,false,false,false,false");
        if(shutdown_days != null)
        {
            if(verifyDaysofWeek(shutdown_days)) {

                SharedPreferences sharedpreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Constants.SHARED_PREFERENCES_SHUTDOWN_DAYS, shutdown_days);
                editor.commit();
                MainActivity.updateDaysIfNecessary();
                if (ForegroundService.isRunning(context)) {
                    ForegroundService.getSettings(context);
                    ForegroundService.restartWorker(context);
                }
            }
            else
            {
                Log.e(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:Shutdown days string malformed. Should be this format: \"false,false,false,false,false,false,false\"");
            }
        }
        else
        {
            Log.v(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:No shutdown days extra found.");
        }
    }

    private boolean verifyDaysofWeek(String daysOfWeek)
    {
        /*
        Pattern pattern = Pattern.compile ("^[true|false]+ (,[true|false]+){6}$");
        Matcher matcher = pattern.matcher(daysOfWeek);
        return matcher.matches();
        //*/  //Regular expression not working switching to old school mode
        String[] array = daysOfWeek.split(",");

        // Initialiser un booléen pour indiquer si la chaîne est valide ou non
        boolean valid = true;

        // Vérifier que le tableau a une longueur de 7
        if (array.length != 7) {
            valid = false;
        } else {
            // Parcourir le tableau et vérifier que chaque élément est égal à true ou false
            for (String element : array) {
                if (!element.equals("true") && !element.equals("false")) {
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    private void setSharedPreference(Context context, String key, boolean value)
    {
        Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::setSharedPreference: Key=" + key + " | Value=" + value);
        // Setup shared preferences for next reboot
        SharedPreferences sharedpreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
}
