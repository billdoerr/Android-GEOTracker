package com.billdoerr.android.geotracker.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.IBinder;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.activities.MainActivity;
import com.billdoerr.android.geotracker.database.DatabaseHelper;
import com.billdoerr.android.geotracker.database.DatabaseManager;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.model.TripDetails;
import com.billdoerr.android.geotracker.database.repo.TripDetailsRepo;
import com.billdoerr.android.geotracker.utils.GPSUtils;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Objects;

/**
 * Singleton class providing GPS location data
 * Reference:  https://developer.android.com/reference/android/location/Location
 */
@SuppressWarnings("UnusedReturnValue")
public class TrackingService extends Service {

    private static final String TRACKING_SERVICE_CHANNEL_ID = "TrackingService";
    private static final int TRACKING_SERVICE_NOTIFICATION_ID = 2;

    // Indicates invalid table index
//    private static final int INVALID_INDEX = -1;

    private Trip mTrip;

    public TrackingService() {
        // Pass
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Context context = getApplicationContext();

        // Need the GPSService
        GPSUtils.startGPSService(context);

        /* Initialize instance of DatabaseManager. This is performed in BaseActivity but if
         *  trip is running and the app is dismissed the TrackingService will restart in background.
         *  This service will need to ensure the database is re-initialized.
         */
        initDatabase();

        // Initialize trip.  Get's active trip object from preferences or creates new trip object.
        initTrip(context);

        // Display notification to user that we are tracking
        sendNotification();

        // Register event bus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        // Unregister from the EventBus
        EventBus.getDefault().unregister(this);

        // Clear notifications
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(TRACKING_SERVICE_NOTIFICATION_ID);

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
     * This method will be called when a MessageEvent is posted
     * @param locationMessageEvent LocationMessageEvent
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationMessageEvent locationMessageEvent) {
        // Get location data
        Location location = locationMessageEvent.getLocation();

        // Write entry to database
        insertLocationIntoDatabase(location);

    }

    /**
     * Display notification in the notification drawer
     */
    private void sendNotification() {
        Resources resources = getResources();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, TRACKING_SERVICE_CHANNEL_ID)
                .setTicker(resources.getString(R.string.notification_tracking_service_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.notification_tracking_service_title))
                .setContentText(resources.getString(R.string.notification_tracking_service_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(TRACKING_SERVICE_NOTIFICATION_ID, notification);
    }

    /**
     * Insert new location into database
     * @param location Location
     */
    private void insertLocationIntoDatabase(Location location) {
        if (mTrip.getId() >= 0) {
            // Set location
            TripDetails tripDetail = new TripDetails();
            tripDetail.setTripId(mTrip.getId());
            tripDetail.setLocation(location);

            // Insert a new record
            TripDetailsRepo.insert(tripDetail);
        }
    }

    /**
     * Retrieve active Trip object from Shared Preferences
     * @param context Context Application context.
     */
    private void initTrip(Context context) {
        Trip trip = PreferenceUtils.getActiveTripFromSharedPrefs(Objects.requireNonNull(context));
        if (trip != null) {
            // Assign to global variable
            mTrip = trip;
        }
    }

    /**
     * Initialize instance of DatabaseManager
     * @return  DatabaseHelper
     */
    private DatabaseHelper initDatabase() {
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        DatabaseManager.initializeInstance(db);
        return db;
    }

}
