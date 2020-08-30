package com.example.client.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PreferenceManager {
    public static final String tagTrackerID = "tracker-id";
    public static final String tagAutoReport = "auto-report";
    public static final String tagPowerSaving = "power-saving";
    public static final String tagReportInterval = "report-interval";
    public static final String preferenceFileName = "com.example.client.preference-file";
    public static final String preferenceTrackerDefaultName = "unknown";

    private Context context;
    private List<HandlePreferenceChanges> preferenceTrackerIdObservers;
    private List<HandlePreferenceChanges> preferenceAutoReportObservers;
    private List<HandlePreferenceChanges> preferencePowerSavingObservers;
    private List<HandlePreferenceChanges> preferenceReportIntervalObservers;

    public PreferenceManager(Context context) {
        this.context = context;
        this.preferenceTrackerIdObservers = new ArrayList<>();
        this.preferenceAutoReportObservers = new ArrayList<>();
        this.preferencePowerSavingObservers = new ArrayList<>();
        this.preferenceReportIntervalObservers = new ArrayList<>();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
    }

    /* TrackerID */
    public void setTrackerID(String trackerID) {
        getSharedPreferences().edit().putString(tagTrackerID, trackerID).apply();
        triggerPreferenceChanges(tagTrackerID, preferenceTrackerIdObservers);
    }
    public String getTrackerID() {
        return getSharedPreferences().getString(tagTrackerID, preferenceTrackerDefaultName);
    }

    /* AutoReport */
    public void setAutoReport(boolean value) {
        getSharedPreferences().edit().putBoolean(tagAutoReport, value).apply();
        triggerPreferenceChanges(tagAutoReport, preferenceAutoReportObservers);
    }
    public boolean getAutoReport() {
        return getSharedPreferences().getBoolean(tagAutoReport, true);
    }

    /* ReportInterval */
    public void setReportInterval(int value) throws ArithmeticException {
        if (value < 5)
            throw new ArithmeticException("The value of report interval cannot less than 5");
        getSharedPreferences().edit().putInt(tagReportInterval, value).apply();
        triggerPreferenceChanges(tagReportInterval, preferenceReportIntervalObservers);
    }
    public int getReportInterval() {
        return getSharedPreferences().getInt(tagReportInterval, 30);
    }

    /* PowerSaving */
    public void setPowerSaving(boolean value) {
        getSharedPreferences().edit().putBoolean(tagPowerSaving, value).apply();
        triggerPreferenceChanges(tagPowerSaving, preferencePowerSavingObservers);
    }

    public boolean getPowerSaving() {
        return getSharedPreferences().getBoolean(tagPowerSaving, false);
    }

    public interface HandlePreferenceChanges {
        void onHandlePreferenceChanges(String changedPreferenceTag);
    }

    public void registerPreferenceChanges(String preference_tag, HandlePreferenceChanges observer) {
        switch (preference_tag) {
            case tagTrackerID:
                preferenceTrackerIdObservers.add(observer);
                break;
            case tagAutoReport:
                preferenceAutoReportObservers.add(observer);
                break;
            case tagReportInterval:
                preferenceReportIntervalObservers.add(observer);
                break;
            case tagPowerSaving:
                preferencePowerSavingObservers.add(observer);
                break;
            default:
                Log.e("PreferenceManager", "Unknown preference tag : " + preference_tag);
        }
    }

    public void unregisterPreferenceChanges(String preference_tag, HandlePreferenceChanges observer) {
        switch (preference_tag) {
            case tagTrackerID:
                preferenceTrackerIdObservers.remove(observer);
                break;
            case tagAutoReport:
                preferenceAutoReportObservers.remove(observer);
                break;
            case tagReportInterval:
                preferenceReportIntervalObservers.remove(observer);
                break;
            case tagPowerSaving:
                preferencePowerSavingObservers.remove(observer);
                break;
            default:
                Log.e("PreferenceManager", "Unknown preference tag : " + preference_tag);
        }
    }

    private void triggerPreferenceChanges(String tag, List<HandlePreferenceChanges> list) {
        for (HandlePreferenceChanges observer : list)
            observer.onHandlePreferenceChanges(tag);
    }
}
