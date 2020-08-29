package com.example.client.manager;

import android.content.Context;

/**
 * This is a super super class that provider a bunch of functionality.
 * The Request will create the response with the help of all these managers
 */
public class Managers {
    private PowerManager powerManager;
    private PreferenceManager preferenceManager;
    private Context context;

    public PowerManager getPowerManager() {
        return powerManager;
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public Context getContext() {
        return context;
    }

    public Managers(Context context) {
        this.powerManager = new PowerManager(context);
        this.preferenceManager = new PreferenceManager(context);
        this.context = context;
    }
}
