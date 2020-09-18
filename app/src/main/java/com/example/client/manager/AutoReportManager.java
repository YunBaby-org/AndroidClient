package com.example.client.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.client.amqp.AmqpHandler;
import com.example.client.amqp.AmqpUtility;
import com.example.client.requests.RequestScanGPS;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AutoReportManager {

    private PreferenceManager preferenceManager;
    private AmqpHandler amqpHandler;
    private Handler handler;
    private String trackerId;
    private final static int AUTO_REPORT_MESSAGE_TAG = 0;

    /* TODO: Refactor this shitty code */
    public AutoReportManager(Context context, Looper looper, GpsLocationManager gpsLocationManager, PreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
        this.trackerId = this.preferenceManager.getTrackerID();
        this.handler = new Handler(looper) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int reportInterval = AutoReportManager.this.preferenceManager.getReportInterval();
                boolean isAutoReportEnabled = AutoReportManager.this.preferenceManager.getAutoReport();

                ensureAmqpHandler();

                if (isAutoReportEnabled) {
                    /* Get GPS Position */
                    JSONObject response = (new RequestScanGPS()).createResponse(gpsLocationManager);
                    try {
                        amqpHandler.publishMessage("tracker-event", AmqpUtility.getResponseRoutingKey(trackerId, response), response);
                        Log.d("AutoReportManager", "Auto report current GPS location");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("AutoReportManager", "Failed to send auto report response");
                    }
                }

                this.sendEmptyMessageDelayed(AutoReportManager.AUTO_REPORT_MESSAGE_TAG, getInterval());
            }
        };

        /* Send message after interval immediately */
        handler.sendEmptyMessageDelayed(AutoReportManager.AUTO_REPORT_MESSAGE_TAG, getInterval());
    }

    public void stop() {
        handler.removeMessages(AutoReportManager.AUTO_REPORT_MESSAGE_TAG);
    }

    public void remove_every_thing() {
        this.stop();
        try {
            if (amqpHandler != null && amqpHandler.getAmqpChannel().isOpen())
                amqpHandler.stop();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void restart() {
        handler.removeMessages(AutoReportManager.AUTO_REPORT_MESSAGE_TAG);
        handler.sendEmptyMessageDelayed(AutoReportManager.AUTO_REPORT_MESSAGE_TAG, getInterval());
    }

    private int getInterval() {
        return this.preferenceManager.getReportInterval() * 1000;
    }

    private void ensureAmqpHandler() {
        if (amqpHandler == null || !amqpHandler.getAmqpChannel().isOpen()) {
            try {
                amqpHandler = new AmqpHandler(trackerId);
                Log.d("AutoReportManager", "Create new instance of Amqp Handler");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
