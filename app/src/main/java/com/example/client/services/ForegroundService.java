package com.example.client.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.client.MainActivity;
import com.example.client.R;
import com.example.client.manager.PreferenceManager;
import com.example.client.runners.ForegroundRunner;

import static com.example.client.App.CHANNEL_ID;

public class ForegroundService extends Service {

    /* TODO: Start the service on boot */
    /* TODO: Restart the service on accident */

    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 1;
    private Thread serviceThread;

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

        setupServiceThread();

        return START_STICKY;
    }

    private void setupServiceThread() {
        if (serviceThread == null) {
            PreferenceManager pm = new PreferenceManager(this);
            serviceThread = new Thread(new ForegroundRunner(this, pm.getTrackerID()));
            serviceThread.start();
        }
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i("ForegroundRunner", "Attempts to stop service thread.");
        this.serviceThread.interrupt();
        super.onDestroy();
    }

}

