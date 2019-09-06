package com.billdoerr.android.geotracker.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.services.GPSService;
import com.billdoerr.android.geotracker.services.GPSUtils;
import com.billdoerr.android.geotracker.services.LocationMessageEvent;
import com.billdoerr.android.geotracker.utils.CoordinateConversionUtils;
import com.billdoerr.android.geotracker.utils.GeoTrackerSharedPreferences;
import com.billdoerr.android.geotracker.utils.PermissionUtils;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class TrackingFragment extends Fragment implements TripDetailFragment.DialogListener {

    private static final String TAG = "TrackingFragment";

    private static final long TIME_DELAY = 1000;
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat( "hh:mm:ss a" , Locale.US);

    private @interface CoordinateType {
        int DECIMAL_DEGREES = 0;
        int DEGREES_MINUTES_SECONDS =  1;
        int DEGREES_DECIMAL_MINUTES = 2;
        int UTM_COORDINATES = 3;
        int MGRS_COORDINATES = 4;
    }

    // Feature supporting this has not been implemented
//    public @interface CoordinateDatum {
//        int WGS84 = 0;
//        int NAD27 =  1;
//    }

    private Intent mGPSServiceIntent;
    private Trip mTrip;
    private Date mCurrentTime;

    // Preference settings
    GeoTrackerSharedPreferences mSharedPrefs;
    private static boolean mIsMetric = false;
    private static boolean mIsNautical = false;
    private static int mCoordinateType;
    // Feature supporting this has not been implemented
//    private static int mCoordinateDatum;

    // UI widgets
    private TextView mTextTripTitle;
    private TextView mTextLatitudeLongitude;
    private TextView mTextLatitudeData;
    private TextView mTextLatitudeUnits;
    private TextView mTextLongitudeData;
    private TextView mTextLongitudeUnits;
    private TextView mTextElevationData;
    private TextView mTextElevationUnits;
    private TextView mTextBearingData;
    private TextView mTextBearingUnits;
    private TextView mTextSpeedData;
    private TextView mTextSpeedUnits;
    private TextView mTextAccuracyData;
    private TextView mTextAccuracyUnits;
    private TextView mTextCurrentTimeData;
    private TextView mTextStartTimeData;
    private TextView mTextEndTimeData;
    private TextView mTextMovingTimeData;
    private TextView mTextPausedTimeData;
    private TextView mTextTotalTimeData;

    private ImageButton mBtnStartTracking;
    private ImageButton mBtnPauseTracking;
    private ImageButton mBtnStopTracking;


    /**
     * Used to get current time
     */
    private Handler mHandler;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            // Always update current time
            mCurrentTime = Calendar.getInstance().getTime();
            mTextCurrentTimeData.setText(sDateFormat.format(mCurrentTime));

            // Are we collecting data?
            if (mTrip.getState() == Trip.TripState.RUNNING ) {
                long diffInMillis = mCurrentTime.getTime() - mTrip.getTripStartTime().getTime();
                mTrip.setTotalTimeInMillis( diffInMillis - mTrip.getPausedTimeInMillis() );
                Log.d(TAG, "Total time in millis:  " + mTrip.getTotalTimeInMillis());
                Log.d(TAG, "Total paused time in millis:  " + mTrip.getPausedTimeInMillis());
            }

            // Update UI.
            // Update only if we are either RUNNING or PAUSED.
            if ( (mTrip.getState() == Trip.TripState.RUNNING) || (mTrip.getState() == Trip.TripState.PAUSED) ) {
                mTextStartTimeData.setText(sDateFormat.format(mTrip.getTripStartTime()));
                mTextMovingTimeData.setText(DateUtils.formatElapsedTime(mTrip.getTotalTimeInMillis() / 1000));
                mTextTotalTimeData.setText(DateUtils.formatElapsedTime( (mCurrentTime.getTime() - mTrip.getTripStartTime().getTime() ) /  1000 ) );
                mTextPausedTimeData.setText(DateUtils.formatElapsedTime(mTrip.getPausedTimeInMillis() / 1000));
            }
            // If paused, update paused time.
            if (mTrip.getState() == Trip.TripState.PAUSED) {
                long pausedTime = mTrip.getPausedTimeInMillis() + mCurrentTime.getTime() - mTrip.getPausedTime().getTime();
                mTextPausedTimeData.setText(DateUtils.formatElapsedTime(pausedTime / 1000));
            }

            // Wait just a gosh darn second
            mHandler.postDelayed(mRunnable,TIME_DELAY);
        }
    };


    /**
     * Required empty public constructor
     */
    public TrackingFragment() {
        // Pass
    }

    public static TrackingFragment newInstance() {
        return new TrackingFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Register event bus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//       setHasOptionsMenu(true);

        // Get Shared Preferences
        getSharedPreferences();

        // Get current time
        mCurrentTime = Calendar.getInstance().getTime();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tracking, container, false);

        // Trip name/detail
        mTextTripTitle = view.findViewById(R.id.textTripTitle);
        mTextTripTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTrip.getState() != Trip.TripState.STOPPED ) {
                    showTripDetailDialog();
                } else {
                    Toast.makeText(getContext(), R.string.toast_new_track, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //  Data grid
        mTextLatitudeLongitude = view.findViewById(R.id.textLatitudeLongitude);

        final TextView textLatitudeLabel = view.findViewById(R.id.textLatitudeLabel);
        mTextLatitudeUnits = view.findViewById(R.id.textLatitudeUnits);
        mTextLatitudeData = view.findViewById(R.id.textLatitudeData);

        final TextView textLongitudeLabel = view.findViewById(R.id.textLongitudeLabel);
        mTextLongitudeUnits = view.findViewById(R.id.textLongitudeUnits);
        mTextLongitudeData = view.findViewById(R.id.textLongitudeData);

        final TextView textElevationLabel = view.findViewById(R.id.textElevationLabel);
        mTextElevationUnits = view.findViewById(R.id.textElevationUnits);
        mTextElevationData = view.findViewById(R.id.textElevationData);

        final TextView textBearingLabel = view.findViewById(R.id.textBearingLabel);
        mTextBearingUnits = view.findViewById(R.id.textBearingUnits);
        mTextBearingData = view.findViewById(R.id.textBearingData);

        final TextView textSpeedLabel = view.findViewById(R.id.textSpeedLabel);
        mTextSpeedUnits = view.findViewById(R.id.textSpeedUnits);
        mTextSpeedData = view.findViewById(R.id.textSpeedData);

        final TextView textAccuracyLabel = view.findViewById(R.id.textAccuracyLabel);
        mTextAccuracyUnits = view.findViewById(R.id.textAccuracyUnits);
        mTextAccuracyData = view.findViewById(R.id.textAccuracyData);

        final TextView textCurrentTimeLabel = view.findViewById(R.id.textCurrentTimeLabel);
        mTextCurrentTimeData = view.findViewById(R.id.textCurrentTimeData);

        final TextView textStartTimeLabel = view.findViewById(R.id.textStartTimeLabel);
        mTextStartTimeData = view.findViewById(R.id.textStartTimeData);

        final TextView textEndTimeLabel = view.findViewById(R.id.textEndTimeLabel);
        mTextEndTimeData = view.findViewById(R.id.textEndTimeData);

        final TextView textTrackingTimeLabel = view.findViewById(R.id.textTrackingTimeLabel);
        mTextMovingTimeData = view.findViewById(R.id.textTrackingTimeData);

        final TextView textPausedTimeLabel = view.findViewById(R.id.textPausedTimeLabel);
        mTextPausedTimeData = view.findViewById(R.id.textPausedTimeData);

        final TextView textTotalTimeLabel = view.findViewById(R.id.textTotalTimeLabel);
        mTextTotalTimeData = view.findViewById(R.id.textTotalTimeData);

        //  Data logging buttons
        mBtnStartTracking = view.findViewById(R.id.btnStartTracking);
        mBtnPauseTracking = view.findViewById(R.id.btnPauseTracking);
        mBtnStopTracking = view.findViewById(R.id.btnStopTracking);

        // Disable all buttons
        setImageButtonState(mBtnStartTracking, false);
        setImageButtonState(mBtnPauseTracking, false);
        setImageButtonState(mBtnStopTracking, false);

        mBtnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //  Have location permissions, begin updates
                startLocationUpdates();

                // Trip paused, update paused time
                if (mTrip.getState() == Trip.TripState.PAUSED) {
                    mTrip.setPausedTimeInMillis( mTrip.getPausedTimeInMillis() + mCurrentTime.getTime() - mTrip.getPausedTime().getTime() );
                }
                // Start trip time
                else {
                    if (mTrip.getTripStartTime() == null) {
                        mTrip.setTripStartTime(Calendar.getInstance().getTime());
                    }
                    mTrip.setPausedTime(null);
                }

                // Update display with start time and update trip state
                mTextStartTimeData.setText(sDateFormat.format(mTrip.getTripStartTime()));
                setState(Trip.TripState.RUNNING);
                mTrip.setPausedTime(null);

            }
        });

        mBtnPauseTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setState(Trip.TripState.PAUSED);
                mTrip.setPausedTime(Calendar.getInstance().getTime());

                // Stop location updates.  Will be resumed in play is clicked
                stopLocationUpdates();
            }
        });

        mBtnStopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop location services
                stopLocationUpdates();

                // Display dialog to save trip details
                showTripDetailDialog();
            }
        });

        // Update UI
        updateUnits();

        // Let's try to get current location
        GPSUtils.getCurrentLocation(Objects.requireNonNull(getContext()));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check location permissions, if granted 'initApp()' will be called
        // https://github.com/permissions-dispatcher/PermissionsDispatcher/issues/90
        checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION, PermissionUtils.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if preferences have been changed
        getSharedPreferences();
        // Update UI, if unit preferences have changed
        updateUnits();
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceUtils.saveActiveTripToSharedPrefs(Objects.requireNonNull(getContext()), mTrip);
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        // Unregister event bus, perform before calling super.onDetach()
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // We don't use savedInstanceState but store state in preference
        PreferenceUtils.saveActiveTripToSharedPrefs(Objects.requireNonNull(getContext()), mTrip);
        super.onSaveInstanceState(outState);
    }


    /**
     * Initialize app stuff goes here
     */
    private void initApp() {
        // Initialize GPS location services
        initializeLocationServices();

        // We don't use savedInstanceState but store state in preference
        mTrip = PreferenceUtils.getActiveTripFromSharedPrefs(Objects.requireNonNull(getContext()));
        if (mTrip == null) {
            // Create fragment state container
            mTrip = new Trip();
            mTrip.setState(Trip.TripState.NOT_STARTED);
        } else {
            if ((mTrip.getState() == Trip.TripState.RUNNING)
                    || (mTrip.getState() == Trip.TripState.PAUSED)) {
                startLocationUpdates();
            }
        }

        // Start timer.  Updates current time and once trip starts updates data
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, TIME_DELAY);

        // Enable/disable buttons
        updateImageButtons();

    }

    /**
     * Get return state from DialogFragment
     * @param save boolean
     */
    @Override
    public void onFinishDialog(boolean save) {
        if (save) {
            // Save data
            saveTrip();

            // Clear active trip from shared preferences
            PreferenceUtils.clearActiveTripFromSharedPrefs(Objects.requireNonNull(getContext()));
        }
    }

    private void saveTrip() {
        // Set trip end time
        mTrip.setTripEndTime(Calendar.getInstance().getTime());

        // Write to database
        //  TODO:  Write to database

        // Update UI
        mTextEndTimeData.setText(sDateFormat.format(mTrip.getTripEndTime()));

        // Create empty trip, initialize, and save to shared preferences.  This
        // has the effect of clearing the preference.
        mTrip = new Trip();
        // Clear state
        setState(Trip.TripState.NOT_STARTED);
//        mTrip.setState(Trip.TripState.STOPPED);
        mTrip.setPausedTimeInMillis(0);
        PreferenceUtils.saveActiveTripToSharedPrefs(Objects.requireNonNull(getContext()), mTrip);

        // Enable/Disable image buttons
        updateImageButtons();

    }

    /**
     * Initialize location services if permissions granted
     */
    private void initializeLocationServices() {
        mGPSServiceIntent = new Intent(getContext(), GPSService.class);
    }

    /**
     * Begin location updates for trip
     */
    private void startLocationUpdates() {
        if (mGPSServiceIntent != null) {
            Objects.requireNonNull(getActivity()).startService(mGPSServiceIntent);
        }
    }

    /**
     * Stop location updates for trip
     */
    private void stopLocationUpdates() {
        if (mGPSServiceIntent != null) {
            Objects.requireNonNull(getActivity()).stopService(mGPSServiceIntent);
        }
    }

    /**
     * This method will be called when a MessageEvent is posted
     * @param locationMessageEvent LocationMessageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationMessageEvent locationMessageEvent) {
        // Get location data
        Location location = locationMessageEvent.getLocation();
        // Update screen data
        updateLocationUI(location);
    }

    /**
     * Update screen data
     * @param location  Location
     */
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateLocationUI(Location location) {

        Log.i(TAG, "updateLocationUI: " + location.toString());

        double latitude = location.getLatitude();       // In decimal degrees
        double longitude = location.getLongitude();     // In decimal degrees
        double altitude = location.getAltitude();       // In meters
        float bearing = location.getBearing();          // In degrees
        float speed = location.getSpeed();              // In meters/second over ground
        float accuracy = location.getAccuracy();        // In meters

        String unit = (mIsMetric ? " m" : " ft");
        String lat_lon = "";
        String strLatitude = "";
        String strLongitude = "";

        // Convert Coordinate Type
        switch (mCoordinateType) {
            case CoordinateType.DECIMAL_DEGREES:
                // Do nothing.  Location is in DECIMAL_DEGREES.
                strLatitude = String.valueOf(latitude);
                strLongitude = String.valueOf(longitude);
                break;
            case CoordinateType.DEGREES_MINUTES_SECONDS:
                strLatitude = CoordinateConversionUtils.latitudeAsDMS(latitude, 2);
                strLongitude = CoordinateConversionUtils.longitudeAsDMS(longitude, 2);
                break;
            case CoordinateType.DEGREES_DECIMAL_MINUTES:
                strLatitude = CoordinateConversionUtils.latitudeAsDDM(latitude, 4);
                strLongitude = CoordinateConversionUtils.longitudeAsDDM(longitude, 4);
                break;
            case CoordinateType.UTM_COORDINATES:
                lat_lon = new CoordinateConversionUtils().latLon2UTM(latitude, longitude);
                // Don't display lat/lon values
                strLatitude = strLongitude = "";
                break;
            case CoordinateType.MGRS_COORDINATES:
                lat_lon = new CoordinateConversionUtils().latLon2MGRUTM(latitude, longitude);
                // Don't display lat/lon values
                strLatitude = strLongitude = "";
                break;
        }

        //  TODO:  Need to perform QA on speed conversion
        // Convert imperial/metric/nautical
        if (mIsMetric) {
            // Convert m/s -> knots
            if (mIsNautical) {
                // knots = meters per second × 1.943844
                speed *= 1.943844;
            // Convert m/s -> km/hr
            } else {
                speed *= 3.6;
            }
        } else {
            if (mIsNautical) {
                // knots = meters per second × 1.943844
                speed *= 1.943844;
            } else {
                // Convert m/s -> mi/hr
                speed *= 2.236936;
            }
            // Convert m -> ft
            altitude *= 3.28084;
            // Convert m -> ft
            accuracy *= 3.28084;
        }



        //  If Coordinate Type is UTM or MGRS then only display full lat/lon string
        if ( (mCoordinateType == CoordinateType.UTM_COORDINATES)
            || (mCoordinateType == CoordinateType.MGRS_COORDINATES) ) {
            lat_lon += " \u00B1 " + String.format("%.0f", accuracy) + unit;

        } else {
            lat_lon = strLatitude + ", " + strLongitude + " \u00B1 " + String.format("%.0f", accuracy) + unit;
        }

        // Update UI
        mTextLatitudeLongitude.setText(lat_lon);
        mTextLatitudeData.setText(strLatitude);
        mTextLongitudeData.setText(strLongitude);
        mTextElevationData.setText(String.format("%.0f", altitude));
        mTextBearingData.setText(Float.toString(bearing));
        mTextSpeedData.setText(Float.toString(speed));
        mTextAccuracyData.setText(String.format("%.0f", accuracy));

    }


    /*
     * ******************************************************************
     * UI methods
     * ******************************************************************
     */

    /**
     * Updates the measurement units
     */
    private void updateUnits() {
        if (mIsMetric) {
            mTextElevationUnits.setText(getString(R.string.textElevationUnits_Metric));
            mTextSpeedUnits.setText(getString(R.string.textSpeedUnits_Metric));
            mTextAccuracyUnits.setText(getString(R.string.textAccuracyUnits_Metric));
        } else {
            mTextElevationUnits.setText(getString(R.string.textElevationUnits_English));
            mTextSpeedUnits.setText(getString(R.string.textSpeedUnits_English));
            mTextAccuracyUnits.setText(getString(R.string.textAccuracyUnits_English));
        }

        if (mIsNautical) {
            mTextSpeedUnits.setText(getString(R.string.textSpeedUnits_Nautical));
        }

    }

    private void showTripDetailDialog() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("trip_detail_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment fragment = TripDetailFragment.newInstance();
        fragment.setTargetFragment(this, 1);
        fragment.show(ft, "trip_detail_dialog");
    }

    /**
     * Updates image button depending on trip state
     */
    private void updateImageButtons() {
        switch (mTrip.getState()) {
            case Trip.TripState.NOT_STARTED: {
                setImageButtonState(mBtnStartTracking, true);
                setImageButtonState(mBtnPauseTracking, false);
                setImageButtonState(mBtnStopTracking, false);
                break;
            }
            case Trip.TripState.RUNNING: {
                setImageButtonState(mBtnStartTracking, false);
                setImageButtonState(mBtnPauseTracking, true);
                setImageButtonState(mBtnStopTracking, true);
                break;
            }
            case Trip.TripState.PAUSED: {
                setImageButtonState(mBtnStartTracking, true);
                setImageButtonState(mBtnPauseTracking, false);
                setImageButtonState(mBtnStopTracking, true);
                break;
            }
            case Trip.TripState.STOPPED: {
                setImageButtonState(mBtnStartTracking, false);
                setImageButtonState(mBtnPauseTracking, false);
                setImageButtonState(mBtnStopTracking, false);
                break;
            }
            case Trip.TripState.NO_PERMISSIONS: {
                setImageButtonState(mBtnStartTracking, false);
                setImageButtonState(mBtnPauseTracking, false);
                setImageButtonState(mBtnStopTracking, false);
                break;
            }
            default: {
                setImageButtonState(mBtnStartTracking, false);
                setImageButtonState(mBtnPauseTracking, false);
                setImageButtonState(mBtnStopTracking, false);
                break;
            }
        }
    }

    /**
     * Enable/Disable image button
     * @param imageButton ImageButton
     * @param state boolean
     */
    private void setImageButtonState(ImageButton imageButton, boolean state) {
        imageButton.setEnabled(state);
        imageButton.setImageAlpha(imageButton.isEnabled() ? 0xFF : 0x3F);
    }

    /**
     * Wrapper function to save trip state then updates state of buttons
     * @param state int
     */
    private void setState(int state) {
        mTrip.setState(state);
        updateImageButtons();
    }

    /*
     * ******************************************************************
     * Android Shared Preferences utility methods
     * ******************************************************************
     */

    /**
     * Get required Shared Preferences
     */
    private void getSharedPreferences() {


        mSharedPrefs = PreferenceUtils.getSharedPreferences(getContext());

        mIsMetric = mSharedPrefs.isMetric();
        mIsNautical = mSharedPrefs.isNautical();
        mCoordinateType = mSharedPrefs.getCoordinateType();

        // Feature supporting this has not been implemented
//        mCoordinateDatum = prefs.getCoordinateDatum();
    }

    /*
     * ******************************************************************
     * Android Permissions utility methods
     * ******************************************************************
     */

    private void checkPermissions(final String permission, final int resultCode) {
        PermissionUtils.checkPermission(Objects.requireNonNull(getActivity()), permission,
                new PermissionUtils.PermissionAskListener() {
                    @Override
                    public void onNeedPermission() {
                        requestPermissions(new String[]{permission},resultCode);
                    }
                    @Override
                    public void onPermissionPreviouslyDenied() {
                        // Show a dialog explaining permission and then request permission
                        PermissionUtils.displayPermissionsRequestDialog(getActivity(),
                                permission,
                                resultCode,
                                getString(R.string.dialog_msg_storage_permissions_required)
                                );
                    }
                    @Override
                    public void onPermissionDisabled() {
                        PermissionUtils.displayAppPermissionDialog(getActivity());
                    }
                    @Override
                    public void onPermissionGranted() {
                        //  Init app
                        initApp();
                    }
                });
    }

}
