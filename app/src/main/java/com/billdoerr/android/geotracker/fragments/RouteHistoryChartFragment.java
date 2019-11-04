package com.billdoerr.android.geotracker.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.repo.TripRepo;
import com.billdoerr.android.geotracker.utils.SharedPreferencesUtils;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class RouteHistoryChartFragment extends Fragment {

    private static final String ARGS_TRIP = "trip";
    private static final String SAVED_XAXIS_DISTANCE = "xaxis";

    private Trip mTrip;
    private BarChart mBarChart;
    private TextView mTextProfile;
    private boolean mXaxisDistance;

    // Preference settings
    private static boolean mIsMetric = false;
    private static boolean mIsNautical = false;

    /**
     * Required empty public constructor
     */
    public RouteHistoryChartFragment() {
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
        if (savedInstanceState != null) {
            mXaxisDistance = savedInstanceState.getBoolean(SAVED_XAXIS_DISTANCE);
        }

        // Get needed shared preferences
        getSharedPreferences();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route_history_bar_chart, container, false);

        mTextProfile = view.findViewById(R.id.textProfile);
        setTextProfile(mTextProfile);

        // Charts
        mBarChart = view.findViewById(R.id.barChart);
        drawChart(mBarChart);

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
        drawChart(mBarChart);
        setTextProfile(mTextProfile);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(SAVED_XAXIS_DISTANCE, mXaxisDistance);
        super.onSaveInstanceState(outState);
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

    /**
     * Draws the LineChart
     * @param barChart LineChart
     */
    private void drawChart(BarChart barChart) {
        // Get/set chart data
        ArrayList<BarEntry> listData = getListData();
        BarDataSet barDataSet = new BarDataSet(listData, getString(R.string.text_chart_profile_route_history));
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Disable touch gestures
        barChart.setTouchEnabled(false);

        // Chart formatting
        barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(8f);

        // Disable chart legend
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        // Set chart description text
        Description description = barChart.getDescription();
        description.setEnabled(true);
        description.setTextSize(18f);
        description.setText(getChartDescription());

        // Refresh chart
        barChart.notifyDataSetChanged();
        barChart.invalidate();

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
        String s = getString(R.string.text_chart_profile_route_history);
        // Chart profile
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
    private ArrayList<BarEntry> getListData() {
        ArrayList<BarEntry> listData = new ArrayList<>();
        List<Trip> trips = TripRepo.getTripsByTripName(mTrip.getName());
        for (int i=0; i<trips.size(); i++) {
            float time = trips.get(i).getTotalTimeInMillis();
            time *= 1.66667e-5;
           listData.add( new BarEntry( (float)(i), time) );
        }
        return listData;
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
