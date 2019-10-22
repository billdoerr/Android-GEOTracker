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
import android.widget.RadioButton;
import android.widget.Spinner;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.ActivityType;
import com.billdoerr.android.geotracker.database.model.Route;
import com.billdoerr.android.geotracker.database.repo.ActivityTypeRepo;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class RouteListFilterFragment extends DialogFragment {

    // Making public since it is used with DialogFragment.show() in calling fragment
    public static final String TAG = "RouteListFilterFragment";

    private static final String ARGS_FILTER_ACTIVE_FLAG = "args_filter_active_flag";
    private static final String ARGS_FILTER_ACTIVITY_TYPE_ID = "args_filter_activity_type_id";

    private static final int CLEAR_FILTER = -1;

    private static int mActiveFlagFilter = -1;
    private static int mActivityTypeIdFilter = -1;

    public RouteListFilterFragment() {
        // Required empty public constructor
    }

    public static RouteListFilterFragment newInstance() {
        return new RouteListFilterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get args
        Bundle args = getArguments();
        if (args != null) {
            mActiveFlagFilter = args.getInt(ARGS_FILTER_ACTIVE_FLAG);
            mActivityTypeIdFilter = args.getInt((ARGS_FILTER_ACTIVITY_TYPE_ID));
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route_list_filter, container, false);

        // Set dialog title
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        getDialog().setTitle(R.string.dialog_title_route_list_filter);

        final Spinner spinnerActivity = view.findViewById(R.id.spinnerActivity);
        final RadioButton radioButtonAll = view.findViewById(R.id.radioButtonAll);
        final RadioButton radioButtonActive = view.findViewById(R.id.radioButtonActive);
        final RadioButton radioButtoninactive = view.findViewById(R.id.radioButtonInactive);
        final Button btnOk = view.findViewById(R.id.btn_ok);
        final Button btnCancel = view.findViewById(R.id.btn_cancel);

        radioButtonAll.setOnClickListener(onRadioButtonClicked);
        radioButtonActive.setOnClickListener(onRadioButtonClicked);
        radioButtoninactive.setOnClickListener(onRadioButtonClicked);

        // Initialize which radio button is checked
        switch (mActiveFlagFilter) {
            case Route.ACTIVE:
                radioButtonActive.setChecked(true);
                break;
            case Route.INACTIVE:
                radioButtoninactive.setChecked(true);
                break;
            default:
                radioButtonAll.setChecked(true);
                break;
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send result to calling fragment
                sendResult(Activity.RESULT_OK);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send result to calling fragment
                sendResult(Activity.RESULT_CANCELED);
            }
        });

        /*
         * Configure spinner.  Ripped from:  https://stackoverflow.com/questions/24712540/set-key-and-value-in-spinner
         */
        // Fill data in spinner
        List<ActivityType> listActivities = getActivities();
        ArrayAdapter<ActivityType> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listActivities);
        spinnerActivity.setAdapter(adapter);

        spinnerActivity.setSelection(getIndex(listActivities, mActivityTypeIdFilter));

        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ActivityType activityType = (ActivityType) parent.getSelectedItem();
                mActivityTypeIdFilter = activityType.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Pass
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Handles the RadioButton's click event.  Defined in layout xml.
     */
    private View.OnClickListener onRadioButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton) view).isChecked();
            // Check which radio button was clicked
            switch (view.getId()) {
                case R.id.radioButtonAll:
                    if (checked)
                        mActiveFlagFilter = -1;
                    break;
                case R.id.radioButtonActive:
                    if (checked)
                        mActiveFlagFilter = Route.ACTIVE;
                    break;
                case R.id.radioButtonInactive:
                    if (checked)
                        mActiveFlagFilter = Route.INACTIVE;
                    break;
            }
        }
    };

    /**
     * Sends result to calling fragment
     * @param result int OK or CANCEL
     */
    private void sendResult(int result) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(ARGS_FILTER_ACTIVE_FLAG, mActiveFlagFilter);
        intent.putExtra(ARGS_FILTER_ACTIVITY_TYPE_ID, mActivityTypeIdFilter);
        getTargetFragment().onActivityResult(getTargetRequestCode(), result, intent);
        dismiss();
    }

    /**
     * Returns list of Activities
     * @return List<ActivityType>
     */
    private List<ActivityType> getActivities() {
        List<ActivityType> activityTypes = ActivityTypeRepo.getActivities();
        ActivityType activityType = new ActivityType();
        activityType.setId(CLEAR_FILTER);
        activityType.setName("");
        activityTypes.add(0, activityType);
        return activityTypes;
    }

    /**
     * Returns index of object in List<ActivityType>
     * @param activities (List<ActivityType>
     * @param id int ActivityId
     * @return int
     */
    private int getIndex(List<ActivityType> activities, int id) {
        int index = 0;
        for (ActivityType activity : activities) {
            if (activity.getId() == id) {
                return index;
            }
            index++;
        }
        return CLEAR_FILTER;
    }


}