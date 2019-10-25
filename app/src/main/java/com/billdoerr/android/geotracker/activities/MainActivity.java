package com.billdoerr.android.geotracker.activities;

import com.billdoerr.android.geotracker.fragments.MapsFragment;

import androidx.fragment.app.Fragment;

/**
 * About this application activity which extends from BaseActivity.
 */
@SuppressWarnings("unused")
public class MainActivity extends BaseActivity {

    @Override
    protected Fragment createFragment() {
        return MapsFragment.newInstance();
    }
}
