package com.billdoerr.android.geotracker.activities;

import androidx.fragment.app.Fragment;

import com.billdoerr.android.geotracker.fragments.TrackingFragment;

public class TrackingActivity extends BaseActivity {

    private static final String TAG = "TrackingActivity";

    @Override
    protected Fragment createFragment() {
        return TrackingFragment.newInstance();
    }

}

