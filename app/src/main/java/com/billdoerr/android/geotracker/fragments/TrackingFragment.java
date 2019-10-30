package com.billdoerr.android.geotracker.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Route;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.repo.RouteRepo;
import com.billdoerr.android.geotracker.database.repo.TripRepo;
import com.billdoerr.android.geotracker.services.TrackingService;
import com.billdoerr.android.geotracker.utils.GPSUtils;
import com.billdoerr.android.geotracker.services.LocationMessageEvent;
import com.billdoerr.android.geotracker.utils.CoordinateConversionUtils;
import com.billdoerr.android.geotracker.utils.GeoTrackerSharedPreferences;
import com.billdoerr.android.geotracker.utils.PermissionUtils;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;
import com.billdoerr.android.geotracker.utils.ServiceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class TrackingFragment extends Fragment {

    private static final int REQUEST_CODE_TRIP_DIALOG_SAVE = 1;
    private static final int REQUEST_CODE_TRIP_DIALOG_CONTINUE = 2;

    // Indicates invalid table index
    private static final int INVALID_INDEX = -1;

    private static final String ARGS_TRIP = "trip";
    private static final String ARGS_SAVE_TRIP_NAME_TO_ROUTES = "save_trip_name_to_routes";

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

    private Intent mTrackingServiceIntent;
    private Trip mTrip;
    private Date mCurrentTime;
    private long mPausedTimeInMillis;

    // Preference settings
    private static boolean mIsMetric = false;
    private static boolean mIsNautical = false;
    private static int mCoordinateType;
    // Feature supporting this has not been implemented
//    private static int mCoordinateDatum;

    // UI widgets
    private EditText mTextTripTitle;
    private TextView mTextLatitudeLongitude;
    private TextView mTextLatitudeData;
    private TextView mTextLongitudeData;
    private TextView mTextElevationData;
    private TextView mTextElevationUnits;
    private TextView mTextBearingData;
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
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            // Always update current time
            mCurrentTime = Calendar.getInstance().getTime();
            mTextCurrentTimeData.setText(sDateFormat.format(mCurrentTime));

            // Update these UI widgets only if we are either STARTED or PAUSED.
            if ( (mTrip.getState() == Trip.TripState.STARTED) || (mTrip.getState() == Trip.TripState.PAUSED) ) {

                // Update paused time
                if (mTrip.getState() == Trip.TripState.PAUSED) {
                    mPausedTimeInMillis = mCurrentTime.getTime() - mTrip.getPausedTime();
                }
                else {
                    // Moving time
                    mTrip.setMovingTimeInMillis( mTrip.getTotalTimeInMillis() - mTrip.getPausedTimeInMillis() );
                }
                mTextPausedTimeData.setText(DateUtils.formatElapsedTime(   (mTrip.getPausedTimeInMillis() + mPausedTimeInMillis) / 1000 ) );

                // Moving time
                mTextMovingTimeData.setText(DateUtils.formatElapsedTime( ( mTrip.getMovingTimeInMillis() ) / 1000 ) );

                // Total time
                mTrip.setTotalTimeInMillis(mCurrentTime.getTime() - mTrip.getStartTime());
                mTextTotalTimeData.setText(DateUtils.formatElapsedTime(  ( mCurrentTime.getTime() - mTrip.getStartTime() ) /  1000 ) );

                // Start time
                mTextStartTimeData.setText(sDateFormat.format(mTrip.getStartTime()));

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Register event bus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);

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
        mTextTripTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Pass
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Pass
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Assign trip name
                mTrip.setName(mTextTripTitle.getText().toString());
            }
        });

        //  Data grid
        mTextLatitudeLongitude  = view.findViewById(R.id.textLatitudeLongitude);
        mTextLatitudeData       = view.findViewById(R.id.textLatitudeData);
        mTextLongitudeData      = view.findViewById(R.id.textLongitudeData);
        mTextElevationUnits     = view.findViewById(R.id.textElevationUnits);
        mTextElevationData      = view.findViewById(R.id.textElevationData);
        mTextBearingData        = view.findViewById(R.id.textBearingData);
        mTextSpeedUnits         = view.findViewById(R.id.textSpeedUnits);
        mTextSpeedData          = view.findViewById(R.id.textSpeedData);
        mTextAccuracyUnits      = view.findViewById(R.id.textAccuracyUnits);
        mTextAccuracyData       = view.findViewById(R.id.textAccuracyData);
        mTextCurrentTimeData    = view.findViewById(R.id.textCurrentTimeData);
        mTextStartTimeData      = view.findViewById(R.id.textStartTimeData);
        mTextEndTimeData        = view.findViewById(R.id.textEndTimeData);
        mTextMovingTimeData     = view.findViewById(R.id.textTrackingTimeData);
        mTextPausedTimeData     = view.findViewById(R.id.textPausedTimeData);
        mTextTotalTimeData      = view.findViewById(R.id.textTotalTimeData);

        //  Data logging buttons
        mBtnStartTracking   = view.findViewById(R.id.btnStartTracking);
        mBtnPauseTracking   = view.findViewById(R.id.btnPauseTracking);
        mBtnStopTracking    = view.findViewById(R.id.btnStopTracking);

        // Disable all buttons
        setImageButtonState(mBtnStartTracking, false);
        setImageButtonState(mBtnPauseTracking, false);
        setImageButtonState(mBtnStopTracking, false);

        mBtnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextTripTitle.getText().length() == 0) {
                    Toast.makeText(getContext(), R.string.toast_trip_name_required, Toast.LENGTH_SHORT).show();
                } else {
                    // Display dialog to save trip details. Need to have a TripId before starting location services.
                    // Tracking will started in the dialog listener 'onActivityResult'
                    // First check if active trip, is so then not dialog not displayed after pause/resume.
                    if (mTrip.getState() != Trip.TripState.PAUSED) {
                        showTripDetailDialog(REQUEST_CODE_TRIP_DIALOG_CONTINUE);
                    } else {
                        // Resume tracking location data
                        resumeTracking();
                    }
                }
            }
        });

        mBtnPauseTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pause tracking location data
                pauseTracking();
            }
        });

        mBtnStopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop location services
                stopTrackingService();

                // Display dialog to save trip details
                showTripDetailDialog(REQUEST_CODE_TRIP_DIALOG_SAVE);
            }
        });


        // Initialize Trip object
        initTrip();

        // Update UI
        updateUnits();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Change the toolbar title text
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle(R.string.fragment_title_track);
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

        // Update trip name in UI
        if (mTrip != null) {
            mTextTripTitle.setText(mTrip.getName());
            // Hide keyboard upon focus.  Thanks to:  Revisit  https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
            Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );
        }
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
        // Only save if active trip
        if ((mTrip.getState() == Trip.TripState.STARTED) || (mTrip.getState() == Trip.TripState.PAUSED)) {
            PreferenceUtils.saveActiveTripToSharedPrefs(Objects.requireNonNull(getContext()), mTrip);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode != Activity.RESULT_OK ) {
            return;
        }

        Trip trip = (Trip) data.getSerializableExtra(ARGS_TRIP);
        boolean saveTripName = data.getBooleanExtra(ARGS_SAVE_TRIP_NAME_TO_ROUTES, false);

        // Save trip to database
        if( requestCode == REQUEST_CODE_TRIP_DIALOG_SAVE) {
            // Save trip
            if (trip != null) {
                // If trip data changed apply globally
                mTrip = trip;

                if (mTrip.getState() == Trip.TripState.PAUSED) {
                    mTrip.setPausedTimeInMillis( mTrip.getPausedTimeInMillis() + mPausedTimeInMillis );
                }

                // End of the road for this trip
                setState(Trip.TripState.STOPPED);

                // Save trip name to routes if option selected
                if (saveTripName) {
                    Route route = new Route();
                    route.setActive(1);
                    route.setName(trip.getName());
                    route.setDesc(trip.getDesc());
                    route.setActivityTypeId(trip.getActivityTypeId());
                    RouteRepo.insert(route);
                }
                saveTrip();
            }
        }
        // Just get update trip details
        else if( requestCode == REQUEST_CODE_TRIP_DIALOG_CONTINUE) {
            // Begin tracking location
            int ret = insertTripIntoDatabase();
            if (ret != INVALID_INDEX) {
                startTracking();
            }
        }

    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case PermissionUtils.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initApp();
                }
                break;
        }
    }

    /**
     * Initialize app stuff goes here
     */
    private void initApp() {
        // Initialize GPS location services
        initializeLocationServices();

        // Let's try to get current location
        GPSUtils.getCurrentLocation(Objects.requireNonNull(getContext()));

        // Resume location updates if currently tracking
        if ((mTrip.getState() == Trip.TripState.STARTED) || (mTrip.getState() == Trip.TripState.PAUSED)) {
            // Set trip title
            mTextTripTitle.setText(mTrip.getName());
            // Resume location updates
            startTrackingService();
        }

        // Start timer.  Updates current time and once trip starts updates data
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, TIME_DELAY);

        // Enable/disable buttons
        updateImageButtons();

    }

    /**
     * Stop TrackingService
     */
    private void stopTrackingService() {
        if (mTrackingServiceIntent != null) {
            Objects.requireNonNull(getActivity()).stopService(mTrackingServiceIntent);
        }
    }

    /*
     * Begin tracking location data
     */
    private void startTracking() {

        // Trip paused, update paused time
        if (mTrip.getStartTime() == 0) {
            mTrip.setStartTime(Calendar.getInstance().getTimeInMillis());
        }

        // Update display with start time
        mTextStartTimeData.setText(sDateFormat.format(mTrip.getStartTime()));

        // Some initialization
        mTrip.setPausedTime(0);
        setState(Trip.TripState.STARTED);

        //  Have location permissions, begin updates
        startTrackingService();

    }

    /**
     * Resume tracking location data
     */
    private void resumeTracking() {
        if (mTrip.getState() != Trip.TripState.PAUSED) return;

        mTrip.setPausedTimeInMillis( mTrip.getPausedTimeInMillis() + mPausedTimeInMillis );
        mTrip.setPausedTime(0);
        mPausedTimeInMillis = 0;

        // Update trip state
        setState(Trip.TripState.STARTED);

        //  Resume tracking
        startTrackingService();
    }

    /**
     * Pause tracking location data
     */
    private void pauseTracking() {
        setState(Trip.TripState.PAUSED);
        mTrip.setPausedTime(Calendar.getInstance().getTimeInMillis());
        mPausedTimeInMillis = 0;

        // Stop location updates.  Will be resumed in play is clicked
        stopTrackingService();
    }

    /**
     * Initialize Trip object
     */
    private void initTrip() {
        // We don't use savedInstanceState but store state in preference
        Trip trip = PreferenceUtils.getActiveTripFromSharedPrefs(Objects.requireNonNull(getContext()));
        // No running trip, initialize trip
        if (trip != null) {
            // Assign to global variable
            mTrip = trip;
            // If active trip, start location updates
            if ((mTrip.getState() == Trip.TripState.STARTED) || (mTrip.getState() == Trip.TripState.PAUSED)) {
                // Set trip title
                mTextTripTitle.setText(mTrip.getName());
                // Resume location updates
                startTrackingService();
            }
        } else {
            // Create fragment state container
            newTrip();
        }
    }

    /**
     * Initialize Trip object
     */
    private void newTrip() {
        mTrip = new Trip();
        mTrip.setState(Trip.TripState.NOT_STARTED);
        mTrip.setPausedTime(0);
        mTrip.setMovingTimeInMillis(0);
        mTrip.setPausedTimeInMillis(0);
        mTrip.setActive(1);
        mTrip.setId(INVALID_INDEX);
        // Set default value for trip name with current date
        mTrip.setName(mCurrentTime.toString());
    }

    /**
     * Save trip to database and clear trip state.
     */
    private void saveTrip() {
        // Set trip end time
        mTrip.setEndTime(Calendar.getInstance().getTimeInMillis());

        // Write to database
        updateImageButtons();

        // Update UI
        mTextEndTimeData.setText(sDateFormat.format(mTrip.getEndTime()));

        // Save trip to database
        insertTripIntoDatabase();

        // Create empty trip, clear state, initialize.
        newTrip();

        // Save to shared preferences.  This has the effect of clearing the preference.
        PreferenceUtils.saveActiveTripToSharedPrefs(Objects.requireNonNull(getContext()), mTrip);

        // Enable/Disable image buttons
        updateImageButtons();

        // Update UI
        clearScreenData();

    }

    /**
     * Add trip to database.  Whether it performs and insert or update is based on the TripId.
     * If the trip = -1, this is an invalid database index and will perform an INSERT, else UPDATE.
     */
    private int insertTripIntoDatabase() {
        int ret;
        // Must be new trip, insert into database
        if (mTrip.getId() == INVALID_INDEX) {
            ret = TripRepo.insert(mTrip);
            mTrip.setId(ret);
        }
        // Existing trip, just update
        else {
            ret = TripRepo.update(mTrip);
        }

        // Returns -1 if error
        if (ret == INVALID_INDEX) {
            Toast.makeText(getContext(), getString(R.string.toast_database_update_error), Toast.LENGTH_SHORT).show();
        } else {
            PreferenceUtils.saveActiveTripToSharedPrefs(Objects.requireNonNull(getContext()), mTrip);
        }

        return  ret;
    }


    /**
     * Initialize location services if permissions granted
     */
    private void initializeLocationServices() {
        GPSUtils.startGPSService(getContext());
        mTrackingServiceIntent = new Intent(getContext(), TrackingService.class);
    }

    /**
     * Start TrackingService
     */
    private void startTrackingService() {
        if (mTrackingServiceIntent != null) {
            // Send locator service trip id
            PreferenceUtils.saveActiveTripToSharedPrefs(Objects.requireNonNull(getContext()), mTrip);
            if (!ServiceUtils.isMyServiceRunning(getContext(), TrackingService.class)) {
                Objects.requireNonNull(getActivity()).startService(mTrackingServiceIntent);
            }
        }
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
        // Update screen data.  The GPSService will insert data into the TripDetails table.
        updateLocationUI(location);
    }

    /**
     * Update screen data
     * @param location  Location
     */
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateLocationUI(Location location) {

        double latitude = location.getLatitude();       // In decimal degrees
        double longitude = location.getLongitude();     // In decimal degrees
        double altitude = location.getAltitude();       // In meters
        float bearing = location.getBearing();          // In degrees
        float speed = location.getSpeed();              // In meters/second over ground
        float accuracy = location.getAccuracy();        // In meters

        // Kind of a hack to update units if change in shared preferences
        getSharedPreferences();

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
            altitude /= 0.3048;
            // Convert m -> ft
            accuracy /= 0.3048;
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

    /**
     * FragmentDialog that allows the editing of the trip's details
     * @param requestCode int
     */
    private void showTripDetailDialog(int requestCode) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = Objects.requireNonNull(Objects.requireNonNull(getActivity()).getSupportFragmentManager()).beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(TripDetailFragment.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        Bundle args = new Bundle();
        args.putSerializable(ARGS_TRIP, mTrip);

        // Create and show the dialog.
        DialogFragment dialogFragment = TripDetailFragment.newInstance();
        dialogFragment.setArguments(args);
        dialogFragment.setTargetFragment(this, requestCode);
        dialogFragment.show(ft, TripDetailFragment.TAG);
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
            case Trip.TripState.STARTED: {
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
            case Trip.TripState.STOPPED:
            case Trip.TripState.NO_PERMISSIONS: {
                setImageButtonState(mBtnStartTracking, false);
                setImageButtonState(mBtnPauseTracking, false);
                setImageButtonState(mBtnStopTracking, false);
                break;
            }
        }
    }

    /**
     * This clears screen of data
     */
    private void clearScreenData() {
        mTextTripTitle.setText(mTrip.getName());
//        mTextLatitudeLongitude.setText("");
//        mTextLatitudeData.setText("");
//        mTextLongitudeData.setText("");
//        mTextElevationData.setText("");
        mTextBearingData.setText("");
        mTextSpeedData.setText("");
        mTextAccuracyData.setText("");
//        mTextCurrentTimeData.setText("");
        mTextStartTimeData.setText("");
        mTextEndTimeData.setText("");
        mTextMovingTimeData.setText("");
        mTextPausedTimeData.setText("");
        mTextTotalTimeData.setText("");

        updateUnits();
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
        GeoTrackerSharedPreferences sharedPrefs = PreferenceUtils.getSharedPreferences(Objects.requireNonNull(getContext()));
        mIsMetric = sharedPrefs.isMetric();
        mIsNautical = sharedPrefs.isNautical();
        mCoordinateType = sharedPrefs.getCoordinateType();
        // Feature supporting this has not been implemented
//        mCoordinateDatum = prefs.getCoordinateDatum();
    }

    /*
     * ******************************************************************
     * Android Permissions utility methods
     * ******************************************************************
     */

    /**
     * Request permission and presents need dialogs to grant permissions
     * @param permission String Permission being requested
     * @param resultCode int
     */
    @SuppressWarnings("SameParameterValue")
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
