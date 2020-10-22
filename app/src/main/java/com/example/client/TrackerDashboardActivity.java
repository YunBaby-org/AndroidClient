package com.example.client;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.client.manager.PreferenceManager;
import com.example.client.room.AppDatabase;
import com.example.client.room.entity.Event;
import com.example.client.room.ulility.EventType;
import com.example.client.services.ForegroundService;
import com.example.client.services.ServiceEventLogger;
import com.example.client.views.ChartMarkerView;
import com.example.client.views.EventRecyclerViewAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrackerDashboardActivity extends AppCompatActivity implements ServiceConnection, ServiceEventLogger.IServiceEventListener {

    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    public static final String INTENT_DISPLAY_CONTENT = "intentDisplayContent";
    private LineChart lineChart;
    private View activityLayout;
    private FloatingActionButton fab;
    private PreferenceManager pm;
    private Boolean isServiceBindingOk = false;
    private static final int CHART_DATASET_SEND_RESPONSE = 0;
    private static final int CHART_DATASET_RECEIVE_REQUEST = 1;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private ArrayList<Event> events;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tracker_activity_actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_remove_credentials:
                attemptsRemoveCredentials();
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

        toolbar = findViewById(R.id.actionBar);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        activityLayout = findViewById(R.id.activity_tracker_dashboard_layout);
        lineChart = findViewById(R.id.lineChart);
        fab = findViewById(R.id.fab);

        fab.setOnClickListener((view) -> {
            switchServiceOnOff();
        });

        initializeLineChart();

        /* Display information on startup */
        Intent intent = getIntent();
        String displayText = intent.getStringExtra(TrackerDashboardActivity.INTENT_DISPLAY_CONTENT);
        if (displayText != null && !displayText.equals("")) {
            Snackbar.make(activityLayout, displayText, Snackbar.LENGTH_SHORT).show();
        }

        /* Hook up recycler view */
        events = new ArrayList<Event>();
        layoutManager = new LinearLayoutManager(this);
        recyclerAdapter = new EventRecyclerViewAdapter(this, events);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
        /* TODO: Apply theme transition during on/off */

        updateServiceUI(isMyServiceRunning(ForegroundService.class), true);
        /* TODO: Does this thing keep running even after activity exit or stop? */
        runServiceStateCheck = checkServiceState(5000, true);
    }

    private void switchServiceOnOff() {
        if (isMyServiceRunning(ForegroundService.class))
            attemptsStopService();
        else
            startService();
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
                List<Event> newEvents = appDatabase.eventDao().getEventsSince(last10Sec, 100);
                events.addAll(0, newEvents);

                /* Update */
                runOnUiThread((Runnable) () -> {
                    updateChartItem(countResponse, countRequest);
                    updateEventViewTime();
                    recyclerAdapter.notifyItemRangeInserted(0, newEvents.size());
                    setupUpdateEvent();                 /* Repeat itself */
                });
            });
        }, 1000);
    }

    private void updateEventViewTime() {
        int updateCount = 0;
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if (holder instanceof EventRecyclerViewAdapter.EventViewHolder) {
                boolean updated = ((EventRecyclerViewAdapter.EventViewHolder) holder).updateTime(this);
                if (updated)
                    updateCount++;
            } else {
                Log.wtf("TrackerDashboardActivity", "Holder is not a instance of EventViewHolder, WTF?");
            }
        }
        Log.d("TrackerDashboardActivity", String.format(Locale.getDefault(), "Event %d view(s) updated", updateCount));
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
        updateServiceUI(isMyServiceRunning(ForegroundService.class), true);
    }

    private Runnable runServiceStateCheck;

    public void startService() {
        if (isMyServiceRunning(ForegroundService.class)) {
            Toast.makeText(this, "服務已啟動", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(TrackerDashboardActivity.this, ForegroundService.class);
            pm.setServiceAutoRestart(true);
            startService(intent);
            attemptsBinding();

            checkServiceState(100, false);
            checkServiceState(500, false);
        }
    }

    private Runnable checkServiceState(int msInterval, boolean repeat) {
        Handler handler = new Handler(getMainLooper());
        /* Create a runnable, perform fab icon update at certain point */
        Runnable run = (Runnable) () -> {
            runOnUiThread((Runnable) () -> {
                updateServiceUI(isMyServiceRunning(ForegroundService.class));
            });
            if (repeat)
                checkServiceState(msInterval, true);
        };
        handler.postDelayed(run, msInterval);
        return run;
    }

    /* WARNING: The default value of lastEnabledValue must match the icon of layout fab */
    private boolean lastEnabledValue = true;

    private void updateServiceUI(boolean isEnabled) {
        updateServiceUI(isEnabled, false);
    }

    private void updateServiceUI(boolean isEnabled, boolean forceUpdate) {
        if (isEnabled != lastEnabledValue || forceUpdate) {
            lastEnabledValue = isEnabled;
            /* There is a bug in Android Material Design fab library, we have to call hide/show to refresh the icon */
            fab.hide();
            int resId = isEnabled ? R.drawable.ic_location_enabled : R.drawable.ic_location_disabled;
            Drawable drawable = getDrawable(resId);
            fab.setImageDrawable(drawable);
            fab.show();

            resId = isEnabled ? R.string.action_bar_title_service_enabled : R.string.action_bar_title_service_disabled;
            getSupportActionBar().setTitle(resId);
            toolbar.setTitle(resId);
            collapsingToolbarLayout.setTitle(getString(resId));
        }
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
        checkServiceState(100, false);
        checkServiceState(500, false);
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