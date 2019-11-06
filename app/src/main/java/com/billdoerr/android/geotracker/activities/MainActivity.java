package com.billdoerr.android.geotracker.activities;

import android.os.Bundle;

import com.billdoerr.android.geotracker.fragments.MapsFragment;

import androidx.fragment.app.Fragment;

/**
 * Main application activity which extends from BaseActivity.
 */
@SuppressWarnings("unused")
public class MainActivity extends BaseActivity {

    @Override
    protected Fragment createFragment() {
        return MapsFragment.newInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display BottomNavigationView
        setBottomNavigationViewVisibility(true);
    }

}
