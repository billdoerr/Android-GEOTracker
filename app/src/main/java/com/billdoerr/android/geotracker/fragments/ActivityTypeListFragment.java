package com.billdoerr.android.geotracker.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.ActivityType;
import com.billdoerr.android.geotracker.database.repo.ActivityTypeRepo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 */
public class ActivityTypeListFragment extends Fragment {

    private List<ActivityType> mActivityTypes;
    private ActivityTypeAdapter mActivityTypeAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We are disabling the options menu.  Refer to onPrepareOptionsMenu()
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_activity_type_list, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogActivityDetails(null);
            }
        });

        // Get data
        mActivityTypes = ActivityTypeRepo.getActivities();

        RecyclerView activityTypeRecyclerView = view.findViewById(R.id.activityTypeList);
        activityTypeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mActivityTypeAdapter = new ActivityTypeAdapter(mActivityTypes);
        activityTypeRecyclerView.setAdapter(mActivityTypeAdapter);

        mActivityTypeAdapter.setActivityTypes(mActivityTypes);
        mActivityTypeAdapter.notifyDataSetChanged();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Change the toolbar title text
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle(R.string.fragment_title_activity_type_list);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /*
     We are disabling the options menu in this fragment.  Must also set
     setHasOptionsMenu(true); in onCreate()
     */
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.clear();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Pass
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    /**
     * Inner class:  ActivityTypeHolder
     */
    private class ActivityTypeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTextName;
        private final TextView mTextDesc;

        private ActivityTypeHolder (View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTextName = itemView.findViewById(R.id.textName);
            mTextDesc = itemView.findViewById(R.id.textDesc);
        }

        private void bind(ActivityType activityType) {
            mTextName.setText(activityType.getName());
            mTextDesc.setText(activityType.getDesc());
        }

        @Override
        public void onClick(View view) {
            int currentPosition = this.getAdapterPosition();  // Store current index
            dialogActivityDetails(mActivityTypes.get(currentPosition));
        }

    }

    /**
     * Inner Class:  ActivityTypeAdapter
     */
    private class ActivityTypeAdapter extends RecyclerView.Adapter<ActivityTypeHolder> {

            private ActivityTypeAdapter(List<ActivityType> activityTypes) {
                mActivityTypes = activityTypes;
            }

            @NonNull
            @Override
            public ActivityTypeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View listItem = layoutInflater.inflate(R.layout.fragment_activity_type_list_item, parent, false);
                return new ActivityTypeHolder(listItem);
            }

            @Override
            public void onBindViewHolder(@NonNull ActivityTypeHolder holder, int position) {
                ActivityType activityType = mActivityTypes.get(position);
                holder.bind(activityType);
            }

            @Override
            public int getItemCount() {
                return mActivityTypes.size();
            }

            private void setActivityTypes(List<ActivityType> activityType) {
                mActivityTypes = activityType;
            }

            private void updateList() {
                mActivityTypes.clear();
                mActivityTypes = ActivityTypeRepo.getActivities();
            }

        }


    /**
     * Presents AlertDialog and allows user to edit data or create new record.
     * @param activityType  Class:  ActivityType
     */
        private void dialogActivityDetails(final ActivityType activityType) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        final View dialogView = View.inflate(getContext(), R.layout.fragment_activity_type_detail, null);
        dialogBuilder.setView(dialogView);

        final EditText textName = dialogView.findViewById(R.id.textName);
        final EditText textDesc = dialogView.findViewById(R.id.textDesc);
        final CheckBox checkActive = dialogView.findViewById(R.id.checkBoxActive);

        if (activityType != null) {
            textName.setText(activityType.getName());
            textDesc.setText(activityType.getDesc());
            checkActive.setChecked(activityType.isActiveFlag() == ActivityType.ACTIVE);
        }

        dialogBuilder.setTitle(R.string.dialog_title_activity_type_new);
        dialogBuilder.setMessage(R.string.dialog_message_activity_type_new);
        dialogBuilder.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (activityType == null) {
                    // Add new record
                    ActivityType at = new ActivityType();
                    at.setName(textName.getText().toString());
                    at.setDesc(textDesc.getText().toString());
                    at.setActive(checkActive.isChecked() ? 1 : 0);
                    ActivityTypeRepo.insert(at);
                    mActivityTypeAdapter.updateList();
                } else {
                    // Update current record
                    activityType.setName(textName.getText().toString());
                    activityType.setDesc(textDesc.getText().toString());
                    activityType.setActive(checkActive.isChecked() ? 1 : 0);
                    ActivityTypeRepo.update(activityType);
                }

                mActivityTypeAdapter.notifyDataSetChanged();
            }
        });
        dialogBuilder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Pass.  Move along nothing to see here.
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

}

