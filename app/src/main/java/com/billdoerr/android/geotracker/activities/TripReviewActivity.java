package com.billdoerr.android.geotracker.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import com.billdoerr.android.geotracker.fragments.TripReviewFragment;

/**
 * Activity which extends from BaseActivity and host the trip review tab layout fragments.
 * Activity class was need to resolve issues with charts not updating and options menu not displaying
 * when switching between fragments.
 */
@SuppressWarnings("unused")
public class TripReviewActivity extends BaseActivity {

    @Override
    protected Fragment createFragment() {
        return TripReviewFragment.newInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display BottomNavigationView
        setBottomNavigationViewVisibility(false);
    }

}
