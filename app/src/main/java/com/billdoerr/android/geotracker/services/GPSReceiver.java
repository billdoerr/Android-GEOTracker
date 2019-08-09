package com.billdoerr.android.geotracker.services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.billdoerr.android.geotracker.R;

import org.greenrobot.eventbus.EventBus;

import androidx.core.content.ContextCompat;

/**
 * Singleton class providing GPS location data
 * Reference:  https://developer.android.com/reference/android/location/Location
 */
public class GPSReceiver implements LocationListener {

    private static final String TAG = "GPSReceiver";

    //The name of the provider with which to register This value must never be null.
    private static final String PROVIDER = LocationManager.GPS_PROVIDER;

    // TODO:  Add to settings
    // Minimum time interval between location updates, in milliseconds
    private static final long MIN_TIME = 2000;

    // TODO:  Add to settings
    // Minimum distance between location updates, in meters
    private static final float MIN_DISTANCE = 1;

    private static GPSReceiver sGPSReceiver;

    private static Context mContext;

    private LocationManager mLocationManager;
    private Location mLocation;

    // A LocationListener whose LocationListener#onLocationChanged method will be called for each
    // location update This value must never be null.
    private LocationListener mLocationListener;

    private GnssStatus mGnssStatus;
    private GnssStatus.Callback mGnssStatusListener;

    /**
     *
     * @param context Context:
     * @return Instance of GPSReceiver
     */
    public static GPSReceiver initializeInstance(Context context) {
        if (sGPSReceiver == null) {
            sGPSReceiver = new GPSReceiver();
            mContext = context;
        }
        return sGPSReceiver;
    }

    /**
     * Returns instance of GPSReceiver, if initialized.
     * @return GPSReceiver:
     */
    public static GPSReceiver getInstance() {
        if (sGPSReceiver == null) {
            throw new IllegalStateException(GPSReceiver.class.getSimpleName() +
        " is not initialized, call initializeInstance(..) method first.");
        }
        return sGPSReceiver;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        // Post location to event bus
        EventBus.getDefault().post(new LocationMessageEvent(location));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // This method was deprecated in API level 29.
        // This callback will never be invoked.
        // Pass
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Pass
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Pass
    }

    /**
     * Returns the current location data
     * @return Location:
     */
    public Location getLocation() {
        return mLocation;
    }

    /**
     * Returns last known location
     * @return
     */
    public Location getCurrentLocation() {
       LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
       Location location = null;
        try {
            location = lm.getLastKnownLocation(PROVIDER);
        } catch (SecurityException e) {
            Log.e(TAG,"Error "+e);;
        }
        return location;
    }


    /**
     * Start location updates
     */
    public void startUpdates() {
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                super.onStarted();
            }

            @Override
            public void onStopped() {
                super.onStopped();
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                super.onFirstFix(ttffMillis);
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                Log.d(TAG, "Satellite Count:  " + Integer.toString(status.getSatelliteCount()));
                super.onSatelliteStatusChanged(status);
            }
        };

        // Start requesting updates
        try {
            mLocationManager.registerGnssStatusCallback(mGnssStatusListener);
            // Begin location updates
            mLocationManager.requestLocationUpdates(PROVIDER,
                    MIN_TIME,
                    MIN_DISTANCE,
                    this);
        } catch (SecurityException e) {
            Log.e(TAG,"Error "+e);
            Log.e(TAG, mContext.getString(R.string.exception_register_gnns_status_callback), e);
        }


    }

    /**
     * Stop location updates
     */
    public void stopUpdates() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

    /**
     * Request app permissions.
     * @return boolean:  True if permissions granted.
     */
    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Calculates the GPS signal strength, using GnssStatus.Callback() through getSatelliteCount(),
     * which gives us the number of satellites that are within our reach, and each of them has
     * a signal-to-noise ratio (getCn0DbHz(i)).
     * Ripped from:  https://stackoverflow.com/questions/48200672/how-to-get-snr-of-each-detected-gps-satellite-in-android-7-0-using-gnssmeasureme
     * @param status  GnnsStatus:
     */
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
