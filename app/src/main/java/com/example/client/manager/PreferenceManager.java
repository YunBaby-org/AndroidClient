package com.example.client.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferenceManager {
    public static final String tagTrackerID = "tracker-id";
    public static final String tagAutoReport = "auto-report";
    public static final String tagPowerSaving = "power-saving";
    public static final String tagReportInterval = "report-interval";
    public static final String preferenceFileName = "com.example.client.preference-file";
    public static final String preferenceTrackerDefaultName = "unknown";

    private Context context;

    private Map<String, List<OnPreferenceChangedListener>> onPreferenceChangedListeners;

    public PreferenceManager(Context context) {
        this.context = context;
        onPreferenceChangedListeners = new HashMap<>();

    }

    @Deprecated
    public void setupListener() {
        getSharedPreferences().registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> {
            triggerListener(s);
        });
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
    }

    /* TrackerID */
    public void setTrackerID(String trackerID) {
        getSharedPreferences().edit().putString(tagTrackerID, trackerID).apply();
        triggerListener(tagTrackerID);
    }
    public String getTrackerID() {
        return getSharedPreferences().getString(tagTrackerID, preferenceTrackerDefaultName);
    }

    /* AutoReport */
    public void setAutoReport(boolean value) {
        getSharedPreferences().edit().putBoolean(tagAutoReport, value).apply();
        triggerListener(tagAutoReport);
    }
    public boolean getAutoReport() {
        return getSharedPreferences().getBoolean(tagAutoReport, true);
    }

    /* ReportInterval */
    public void setReportInterval(int value) throws ArithmeticException {
        if (value < 5)
            throw new ArithmeticException("The value of report interval cannot less than 5");
        getSharedPreferences().edit().putInt(tagReportInterval, value).apply();
        triggerListener(tagReportInterval);
    }
    public int getReportInterval() {
        return getSharedPreferences().getInt(tagReportInterval, 30);
    }

    /* PowerSaving */
    public void setPowerSaving(boolean value) {
        getSharedPreferences().edit().putBoolean(tagPowerSaving, value).apply();
        triggerListener(tagPowerSaving);
    }

    public boolean getPowerSaving() {
        return getSharedPreferences().getBoolean(tagPowerSaving, false);
    }

    public void registerListener(String preference_tag, OnPreferenceChangedListener listener) {
        if (!onPreferenceChangedListeners.containsKey(preference_tag))
            onPreferenceChangedListeners.put(preference_tag, new ArrayList<>());
        onPreferenceChangedListeners.get(preference_tag).add(listener);
    }

    public void unregisterListener(String preference_tag, OnPreferenceChangedListener listener) {
        if (onPreferenceChangedListeners.containsKey(preference_tag))
            onPreferenceChangedListeners.get(preference_tag).remove(listener);
        else
            Log.e("PreferenceManager", "No such listener");
    }

    public void triggerListener(String preference_tag) {
        List<OnPreferenceChangedListener> listeners = onPreferenceChangedListeners.get(preference_tag);
        if (listeners != null) {
            for (OnPreferenceChangedListener listener : listeners)
                listener.onPreferenceChanged(this, preference_tag);
        }
    }

    public interface OnPreferenceChangedListener {
        void onPreferenceChanged(PreferenceManager preferenceManager, String preference_tag);
    }

}
