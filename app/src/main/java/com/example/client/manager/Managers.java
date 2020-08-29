package com.example.client.manager;

import android.content.Context;
import android.os.HandlerThread;

/**
 * This is a super super class that provider a bunch of functionality.
 * Consumed request will create its response with the help of all these managers
 */
public class Managers {
    private PowerManager powerManager;
    private PreferenceManager preferenceManager;
    private GpsLocationManager gpsLocationManager;
    private Context context;
    private HandlerThread workerThread;

    public PowerManager getPowerManager() {
        return powerManager;
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public Context getContext() {
        return context;
    }

    public GpsLocationManager getGpsLocationManager() {
        return gpsLocationManager;
    }

    public Managers(Context context, HandlerThread workerThread) {
        this.powerManager = new PowerManager(context);
        this.preferenceManager = new PreferenceManager(context);
        this.gpsLocationManager = new GpsLocationManager(context, workerThread.getLooper());
        this.workerThread = workerThread;
        this.context = context;
    }
}
