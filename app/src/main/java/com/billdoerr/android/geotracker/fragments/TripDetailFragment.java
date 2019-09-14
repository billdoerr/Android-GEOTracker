package com.billdoerr.android.geotracker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

public class TripDetailFragment extends DialogFragment {

    // Making public since it is used with DialogFragment.show() in calling fragment
    public static final String TAG = "TripDetailFragment";

    private static final int REQUEST_CODE_TRIP_DIALOG_CONTINUE = 2;

    private static final String ARGS_TRIP = "Trip";

    private Trip mTrip;

    private EditText mTextName;
    private EditText mTextDesc;
    private CheckBox mCheckActive;

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

        mTextName = view.findViewById(R.id.textName);
        mTextDesc = view.findViewById(R.id.textDesc);
        mCheckActive = view.findViewById(R.id.checkBoxActive);

        final Button btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send result to calling fragment
                if (mTextName.getText().length() > 0 ) {
                    mTrip.setTripName(mTextName.getText().toString());
                    mTrip.setTripDesc(mTextDesc.getText().toString());
                    // Convert boolean to integer
                    mTrip.setTripActiveFlag(mCheckActive.isChecked()?1:0);
                    sendResult(mTrip, Activity.RESULT_OK);
                }
                // Inform user that Trip Name is required
                else {
                    Toast.makeText(getContext(), R.string.toast_trip_name_required, Toast.LENGTH_SHORT).show();
                }

            }
        });

        final Button btnContinue = view.findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send result to calling fragment
                if (mTextName.getText().length() > 0 ) {
                    mTrip.setTripName(mTextName.getText().toString());
                    mTrip.setTripDesc(mTextDesc.getText().toString());
                    // Convert boolean to integer
                    mTrip.setTripActiveFlag(mCheckActive.isChecked()?1:0);
                    sendResult(mTrip, Activity.RESULT_OK);
                }
            }
        });

        final Button btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Don't need to send any data
                sendResult(mTrip, Activity.RESULT_CANCELED);
            }
        });


        // Disable Cancel and enable Continue
        if (getTargetRequestCode() == REQUEST_CODE_TRIP_DIALOG_CONTINUE) {
            btnSave.setVisibility(View.GONE);
            btnContinue.setVisibility(View.VISIBLE);
        }

        if (mTrip != null) {
            mTextName.setText(mTrip.getTripName());
            mTextDesc.setText(mTrip.getTripDesc());
            // Boolean stored as integer in SQLite, so convert to integer.
            mCheckActive.setChecked(mTrip.getTripActiveFlag() == Trip.ACTIVE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Sends result to calling fragment
     * @param trip Trip
     */
    private void sendResult(Trip trip, int result) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(ARGS_TRIP, trip);
        getTargetFragment().onActivityResult(getTargetRequestCode(), result, intent);
        dismiss();
    }

}