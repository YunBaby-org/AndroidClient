package com.example.client.services;

import android.content.Context;
import android.util.Log;

import com.example.client.amqp.AmqpAuthentication;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ServiceRunner implements Runnable {

    private Context context;
    private ServiceState serviceState;

    public ServiceRunner(Context context) {
        this.context = context;
    }

    @Override
    public void run() {

        boolean keepServiceThreadRunning = true;
        serviceState = new ServiceState(context);

        while (keepServiceThreadRunning) {

            ForegroundService.emitEvent(ServiceEventLogger.Event.Info("啟動服務"));
            if (!initializeServiceContext()) {
                ForegroundService.emitEvent(ServiceEventLogger.Event.Error("啟動失敗 :("));
                serviceState.getAppDatabase().eventDao().insertAll(
                        Event.warning(R.string.event_description_enable_service_failed, null)
                );
                if (sleep(5000)) break;
                continue;
            }

            /* Monitor service state */
            while (true) {
                /* Sleep for a while */
                if (sleep(5000)) {
                    keepServiceThreadRunning = false;
                    break;
                }

                /* Test if the state of foreground service is good */
                if (!serviceState.healthCheck()) {
                    /* Oh no it broken, let restart it */
                    ForegroundService.emitEvent(ServiceEventLogger.Event.Error("平安符狀態異常, 重新啟動"));
                    break;
                }
            }

            /* Delay for a while before restart service */
            if (keepServiceThreadRunning && sleep(5000)) break;

            releaseServiceContext();
        }
    }

    private boolean sleep(int ms) {
        try {
            Thread.sleep(ms);
            return false;
        } catch (InterruptedException e) {
            Log.e("ServiceRunner", "Interrupted");
            return true;
        }
    }

    private boolean initializeServiceContext() {
        try {
            serviceState.initialize();
            return true;
        } catch (IOException | AmqpAuthentication.BadRequestException | JSONException | TimeoutException e) {
            /* TODO: handle exception individually */
            e.printStackTrace();
            Log.e("ServiceRunner", "Failed to initialize service state");
            return false;
        }
    }

    private boolean releaseServiceContext() {
        /* Release resources */
        serviceState.release();
        return true;
    }

}
