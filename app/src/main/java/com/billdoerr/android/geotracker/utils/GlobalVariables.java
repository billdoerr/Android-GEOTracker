package com.billdoerr.android.geotracker.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

//  TODO:  Remove if not used
/*
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}