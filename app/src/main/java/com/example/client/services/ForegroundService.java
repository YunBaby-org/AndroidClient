package com.example.client.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.client.MainActivity;
import com.example.client.R;
import com.example.client.manager.PreferenceManager;
import com.example.client.runners.ForegroundRunner;
import com.google.android.gms.common.util.concurrent.HandlerExecutor;

import static com.example.client.App.CHANNEL_ID;

public class ForegroundService extends Service {

    /* TODO: Start the service on boot */
    /* TODO: Restart the service on accident */

    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 1;
    private Thread consumerThread;

    /* WARNING: Current threading setup works for only one instance of consumer */
    /* If you want to scale the amount of consumer, consider making more thread for executor */
    private HandlerExecutor workExecutor;
    private Handler workerHandler;
    private HandlerThread workerThread;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ForegroundService", "Creating foreground service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ForegroundService", "onStartCommand");

        /* Create intent to handle the onClick action on foreground service notification */
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.foreground_service_tracking_title))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification);

        setupWorkerThread();
        setupConsumerThread();

        return START_STICKY;
    }

    private void setupWorkerThread() {
        if (workerThread == null) {
            workerThread = new HandlerThread("WorkerThread", Process.THREAD_PRIORITY_LESS_FAVORABLE);
            workerThread.start();
            workerHandler = new Handler(workerThread.getLooper());
            workExecutor = new HandlerExecutor(workerHandler.getLooper());
        }
    }

    private void setupConsumerThread() {
        if (consumerThread == null) {
            PreferenceManager manager = new PreferenceManager(this);
            consumerThread = new Thread(new ForegroundRunner(this, manager.getTrackerID(), workerThread, workExecutor));
            consumerThread.start();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

