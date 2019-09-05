package com.billdoerr.android.geotracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class TripDetailFragment extends DialogFragment {

    private static final String TAG = "TripDetailFragment";

    private static final String ARGS_TRIP = "Trip";

    private Trip mTrip;

    public interface DialogListener {
        void onFinishDialog(boolean save);
    }

    public TripDetailFragment() {
        // Required empty public constructor
    }

    public static TripDetailFragment newInstance() {
        return new TripDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get args
        Bundle args = getArguments();
        if (args != null) {
            mTrip = (Trip) args.getSerializable(ARGS_TRIP);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_detail, container, false);

        final EditText textName = (EditText) view.findViewById(R.id.textName);
        final EditText textDesc = (EditText) view.findViewById(R.id.textDesc);
        final CheckBox checkActive = (CheckBox) view.findViewById(R.id.checkBoxActive);
        final Button btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogListener dialogListener = (DialogListener) getTargetFragment();
                dialogListener.onFinishDialog(true);
                dismiss();
            }
        });
        final Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogListener dialogListener = (DialogListener) getTargetFragment();
                dialogListener.onFinishDialog(false);
                dismiss();
            }
        });

        if (mTrip != null) {
            textName.setText(mTrip.getTripName());
            textDesc.setText(mTrip.getTripDesc());
            checkActive.setChecked(mTrip.getTripActiveFlag() == Trip.ACTIVE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}