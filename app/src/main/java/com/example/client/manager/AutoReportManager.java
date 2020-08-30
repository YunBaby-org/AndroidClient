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

public class AutoReportManager {

    private PreferenceManager pm;
    private GpsLocationManager gpsLocationManager;
    private AmqpHandler amqpHandler;
    private Handler handler;
    private String trackerId;

    /* TODO: Refactor this shitty code */
    public AutoReportManager(Context context, Looper looper, GpsLocationManager gpsLocationManager) {
        this.pm = new PreferenceManager(context);
        this.trackerId = pm.getTrackerID();
        this.handler = new Handler(looper) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int reportInterval = pm.getReportInterval();
                boolean isAutoReportEnabled = pm.getAutoReport();

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

                this.sendEmptyMessageDelayed(0, reportInterval * 1000);
            }
        };

        /* Send message after interval immediately */
        handler.sendEmptyMessageDelayed(0, pm.getReportInterval() * 1000);
    }

    private void ensureAmqpHandler() {
        if (amqpHandler == null) {
            try {
                amqpHandler = new AmqpHandler(trackerId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
