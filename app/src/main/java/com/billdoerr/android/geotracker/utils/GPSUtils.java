package com.billdoerr.android.geotracker.utils;

import android.content.Context;
import android.content.Intent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.services.GPSService;
import com.billdoerr.android.geotracker.services.LocationMessageEvent;

import org.greenrobot.eventbus.EventBus;

public class GPSUtils {

    private static final String TAG = "GPSUtils";

//    /**
//     * Returns last known location and also posts to EventBus
//     */
//    public static Location getInitialLocation(Context context) {
//        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
//        try {
//            return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        } catch (SecurityException e) {
//            Log.e(TAG, e.getMessage());
//        }
//        return null;
//    }

    /**
     * Start GPSService if not running
     * @param context Context
     */
    public static void startGPSService(Context context) {
        boolean isGPSService = ServiceUtils.isMyServiceRunning(context, GPSService.class);
        if (!isGPSService) {
            Log.i(TAG, context.getString(R.string.msg_gps_service_restart));
            context.startService(new Intent(context, GPSService.class));
        }
    }

    /**
     * Returns last known location and also posts to EventBus
     */
    public static void getCurrentLocation(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        try {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.e(TAG,e.getMessage());
        }

        // Post to event bus
        EventBus.getDefault().post(new LocationMessageEvent(location));

    }

    /**
     * Calculates the GPS signal strength, using GnssStatus.Callback() through getSatelliteCount(),
     * which gives us the number of satellites that are within our reach, and each of them has
     * a signal-to-noise ratio (getCn0DbHz(i)).
     * Ripped from:  https://stackoverflow.com/questions/48200672/how-to-get-snr-of-each-detected-gps-satellite-in-android-7-0-using-gnssmeasureme
     * @param status  GnnsStatus:
     */
    @SuppressWarnings("unused")
    private void getGpsSatelliteSNR(GnssStatus status) {

        int satelliteCount = status.getSatelliteCount();

        int usedSatellites = 0;
        float totalSnr = 0;
        for (int i = 0; i < satelliteCount; i++){
            if (status.usedInFix(i)) {
                usedSatellites++;
                totalSnr += status.getCn0DbHz(i); //this method obtains the signal from each satellite
            }
        }
        // we calculate the average of the power of the GPS signal
        float avgSnr = (usedSatellites > 0) ? totalSnr / usedSatellites: 0.0f;

        Log.d(TAG, "Number used satellites: " + usedSatellites + " SNR: " + totalSnr+"avg SNR: "+avgSnr);
    }

}
