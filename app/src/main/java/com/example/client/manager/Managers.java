package com.example.client.manager;

import android.content.Context;
import android.os.HandlerThread;

import com.google.android.gms.common.util.concurrent.HandlerExecutor;

/**
 * This is a super super class that provider a bunch of functionality.
 * Consumed request will create its response with the help of all these managers
 */
public class Managers {
    private PowerManager powerManager;
    private PreferenceManager preferenceManager;
    private GpsLocationManager gpsLocationManager;
    private WirelessSignalManager wirelessSignalManager;
    private Context context;
    private HandlerThread workerThread;
    private HandlerExecutor workExecutor;

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

    public HandlerThread getWorkerThread() {
        return workerThread;
    }

    public Managers(Context context, HandlerThread workerThread, HandlerExecutor workExecutor) {
        this.powerManager = new PowerManager(context);
        this.preferenceManager = new PreferenceManager(context);
        this.gpsLocationManager = new GpsLocationManager(context, workerThread.getLooper());
        this.wirelessSignalManager = new WirelessSignalManager(context);
        this.workerThread = workerThread;
        this.context = context;
        this.workExecutor = workExecutor;
    }

    public HandlerExecutor getWorkExecutor() {
        return workExecutor;
    }

    public WirelessSignalManager getWirelessSignalManager() {
        return wirelessSignalManager;
    }
}
