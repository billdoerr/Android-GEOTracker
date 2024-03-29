package com.billdoerr.android.geotracker.utils;

import android.app.ActivityManager;
import android.content.Context;

public class ServiceUtils {

    /**
     * Checks if service is currently running.
     * @param serviceClass Class<?>
     * @return boolean Returns true if passed service is running.
     */
    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
