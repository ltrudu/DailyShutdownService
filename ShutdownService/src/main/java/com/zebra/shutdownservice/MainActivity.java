package com.zebra.shutdownservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.zebra.emdkprofilemanagerhelper.IResultCallbacks;

import java.util.ArrayList;

// The service can be launched using the graphical user interface, intent actions or adb.
//
// If the option "Start on boot" is enabled, the service will be automatically launched when the boot is complete.
//
// Power events occur when the device is connected to a power source (AC/USB/Wireless).
// If the option "Start when charging / Stop when charging" is enabled, the power events will be monitored.
// The ForegroundService will be launched when the device is connected to a power source
//
//
// The service respond to two intent actions (both uses the category: android.intent.category.DEFAULT)
// - "com.zebra.shutdownservice.startservice" sent on the component "com.zebra.shutdownservice/com.zebra.shutdownservice.StartServiceBroadcastReceiver":
//   Start the service.
//   If the device get rebooted the service will start automatically once the reboot is completed.
// - "com.zebra.shutdownservice.stopservice" sent on the component "com.zebra.shutdownservice/com.zebra.shutdownservice.StopServiceBroadcastReceiver":
//   Stop the service.
//   If the device is rebooted, the service will not be started.
//
// The service can be started and stopped manually using the following adb commands:
//  - Start service:
//      adb shell am broadcast -a com.zebra.shutdownservice.startservice -n com.zebra.shutdownservice/com.zebra.shutdownservice.StartServiceBroadcastReceiver
//  - Stop service:
//      adb shell am broadcast -a com.zebra.shutdownservice.stopservice -n com.zebra.shutdownservice/com.zebra.shutdownservice.StopServiceBroadcastReceiver
//  - Setup service
//          The service can be configured using the following intent:
//          adb shell am broadcast -a com.zebra.shutdownservice.setupservice -n com.zebra.shutdownservice/com.zebra.shutdownservice.SetupServiceBroadcastReceiver --es startonboot "true" --es startoncharging "true"
//          The command must contain at least one of the extras:
//          - Configure autostart on boot:
//          --es startonboot "true"
//          - Configure autostart on power connection (AC/USB/Wireless)
//          --es startoncharging "true"
//          The extras value can be set to "true" or "1" to enable the option and "false" or "0" to disable the option.
//
//          --es days "true,false,true,true,false,false,false"
//          The extras value contains the days that should perform a shutdown. The booleans corresponds to these days
//                  "monday,tuesday,wednesday,thursday,friday,saturday,sunday"
//          --ei hours 18
//          The extras value is an integer that represent the desired hour of shutdown.
//          --ei minutes 30
//          The extras value is an integer that represent the desired minutes of shutdown.
//          --ei seconds 20
//          The extras value is an integer that represent the desired seconds of shutdown.
//
//          In this example we set a shudown to occurs at 18:30:20 every monday,wednesday and thursday.
// adb shell am broadcast -a com.zebra.shutdownservice.setupservice -n com.zebra.shutdownservice/com.zebra.shutdownservice.SetupServiceBroadcastReceiver --ei hours 18 --ei minutes 30 --ei seconds 20 --es days "true,false,true,true,false,false,false"
public class MainActivity extends AppCompatActivity {

    private Switch mStartStopServiceSwitch = null;
    private Switch mAutoStartServiceOnBootSwitch = null;
    private Switch mAutoStartServiceOnCraddleSwitch = null;

    private EditText etHours, etMinutes, etSeconds;

    private CheckBox cbAllCheckboxes = null;

    private ArrayList<CheckBox> cbCheckboxes = null;
    public static MainActivity mMainActivity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etHours = findViewById(R.id.etHours);
        etMinutes = findViewById(R.id.etMinutes);
        etSeconds = findViewById(R.id.etSeconds);
        initDaysCheckboxes();

        getSettings();

        ((Button)findViewById(R.id.btApply)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                if(etHours.getText().toString().isEmpty())
                    etHours.setText("18");
                if(etMinutes.getText().toString().isEmpty())
                    etMinutes.setText("0");
                if(etSeconds.getText().toString().isEmpty())
                    etSeconds.setText("0");
                int hours = Integer.valueOf(etHours.getText().toString());
                int minutes = Integer.valueOf(etMinutes.getText().toString());
                int seconds = Integer.valueOf(etSeconds.getText().toString());
                boolean[] shutdown_days_boolean_array = PreferencesHelper.getBooleanArrayFromCheckBoxes(cbCheckboxes);
                String shutdown_days_string = PreferencesHelper.booleanArrayToString(shutdown_days_boolean_array,',');
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Constants.SHARED_PREFERENCES_SHUTDOWN_HOURS, hours);
                editor.putInt(Constants.SHARED_PREFERENCES_SHUTDOWN_MINUTES, minutes);
                editor.putInt(Constants.SHARED_PREFERENCES_SHUTDOWN_SECONDS, seconds);
                editor.putString(Constants.SHARED_PREFERENCES_SHUTDOWN_DAYS, shutdown_days_string);
                editor.commit();
                if(ForegroundService.isRunning(MainActivity.this))
                {
                    ForegroundService.sdHours = hours;
                    ForegroundService.sdMinutes = minutes;
                    ForegroundService.sdSeconds = seconds;
                    ForegroundService.sdShutdowndays = shutdown_days_boolean_array;
                    ForegroundService.restartWorker(MainActivity.this);
                }
            }
        });

        ((Button)findViewById(R.id.btLicense)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, LicenceActivity.class);
                startActivity(myIntent);
            }
        });

        mStartStopServiceSwitch = (Switch)findViewById(R.id.startStopServiceSwitch);
        mStartStopServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    mStartStopServiceSwitch.setText(getString(R.string.serviceStarted));
                    if(!ForegroundService.isRunning(MainActivity.this))
                        ForegroundService.startService(MainActivity.this);
                }
                else
                {
                    mStartStopServiceSwitch.setText(getString(R.string.serviceStopped));
                    if(ForegroundService.isRunning(MainActivity.this))
                        ForegroundService.stopService(MainActivity.this);
                }
            }
        });

        mAutoStartServiceOnBootSwitch = (Switch)findViewById(R.id.startOnBootSwitch);
        mAutoStartServiceOnBootSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    mAutoStartServiceOnBootSwitch.setText(getString(R.string.startOnBoot));
                }
                else
                {
                    mAutoStartServiceOnBootSwitch.setText(getString(R.string.doNothingOnBoot));
                }
                SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_BOOT, isChecked);
                editor.commit();
            }
        });

        mAutoStartServiceOnCraddleSwitch = (Switch)findViewById(R.id.startOnCraddle);
        mAutoStartServiceOnCraddleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    mAutoStartServiceOnCraddleSwitch.setText(getString(R.string.startOnCharging));
                    // Launch the watcher service
                    if(!PowerEventsWatcherService.isRunning(MainActivity.this))
                        PowerEventsWatcherService.startService(MainActivity.this);
                    // Let's check if we are already connected on power to launch ForegroundService if necessary
                    BatteryManager myBatteryManager = (BatteryManager) MainActivity.this.getSystemService(Context.BATTERY_SERVICE);
                    if(myBatteryManager.isCharging() && !ForegroundService.isRunning(MainActivity.this))
                        ForegroundService.startService(MainActivity.this);
                }
                else
                {
                    mAutoStartServiceOnCraddleSwitch.setText(getString(R.string.doNothingOnCharging));
                    // Stop the watcher service
                    if(PowerEventsWatcherService.isRunning(MainActivity.this))
                        PowerEventsWatcherService.stopService(MainActivity.this);
                }
                SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_CHARGING, isChecked);
                editor.commit();
            }
        });
        initDaysCheckboxes();
        updateSwitches();
        launchPowerEventsWatcherServiceIfNecessary();
    }

    @Override
    protected void onResume() {
        mMainActivity = this;
        super.onResume();
        updateSwitches();
        launchPowerEventsWatcherServiceIfNecessary();
    }

    protected void getSettings()
    {
        SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int hours = sharedpreferences.getInt(Constants.SHARED_PREFERENCES_SHUTDOWN_HOURS, 18);
        int minutes = sharedpreferences.getInt(Constants.SHARED_PREFERENCES_SHUTDOWN_MINUTES, 0);
        int seconds = sharedpreferences.getInt(Constants.SHARED_PREFERENCES_SHUTDOWN_SECONDS, 0);
        etHours.setText(String.valueOf(hours));
        etMinutes.setText(String.valueOf(minutes));
        etSeconds.setText(String.valueOf(seconds));
        String shutdown_days_string = sharedpreferences.getString(Constants.SHARED_PREFERENCES_SHUTDOWN_DAYS,"false,false,false,false,false,false,false");
        boolean[] shutdown_days_boolean_array = PreferencesHelper.stringToBooleanArray(shutdown_days_string,',');
        PreferencesHelper.applyBooleanArrayToCheckBoxes(shutdown_days_boolean_array, cbCheckboxes);
    }

    private void initDaysCheckboxes()
    {
        cbCheckboxes = new ArrayList<>();
        CheckBox cbCheckbox = findViewById(R.id.cbMonday);
        cbCheckboxes.add(cbCheckbox);
        cbCheckbox = findViewById(R.id.cbTuesday);
        cbCheckboxes.add(cbCheckbox);
        cbCheckbox = findViewById(R.id.cbWednesday);
        cbCheckboxes.add(cbCheckbox);
        cbCheckbox = findViewById(R.id.cbThursday);
        cbCheckboxes.add(cbCheckbox);
        cbCheckbox = findViewById(R.id.cbFriday);
        cbCheckboxes.add(cbCheckbox);
        cbCheckbox = findViewById(R.id.cbSaturday);
        cbCheckboxes.add(cbCheckbox);
        cbCheckbox = findViewById(R.id.cbSunday);
        cbCheckboxes.add(cbCheckbox);
    }


    public void updateSwitches()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(ForegroundService.isRunning(MainActivity.this))
                {
                    setServiceStartedSwitchValues(true, getString(R.string.serviceStarted));
                }
                else
                {
                    setServiceStartedSwitchValues(false, getString(R.string.serviceStopped));
                }

                SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                boolean startServiceOnBoot = sharedpreferences.getBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_BOOT, false);
                setAutoStartServiceOnBootSwitch(startServiceOnBoot, startServiceOnBoot ? getString(R.string.startOnBoot) : getString(R.string.doNothingOnBoot));

                boolean startServiceOnCharging = sharedpreferences.getBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_CHARGING, false);
                setAutoStartServiceOnChargingSwitch(startServiceOnCharging, startServiceOnCharging ? getString(R.string.startOnCharging) : getString(R.string.doNothingOnCharging));
            }
        });

    }

    private void launchPowerEventsWatcherServiceIfNecessary()
    {
        // We need to launch the PowerEventsWatcher Service if necessary
        SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean startServiceOnCharging = sharedpreferences.getBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_CHARGING, false);
        if(startServiceOnCharging)
        {
            // Launch the service if it was not running
            if(!PowerEventsWatcherService.isRunning(this))
                PowerEventsWatcherService.startService(this);

            // Let's check if we are already connected on power to launch ForegroundService if necessary
            BatteryManager myBatteryManager = (BatteryManager) MainActivity.this.getSystemService(Context.BATTERY_SERVICE);
            if(myBatteryManager.isCharging() && !ForegroundService.isRunning(MainActivity.this))
                ForegroundService.startService(MainActivity.this);
        }
    }

    @Override
    protected void onPause() {
        mMainActivity = null;
        super.onPause();
    }

    private void setServiceStartedSwitchValues(final boolean checked, final String text)
    {
        mStartStopServiceSwitch.setChecked(checked);
        mStartStopServiceSwitch.setText(text);
    }

    private void setAutoStartServiceOnBootSwitch(final boolean checked, final String text)
    {
        mAutoStartServiceOnBootSwitch.setChecked(checked);
        mAutoStartServiceOnBootSwitch.setText(text);
    }

    private void setAutoStartServiceOnChargingSwitch(final boolean checked, final String text)
    {
        mAutoStartServiceOnCraddleSwitch.setChecked(checked);
        mAutoStartServiceOnCraddleSwitch.setText(text);
    }


    public static void updateGUISwitchesIfNecessary()
    {
        // Update GUI if necessary
        if(MainActivity.mMainActivity != null) // The application default activity has been opened
        {
            MainActivity.mMainActivity.updateSwitches();
        }
    }

    public static void updateTimeIfNecessary()
    {
        if(MainActivity.mMainActivity != null) // The application default activity has been opened
        {
            MainActivity.mMainActivity.getSettings();
        }
    }

    public static void updateDaysIfNecessary() {
        if(MainActivity.mMainActivity != null) // The application default activity has been opened
        {
            MainActivity.mMainActivity.getSettings();
        }
    }
}
