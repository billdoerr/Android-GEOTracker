package com.billdoerr.android.geotracker.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.MeasureFormat;
import android.icu.util.Calendar;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.icu.util.ULocale;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.services.GPSReceiver;
import com.billdoerr.android.geotracker.services.LocationMessageEvent;
import com.billdoerr.android.geotracker.utils.GlobalVariables;
import com.billdoerr.android.geotracker.utils.PermissionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class TripDataLoggingFragment extends Fragment {

    private static final String TAG = "TripDataLoggingFragment";

    private static final String TRIP_STATE = "bundle_trip_state";
    private static final String FRAGMENT_STATE = "bundle_fragment_state";
    private static final long TIME_DELAY = 1000;
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat( "hh:mm:ss a" , Locale.US);

    private GPSReceiver mGPSReceiver;
    private Trip mTrip;

    //  State stuff
    @interface TripState {
        int NO_PERMISSIONS = -99;
        int NOT_STARTED = -1;
        int RUNNING = 0;
        int PAUSED =  1;
        int STOPPED = 2;
    }
    private static class FragmentState implements Serializable {
        int  State;
        Date CurrentTime;
        Date PausedTime;
        long TotalTimeInMillis;
        long PausedTimeInMillis;
    }
    private FragmentState mFragmentState;


    // UI widgets
    private TextView mTextLatitudeLongitude;
    private TextView mTextLatitudeData;
    private TextView mTextLongitudeData;
    private TextView mTextElevationData;
    private TextView mTextBearingData;
    private TextView mTextSpeedData;
    private TextView mTextAccuracyData;
    private TextView mTextCurrentTimeData;
    private TextView mTextStartTimeData;
    private TextView mTextEndTimeData;
    private TextView mTextTripTimeData;
    private TextView mTextTotalTimeData;

    private ImageButton mBtnStartLogging;
    private ImageButton mBtnPauseLogging;
    private ImageButton mBtnStopLogging;

    /**
     * Used to get current time
     */
    private Handler mHandler;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            // Always update current time
            mFragmentState.CurrentTime = Calendar.getInstance().getTime();
            mTextCurrentTimeData.setText(sDateFormat.format(mFragmentState.CurrentTime));

            // Are we collecting data
            if (mFragmentState.State == TripState.RUNNING ) {
                long diffInMillis = mFragmentState.CurrentTime.getTime() - mTrip.getTripStartTime().getTime();
                mFragmentState.TotalTimeInMillis = diffInMillis - mFragmentState.PausedTimeInMillis;
                Log.d(TAG, "Total time in millis:  " + mFragmentState.TotalTimeInMillis);
                Log.d(TAG, "Total paused time in millis:  " + mFragmentState.PausedTimeInMillis);
            }

            // Update only if we are either RUNNING or PAUSED
            if ( (mFragmentState.State == TripState.RUNNING) || (mFragmentState.State == TripState.PAUSED) ) {
                // Update UI
                mTextStartTimeData.setText(sDateFormat.format(mTrip.getTripStartTime()));
                mTextTripTimeData.setText(DateUtils.formatElapsedTime(mFragmentState.TotalTimeInMillis / 1000));
                mTextTotalTimeData.setText(DateUtils.formatElapsedTime( (mFragmentState.CurrentTime.getTime() - mTrip.getTripStartTime().getTime() ) /  1000 ) );
            }

            // Wait just a gosh darn second
            mHandler.postDelayed(mRunnable,TIME_DELAY);
        }
    };


    /**
     * Required empty public constructor
     */
    public TripDataLoggingFragment() {
        // Pass
    }

    public static TripDataLoggingFragment newInstance() {
        return new TripDataLoggingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create fragment state container
        mFragmentState = new FragmentState();
        mFragmentState.State = TripState.NOT_STARTED;

//       setHasOptionsMenu(true);

        // Get args
        Bundle args = getArguments();
        if (args != null) {
            mTrip = (Trip) args.getSerializable(GlobalVariables.ARGS_TRIP);
        }
        else {
            mTrip = new Trip();
        }

        /* If the Fragment was destroyed in between (screen rotation), we need to recover the savedState first */
        /* However, if it was not, it stays in the instance from the last onDestroyView() and we don't want to overwrite it */
        /* https://stackoverflow.com/questions/15313598/once-for-all-how-to-correctly-save-instance-state-of-fragments-in-back-stack */
        if (savedInstanceState != null) {
            mTrip = (Trip) savedInstanceState.getSerializable(TRIP_STATE);
            mFragmentState = (FragmentState) savedInstanceState.getSerializable(FRAGMENT_STATE);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_data_logging, container, false);

        //  Data grid
        mTextLatitudeLongitude = (TextView) view.findViewById(R.id.textLatitudeLongitude);

        final TextView mTextLatitude = (TextView) view.findViewById(R.id.textLatitude);
        final TextView mTextLatitudeUnits = (TextView) view.findViewById(R.id.textLatitudeUnits);
        mTextLatitudeData = (TextView) view.findViewById(R.id.textLatitudeData);

        final TextView mTextLongitude = (TextView) view.findViewById(R.id.textLongitude);
        final TextView mTextLongitudeUnits = (TextView) view.findViewById(R.id.textLongitudeUnits);
        mTextLongitudeData = (TextView) view.findViewById(R.id.textLongitudeData);

        final TextView mTextElevation = (TextView) view.findViewById(R.id.textElevation);
        final TextView mTextElevationUnits = (TextView) view.findViewById(R.id.textElevationUnits);
        mTextElevationData = (TextView) view.findViewById(R.id.textElevationData);

        final TextView mTextBearing = (TextView) view.findViewById(R.id.textBearing);
        final TextView mTextBearingUnits = (TextView) view.findViewById(R.id.textBearingUnits);
        mTextBearingData = (TextView) view.findViewById(R.id.textBearingData);

        final TextView mTextSpeed = (TextView) view.findViewById(R.id.textSpeed);
        final TextView mTextSpeedUnits = (TextView) view.findViewById(R.id.textSpeedUnits);
        mTextSpeedData = (TextView) view.findViewById(R.id.textSpeedData);

        final TextView mTextAccuracy = (TextView) view.findViewById(R.id.textAccuracy);
        final TextView mTextAccuracyUnits = (TextView) view.findViewById(R.id.textAccuracyUnits);
        mTextAccuracyData = (TextView) view.findViewById(R.id.textAccuracyData);

        final TextView mTextCurrentTime = (TextView) view.findViewById(R.id.textCurrentTime);
        mTextCurrentTimeData = (TextView) view.findViewById(R.id.textCurrentTimeData);

        final TextView mTextStartTime = (TextView) view.findViewById(R.id.textStartTime);
        mTextStartTimeData = (TextView) view.findViewById(R.id.textStartTimeData);

        final TextView mTextEndTime = (TextView) view.findViewById(R.id.textEndTime);
        mTextEndTimeData = (TextView) view.findViewById(R.id.textEndTimeData);

        final TextView mTextTripTime = (TextView) view.findViewById(R.id.textTripTime);
        mTextTripTimeData = (TextView) view.findViewById(R.id.textTripTimeData);

        final TextView mTextTotalTime = (TextView) view.findViewById(R.id.textTotalTime);
        mTextTotalTimeData = (TextView) view.findViewById(R.id.textTotalTimeData);

        //  Data logging buttons
        mBtnStartLogging = (ImageButton) view.findViewById(R.id.btnStartLogging);
        mBtnPauseLogging = (ImageButton) view.findViewById(R.id.btnPauseLogging);
        mBtnStopLogging = (ImageButton) view.findViewById(R.id.btnStopLogging);

        mBtnStartLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {

                    //  Have location permissions, begin updates
                    startLocationUpdates();

                    if (mFragmentState.State == TripState.PAUSED) {
                        mFragmentState.PausedTimeInMillis += mFragmentState.CurrentTime.getTime() - mFragmentState.PausedTime.getTime();
                    } else {
                        if (mTrip.getTripStartTime() == null) {
                            mTrip.setTripStartTime(Calendar.getInstance().getTime());
                        }
                        mFragmentState.PausedTime = null;
                    }

                    mTextStartTimeData.setText(sDateFormat.format(mTrip.getTripStartTime()));
                    mFragmentState.State = TripState.RUNNING;
                    mFragmentState.PausedTime = null;

                // No permissions
                } else {
                    mFragmentState.State = TripState.NO_PERMISSIONS;
                }

                // Enable/Disable image buttons
                updateImageButtons();

            }
        });

        mBtnPauseLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentState.State = TripState.PAUSED;
                mFragmentState.PausedTime = Calendar.getInstance().getTime();
                mGPSReceiver.stopUpdates();

                // Enable/Disable image buttons
                updateImageButtons();
            }
        });

        mBtnStopLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTrip.setTripEndTime(Calendar.getInstance().getTime());

                // Update UI
                mTextEndTimeData.setText(sDateFormat.format(mTrip.getTripEndTime()));

                mFragmentState.State = TripState.STOPPED;
                mFragmentState.PausedTimeInMillis = 0;
                mGPSReceiver.stopUpdates();

                // Enable/Disable image buttons
                updateImageButtons();
            }
        });

        // Start timer.  Updates current time and once trip starts updates data
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, TIME_DELAY);

        //  Initialize GPS and get current GPS location
        if (checkPermission()) {
            mGPSReceiver = GPSReceiver.initializeInstance(getContext());
            updateLocation(mGPSReceiver.getCurrentLocation());
        // No permissions
        } else {
            mFragmentState.State = TripState.NO_PERMISSIONS;
        }

        // Enable/Disable image buttons
        updateImageButtons();


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mTrip = (Trip) savedInstanceState.getSerializable(TRIP_STATE);
            mFragmentState = (FragmentState) savedInstanceState.getSerializable(FRAGMENT_STATE);

            // Get last known location
            updateLocation(mGPSReceiver.getCurrentLocation());

            // Enable/Disable image buttons
            updateImageButtons();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TRIP_STATE, mTrip);
        outState.putSerializable(FRAGMENT_STATE, mFragmentState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Register event bus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        // Unregister event bus, perform before calling super.onDetach()
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

    /**
     * Initialize location services if permissions granted
     */
    private void intializeLocationServices() {
        mGPSReceiver = GPSReceiver.initializeInstance(getContext());
        updateLocation(mGPSReceiver.getCurrentLocation());
        mFragmentState.State = TripState.NOT_STARTED;
        updateImageButtons();

    }

    /**
     * Begin location updates for trip
     */
    private void startLocationUpdates() {
        mGPSReceiver.startUpdates();
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationMessageEvent locationMessageEvent) {

        // Get location data
        Location location = locationMessageEvent.getLocation();

        // Update screen data
        updateLocation(location);

    }

    /**
     * Update screen data
     * @param location  Location
     */
    private void updateLocation(Location location) {

        MeasureFormat fmtFr = MeasureFormat.getInstance(ULocale.FRENCH, MeasureFormat.FormatWidth.SHORT);
        Measure measure = new Measure(23, MeasureUnit.CELSIUS);

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        float bearing = location.getBearing();
        float speed = location.getSpeed();
        float accuracy = location.getAccuracy();

        String tmp = String.format("%.5f", latitude) + ", " + String.format("%.5f", longitude) + "   +/- " + String.format("%.0f", accuracy);

        mTextLatitudeLongitude.setText(tmp);
        mTextLatitudeData.setText(String.format("%.5f", latitude));
        mTextLongitudeData.setText(String.format("%.5f", longitude));
        mTextElevationData.setText(String.format("%.0f", altitude));
        mTextBearingData.setText(Float.toString(bearing));
        mTextSpeedData.setText(Float.toString(speed));
        mTextAccuracyData.setText(String.format("%.0f", accuracy));

    }


    /**
     * ******************************************************************
     * Utility methods
     * ******************************************************************
     */

    /**
     * Updates image button depending on trip state
     */
    private void updateImageButtons() {
        switch (mFragmentState.State) {
            case TripState.NOT_STARTED: {
                setImageButtonState(mBtnStartLogging, true);
                setImageButtonState(mBtnPauseLogging, false);
                setImageButtonState(mBtnStopLogging, false);
                break;
            }
            case TripState.RUNNING: {
                setImageButtonState(mBtnStartLogging, false);
                setImageButtonState(mBtnPauseLogging, true);
                setImageButtonState(mBtnStopLogging, true);
                break;
            }
            case TripState.PAUSED: {
                setImageButtonState(mBtnStartLogging, true);
                setImageButtonState(mBtnPauseLogging, false);
                setImageButtonState(mBtnStopLogging, true);
                break;
            }
            case TripState.STOPPED: {
                setImageButtonState(mBtnStartLogging, false);
                setImageButtonState(mBtnPauseLogging, false);
                setImageButtonState(mBtnStopLogging, false);
                break;
            }
            case TripState.NO_PERMISSIONS: {
                setImageButtonState(mBtnStartLogging, false);
                setImageButtonState(mBtnPauseLogging, false);
                setImageButtonState(mBtnStopLogging, false);
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
     * ******************************************************************
     * Android Permissions utility methods
     * ******************************************************************
     */

    /**
     * https://github.com/shikto1/RuntimePermissionInMarsmallow/blob/master/app/src/main/java/shishirstudio/runtimepermissioninandroid/MainActivity.java
     * Request app permissions.
     * @return boolean:  True if permissions granted.
     */
    private boolean checkPermission() {

        boolean authorized = false;

        // Determine whether you have been granted a particular permission.
        // Returns PERMISSION_GRANTED if you have the permission, or PERMISSION_DENIED if not.
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

           Log.d(TAG, "checkSelfPermission" + ":  True");

            // Returns TRUE, if
            // The permission asked before but the user denied without checking ‘Never ask again’.
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.i(TAG, "shouldShowRequestPermissionRationale" + ":  True");
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                displayPermissionsRequestDialog();

            } else
            // Returns FALSE, if
            // The permission is requested first time.
            // The permission asked before but the user denied with checking ‘Never ask again’.
            {
               Log.d(TAG, "shouldShowRequestPermissionRationale" + ":  False");

                // If first time asking
                if (PermissionUtils.isFirstTimeAsking(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        GlobalVariables.PREF_KEY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)) {

                   Log.d(TAG, "First time asking." );

                    PermissionUtils.setFirstTimeAsking(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            GlobalVariables.PREF_KEY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION, false);

                    // No explanation needed; request the permission.
                    // The onRequestPermissionsResult callback method gets the result of the request.
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            GlobalVariables.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                } else
                // Permission disabled by device policy or user denied permanently. Show proper error message
                {
                   Log.d(TAG, "Permission disable by device policy or user denied permanently. Show proper error message." );

                    // Request user modify app permissions
                    displayAppPermissionDialog();
                }
            }
        } else {
            // Permission has already been granted
            Log.i(TAG, getString(R.string.msg_location_permissions__already_granted));

            authorized = true;
        }

        Log.i(TAG, getString(R.string.msg_has_location_permissions) + ": " +  authorized);
        return authorized;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case GlobalVariables.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted, yay!
                    Log.i(TAG, getString(R.string.msg_has_location_permissions  ));

                    // Init GPS
                    intializeLocationServices();

                } else {
                    // Permission denied, boo!
                    // Disable the functionality that depends on this permission.
                    Log.i(TAG, getString(R.string.msg_no_location_permissions));
                }
                return;
            }
        }
    }

    /**
     * Display dialog asking why we need permissions.  If Ok pressed, intent sent to open
     * app permissions settings to have user manually enable permissions.
     */
    private void displayAppPermissionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.dialog_msg_grant_permissions));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.btn_permit_manually), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(getString(R.string.btn_cancel), null);
        builder.show();
    }

    /**
     * Show an alert dialog here with request explanation
     */
    private void displayPermissionsRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.dialog_msg_location_permissions_required));
        builder.setTitle(getString(R.string.dialog_title_please_grant_permissions));
        builder.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        GlobalVariables.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });
        builder.setNeutralButton(getString(R.string.btn_cancel),null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
