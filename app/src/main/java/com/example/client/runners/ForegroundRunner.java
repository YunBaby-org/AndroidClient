package com.example.client.runners;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.client.manager.Managers;
import com.example.client.manager.PreferenceManager;
import com.google.android.gms.location.LocationRequest;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * The consumer logic for foreground service
 */
public class ForegroundRunner implements Runnable {

    private Context context;
    private Thread amqpConsumerThread;
    private HandlerThread workThread;
    private Handler workerHandler;

    private String trackerId;
    private Managers managers;

    public ForegroundRunner(Context context, String trackerId) {
        this.context = context;
        this.trackerId = trackerId;
    }

    @Override
    public void run() {
        /* Setup worker thread */
        this.workThread = new HandlerThread("Worker Thread");
        this.workThread.start();

        /* Manager */
        this.managers = new Managers(context, workThread);

        /* Setup AMQP consumer thread */
        this.amqpConsumerThread = new Thread(new AmqpConsumerRunner(managers, trackerId));
        this.amqpConsumerThread.start();

        /* Register listener to deal with preference changes */
        /* TODO: handle the change operation of tracker id, or just forbid it */
        PreferenceManager pm = this.managers.getPreferenceManager();
        pm.registerListener(PreferenceManager.tagAutoReport, handlePreferenceChangeAutoReport());
        pm.registerListener(PreferenceManager.tagReportInterval, handlePreferenceChangeReportInterval());

        /* Start receiving update from GPS & Wifi */
        managers.getGpsLocationManager().setupLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY, pm.getReportInterval(), null);
        managers.getWirelessSignalManager().setupWifiScanReceiver(null);

        while (true) {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                Log.i("ForegroundRunner", "Attempts to stop");
                try {
                    managers.fire_wall_these_managers();
                } catch (IOException | TimeoutException ex) {
                    ex.printStackTrace();
                    Log.e("ForegroundRunner", "Cannot stop it owowo.");
                }
                workThread.quit();
                amqpConsumerThread.interrupt();
                workThread = null;
                amqpConsumerThread = null;
                managers = null;
                break;
            }
            Log.i("ForegroundRunner", "alive");
        }
    }

    @NotNull
    private PreferenceManager.OnPreferenceChangedListener handlePreferenceChangeReportInterval() {
        return (preferenceManager, preference_tag) -> {
            Log.d("ForegroundRunner", "Change report interval");
            int interval = preferenceManager.getReportInterval();
            managers.getGpsLocationManager().setupLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY, interval, null);
            this.managers.getAutoReportManager().restart();
        };
    }

    @NotNull
    private PreferenceManager.OnPreferenceChangedListener handlePreferenceChangeAutoReport() {
        return (preferenceManager, preference_tag) -> {
            Log.d("ForegroundRunner", "Change auto report");
            if (preferenceManager.getAutoReport())
                this.managers.getAutoReportManager().restart();
            else
                this.managers.getAutoReportManager().stop();
        };
    }

}
