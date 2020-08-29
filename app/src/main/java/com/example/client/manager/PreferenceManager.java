package com.example.client.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    public static final String tagTrackerID = "tracker-id";
    public static final String tagAutoReport = "auto-report";
    public static final String tagPowerSaving = "power-saving";
    public static final String tagReportInterval = "report-interval";
    public static final String preferenceFileName = "com.example.client.preference-file";
    public static final String preferenceTrackerDefaultName = "unknown";
    Context context;

    public PreferenceManager(Context context) {
        this.context = context;
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
    }

    /* TrackerID */
    public void setTrackerID(String trackerID) {
        SharedPreferences settings = getSharedPreferences();
        settings.edit()
                .putString(tagTrackerID, trackerID)
                .apply();
    }

    public String getTrackerID() {
        return getSharedPreferences().getString(tagTrackerID, preferenceTrackerDefaultName);
    }

    /* AutoReport */
    public void setAutoReport(boolean value) {
        getSharedPreferences().edit().putBoolean(tagAutoReport, value).apply();
    }

    public boolean getAutoReport() {
        return getSharedPreferences().getBoolean(tagAutoReport, true);
    }

    /* ReportInterval */
    public void setReportInterval(int value) throws ArithmeticException {
        if (value < 5)
            throw new ArithmeticException("The value of report interval cannot less than 5");
        getSharedPreferences().edit().putInt(tagReportInterval, value).apply();
    }

    public int getReportInterval() {
        return getSharedPreferences().getInt(tagReportInterval, 30);
    }

    /* PowerSaving */
    public void setPowerSaving(boolean value) {
        getSharedPreferences().edit().putBoolean(tagPowerSaving, value).apply();
    }

    public boolean getPowerSaving() {
        return getSharedPreferences().getBoolean(tagPowerSaving, false);
    }

}
