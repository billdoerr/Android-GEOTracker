package com.billdoerr.android.geotracker.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.activities.MainActivity;
import com.billdoerr.android.geotracker.utils.GeoTrackerSharedPreferences;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


/**
 * Singleton class providing GPS location data
 * Reference:  https://developer.android.com/reference/android/location/Location
 */
public class GPSService extends Service implements LocationListener {

    private static final String TAG = "GPSService";

    private static final String GPS_SERVICE_CHANNEL_ID = "GPSService";
    private static final int GPS_SERVICE_NOTIFICATION_ID = 1;

    //The name of the provider with which to register This value must never be null.
    private static final String PROVIDER = LocationManager.GPS_PROVIDER;

    // Minimum time interval between location updates, in milliseconds. Value configured in  settings.
    private long mUpdateInterval = 2000;

    // Minimum distance between location updates, in meters. Value configured in  settings.
    private float mUpdateDistance = 1;

    private Context mContext;
    private LocationManager mLocationManager;

    public GPSService() {
        // Pass
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(TAG, getResources().getString(R.string.msg_gps_service_starting));

        mContext = getApplicationContext();

        // We need some preference settings
        GeoTrackerSharedPreferences sharedPrefs = PreferenceUtils.getSharedPreferences(mContext);

        mUpdateInterval = sharedPrefs.getLocationServicesUpdateInterval();
        mUpdateDistance = sharedPrefs.getLocationServicesUpdateDistance();

        // Start location updates
        startUpdates();

        // Display notification to user that we are tracking
        sendNotification();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Stop location updates
        stopUpdates();

        // Clear notifications
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(GPS_SERVICE_NOTIFICATION_ID);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Pass
        // We don't provide binding, so return null
        return null;
    }

    /**
     * Display notification in the notification drawer
     */
    private void sendNotification() {
        Resources resources = getResources();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, GPS_SERVICE_CHANNEL_ID)
                .setTicker(resources.getString(R.string.notification_gps_service_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.notification_gps_service_title))
                .setContentText(resources.getString(R.string.notification_gps_service_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(GPS_SERVICE_NOTIFICATION_ID, notification);
    }


    /*
     * ******************************************************************
     * Location Listener methods
     * ******************************************************************
     */

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged: " + location);
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
     * Start location updates
     */
    private void startUpdates() {

        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        GnssStatus.Callback gnssStatusListener = new GnssStatus.Callback() {
//            @Override
//            public void onStarted() {
//                super.onStarted();
//            }

//            @Override
//            public void onStopped() {
//                super.onStopped();
//            }

            @Override
            public void onFirstFix(int ttffMillis) {
                super.onFirstFix(ttffMillis);
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
//                Log.d(TAG, "Satellite Count:  " + Integer.toString(status.getSatelliteCount()));
                super.onSatelliteStatusChanged(status);
            }
        };

        // Start requesting updates
        try {
            mLocationManager.registerGnssStatusCallback(gnssStatusListener);
            // Begin location updates
            mLocationManager.requestLocationUpdates(PROVIDER,
                    mUpdateInterval,
                    mUpdateDistance,
                    this);
        } catch (SecurityException e) {
            Log.e(TAG,"Error "+e);
            Log.e(TAG, mContext.getString(R.string.exception_register_gnns_status_callback), e);
        }

    }

    /**
     * Stop location updates
     */
    private void stopUpdates() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

}
