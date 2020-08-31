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
    private WirelessSignalManager wirelessSignalManager;
    private AutoReportManager autoReportManager;
    private Context context;
    private HandlerThread workerThread;

    public Managers(Context context, HandlerThread workerThread) {
        this.context = context;
        this.workerThread = workerThread;
        this.powerManager = new PowerManager(context);
        this.preferenceManager = new PreferenceManager(context);
        this.gpsLocationManager = new GpsLocationManager(context, workerThread.getLooper());
        this.wirelessSignalManager = new WirelessSignalManager(context);
        this.autoReportManager = new AutoReportManager(context, workerThread.getLooper(), gpsLocationManager, preferenceManager);
    }

    public PowerManager getPowerManager() {
        return powerManager;
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public Context getContext() {
        return context;
    }

    public WirelessSignalManager getWirelessSignalManager() {
        return wirelessSignalManager;
    }

    public GpsLocationManager getGpsLocationManager() {
        return gpsLocationManager;
    }

    public HandlerThread getWorkerThread() {
        return workerThread;
    }

    public AutoReportManager getAutoReportManager() {
        return autoReportManager;
    }
}
