package com.example.client.runners;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.example.client.manager.Managers;
import com.google.android.gms.location.LocationRequest;

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

        /* Start receiving update from GPS & Wifi */
        managers.getGpsLocationManager().setupLocationRequest(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, 30, null);
        managers.getWirelessSignalManager().setupWifiScanReceiver(null);
    }

}
