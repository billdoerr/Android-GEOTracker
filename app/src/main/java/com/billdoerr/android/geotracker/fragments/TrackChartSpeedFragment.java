package com.billdoerr.android.geotracker.fragments;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class TrackChartSpeedFragment extends Fragment {

    private static final String ARGS_TRIP = "trip";
    private static final String SAVED_XAXIS_DISTANCE = "xaxis";

    private Trip mTrip;
    private LineChart mLineChart;
    private TextView mTextProfile;
    private boolean mXaxisDistance;

    // Preference settings
    private static boolean mIsMetric = false;
    private static boolean mIsNautical = false;

    /**
     * Required empty public constructor
     */
    public TrackChartSpeedFragment() {
        // Pass
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mXaxisDistance = true;

        setHasOptionsMenu(true);

        // Get trip data
        Bundle args = getArguments();
        mTrip = (Trip) Objects.requireNonNull(args).getSerializable(ARGS_TRIP);

        // X-Axis flag
        if(savedInstanceState != null) {
            mXaxisDistance = savedInstanceState.getBoolean(SAVED_XAXIS_DISTANCE);
        }

        // Get needed shared preferences
        getSharedPreferences();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_track_line_chart, container, false);

        mTextProfile = view.findViewById(R.id.textProfile);
        setTextProfile(mTextProfile);

        // Charts
        mLineChart = view.findViewById(R.id.lineChart);
        drawChart(mLineChart);

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

        // If preferences change update charts
        drawChart(mLineChart);
        setTextProfile(mTextProfile);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(SAVED_XAXIS_DISTANCE, mXaxisDistance);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_chart_x_axis, menu);
        if (mXaxisDistance) {
            menu.findItem(R.id.action_chart_x_axis_distance).setChecked(true);
        } else {
            menu.findItem(R.id.action_chart_x_axis_time).setChecked(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_chart_x_axis_distance:
                mXaxisDistance = true;
                item.setChecked(true);
                drawChart(mLineChart);
                setTextProfile(mTextProfile);
                return true;
            case R.id.action_chart_x_axis_time:
                mXaxisDistance = false;
                item.setChecked(true);
                drawChart(mLineChart);
                setTextProfile(mTextProfile);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Draws the LineChart
     * @param lineChart LineChart
     */
    private void drawChart(LineChart lineChart) {
        // Get/set chart data
        ArrayList<Entry> listData = getListData();
        LineDataSet lineDataSet = new LineDataSet(listData, getString(R.string.text_chart_profile_speed));
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        // Disable touch gestures
        lineChart.setTouchEnabled(false);

        // Chart formatting
        lineDataSet.setColors(Color.BLACK);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(8f);

        // Disable chart legend
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        // Set chart description text
        Description description = lineChart.getDescription();
        description.setEnabled(true);
        description.setTextSize(18f);
        description.setText(getChartDescription());

        // Refresh chart
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

    }

    /**
     * Set chart description
     */
    private String getChartDescription() {
        String s;
        if (mXaxisDistance) {
            if (mIsMetric) {
                s = getString(R.string.text_chart_distance_metric);
            } else {
                s = getString(R.string.text_chart_distance_english);
            }
        } else {
            s = getString(R.string.text_chart_time);
        }
        return s;
    }

    /**
     * Update profile text and chart description
     * @param textView TextView
     */
    private void setTextProfile(TextView textView) {
        String s = getString(R.string.text_chart_profile_speed);
        if (mIsMetric) {
            s = s + " (" + getString(R.string.text_chart_speed_metric) + ")";
        } else if (mIsNautical) {
            s = s + " (" + getString(R.string.text_chart_speed_nautical) + ")";
        } else {
            s = s + " (" + getString(R.string.text_chart_speed_english) + ")";
        }
        textView.setText(s);
    }

    /**
     * Returns ListData for use by LineChart
     * @return ArrayList<Entry>
     */
    /*
     * This library does not support drawing LineChart data from an Entry
     * list not sorted by the x-position of the entries in ascending manner.
     */
    private ArrayList<Entry> getListData() {
        ArrayList<Entry> listData = new ArrayList<>();
        List<TripDetails> tripDetails = TripDetailsRepo.getTripDetails(mTrip.getId());
        float totalDistance = 0;
        float xAxis;
        for (int i=0; i<tripDetails.size() - 2; i++) {
            float distance = getDistance(
                    tripDetails.get(i).getLatitude(),
                    tripDetails.get(i).getLongitude(),
                    tripDetails.get(i+1).getLatitude(),
                    tripDetails.get(i+1).getLongitude()
            );

            // Convert to seconds
            double timeDelta = ( tripDetails.get(i+1).getTimeStamp() - tripDetails.get(i).getTimeStamp() )/1000;

            // Speed is now meters per second
            float speed = distance/(float)timeDelta;

            if  ( (mIsMetric) && (!mIsNautical) ) {
                // Converts meters per second to kilometers per hour
                speed = CoordinateConversionUtils.mpsToKmHr(speed);
            } else if (mIsNautical) {
                // Converts meters per second to kilometers per hour
                speed = CoordinateConversionUtils.mpsToKnots(speed);
            } else {
                // Converts meters per second to miles per hour
                speed = CoordinateConversionUtils.mpsToMph(speed);
            }

            // X-Axis
            float time = tripDetails.get(i).getTimeStamp() - tripDetails.get(0).getTimeStamp();
            time *= 1.66667e-5;

            // Distance unit conversion already applied
            totalDistance += distance;

            if (mXaxisDistance) {
                // Distance will be the -axis
//                xAxis = totalDistance;
                if (mIsMetric) {
                    // Convert meters to kilometers
                    xAxis = totalDistance/1000f;
                }
                else {
                    xAxis = CoordinateConversionUtils.mToMiles(totalDistance);
                }
            } else {
                // Time in minutes will be the x-axis
                xAxis = time;
            }

            listData.add( new Entry( xAxis, speed) );
        }

        return listData;
    }

    /**
     * Computes the approximate distance in meters between two locations,
     * and optionally the initial and final bearings of the shortest path between them.
     *
     * @param startLatitude double Starting latitude
     * @param startLongitude double Starting longitude
     * @param endLatitude double Ending latitude
     * @param endLongitude double Ending longitude
     * @return float Distance between two geopoints
     */
    private float getDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] distance = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, distance);
        return distance[0];
    }

    /**
     * Get required Shared Preferences
     */
    private void getSharedPreferences() {
        SharedPreferencesUtils sharedPrefs = PreferenceUtils.getSharedPreferences(Objects.requireNonNull(getContext()));
        mIsMetric = sharedPrefs.isMetric();
        mIsNautical = sharedPrefs.isNautical();
    }

}
