package com.billdoerr.android.geotracker.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.model.TripDetails;
import com.billdoerr.android.geotracker.database.repo.TripDetailsRepo;
import com.billdoerr.android.geotracker.utils.CoordinateConversionUtils;
import com.billdoerr.android.geotracker.utils.SharedPreferencesUtils;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

@SuppressWarnings("FieldCanBeLocal")
public class TrackDetailFragment extends Fragment {

    private static final String ARGS_TRIP = "trip";

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

    private Trip mTrip;
    private List<TripDetails> mTripDetails;
    private TextView mTextStartLocation;
    private TextView mTextEndLocation;

    // Preference settings
    private static boolean mIsMetric = false;
    private static int mCoordinateType;

    /**
     * Required empty public constructor
     */
    public TrackDetailFragment() {
        // Pass
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        // Get trip data
        Bundle args = getArguments();
        mTrip = (Trip) Objects.requireNonNull(args).getSerializable(ARGS_TRIP);

        // Get needed shared preferences
        getSharedPreferences();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_track_detail, container, false);

        mTextStartLocation = view.findViewById(R.id.textStartLocation);
        mTextEndLocation = view.findViewById(R.id.textEndLocation);

        TextView textTripTitle = view.findViewById(R.id.textTripTitle);
        TextView textStartTime = view.findViewById(R.id.textStartTimeData);
        TextView textEndTime = view.findViewById(R.id.textEndTimeData);
        TextView textMovingTime = view.findViewById(R.id.textTrackingTimeData);
        TextView textPausedTime = view.findViewById(R.id.textPausedTimeData);
        TextView textTotalTime = view.findViewById(R.id.textTotalTimeData);

        if (mTrip != null) {
            textTripTitle.setText(mTrip.getName());

            textStartTime.setText(sDateFormat.format(mTrip.getStartTime()));
            textEndTime.setText(sDateFormat.format(mTrip.getEndTime()));

            textMovingTime.setText(DateUtils.formatElapsedTime(( mTrip.getTotalTimeInMillis() - mTrip.getPausedTimeInMillis() )/1000) );
            textPausedTime.setText(DateUtils.formatElapsedTime(mTrip.getPausedTimeInMillis() / 1000) );
            textTotalTime.setText(DateUtils.formatElapsedTime(mTrip.getTotalTimeInMillis() / 1000) );

            // Update UI
            updateUI();

        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Change the toolbar title text
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle(R.string.fragment_tile_track_detail);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if preferences have been changed
        getSharedPreferences();

        // Update UI, if unit preferences have changed
        updateUI();

    }

    // We are disabling the options menu in this fragment.  Must also set
    // setHasOptionsMenu(true); in onCreate()
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.clear();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Pass
    }

    /*
    * Update UI, if preferences change
     */
    private void updateUI() {
        mTripDetails = TripDetailsRepo.getTripDetails(mTrip.getId());
        String s;
        if ( (mTripDetails != null) && (!mTripDetails.isEmpty()) ) {
            s = formatLocation(mTripDetails.get(0));
            mTextStartLocation.setText(s);
            s = formatLocation(mTripDetails.get(mTripDetails.size()-1));
            mTextEndLocation.setText(s);
        }
    }

    /**
     * Update screen data
     * @param tripDetails  TripDetails
     */
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private String formatLocation(TripDetails tripDetails) {

        double latitude = tripDetails.getLatitude();       // In decimal degrees
        double longitude = tripDetails.getLongitude();     // In decimal degrees
        double altitude = tripDetails.getAltitude();        // In meters

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
        if (!mIsMetric) {
            // Convert m -> ft
            altitude = CoordinateConversionUtils.mToFt(altitude);
        }

        //  If Coordinate Type is UTM or MGRS then only display full lat/lon string
        if ( (mCoordinateType == CoordinateType.UTM_COORDINATES)
                || (mCoordinateType == CoordinateType.MGRS_COORDINATES) ) {
            lat_lon += " \u00B1 " + String.format("%.0f", altitude) + unit;

        } else {
            lat_lon = strLatitude + ", " + strLongitude + " \u00B1 " + String.format("%.0f", altitude) + unit;
        }

        return lat_lon;

    }

    /**
     * Get required Shared Preferences
     */
    private void getSharedPreferences() {
        SharedPreferencesUtils sharedPrefs = PreferenceUtils.getSharedPreferences(Objects.requireNonNull(getContext()));
        mIsMetric = sharedPrefs.isMetric();
        mCoordinateType = sharedPrefs.getCoordinateType();
        // Feature supporting this has not been implemented
//        mCoordinateDatum = prefs.getCoordinateDatum();
    }

}
