package com.example.client.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.client.MainActivity;
import com.example.client.R;
import com.example.client.manager.PreferenceManager;

import static com.example.client.App.CHANNEL_ID;
import static com.example.client.services.ServiceEventLogger.Event;
import static com.example.client.services.ServiceEventLogger.IServiceEventListener;

public class ForegroundService extends Service {

    /* TODO: Start the service on boot */
    /* TODO: Restart the service on accident */

    public static void emitEvent(Event e) {
        getServiceBinder().emitEvent(e);
    }

    private static ServiceBinder getServiceBinder() {
        return ForegroundService.binder;
    }

    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 1;
    private Thread serviceThread;
    private static ServiceBinder binder = new ServiceBinder();

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

    @Override
    public boolean stopService(Intent name) {
        this.serviceThread.interrupt();
        return super.stopService(name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return ForegroundService.getServiceBinder();
    }

    @Override
    public void onDestroy() {
        Log.i("ForegroundRunner", "Attempts to stop service thread.");
        if (this.serviceThread != null)
            this.serviceThread.interrupt();
        super.onDestroy();
    }

    private void setupServiceThread() {
        if (serviceThread == null) {
            PreferenceManager pm = new PreferenceManager(this);
            serviceThread = new Thread(new ServiceRunner(this));
            serviceThread.start();
        }
    }

    public static class ServiceBinder extends Binder {
        private ServiceEventLogger logger = new ServiceEventLogger();

        public void addEventListener(IServiceEventListener listener) {
            logger.addListener(listener);
        }

        public void removeEventListener(IServiceEventListener listener) throws Exception {
            logger.removeListener(listener);
        }

        protected void emitEvent(ServiceEventLogger.Event e) {
            logger.emitEvent(e);
        }
    }

}

