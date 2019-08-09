package com.billdoerr.android.geotracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.billdoerr.android.geotracker.BuildConfig;
import com.billdoerr.android.geotracker.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


/**
 *  Fragment that displays information about this application.  Application name and version #.
 *  // TODO:  Remove below comment when AboutActivity is deleted.
 *  Created by the AboutActivity class.
 */
public class AboutFragment extends Fragment {

    private static final String TAG = "AboutFragment";

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_about, container, false);

        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String versionName = BuildConfig.VERSION_NAME;

        TextView txtVersion = v.findViewById(R.id.txtVersion);
        txtVersion.setText(versionName + " (" + versionCode + ")");

        return v;
    }

}

