package com.example.client.runners;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.client.amqp.AmqpAuthentication;
import com.example.client.amqp.AmqpChannelFactory;
import com.example.client.manager.PreferenceManager;
import com.example.client.services.ForegroundService;
import com.example.client.services.ServiceState;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONException;

import java.io.IOException;

import static com.example.client.services.ServiceEventLogger.Event;

/**
 * The consumer logic for foreground service
 */
@Deprecated
public class ForegroundRunner implements Runnable {

    private Context context;
    private Thread amqpConsumerThread;
    private HandlerThread workThread;
    private Handler workerHandler;

    private String trackerId;
    private ServiceState serviceState;

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
                        .setPassword(AmqpAuthentication.obtainAccessToken(pm))
                        .setPort(pm.getAmqpPort())
                        .setVhost("/")
                );
                ForegroundService.emitEvent(Event.Info("取得登入憑證"));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ForegroundRunner", "Failed to obtain access token: Invalid response");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) { /* Handle interruption correctly */
                    break;
                }
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ForegroundRunner", "Failed to obtain access token: Connection timeout or refused");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) { /* Handle interruption correctly */
                    break;
                }
                continue;
            } catch (AmqpAuthentication.BadRequestException e) {
                e.printStackTrace();
                Log.e("ForegroundRunner", "Failed to obtain access token: Request failed");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) { /* Handle interruption correctly */
                    break;
                }
                continue;
            }

            /* Manager */
            this.serviceState = new ServiceState(context);

            /* Setup AMQP consumer thread */
            this.amqpConsumerThread = new Thread(new AmqpConsumerRunner(serviceState, trackerId));
            this.amqpConsumerThread.start();

            /* Start receiving update from GPS & Wifi */
            serviceState.getGpsLocationManager().setupLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY, pm.getReportIntervalGps(), null);
            serviceState.getWirelessSignalManager().setupWifiScanReceiver(null);

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
        serviceState.release();
        serviceState = null;
        workThread.quit();
        workThread = null;
        amqpConsumerThread.interrupt();
        amqpConsumerThread = null;
    }

}
