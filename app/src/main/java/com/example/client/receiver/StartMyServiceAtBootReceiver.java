package com.example.client.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.client.manager.PreferenceManager;
import com.example.client.services.ForegroundService;

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i("AutoRestart", "Triggered");
            PreferenceManager pm = new PreferenceManager(context);
            if (pm.getServiceAutoRestart()) {
                Intent serviceIntent = new Intent(context, ForegroundService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(serviceIntent);
                else
                    context.startService(serviceIntent);
            }
        }
    }
}
