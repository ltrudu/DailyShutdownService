package com.zebra.shutdownservice;

public class Constants {
    public static final String TAG  ="ForegroundService";

    // Shared preference keys
    public static final String SHARED_PREFERENCES_NAME = "ForegroundService";
    public static final String SHARED_PREFERENCES_START_SERVICE_ON_BOOT = "startonboot";
    public static final String SHARED_PREFERENCES_START_SERVICE_ON_CHARGING = "startoncharging";
    public static final String SHARED_PREFERENCES_SHUTDOWN_HOURS = "sdhours";
    public static final String SHARED_PREFERENCES_SHUTDOWN_MINUTES = "sdminutes";
    public static final String SHARED_PREFERENCES_SHUTDOWN_SECONDS = "sdseconds";
    public static final String SHARED_PREFERENCES_SHUTDOWN_DAYS = "shutdowndays";

    public static final String EXTRA_CONFIGURATION_START_ON_BOOT = "startonboot";
    public static final String EXTRA_CONFIGURATION_START_ON_CHARGING = "startoncharging";
    public static final String EXTRA_CONFIGURATION_HOURS = "hours";
    public static final String EXTRA_CONFIGURATION_MINUTES = "minutes";
    public static final String EXTRA_CONFIGURATION_SECONDS = "seconds";

    public static final String EXTRA_CONFIGURATION_DAYS_OF_WEEK = "days";


}
