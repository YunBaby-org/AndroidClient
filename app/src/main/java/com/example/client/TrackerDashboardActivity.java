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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.client.manager.PreferenceManager;
import com.example.client.room.AppDatabase;
import com.example.client.room.ulility.EventType;
import com.example.client.services.ForegroundService;
import com.example.client.services.ServiceEventLogger;
import com.example.client.views.ChartMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrackerDashboardActivity extends AppCompatActivity implements ServiceConnection, ServiceEventLogger.IServiceEventListener {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    public static final String INTENT_DISPLAY_CONTENT = "intentDisplayContent";
    private LineChart lineChart;
    private View activityLayout;
    private Button buttonDisableService;
    private Button buttonUnregisterTracker;
    private PreferenceManager pm;
    private Boolean isServiceBindingOk = false;
    private static final int CHART_DATASET_SEND_RESPONSE = 0;
    private static final int CHART_DATASET_RECEIVE_REQUEST = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tracker_activity_actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_dashboard);
        setSupportActionBar(findViewById(R.id.actionBar));

        pm = new PreferenceManager(this);

        activityLayout = findViewById(R.id.activity_tracker_dashboard_layout);
        lineChart = findViewById(R.id.lineChart);
        Button buttonEnableService = findViewById(R.id.buttonEnableService);
        buttonDisableService = findViewById(R.id.buttonDisableService);
        buttonUnregisterTracker = findViewById(R.id.buttonUnregisterTracker);

        buttonEnableService.setOnClickListener((view) -> {
            startService();
        });
        buttonDisableService.setOnClickListener((view) -> {
            attemptsStopService();
        });
        buttonUnregisterTracker.setOnClickListener((view) -> {
            attemptsRemoveCredentials();
        });

        initializeLineChart();

        Intent intent = getIntent();
        String displayText = intent.getStringExtra(TrackerDashboardActivity.INTENT_DISPLAY_CONTENT);
        if (displayText != null && !displayText.equals("")) {
            Snackbar.make(activityLayout, displayText, Snackbar.LENGTH_SHORT).show();
        }

    }

    private void initializeLineChart() {
        /* Generate fake data */
        ArrayList<Entry> values0 = new ArrayList<>();
        ArrayList<Entry> values1 = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            values0.add(new Entry(i, (float) (Math.random() * 10)));
            values1.add(new Entry(i, -(float) (Math.random() * 10)));
        }

        /* Get color */
        int accentColor = this.getResources().getColor(R.color.colorAccent, getTheme());
        int accentColorDeep1 = this.getResources().getColor(R.color.colorAccentDeep1, getTheme());
        int requestLineColor = this.getResources().getColor(R.color.colorChartRequestLine, getTheme());
        int responseLineColor = this.getResources().getColor(R.color.colorChartResponseLine, getTheme());

        /* Generate SEND_RESPONSE DataSet */
        LineDataSet set0 = new LineDataSet(values0, "SEND-RESPONSE");
        set0.setLineWidth(1.75f);
        set0.setCircleRadius(5f);
        set0.setCircleHoleRadius(2.5f);
        set0.setColor(responseLineColor);
        set0.setCircleColor(responseLineColor);
        ;
        set0.setHighLightColor(responseLineColor);
        set0.setDrawValues(false);
        set0.setDrawHighlightIndicators(false);

        /* Generate RECEIVE_REQUEST DataSet */
        LineDataSet set1 = new LineDataSet(values1, "RECEIVE-REQUEST");
        set1.setLineWidth(1.75f);
        set1.setCircleRadius(5f);
        set1.setCircleHoleRadius(2.5f);
        set1.setColor(requestLineColor);
        set1.setCircleColor(requestLineColor);
        ;
        set1.setHighLightColor(requestLineColor);
        set1.setDrawValues(false);
        set1.setDrawHighlightIndicators(false);

        ILineDataSet[] dataSets = new ILineDataSet[2];
        dataSets[CHART_DATASET_SEND_RESPONSE] = set0;
        dataSets[CHART_DATASET_RECEIVE_REQUEST] = set1;
        LineData lineData = new LineData(dataSets);

        /* Setup Chart */
        ((LineDataSet) lineData.getDataSetByIndex(0)).setCircleHoleColor(accentColor);
        ((LineDataSet) lineData.getDataSetByIndex(1)).setCircleHoleColor(accentColor);
        lineChart.getDescription().setEnabled(false);
        /* Touch interaction */
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);

        lineChart.setBackgroundColor(Color.TRANSPARENT);
        lineChart.setViewPortOffsets(0, 10, 0, 10);
        lineChart.setData(lineData);

        /* Setting legend (only possible after setting data) */
        Legend l = lineChart.getLegend();
        l.setEnabled(false);

        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisLeft().setSpaceTop(40);
        lineChart.getAxisLeft().setSpaceBottom(40);
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setGridColor(accentColorDeep1);
        lineChart.getXAxis().setEnabled(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);

        /* Setup Marker */
        ChartMarkerView markerView = new ChartMarkerView(this, R.layout.custom_marker_view);
        markerView.setChartView(lineChart);
        lineChart.setMarker(markerView);

        lineChart.animateX(2000);
        // lineChart.getAxisLeft().setAxisMinimum(-5);
        // lineChart.getAxisLeft().setAxisMaximum( 5);

        setupUpdateEvent();
    }

    private void setupUpdateEvent() {
        Handler handler = new Handler(getMainLooper());

        handler.postDelayed((Runnable) () -> {
            executorService.execute((Runnable) () -> {
                /* Get time */
                Calendar c = Calendar.getInstance();
                Date current = new Date();
                c.setTime(current);
                c.add(Calendar.SECOND, -1);
                Date last10Sec = c.getTime();

                /* Query */
                AppDatabase appDatabase = AppDatabase.getDatabase(TrackerDashboardActivity.this);
                int countResponse = appDatabase.eventDao().countItem(EventType.SEND_RESPONSE, last10Sec, current);
                int countRequest = appDatabase.eventDao().countItem(EventType.RECEIVE_REQUEST, last10Sec, current);

                /* Update */
                runOnUiThread((Runnable) () -> {
                    updateChartItem(countResponse, countRequest);
                    setupUpdateEvent();                 /* Repeat itself */
                });
            });
        }, 1000);
    }

    private void updateChartItem(float countResponse, float countRequest) {
        LineData lineData = lineChart.getData();
        ILineDataSet responseDataSet = lineData.getDataSetByIndex(CHART_DATASET_SEND_RESPONSE);
        ILineDataSet requestDataSet = lineData.getDataSetByIndex(CHART_DATASET_RECEIVE_REQUEST);

        Entry lastEntry;
        lastEntry = responseDataSet.getEntryForIndex(responseDataSet.getEntryCount() - 1);
        responseDataSet.addEntry(new Entry(lastEntry.getX() + 1, countResponse));
        responseDataSet.removeEntry(0);

        lastEntry = requestDataSet.getEntryForIndex(requestDataSet.getEntryCount() - 1);
        requestDataSet.addEntry(new Entry(lastEntry.getX() + 1, -countRequest));
        requestDataSet.removeEntry(0);

        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    private void attemptsStopService() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.TrackerDashboardActivity_prompt_stop_tracking);
        dialog.setPositiveButton(R.string.prompt_confirmStop, (dialogInterface, i) -> {
            stopService();
        });
        dialog.setNegativeButton(R.string.prompt_cancel, (dialogInterface, i) -> {

        });
        dialog.create().show();
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

    private void attemptsRemoveCredentials() {
        /* Display a alert dialog */
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.TrackerDashboardActivity_prompt_remove_credentials);
        dialog.setPositiveButton(R.string.prompt_confirmRemove, (dialogInterface, i) -> {
            stopService();
            removeCredentials();
        });
        dialog.setNegativeButton(R.string.prompt_cancel, (dialogInterface, i) -> {

        });
        dialog.create().show();
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

    @Override
    public void onEventOccurred(ServiceEventLogger.Event e) {
        runOnUiThread(() -> {
            String info = e.getContent() == null ? getString(e.getContentId()) : e.getContent();
            // TextView tv = new TextView(this);
            // tv.setText(String.format("[%s] %s", e.getLevel().toString(), info));
            // tv.setTextColor(Color.argb(0xff, 0x14, 0x14, 0x14));
            // eventLayout.addView(tv);
        });
    }
}