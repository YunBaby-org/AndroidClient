package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.client.manager.PreferenceManager;
import com.example.client.services.ForegroundService;
import com.google.android.material.snackbar.Snackbar;

public class TrackerDashboardActivity extends AppCompatActivity {

    public static final String INTENT_DISPLAY_CONTENT = "intentDisplayContent";
    private View snackbar;
    private TextView textViewTrackerID;
    private Button buttonEnableService;
    private Button buttonDisableService;
    private Button buttonUnregisterTracker;
    private Button buttonAppSettings;
    private PreferenceManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_dashboard);

        pm = new PreferenceManager(this);

        snackbar = findViewById(R.id.activity_tracker_dashboard_snackbar);
        textViewTrackerID = findViewById(R.id.textviewTrackerID);
        buttonEnableService = findViewById(R.id.buttonEnableService);
        buttonDisableService = findViewById(R.id.buttonDisableService);
        buttonAppSettings = findViewById(R.id.buttonAppSettings);
        buttonUnregisterTracker = findViewById(R.id.buttonUnregisterTracker);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void startService() {
        Intent intent = new Intent(TrackerDashboardActivity.this, ForegroundService.class);
        startService(intent);
    }

    public void stopService() {
        Intent intent = new Intent(TrackerDashboardActivity.this, ForegroundService.class);
        stopService(intent);
    }

    private void removeCredentials() {
        pm.setRefreshToken("");
        pm.setRegistered(false);
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

}