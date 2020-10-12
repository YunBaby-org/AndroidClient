package com.example.client.services;

import android.content.Context;
import android.os.HandlerThread;

import com.example.client.amqp.AmqpAuthentication;
import com.example.client.manager.AmqpConnectionManager;
import com.example.client.manager.AutoReportManager;
import com.example.client.manager.GpsLocationManager;
import com.example.client.manager.PowerManager;
import com.example.client.manager.PreferenceManager;
import com.example.client.manager.RequestManager;
import com.example.client.manager.WirelessSignalManager;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * ServiceState contains all the state it require to run the foreground service.
 * This class is not thread safe, use these non-readonly operation on your own risk
 */
public class ServiceState {
    private Context context;

    private AmqpConnectionManager amqpConnectionManager;

    private HandlerThread workerThread;
    private HandlerThread workerThread2;

    private PowerManager powerManager;
    private PreferenceManager preferenceManager;

    private GpsLocationManager gpsLocationManager;
    private WirelessSignalManager wirelessSignalManager;
    private AutoReportManager autoReportManager;

    private RequestManager requestManager;

    public ServiceState(Context context) {
        this.context = context;
    }

    public void initialize() throws IOException, AmqpAuthentication.BadRequestException, JSONException, TimeoutException {
        this.workerThread = new HandlerThread("Worker Thread");
        this.workerThread.start();
        this.workerThread2 = new HandlerThread("Worker Thread2");
        this.workerThread2.start();
        this.powerManager = new PowerManager(context);
        this.preferenceManager = new PreferenceManager(context);
        this.wirelessSignalManager = new WirelessSignalManager(context);
        this.wirelessSignalManager.setupWifiScanReceiver(null);
        this.gpsLocationManager = new GpsLocationManager(context, workerThread2.getLooper());
        this.gpsLocationManager.setupLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY, preferenceManager.getReportIntervalGps(), null);

        this.amqpConnectionManager = new AmqpConnectionManager();
        this.amqpConnectionManager.applySetting(new AmqpConnectionManager.ConnectionSetting()
                .setHostname(preferenceManager.getAmqpHostname())
                .setUsername(preferenceManager.getTrackerID())
                .setPassword(AmqpAuthentication.obtainAccessToken(preferenceManager))
                .setPort(preferenceManager.getAmqpPort())
                .setVhost("/")
        );
        this.amqpConnectionManager.start();
        this.autoReportManager = new AutoReportManager(
                amqpConnectionManager.getConnection().createChannel(),
                workerThread.getLooper(),
                gpsLocationManager,
                wirelessSignalManager,
                preferenceManager);
        this.requestManager = new RequestManager(
                this.preferenceManager.getTrackerID(),
                amqpConnectionManager.getConnection().createChannel(),
                this
        );
    }

    public boolean healthCheck() {
        if (context == null) return false;
        if (!amqpConnectionManager.healthCheck()) return false;
        if (!workerThread.isAlive()) return false;
        if (!gpsLocationManager.healthCheck()) return false;
        if (!autoReportManager.healthCheck()) return false;
        if (!requestManager.healthCheck()) return false;
        return true;
    }

    public void release() {
        /* Stop GPS/Wifi relate shit */
        autoReportManager.releaseResources();
        gpsLocationManager.stop();
        wirelessSignalManager.stop();

        /* Close Amqp Connection */
        requestManager.stop();
        amqpConnectionManager.stop();

        /* Interrupt worker thread */
        workerThread.interrupt();

        /* These classes are stateless */
        preferenceManager = null;
        powerManager = null;
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
