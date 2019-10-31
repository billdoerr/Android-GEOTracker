package com.billdoerr.android.geotracker.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Route;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.repo.ActivityTypeRepo;
import com.billdoerr.android.geotracker.database.repo.RouteRepo;
import com.billdoerr.android.geotracker.database.repo.TripRepo;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class TripListFragment extends Fragment {

    @SuppressWarnings("SpellCheckingInspection")
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat( "ddMMMyyyy hh:mm:ss a" , Locale.US);

    private static final int REQUEST_CODE_TRIP_DIALOG_SAVE = 1;
    private static final int REQUEST_CODE_TRIP_DIALOG_FILTER = 3;

    private static final String ARGS_TRIP = "trip";
    private static final String ARGS_SAVE_TRIP_NAME_TO_ROUTES = "save_trip_name_to_routes";
    private static final String ARGS_FILTER_ACTIVE_FLAG = "args_filter_active_flag";
    private static final String ARGS_FILTER_ACTIVITY_TYPE_ID = "args_filter_activity_type_id";

    private static final int CLEAR_FILTER = -1;

    private TripAdapter mTripAdapter;
    private List<Trip> mTrips;
    private int mCurrentPosition;
    private ViewFlipper mViewFlipper;

    private static int mActiveFlagFilter = CLEAR_FILTER;
    private static int mActivityTypeIdFilter = CLEAR_FILTER;

    /**
     * Required empty public constructor
     */
    public TripListFragment() {
        // Pass
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_trip_list, container, false);

        // Get trips. Only returns list where end time != 0.
        mTrips = TripRepo.getTrips();

        mViewFlipper = view.findViewById(R.id.viewFlipper);
        if (mTrips.isEmpty()) {
            mViewFlipper.setDisplayedChild(mViewFlipper.indexOfChild(view.findViewById(R.id.layout_no_trip_data)));
        }

        RecyclerView tripRecyclerView = view.findViewById(R.id.list);
        tripRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mTripAdapter = new TripAdapter(mTrips);
        tripRecyclerView.setAdapter(mTripAdapter);

        mTripAdapter.setTrips(mTrips);
        mTripAdapter.notifyDataSetChanged();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Change the toolbar title text
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle(R.string.fragment_title_trip_list);

        // Check for applied filters
        if (savedInstanceState != null) {
            mActiveFlagFilter = savedInstanceState.getInt(ARGS_FILTER_ACTIVE_FLAG);
            mActivityTypeIdFilter = savedInstanceState.getInt(ARGS_FILTER_ACTIVITY_TYPE_ID);

            // Retrieve filter results
            filterResults();
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARGS_FILTER_ACTIVE_FLAG, mActiveFlagFilter);
        outState.putInt(ARGS_FILTER_ACTIVITY_TYPE_ID, mActivityTypeIdFilter);
    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode != Activity.RESULT_OK ) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_TRIP_DIALOG_SAVE:
                Trip trip = (Trip) data.getSerializableExtra(ARGS_TRIP);
                boolean saveTripName = data.getBooleanExtra(ARGS_SAVE_TRIP_NAME_TO_ROUTES, false);
                // Save trip
                if (trip != null) {
                    updateTrip(trip);
                    // Save trip name to routes if option selected
                    if (saveTripName) {
                        Route route = new Route();
                        route.setActive(1);
                        route.setName(trip.getName());
                        route.setDesc(trip.getDesc());
                        route.setActivityTypeId(trip.getActivityTypeId());
                        RouteRepo.insert(route);
                    }
                }
                break;
             case REQUEST_CODE_TRIP_DIALOG_FILTER:
                 mActiveFlagFilter = data.getIntExtra(ARGS_FILTER_ACTIVE_FLAG, -1);
                 mActivityTypeIdFilter = data.getIntExtra(ARGS_FILTER_ACTIVITY_TYPE_ID, -1);
                 // Retrieve filter results
                 filterResults();
                 break;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_trip_list, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) Objects.requireNonNull(getActivity()).getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);

        // Listening to search query text change
        // Ripped from:  https://www.androidhive.info/2017/11/android-recyclerview-with-search-filter-functionality/
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Filter recycler view when query submitted
                mTripAdapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                // Filter recycler view when text is changed
                mTripAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_apply_filter:
                showTripFilterDialog(REQUEST_CODE_TRIP_DIALOG_FILTER);
                return true;
            case R.id.action_clear_filter:
                // Reset flags
                mActiveFlagFilter = CLEAR_FILTER;
                mActivityTypeIdFilter = CLEAR_FILTER;
                // Clear list, re-query and notify data changed
                mTrips.clear();
                mTrips = TripRepo.getTrips();
                mTripAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Retrieve filter results
     */
    private void filterResults() {
        mTrips.clear();
        mTrips = TripRepo.filterTrips(mActiveFlagFilter, mActivityTypeIdFilter);
        mTripAdapter.notifyDataSetChanged();
    }

    /**
     * Save trip to database and clear trip state.
     * Ripped from https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data
     */
    private void updateTrip(Trip trip) {
        // Update record
        int ret = TripRepo.update(trip);

        // Returns -1 if error
        if (ret != -1) {
            // Update RecyclerView
            mTrips.set(mCurrentPosition, trip);
            mTripAdapter.notifyItemChanged(mCurrentPosition);

            // If active trip, save to shared preferences
            if ((trip.getState() == Trip.TripState.PAUSED) || (trip.getState() == Trip.TripState.STARTED)) {
                PreferenceUtils.saveActiveTripToSharedPrefs(Objects.requireNonNull(getContext()), trip);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.toast_database_update_error), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Save trip to database and clear trip state.
     * Ripped from https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data
     */
    private void deleteTrip(Trip trip, int position) {
        // Delete from database
        int ret = TripRepo.delete(trip.getId());

        // If 0, then no records updated
        if (ret > 0) {
            // Update RecyclerView
            mTrips.remove(position);
            mTripAdapter.notifyItemRemoved(position);
            if (mTrips.isEmpty()) {
                mViewFlipper.setDisplayedChild(1);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.toast_database_delete_error), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * FragmentDialog that allows the filtering of the trip list
     * @param requestCode int
     */
    @SuppressWarnings("SameParameterValue")
    private void showTripDetailDialog(Trip trip, int requestCode) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = Objects.requireNonNull(Objects.requireNonNull(getActivity()).getSupportFragmentManager()).beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(TripDetailFragment.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        Bundle args = new Bundle();
        args.putSerializable(ARGS_TRIP, trip);

        // Create and show the dialog.
        DialogFragment dialogFragment = TripDetailFragment.newInstance();
        dialogFragment.setArguments(args);
        dialogFragment.setTargetFragment(this, requestCode);
        dialogFragment.show(ft, TripDetailFragment.TAG);
    }

    /**
     * Display confirmation dialog
     * @param trip Trip
     */
    private void showTripDeleteDialog(final Trip trip, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.dialog_title_trip_delete);
        builder.setMessage(R.string.dialog_msg_trip_delete);
        builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                deleteTrip(trip, position);

            }
        });
        builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * FragmentDialog that allows the editing of the trip's details
     * @param requestCode int
     */
    @SuppressWarnings("SameParameterValue")
    private void showTripFilterDialog(int requestCode) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = Objects.requireNonNull(Objects.requireNonNull(getActivity()).getSupportFragmentManager()).beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(TripDetailFragment.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        Bundle args = new Bundle();
        args.putSerializable(ARGS_TRIP, new Trip());
        args.putInt(ARGS_FILTER_ACTIVE_FLAG, mActiveFlagFilter);
        args.putInt(ARGS_FILTER_ACTIVITY_TYPE_ID, mActivityTypeIdFilter);

        // Create and show the dialog.
        DialogFragment dialogFragment = TripListFilterFragment.newInstance();
        dialogFragment.setArguments(args);
        dialogFragment.setTargetFragment(this, requestCode);
        dialogFragment.show(ft, TripListFilterFragment.TAG);
    }


    /*
     * Inner class:  TripHolder
     */
    private class TripHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTextName;
        private final TextView mTextDesc;
        private final TextView mTextActivity;
        private final TextView mTextViewOption;
        private final TextView mTextTotalTimeData;
        private final TextView mTextStartTimeData;

        TripHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTextName = itemView.findViewById(R.id.textName);
            mTextDesc = itemView.findViewById(R.id.textDesc);
            mTextActivity = itemView.findViewById(R.id.textActivity);
            mTextViewOption = itemView.findViewById(R.id.textViewOptions);
            mTextTotalTimeData = itemView.findViewById(R.id.textTotalTimeData);
            mTextStartTimeData = itemView.findViewById(R.id.textStartTimeData);

        }

        void bind(Trip trip) {
            mTextName.setText(trip.getName());
            mTextDesc.setText(trip.getDesc());
            // Query the ActivityTypeName.  Not sure if I should have perform inner join on query to retrieve this or not.mm
            mTextActivity.setText(ActivityTypeRepo.getActivityName(trip.getActivityTypeId()));
            mTextTotalTimeData.setText(DateUtils.formatElapsedTime(  ( trip.getTotalTimeInMillis() ) /  1000 ) );
            mTextStartTimeData.setText(sDateFormat.format(trip.getStartTime()));
        }

        @Override
        public void onClick(View view) {
            // Store current index
            mCurrentPosition = this.getAdapterPosition();
        }

    }

    /*
     * Inner Class:  TripAdapter
     */
    private class TripAdapter extends RecyclerView.Adapter<TripHolder> implements Filterable {

        private List<Trip> tripListFiltered;

        TripAdapter(List<Trip> trips) {
            mTrips = trips;
        }

        @NonNull
        @Override
        public TripHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.fragment_trip_list_item, parent, false);
            return new TripHolder(listItem);
        }

        @Override
        public void onBindViewHolder(@NonNull final TripHolder holder, int position) {
            final Trip trip = mTrips.get(position);
            holder.bind(trip);

            // Ripped from https://www.simplifiedcoding.net/create-options-menu-recyclerview-item-tutorial/
            holder.mTextViewOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Creating a popup menu
                    PopupMenu popup = new PopupMenu(Objects.requireNonNull(getContext()), holder.mTextViewOption);
                    // Inflating menu from xml resource
                    popup.inflate(R.menu.menu_trip_list_item);
                    // Adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.editTrip:
                                    mCurrentPosition = holder.getAdapterPosition();
                                    showTripDetailDialog(trip, REQUEST_CODE_TRIP_DIALOG_SAVE);
                                    return true;
                                case R.id.deleteTrip:
                                    showTripDeleteDialog(trip, holder.getAdapterPosition());
                                    return true;
                                case R.id.reviewTrip:
                                    // Pass in trip id
                                    Bundle args = new Bundle();
                                    args.putSerializable(ARGS_TRIP, trip);

                                    // Create fragment
                                    TripReviewFragment fragment= new TripReviewFragment();
                                    fragment.setArguments(args);
                                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, fragment, TripReviewFragment.TAG)
                                            .addToBackStack(TripReviewFragment.TAG)
                                            .commit();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    // Displaying the popup
                    popup.show();

                }
            });
        }

        @Override
        public int getItemCount() {
            return mTrips.size();
        }

        void setTrips(List<Trip> trips) {
            mTrips = trips;
        }

        // Ripped from:  https://www.androidhive.info/2017/11/android-recyclerview-with-search-filter-functionality/
        @Override
        public Filter getFilter() {
            return new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        tripListFiltered = mTrips;
                    } else {
                        List<Trip> filteredList = new ArrayList<>();
                        for (Trip row : mTrips) {

                            // Name match condition. This might differ depending on your requirement
                            // here we are looking for name or phone number match
                            if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getName().contains(charSequence)) {
                                filteredList.add(row);
                            }
                        }

                        tripListFiltered = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = tripListFiltered;
                    return filterResults;
                }

                @SuppressWarnings (value="unchecked")
                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mTrips = (ArrayList<Trip>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

}
