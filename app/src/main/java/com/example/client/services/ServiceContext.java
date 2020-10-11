package com.example.client.services;

import android.content.Context;
import android.os.HandlerThread;
import android.util.Log;

import com.example.client.manager.AutoReportManager;
import com.example.client.manager.GpsLocationManager;
import com.example.client.manager.PowerManager;
import com.example.client.manager.PreferenceManager;
import com.example.client.manager.WirelessSignalManager;

/**
 * This is a super super class that provider a bunch of functionality.
 * Consumed request will create its response with the help of all these managers
 */
public class ServiceContext {
    private PowerManager powerManager;
    private PreferenceManager preferenceManager;
    private GpsLocationManager gpsLocationManager;
    private WirelessSignalManager wirelessSignalManager;
    private AutoReportManager autoReportManager;
    private Context context;
    private HandlerThread workerThread;

    public ServiceContext(Context context, HandlerThread workerThread) {
        this.context = context;
        this.workerThread = workerThread;
        this.powerManager = new PowerManager(context);
        this.preferenceManager = new PreferenceManager(context);
        this.gpsLocationManager = new GpsLocationManager(context, workerThread.getLooper());
        this.wirelessSignalManager = new WirelessSignalManager(context);
        this.autoReportManager = new AutoReportManager(workerThread.getLooper(), gpsLocationManager, wirelessSignalManager, preferenceManager);
    }

    public void fire_wall_these_managers() {
        Log.e("ServiceContext", "Wireless Signal Manager");
        wirelessSignalManager.stop();
        Log.e("ServiceContext", "GPS Location Manager");
        gpsLocationManager.stop();
        Log.e("ServiceContext", "Stop AutoReport Manager");
        autoReportManager.releaseResources();
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