package com.billdoerr.android.geotracker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.ActivityType;
import com.billdoerr.android.geotracker.database.model.Route;
import com.billdoerr.android.geotracker.database.repo.ActivityTypeRepo;
import com.billdoerr.android.geotracker.database.repo.RouteRepo;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class RouteDetailFragment extends DialogFragment {

    // Making public since it is used with DialogFragment.show() in calling fragment
    public static final String TAG = "RouteDetailFragment";

    private static final String ARGS_ROUTE = "route";

    private static final int REQUEST_CODE_ROUTE_DIALOG_ADD = 1;
    private static final int REQUEST_CODE_ROUTE_DIALOG_EDIT = 2;

    private Route mRoute;

    public RouteDetailFragment() {
        // Required empty public constructor
    }

    public static RouteDetailFragment newInstance() {
        return new RouteDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get args
        Bundle args = getArguments();
        if (args != null) {
            mRoute = (Route) args.getSerializable(ARGS_ROUTE);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route_detail, container, false);

        // Set dialog title
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        int requestCode = getTargetRequestCode();
        if (requestCode == REQUEST_CODE_ROUTE_DIALOG_ADD) {
            getDialog().setTitle(R.string.dialog_title_route_add);
        } else {
            getDialog().setTitle(R.string.dialog_title_route_edit);
        }

        final EditText textDesc = view.findViewById(R.id.textDesc);
        final CheckBox checkActive = view.findViewById(R.id.checkBoxActive);
        final Spinner spinnerActivity = view.findViewById(R.id.spinnerActivity);

        final AutoCompleteTextView textName = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
        List<String> array = RouteRepo.getRoutesNames();
        ArrayAdapter<String> adapterAutoComplete = new ArrayAdapter<String> (getContext(), android.R.layout.select_dialog_item, array);
        textName.setThreshold(1);
        textName.setAdapter(adapterAutoComplete);

        final Button btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send result to calling fragment
                if (textName.getText().length() > 0 ) {
                    mRoute.setName(textName.getText().toString());
                    mRoute.setDesc(textDesc.getText().toString());
                    // Convert boolean to integer
                    mRoute.setActive(checkActive.isChecked()?1:0);
                    sendResult(mRoute, Activity.RESULT_OK);
                }
                // Inform user that Route Name is required
                else {
                    Toast.makeText(getContext(), R.string.toast_route_name_required, Toast.LENGTH_SHORT).show();
                }

            }
        });

        final Button btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Don't need to send any data
                sendResult(mRoute, Activity.RESULT_CANCELED);
            }
        });

        /*
         * Configure spinner.  Ripped from:  https://stackoverflow.com/questions/24712540/set-key-and-value-in-spinner
         */
        // Fill data in spinner
        List<ActivityType> listActivities = getActivities();
        ArrayAdapter<ActivityType> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listActivities);
        spinnerActivity.setAdapter(adapter);

        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ActivityType activityType = (ActivityType) parent.getSelectedItem();
                mRoute.setActivityTypeId(activityType.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Pass
            }
        });

        if (mRoute != null) {
            textName.setText(mRoute.getName());
            textDesc.setText(mRoute.getDesc());
            // Boolean stored as integer in SQLite, so convert to integer.
            checkActive.setChecked(mRoute.isActive() == Route.ACTIVE);
            // Set route's activity
            spinnerActivity.setSelection(adapter.getPosition(getActivity(listActivities, mRoute.getActivityTypeId())));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Sends result to calling fragment
     * @param route Route
     */
    private void sendResult(Route route, int result) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(ARGS_ROUTE, route);
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
     * Finds the ActivityType from a List<ActivityType> from the Route object
     * @param activities List<ActivityType>
     * @param id int Route id.
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