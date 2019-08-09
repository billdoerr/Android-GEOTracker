package com.billdoerr.android.geotracker.activities;

import android.content.pm.PackageManager;
import android.util.Log;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.fragments.TripListFragment;
import com.billdoerr.android.geotracker.utils.GlobalVariables;

import androidx.fragment.app.Fragment;

public class TripsActivity extends BaseActivity {

    private static final String TAG = "TripsActivity";

    @Override
    protected Fragment createFragment() {
        return TripListFragment.newInstance();
    }

}
