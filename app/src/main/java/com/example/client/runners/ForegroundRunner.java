package com.example.client.runners;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.client.amqp.AmqpChannelFactory;
import com.example.client.manager.PreferenceManager;
import com.example.client.services.ForegroundService;
import com.example.client.services.ServiceContext;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.client.services.ServiceEventLogger.Event;

/**
 * The consumer logic for foreground service
 */
public class ForegroundRunner implements Runnable {

    private Context context;
    private Thread amqpConsumerThread;
    private HandlerThread workThread;
    private Handler workerHandler;

    private String trackerId;
    private ServiceContext serviceContext;

    public ForegroundRunner(Context context, String trackerId) {
        this.context = context;
        this.trackerId = trackerId;
    }

    @Override
    public void run() {
        boolean stopForegroundRunner = false;
        while (!stopForegroundRunner) {
            ForegroundService.emitEvent(Event.Info("啟動服務"));
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
                ForegroundService.emitEvent(Event.Info("取得登入憑證"));
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("ForegroundService", "Interrupted");
                return;
            }

            /* Manager */
            this.serviceContext = new ServiceContext(context, workThread);

            /* Setup AMQP consumer thread */
            this.amqpConsumerThread = new Thread(new AmqpConsumerRunner(serviceContext, trackerId));
            this.amqpConsumerThread.start();

            /* Start receiving update from GPS & Wifi */
            serviceContext.getGpsLocationManager().setupLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY, pm.getReportIntervalGps(), null);
            serviceContext.getWirelessSignalManager().setupWifiScanReceiver(null);

            ForegroundService.emitEvent(Event.Info("準備完成"));
            while (true) {
                try {
                    Thread.sleep(1000);

                    if (!amqpConsumerThread.isAlive()) {
                        ForegroundService.emitEvent(Event.Error("AMQP 連線異常，嘗試回復連線..."));
                        throw new Exception("Amqp Consumer Thread stopped");
                    }
                    if (!workThread.isAlive()) {
                        ForegroundService.emitEvent(Event.Error("Worker 異常，嘗試回復狀態..."));
                        throw new Exception("Worker Thread stopped");
                    }
                } catch (Exception e) {
                    ForegroundService.emitEvent(Event.Error("回收資源"));
                    Log.i("ForegroundRunner", "Attempts to restart foreground runner");
                    try {
                        stop_runner();
                        stopForegroundRunner = true;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Log.e("ForegroundRunner", "Failed to stop foreground runner");
                        /* Commit suicide because the app is about to break anyway */
                        // android.os.Process.killProcess(android.os.Process.myPid());
                    }
                    ForegroundService.emitEvent(Event.Error("嘗試重啟服務"));
                    break;
                }
                Log.i("ForegroundRunner", "alive");
            }
        }
    }

    private void stop_runner() throws IOException {
        serviceContext.fire_wall_these_managers();
        serviceContext = null;
        workThread.quit();
        workThread = null;
        amqpConsumerThread.interrupt();
        amqpConsumerThread = null;
    }

    private void restart_connection(String host, String token) throws InterruptedException {
        AmqpChannelFactory.getInstance().restart_with_password(obtainAccessToken(host, token));
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
                ForegroundService.emitEvent(Event.Error("無法與伺服器連線"));
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
