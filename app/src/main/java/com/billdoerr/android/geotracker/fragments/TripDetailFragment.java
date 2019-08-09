package com.billdoerr.android.geotracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.utils.GlobalVariables;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class TripDetailFragment extends Fragment {

    private static final String TAG = "TripDetailFragment";

    private Trip mTrip;

    public TripDetailFragment() {
        // Required empty public constructor
    }

    public static TripDetailFragment newInstance() {
        return new TripDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//       setHasOptionsMenu(true);

        // Get args
        Bundle args = getArguments();
        if (args != null) {
            mTrip = (Trip) args.getSerializable(GlobalVariables.ARGS_TRIP);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_detail, container, false);

        final EditText textName = (EditText) view.findViewById(R.id.textName);
        final EditText textDesc = (EditText) view.findViewById(R.id.textDesc);
        final CheckBox checkActive = (CheckBox) view.findViewById(R.id.checkBoxActive);

        if (mTrip != null) {
            textName.setText(mTrip.getTripName());
            textDesc.setText(mTrip.getTripDesc());
            checkActive.setChecked(mTrip.getTripActiveFlag() == Trip.ACTIVE);
        }

        return view;
    }

}