package com.billdoerr.android.geotracker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by fabio on 24/01/2016.
 * https://github.com/fabcira/neverEndingAndroidService/blob/master/app/src/main/java/oak/shef/ac/uk/testrunningservicesbackgroundrelaunched/SensorRestarterBroadcastReceiver.java
 */
public class GPSServiceRestartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, GPSService.class));
    }

}