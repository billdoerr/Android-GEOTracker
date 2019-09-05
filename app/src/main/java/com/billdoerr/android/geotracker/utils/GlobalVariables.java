package com.billdoerr.android.geotracker.utils;

import android.app.Application;
import android.content.res.Configuration;

/**
* Define global variables is by extending the Application class.
* This is the base class for maintaining global application state.
 *
 * The Application class in Android is the base class within an Android app that contains all other
 * components such as activities and services. The Application class, or any subclass of the Application
 * class, is instantiated before any other class when the process for your application/package is created.
 *
 * This class is primarily used for initialization of global state before the first Activity is displayed.
 * Note that custom Application objects should be used carefully and are often not needed at all.
 *
*/
public class GlobalVariables extends Application {

    private static final String TAG = "GlobalVariables";

    @Override
    public void onCreate() {
        super.onCreate();
//        mSingleton = this;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


}