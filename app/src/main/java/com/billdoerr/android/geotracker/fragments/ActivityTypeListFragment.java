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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 */
public class ActivityTypeListFragment extends Fragment {

    private static final String TAG = "ActivityTypeListFragment";

    private List<ActivityType> mActivityTypes;
    private RecyclerView mActivityTypeRecyclerView;
    private ActivityTypeAdapter mActivityTypeAdapter;
    private FloatingActionButton mFab;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We are disabling the options menu.  Refer to onPrepareOptionsMenu()
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_activity_type_list, container, false);

        mFab = (FloatingActionButton) view.findViewById(R.id.fabAdd);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogActivityDetails(null);
            }
        });

        // Get data
        mActivityTypes = new ActivityTypeRepo().getActivities();

        mActivityTypeRecyclerView = (RecyclerView) view.findViewById(R.id.activityTypeList);
        mActivityTypeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mActivityTypeAdapter = new ActivityTypeAdapter(mActivityTypes);
        mActivityTypeRecyclerView.setAdapter(mActivityTypeAdapter);

        mActivityTypeAdapter.setActivityTypes(mActivityTypes);
        mActivityTypeAdapter.notifyDataSetChanged();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // We are disabling the options menu in this fragment.  Must also set
    // setHasOptionsMenu(true); in onCreate()
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Pass
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    /**
     * Inner class:  ActivityTypeHolder
     */
    private class ActivityTypeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String TAG = "ActivityTypeHolder";

        private ActivityType mActivityType;
        private CardView cv;
        private TextView mTextName;
        private TextView mTextDesc;
        private int mCurrentPosition;

        public ActivityTypeHolder (View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            cv = (CardView) itemView.findViewById(R.id.cv);
            mTextName = (TextView) itemView.findViewById(R.id.textName);
            mTextDesc = (TextView) itemView.findViewById(R.id.textDesc);
        }

        public void bind(ActivityType activityType) {
            mActivityType = activityType;
            mTextName.setText(mActivityType.getActivityTypeName());
            mTextDesc.setText(mActivityType.getActivityTypeDesc());
        }

        @Override
        public void onClick(View view) {
            mCurrentPosition = this.getAdapterPosition();  // Store current index
            dialogActivityDetails(mActivityTypes.get(mCurrentPosition));
        }

    }

    /**
     * Inner Class:  ActivityTypeAdapter
     */
    private class ActivityTypeAdapter extends RecyclerView.Adapter<ActivityTypeHolder> {

            private static final String TAG = "ActivityTypeAdapter";

            public ActivityTypeAdapter(List<ActivityType> activityTypes) {
                mActivityTypes = activityTypes;
            }

            @Override
            public ActivityTypeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View listItem = layoutInflater.inflate(R.layout.fragment_activity_type_list_item, parent, false);

                ActivityTypeHolder viewHolder = new ActivityTypeHolder(listItem);

                return viewHolder;
            }

            @Override
            public void onBindViewHolder(ActivityTypeHolder holder, int position) {
                ActivityType activityType = mActivityTypes.get(position);
                holder.bind(activityType);
            }

            @Override
            public int getItemCount() {
                return mActivityTypes.size();
            }

            public void setActivityTypes(List<ActivityType> activityType) {
                mActivityTypes = activityType;
            }

            public void updateList() {
                mActivityTypes.clear();
                mActivityTypes = new ActivityTypeRepo().getActivities();
            }

        }


    /**
     * Presents AlertDialog and allows user to edit data or create new record.
     * @param activityType  Class:  ActivityType
     */
        private void dialogActivityDetails(final ActivityType activityType) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.fragment_activity_type_detail, null);
        dialogBuilder.setView(dialogView);

        final EditText textName = (EditText) dialogView.findViewById(R.id.textName);
        final EditText textDesc = (EditText) dialogView.findViewById(R.id.textDesc);
        final CheckBox checkActive = (CheckBox) dialogView.findViewById(R.id.checkBoxActive);

        if (activityType != null) {
            textName.setText(activityType.getActivityTypeName());
            textDesc.setText(activityType.getActivityTypeDesc());
            checkActive.setChecked(activityType.getActivityTypeActiveFlag() == ActivityType.ACTIVE);
        }

        dialogBuilder.setTitle(R.string.dialog_title_activity_type_new);
        dialogBuilder.setMessage(R.string.dialog_message_activity_type_new);
        dialogBuilder.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (activityType == null) {
                    // Add new record
                    ActivityType at = new ActivityType();
                    at.setActivityTypeName(textName.getText().toString());
                    at.setActivityTypeDesc(textDesc.getText().toString());
                    at.setActivityTypeActiveFlag(checkActive.isChecked() ? 1 : 0);
                    new ActivityTypeRepo().insert(at);
                    mActivityTypeAdapter.updateList();
                } else {
                    // Update current record
                    activityType.setActivityTypeName(textName.getText().toString());
                    activityType.setActivityTypeDesc(textDesc.getText().toString());
                    activityType.setActivityTypeActiveFlag(checkActive.isChecked() ? 1 : 0);
                    new ActivityTypeRepo().update(activityType);
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
