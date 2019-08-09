package com.billdoerr.android.geotracker.utils;

import android.app.Application;

/**
* Define global variables is by extending the Application class.
* This is the base class for maintaining global application state.
*/
public class GlobalVariables extends Application {

    private static final String TAG = "GlobalVariables";

    // Shared preferences
    public static final String PREF_KEY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            = "com.billdoerr.android.carputer.settings.SettingsActivity.PREF_KEY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION ";

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public static final String ARGS_TRIP = "Trip";


}