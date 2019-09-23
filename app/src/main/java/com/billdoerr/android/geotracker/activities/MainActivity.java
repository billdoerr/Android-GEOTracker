package com.billdoerr.android.geotracker.activities;

import com.billdoerr.android.geotracker.fragments.MapsFragment;

import androidx.fragment.app.Fragment;

/**
 * About this application activity which extends from BaseActivity.
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected Fragment createFragment() {
        return MapsFragment.newInstance();
    }
}
