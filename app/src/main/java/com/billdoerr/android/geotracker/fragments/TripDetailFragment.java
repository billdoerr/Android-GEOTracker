package com.billdoerr.android.geotracker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.ActivityType;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.repo.ActivityTypeRepo;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class TripDetailFragment extends DialogFragment {

    // Making public since it is used with DialogFragment.show() in calling fragment
    public static final String TAG = "TripDetailFragment";

    private static final int REQUEST_CODE_TRIP_DIALOG_CONTINUE = 2;

    private static final String ARGS_TRIP = "trip";

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

        // Set dialog title
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        getDialog().setTitle(R.string.dialog_title_trip_edit);

        final EditText textName = view.findViewById(R.id.textName);
        final EditText textDesc = view.findViewById(R.id.textDesc);
        final CheckBox checkActive = view.findViewById(R.id.checkBoxActive);
        final Spinner spinnerActivity = view.findViewById(R.id.spinnerActivity);

        final Button btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send result to calling fragment
                if (textName.getText().length() > 0 ) {
                    mTrip.setName(textName.getText().toString());
                    mTrip.setDesc(textDesc.getText().toString());
                    // Convert boolean to integer
                    mTrip.setActive(checkActive.isChecked()?1:0);
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
                if (textName.getText().length() > 0 ) {
                    mTrip.setName(textName.getText().toString());
                    mTrip.setDesc(textDesc.getText().toString());
                    // Convert boolean to integer
                    mTrip.setActive(checkActive.isChecked()?1:0);
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

        /*
        * Configure spinner.  Ripped from:  https://stackoverflow.com/questions/24712540/set-key-and-value-in-spinner
         */
        // Fill data in spinner
        List<ActivityType> listActivities = getActivities();
        ArrayAdapter<ActivityType> adapter = new ArrayAdapter<ActivityType>(getContext(), android.R.layout.simple_spinner_dropdown_item, listActivities);
        spinnerActivity.setAdapter(adapter);

        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ActivityType activityType = (ActivityType) parent.getSelectedItem();
                mTrip.setActivityTypeId(activityType.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Pass
            }
        });


        // Disable Cancel and enable Continue
        if (getTargetRequestCode() == REQUEST_CODE_TRIP_DIALOG_CONTINUE) {
            btnSave.setVisibility(View.GONE);
            btnContinue.setVisibility(View.VISIBLE);
        }

        if (mTrip != null) {
            textName.setText(mTrip.getName());
            textDesc.setText(mTrip.getDesc());
            // Boolean stored as integer in SQLite, so convert to integer.
            checkActive.setChecked(mTrip.isActive() == Trip.ACTIVE);
            // Set trip's activity
            spinnerActivity.setSelection(adapter.getPosition(getActivity(listActivities, mTrip.getActivityTypeId())));
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

    /**
     * Returns list of Activities
     * @return List<ActivityType>
     */
    private List<ActivityType> getActivities() {
        return ActivityTypeRepo.getActivities();
    }

    /**
     * Finds the ActivityType from a List<ActivityType> from the Trip object
     * @param activities List<ActivityType>
     * @param id int Trip id.
     * @return ActivityType
     */
    private ActivityType getActivity(List<ActivityType> activities, int id) {
        for (ActivityType activity : activities) {
            if (activity.getId() == id) {
                return activity;
            }
        }
        return null;
    }

}