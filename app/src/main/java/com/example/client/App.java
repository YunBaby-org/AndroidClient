package com.example.client;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

public class App extends Application {

    public static final String CHANNEL_ID = "ForegroundService-NotificationChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Application", "Create Application");
        CreateNotificationChannel();
    }

    private void CreateNotificationChannel() {

        /* Notification channel is a feature that only exist above SDK 26, create it if the condition is met */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            /* Create notification channel on startup */
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.setDescription(getString(R.string.notification_channel_description));

            /* Register this channel with notification manager */
            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.createNotificationChannel(notificationChannel);

        }

    }
}

