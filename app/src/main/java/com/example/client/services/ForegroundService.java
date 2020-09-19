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
import com.example.client.runners.ForegroundRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.example.client.App.CHANNEL_ID;

public class ForegroundService extends Service {

    /* TODO: Start the service on boot */
    /* TODO: Restart the service on accident */

    public static void emitEvent(EventLevel level, int resourceId) {
        getServiceBinder().emitEvent(Event.createEvent(level, resourceId));
    }

    public static void emitEvent(EventLevel level, String content) {
        getServiceBinder().emitEvent(Event.createEvent(level, content));
    }

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

    private void setupServiceThread() {
        if (serviceThread == null) {
            PreferenceManager pm = new PreferenceManager(this);
            serviceThread = new Thread(new ForegroundRunner(this, pm.getTrackerID()));
            serviceThread.start();
        }
    }

    @Override
    public boolean stopService(Intent name) {
        this.serviceThread.interrupt();
        return super.stopService(name);
    }

    public enum EventLevel {Critical, Error, Warning, Info, Debug}

    public static class ServiceBinder extends Binder {
        private final List<EventListener> eventListeners = Collections.synchronizedList(new ArrayList<>());

        public void addEventListener(EventListener listener) {
            synchronized (eventListeners) {
                if (!eventListeners.contains(listener))
                    eventListeners.add(listener);
                else
                    Log.w("ForegroundService", "Event listener already registered");
            }
        }

        public boolean removeEventListener(EventListener listener) {
            synchronized (eventListeners) {
                return eventListeners.remove(listener);
            }
        }

        protected void emitEvent(Event event) {
            synchronized (eventListeners) {
                for (EventListener e : eventListeners)
                    e.onEventOccurred(event);
            }
        }
    }

    public interface EventListener {
        void onEventOccurred(Event event);
    }

    public static class Event {
        private Date eventTime;
        private ForegroundService.EventLevel eventLevel;
        private String content;
        private int resourceHint;

        private Event(Date eventTime, ForegroundService.EventLevel eventLevel, int resourceHint, String content) {
            this.eventTime = eventTime;
            this.eventLevel = eventLevel;
            this.content = content;
            this.resourceHint = resourceHint;
        }

        public static Event createEvent(Date eventTime, ForegroundService.EventLevel eventLevel, int resourceHint, String content) {
            return new Event(eventTime, eventLevel, resourceHint, content);
        }

        public static Event createEvent(ForegroundService.EventLevel eventLevel, int resourceHint, String content) {
            return new Event(new Date(), eventLevel, resourceHint, content);
        }

        public static Event createEvent(ForegroundService.EventLevel eventLevel, int resourceHint) {
            return new Event(new Date(), eventLevel, resourceHint, "");
        }

        public static Event createEvent(ForegroundService.EventLevel eventLevel, String content) {
            return new Event(new Date(), eventLevel, -1, content);
        }

        public Date getEventTime() {
            return eventTime;
        }

        public ForegroundService.EventLevel getEventLevel() {
            return eventLevel;
        }

        public String getContent() {
            return content;
        }

        public int getResourceHint() {
            return resourceHint;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return ForegroundService.getServiceBinder();
    }

    @Override
    public void onDestroy() {
        Log.i("ForegroundRunner", "Attempts to stop service thread.");
        this.serviceThread.interrupt();
        super.onDestroy();
    }

}

