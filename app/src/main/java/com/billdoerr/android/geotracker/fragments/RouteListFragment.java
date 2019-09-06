package com.billdoerr.android.geotracker.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.ActivityType;
import com.billdoerr.android.geotracker.database.model.Route;
import com.billdoerr.android.geotracker.database.repo.ActivityTypeRepo;
import com.billdoerr.android.geotracker.database.repo.RouteRepo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 */
public class RouteListFragment extends Fragment {

    private static final String TAG = "RouteListFragment";

    private List<Route> mRoutes;
    private RouteAdapter mRouteAdapter;
    private int mSelectedActivityType;      // Need to declare as instance variable since assigned in inner class

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We are disabling the options menu.  Refer to onPrepareOptionsMenu()
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_route_list, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogRouteDetails(null);
            }
        });

        // Get data
        mRoutes = new RouteRepo().getRoutes();

        RecyclerView routeRecyclerView = view.findViewById(R.id.routeList);
        routeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRouteAdapter = new RouteAdapter(mRoutes);
        routeRecyclerView.setAdapter(mRouteAdapter);

        mRouteAdapter.setRoutes(mRoutes);
        mRouteAdapter.notifyDataSetChanged();

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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // We are disabling the options menu in this fragment.  Must also set
    // setHasOptionsMenu(true); in onCreate()
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
    private class RouteHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String TAG = "RouteHolder";

        private Route mRoute;
        private CardView cv;
        private TextView mTextName;
        private TextView mTextDesc;
        private int mCurrentPosition;

        public RouteHolder (View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            cv = itemView.findViewById(R.id.cv);
            mTextName = itemView.findViewById(R.id.textName);
            mTextDesc = itemView.findViewById(R.id.textDesc);
        }

        public void bind(Route route) {
            mRoute = route;
            mTextName.setText(mRoute.getRouteName());
            mTextDesc.setText(mRoute.getRouteDesc());
        }

        @Override
        public void onClick(View view) {
            mCurrentPosition = this.getAdapterPosition();  // Store current index
            dialogRouteDetails(mRoutes.get(mCurrentPosition));
        }

    }

    /**
     * Inner Class:  ActivityTypeAdapter
     */
    private class RouteAdapter extends RecyclerView.Adapter<RouteHolder> {

        private static final String TAG = "ActivityTypeAdapter";

        public RouteAdapter(List<Route> routes) {
            mRoutes = routes;
        }

        @NonNull
        @Override
        public RouteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.fragment_route_list_item, parent, false);
            return new RouteHolder(listItem);
        }

        @Override
        public void onBindViewHolder(@NonNull RouteHolder holder, int position) {
            Route route = mRoutes.get(position);
            holder.bind(route);
        }

        @Override
        public int getItemCount() {
            return mRoutes.size();
        }

        public void setRoutes(List<Route> route) {
            mRoutes = route;
        }

        public void updateList() {
            mRoutes.clear();
            mRoutes = new RouteRepo().getRoutes();
        }

    }

    /**
     * Presents AlertDialog and allows user to edit data or create new record.
     * @param route  Class:  Route
     */
    private void dialogRouteDetails(final Route route) {

        // Create list of ActivityType
        final List<ActivityType> activityTypes = new ActivityTypeRepo().getActivities();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        final View dialogView = View.inflate(getContext(), R.layout.fragment_route_detail, null);
        dialogBuilder.setView(dialogView);

        final EditText textName = dialogView.findViewById(R.id.textName);
        final EditText textDesc = dialogView.findViewById(R.id.textDesc);
        final CheckBox checkActive = dialogView.findViewById(R.id.checkBoxActive);
        final Spinner spinnerActivity = dialogView.findViewById(R.id.spinnerActivity);

        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedActivityType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Pass
            }
        });

        // Create list for spinner data
        ArrayList<String> items = new ArrayList<>();
        for (int i=0; i < activityTypes.size(); i++) {
            items.add(activityTypes.get(i).getActivityTypeName());
        }

        // Assign adapter to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, items);
        spinnerActivity.setAdapter(adapter);

        // If data exists populate widget values
        if (route != null) {
            textName.setText(route.getRouteName());
            textDesc.setText(route.getRouteDesc());
            checkActive.setChecked(route.getRouteActiveFlag() == Route.ACTIVE);
            spinnerActivity.setSelection(getListIndex(activityTypes, route.getRouteActivityTypeId()));
        }

        // Build dialog
        dialogBuilder.setTitle(R.string.dialog_title_route_new);
        dialogBuilder.setMessage(R.string.dialog_message_route_new);
        dialogBuilder.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (route == null) {
                    // Add new record
                    Route newRoute = new Route();
                    newRoute.setRouteName(textName.getText().toString());
                    newRoute.setRouteDesc(textDesc.getText().toString());
                    newRoute.setRouteActiveFlag(checkActive.isChecked() ? 1 : 0);
                    newRoute.setRouteActivityTypeId(activityTypes.get(mSelectedActivityType).getActivityTypeId());
                    new RouteRepo().insert(newRoute);
                    mRouteAdapter.updateList();
                } else {
                    // Update current record
                    route.setRouteName(textName.getText().toString());
                    route.setRouteDesc(textDesc.getText().toString());
                    route.setRouteActiveFlag(checkActive.isChecked() ? 1 : 0);
                    route.setRouteActivityTypeId(activityTypes.get(mSelectedActivityType).getActivityTypeId());
                    new RouteRepo().update(route);
                }

                mRouteAdapter.notifyDataSetChanged();
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

    /**
     * Returns list position whose data has index of rowId
     * @param list List<ActivityType></ActivityType>
     * @param rowId int:  Database primary index
     * @return int:  Position in list
     */
    private int getListIndex(List<ActivityType> list, int rowId) {
        int i;
        for ( i=0; i < list.size(); i++) {
            if (list.get(i).getActivityTypeId() == rowId) {
                break;
            }
        }
        return i;
    }

}

