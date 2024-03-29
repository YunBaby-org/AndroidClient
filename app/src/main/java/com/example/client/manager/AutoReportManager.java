package com.example.client.manager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.client.amqp.AmqpUtility;
import com.example.client.requests.RequestScanGPS;
import com.example.client.requests.RequestScanWifiSignal;
import com.example.client.room.AppDatabase;
import com.example.client.room.entity.Event;
import com.rabbitmq.client.Channel;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AutoReportManager implements IHealthCheckable {

    private WirelessSignalManager wirelessSignalManager;
    private GpsLocationManager gpsLocationManager;
    private PreferenceManager preferenceManager;
    private AppDatabase appDatabase;
    private Channel amqpChannel;
    private Handler handler;
    private String trackerId;

    private final static int AUTO_REPORT_GPS_MESSAGE_TYPE = 0;
    private final static int AUTO_REPORT_WIFI_MESSAGE_TYPE_SCANNING = 1;
    private final static int AUTO_REPORT_WIFI_MESSAGE_TYPE_GET_RESULT = 2;

    /* TODO: Refactor this shitty code */
    public AutoReportManager(Channel amqpChannel,
                             Looper looper,
                             GpsLocationManager gpsLocationManager,
                             WirelessSignalManager wirelessSignalManager,
                             PreferenceManager preferenceManager,
                             AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
        this.wirelessSignalManager = wirelessSignalManager;
        this.gpsLocationManager = gpsLocationManager;
        this.preferenceManager = preferenceManager;
        this.trackerId = preferenceManager.getTrackerID();
        this.amqpChannel = amqpChannel;
        this.handler = new Handler(looper) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                try {
                    switch (msg.what) {
                        case AUTO_REPORT_GPS_MESSAGE_TYPE:
                            eventGPS(msg.arg1, msg.arg2);
                            break;
                        case AUTO_REPORT_WIFI_MESSAGE_TYPE_SCANNING:
                            eventWifiScan(msg.arg1, msg.arg2);
                            break;
                        case AUTO_REPORT_WIFI_MESSAGE_TYPE_GET_RESULT:
                            eventWifiResult(msg.arg1, msg.arg2);
                            break;
                    }
                } catch (Exception | WirelessSignalManager.WirelessScanFailure e) {
                    e.printStackTrace();
                    switch (msg.what) {
                        case AUTO_REPORT_GPS_MESSAGE_TYPE:
                            restartGps();
                            break;
                        case AUTO_REPORT_WIFI_MESSAGE_TYPE_SCANNING:
                        case AUTO_REPORT_WIFI_MESSAGE_TYPE_GET_RESULT:
                            restartWifi();
                            break;
                    }
                }
            }
        };

        restart();
    }

    private void eventGPS(int arg1, int arg2) throws Exception {
        if (preferenceManager.getAutoReportGps()) {
            /* Create response */
            JSONObject response = (new RequestScanGPS()).createResponse(gpsLocationManager);

            /* Sending message */
            AmqpUtility.sendTrackerResponse(amqpChannel, trackerId, response);
            Log.d("AutoReportManager", "Auto Report Current GPS Location");

            appDatabase.eventDao().insertAll(
                    Event.response(Event.ID.AUTO_REPORT_GPS, "自動回報 GPS 位置")
            );
        }
        /* Next pending operation */
        handler.sendEmptyMessageDelayed(AutoReportManager.AUTO_REPORT_GPS_MESSAGE_TYPE, getIntervalGps());
    }

    private void eventWifiScan(int arg1, int arg2) throws WirelessSignalManager.WirelessScanFailure {
        if (preferenceManager.getAutoReportWifi()) {
            wirelessSignalManager.invokeScanning();
            Log.d("AutoReportManager", "Initiate wireless scanning request");
            appDatabase.eventDao().insertAll(
                    Event.response(Event.ID.AUTO_REPORT_SCAN_WIFI, "掃描周遭 Wi-Fi 訊號")
            );
        }
        Message message = new Message();
        message.what = AUTO_REPORT_WIFI_MESSAGE_TYPE_GET_RESULT;
        message.arg1 = 0;  /* Current retry 0 times */
        message.arg2 = Math.max(5, getIntervalWifi() / 500 - 2); /* Max attempts */
        handler.sendMessageDelayed(message, 500);
    }

    // TODO: Potential bug, the wifi result never return during a smoking test.
    private void eventWifiResult(int arg1, int arg2) throws Exception {
        if (preferenceManager.getAutoReportWifi()) {
            if (arg1 >= arg2) {
                handler.sendEmptyMessageDelayed(AUTO_REPORT_WIFI_MESSAGE_TYPE_SCANNING, getIntervalWifi());
                Log.w("AutoReportManager", "Auto Report Wifi Signals Scan Timeout");
                return;
            }
            WirelessSignalManager.WirelessScanResult result = wirelessSignalManager.tryGetWirelessScanResult();
            if (result != null) {
                /* We got the result */
                JSONObject response = (new RequestScanWifiSignal()).createResponse(wirelessSignalManager, result);

                /* Send it */
                AmqpUtility.sendTrackerResponse(amqpChannel, trackerId, response);
                appDatabase.eventDao().insertAll(
                        Event.response(Event.ID.AUTO_REPORT_WIFI, "自動回報 Wi-Fi 訊號")
                );
                Log.d("AutoReportManager", "Auto Report Surrounding Wifi Signals");

                /* Keep the cycle going */
                handler.sendEmptyMessageDelayed(AUTO_REPORT_WIFI_MESSAGE_TYPE_SCANNING, getIntervalWifi());
            } else {
                /* We didn't retrieve the response this time, try it 0.5s later */
                Message message = new Message();
                message.what = AUTO_REPORT_WIFI_MESSAGE_TYPE_GET_RESULT;
                message.arg1 = arg1 + 1;
                message.arg2 = Math.max(5, getIntervalWifi() / 500 - 2); /* Max attempts */
                handler.sendMessageDelayed(message, 500);
            }
        } else {
            /* Keep the cycle going */
            handler.sendEmptyMessageDelayed(AUTO_REPORT_WIFI_MESSAGE_TYPE_SCANNING, getIntervalWifi());
        }
    }

    public void releaseResources() {
        this.stop();
        if (amqpChannel != null && amqpChannel.isOpen()) {
            try {
                try {
                    amqpChannel.close();
                } catch (TimeoutException e) {
                    amqpChannel.abort();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        amqpChannel = null;
    }

    public void restart() {
        restartGps();
        restartWifi();
    }

    public void restartGps() {
        handler.removeMessages(AutoReportManager.AUTO_REPORT_GPS_MESSAGE_TYPE);
        handler.sendEmptyMessageDelayed(AutoReportManager.AUTO_REPORT_GPS_MESSAGE_TYPE, getIntervalGps());
    }

    public void restartWifi() {
        handler.removeMessages(AutoReportManager.AUTO_REPORT_WIFI_MESSAGE_TYPE_SCANNING);
        handler.removeMessages(AutoReportManager.AUTO_REPORT_WIFI_MESSAGE_TYPE_GET_RESULT);
        handler.sendEmptyMessageDelayed(AutoReportManager.AUTO_REPORT_WIFI_MESSAGE_TYPE_SCANNING, getIntervalWifi());
    }

    public void stop() {
        handler.removeMessages(AutoReportManager.AUTO_REPORT_GPS_MESSAGE_TYPE);
        handler.removeMessages(AutoReportManager.AUTO_REPORT_WIFI_MESSAGE_TYPE_SCANNING);
        handler.removeMessages(AutoReportManager.AUTO_REPORT_WIFI_MESSAGE_TYPE_GET_RESULT);
    }

    private int getIntervalGps() {
        return this.preferenceManager.getReportIntervalGps() * 1000;
    }

    private int getIntervalWifi() {
        return this.preferenceManager.getReportIntervalWifi() * 1000;
    }

    @Override
    public boolean healthCheck() {
        if (!handler.getLooper().getThread().isAlive()) return false;
        if (amqpChannel == null) return false;
        if (!amqpChannel.isOpen()) return false;
        return true;
    }
}
