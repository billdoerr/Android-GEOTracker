package com.billdoerr.android.geotracker.activities;

import androidx.fragment.app.Fragment;

import com.billdoerr.android.geotracker.fragments.MapsFragment;

public class MapsActivity extends BaseActivity {

    private static final String TAG = "MapsActivity";

    @Override
    protected Fragment createFragment() {
        return MapsFragment.newInstance();
    }

}
