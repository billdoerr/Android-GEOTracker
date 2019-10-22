package com.billdoerr.android.geotracker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.billdoerr.android.geotracker.R;

/**
 * Created by fabio on 24/01/2016.
 * https://github.com/fabcira/neverEndingAndroidService/blob/master/app/src/main/java/oak/shef/ac/uk/testrunningservicesbackgroundrelaunched/SensorRestarterBroadcastReceiver.java
 */
public class TrackingServiceRestartBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "TrackingServiceRestart";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, context.getString(R.string.msg_tracking_service_restart));
        context.startService(new Intent(context, TrackingService.class));
    }

}