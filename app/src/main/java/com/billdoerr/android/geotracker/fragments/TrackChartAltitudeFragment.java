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

public class TrackChartAltitudeFragment extends Fragment {

    private static final String ARGS_TRIP = "trip";
    private static final String SAVED_XAXIS_DISTANCE = "xaxis";

    private Trip mTrip;
    private LineChart mLineChart;
    private TextView mTextProfile;
    private boolean mXaxisDistance;

    // Preference settings
    private static boolean mIsMetric = false;

    /**
     * Required empty public constructor
     */
    public TrackChartAltitudeFragment() {
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
        LineDataSet lineDataSet = new LineDataSet(listData, getString(R.string.text_chart_profile_altitude));
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        // Disable touch gestures
        lineChart.setTouchEnabled(false);

        // Chart formatting
//        lineDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
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
        String s = getString(R.string.text_chart_profile_altitude);
        // Chart profile
        if (mIsMetric) {
            s = s + " (" + getString(R.string.text_chart_elevation_metric) + ")";
        } else {
            s = s + " (" + getString(R.string.text_chart_elevation_english) + ")";
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
        for (int i=0; i<tripDetails.size() - 1; i++) {
            double altitude = tripDetails.get(i).getAltitude();
            float distance = getDistance(
                    tripDetails.get(i).getLatitude(),
                    tripDetails.get(i).getLongitude(),
                    tripDetails.get(i+1).getLatitude(),
                    tripDetails.get(i+1).getLongitude()
            );

            if (!mIsMetric) {
                // Convert meters to miles for altitude
                altitude = CoordinateConversionUtils.mToFt(altitude);
                distance = CoordinateConversionUtils.mToMiles(distance);
            }
            else {
                // Convert meters to kilometers
                distance /= 1000f;
            }

            // X-Axis
            float time = tripDetails.get(i).getTimeStamp() - tripDetails.get(0).getTimeStamp();
            time *= 1.66667e-5;

            // Distance unit conversion already applied
            totalDistance += distance;

            if (mXaxisDistance) {
                // Distance will be the -axis
                xAxis = totalDistance;
            } else {
                // Time in minutes will be the x-axis
                xAxis = time;
            }

            listData.add( new Entry( xAxis, (float)altitude) );
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
    }

}
