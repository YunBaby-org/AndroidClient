package com.example.client;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.client.manager.PreferenceManager;
import com.example.client.services.ForegroundService;
import com.google.android.material.snackbar.Snackbar;

public class TrackerDashboardActivity extends AppCompatActivity implements ServiceConnection, ForegroundService.EventListener {

    public static final String INTENT_DISPLAY_CONTENT = "intentDisplayContent";
    private View snackbar;
    private TextView textViewTrackerID;
    private LinearLayout eventLayout;
    private Button buttonDisableService;
    private Button buttonUnregisterTracker;
    private Button buttonAppSettings;
    private PreferenceManager pm;
    private Boolean isServiceBindingOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_dashboard);

        pm = new PreferenceManager(this);

        snackbar = findViewById(R.id.activity_tracker_dashboard_snackbar);
        textViewTrackerID = findViewById(R.id.textviewTrackerID);
        Button buttonEnableService = findViewById(R.id.buttonEnableService);
        buttonDisableService = findViewById(R.id.buttonDisableService);
        buttonAppSettings = findViewById(R.id.buttonAppSettings);
        buttonUnregisterTracker = findViewById(R.id.buttonUnregisterTracker);
        eventLayout = findViewById(R.id.EventLayout);

        buttonAppSettings.setOnClickListener((view) -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        buttonEnableService.setOnClickListener((view) -> {
            startService();
        });
        buttonDisableService.setOnClickListener((view) -> {
            stopService();
        });
        buttonUnregisterTracker.setOnClickListener((view) -> {
            stopService();
            removeCredentials();
        });

        Intent intent = getIntent();
        String displayText = intent.getStringExtra(TrackerDashboardActivity.INTENT_DISPLAY_CONTENT);
        if (displayText != null && !displayText.equals("")) {
            Snackbar.make(snackbar, displayText, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        textViewTrackerID.setText(pm.getTrackerID());

        if (isMyServiceRunning(ForegroundService.class)) {
            bindToService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void startService() {
        Intent intent = new Intent(TrackerDashboardActivity.this, ForegroundService.class);
        pm.setServiceAutoRestart(true);
        startService(intent);
        attemptsBinding();
    }

    public void bindToService() {
        Intent intent = new Intent(TrackerDashboardActivity.this, ForegroundService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void stopService() {
        Intent intent = new Intent(TrackerDashboardActivity.this, ForegroundService.class);
        pm.setServiceAutoRestart(false);
        if (isServiceBindingOk && isMyServiceRunning(ForegroundService.class)) {
            try {
                unbindService(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopService(intent);
    }

    private void removeCredentials() {
        pm.setRefreshToken("");
        pm.setRegistered(false);
        pm.setTrackerID("");
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.i("FS", "Service binding established");
        ((ForegroundService.ServiceBinder) iBinder).addEventListener(this);
        isServiceBindingOk = true;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        Log.i("FS", "Service fuck up");
        isServiceBindingOk = false;
    }

    @Override
    public void onNullBinding(ComponentName name) {
        Log.i("FS", "Service binding null");
        isServiceBindingOk = false;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.i("FS", "Service binding disconnected");
        isServiceBindingOk = false;
        /* Attempts to establish binding */
        attemptsBinding();
    }

    @Override
    public void onEventOccurred(ForegroundService.Event event) {
        runOnUiThread(() -> {
            String info = event.getContent().equals("") ? getString(event.getResourceHint()) : event.getContent();
            TextView tv = new TextView(this);
            tv.setText(String.format("[%s] %s", event.getEventLevel().toString(), info));
            tv.setTextColor(Color.argb(0xff, 0x14, 0x14, 0x14));
            eventLayout.addView(tv);
        });
    }

    private void attemptsBinding() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            /* Binding is on, nothing to do */
            if (!PreferenceManager.getServiceAutoRestart(this))
                return;
            if (isServiceBindingOk)
                return;
            /* Is service running */
            if (isMyServiceRunning(ForegroundService.class))
                bindToService();
            /* Re-Distribute this event */
            attemptsBinding();
        }, 5000);
    }
}