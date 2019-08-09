package com.billdoerr.android.geotracker.activities;

import com.billdoerr.android.geotracker.fragments.AboutFragment;

import androidx.fragment.app.Fragment;

/**
 * About this application activity which extends from BaseActivity.
 */
public class AboutActivity extends BaseActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected Fragment createFragment() {
        return AboutFragment.newInstance();
    }
}
