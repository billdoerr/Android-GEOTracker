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
public class GPSServiceRestartBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GPSServiceRestart";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, context.getString(R.string.msg_gps_service_restart));
        context.startService(new Intent(context, GPSService.class));
    }

}