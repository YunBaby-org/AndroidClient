package com.example.client.runners;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.client.amqp.AmqpChannelFactory;
import com.example.client.manager.Managers;
import com.example.client.manager.PreferenceManager;
import com.google.android.gms.location.LocationRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
        while (true) {
            PreferenceManager pm = new PreferenceManager(context);

            /* Setup worker thread */
            this.workThread = new HandlerThread("Worker Thread");
            this.workThread.start();

            /* Init amqp factory */
            try {
                AmqpChannelFactory.getInstance().start(new AmqpChannelFactory.ConnectionSetting()
                        .setHostname(pm.getAmqpHostname())
                        .setUsername(pm.getTrackerID())
                        .setPassword(obtainAccessToken(pm.getAmqpHostname(), pm.getRefreshToken()))
                        .setPort(pm.getAmqpPort())
                        .setVhost("/")
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("ForegroundService", "Interrupted");
                return;
            }

            /* Manager */
            this.managers = new Managers(context, workThread);

            /* Setup AMQP consumer thread */
            this.amqpConsumerThread = new Thread(new AmqpConsumerRunner(managers, trackerId));
            this.amqpConsumerThread.start();

            /* Register listener to deal with preference changes */
            pm.registerListener(PreferenceManager.tagAutoReport, handlePreferenceChangeAutoReport());
            pm.registerListener(PreferenceManager.tagReportInterval, handlePreferenceChangeReportInterval());

            /* TODO: handle the change operation of tracker id, or just forbid it */
            /* Start receiving update from GPS & Wifi */
            managers.getGpsLocationManager().setupLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY, pm.getReportInterval(), null);
            managers.getWirelessSignalManager().setupWifiScanReceiver(null);

            while (true) {
                try {
                    Thread.sleep(1000);

                    if (!amqpConsumerThread.isAlive())
                        throw new Exception("Amqp Consumer Thread stopped");
                    if (!workThread.isAlive())
                        throw new Exception("Worker Thread stopped");
                } catch (Exception e) {
                    Log.i("ForegroundRunner", "Attempts to restart foreground runner");
                    try {
                        stop_runner();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Log.e("ForegroundRunner", "Failed to stop foreground runner");
                        /* Commit suicide because the app is about to break anyway */
                        // android.os.Process.killProcess(android.os.Process.myPid());
                    }
                    break;
                }
                Log.i("ForegroundRunner", "alive");
            }
        }
    }

    private void stop_runner() throws IOException {
        managers.fire_wall_these_managers();
        managers = null;
        workThread.quit();
        workThread = null;
        amqpConsumerThread.interrupt();
        amqpConsumerThread = null;
    }

    private void restart_connection(String host, String token) throws InterruptedException {
        AmqpChannelFactory.getInstance().restart_with_password(obtainAccessToken(host, token));
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

    private String obtainAccessToken(String hostname, String refresh_token) throws InterruptedException {
        /* 重複執行這個過程，說真的如果沒有取得可用的 Access token，你什麼都不能做 ... */
        while (true) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = null;
                request = new Request.Builder()
                        .url(String.format("http://%s/api/v1/mobile/trackers/tokens", hostname))
                        .patch(createRequestPayload(refresh_token))
                        .build();
                Response httpResponse = client.newCall(request).execute();
                String result = httpResponse.body().string();
                Log.i("ForegroundRunner", result);
                return new JSONObject(result).getJSONObject("payload").getString("access_token");
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Log.e("ForegroundRunner", "Failed to obtain refresh token");
            }
            Thread.sleep(3000);
        }
    }

    private RequestBody createRequestPayload(String refresh_token) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("refresh_token", refresh_token);
        return RequestBody.create(object.toString(), MediaType.parse("application/json; charset=utf-8"));
    }

}
