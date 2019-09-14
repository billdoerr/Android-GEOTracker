package com.billdoerr.android.geotracker.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.repo.TripRepo;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class TripListFragment extends Fragment {

    private static final String TAG = "TripListFragment";

    private static final int REQUEST_CODE_TRIP_DIALOG_SAVE = 1;

    private static final String ARGS_TRIP = "Trip";

    private TripAdapter mTripAdapter;
    private List<Trip> mTrips;
    private int mCurrentPosition;
    private ViewFlipper mViewFlipper;

    /**
     * Required empty public constructor
     */
    public TripListFragment() {
        // Pass
    }

    public static TripListFragment newInstance() {
        return new TripListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO:  enable options menu??
       setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_trip_list, container, false);

        // Get data
        mTrips = TripRepo.getTrips();

        mViewFlipper = view.findViewById(R.id.viewFlipper);
        if (mTrips.isEmpty()) {
            mViewFlipper.setDisplayedChild(mViewFlipper.indexOfChild(view.findViewById(R.id.layout_no_data)));
        }

        RecyclerView tripRecyclerView = view.findViewById(R.id.tripList);
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
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_title_trip_list);
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

    // TODO:  onCreateOptionsMenu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//       inflater.inflate(R.menu.fragment_crime_list, menu);
//
//       MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
//       if (mSubtitleVisible) {
//           subtitleItem.setTitle(R.string.hide_subtitle);
//       } else {
//           subtitleItem.setTitle(R.string.show_subtitle);
//       }
    }

    // TODO:  onOptionsItemSelected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
//           case R.id.new_crime:
//               addCrime();
//               return true;
//           case R.id.show_subtitle:
//               mSubtitleVisible = !mSubtitleVisible;
//               getActivity().invalidateOptionsMenu();
//               updateSubtitle();
//               return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode != Activity.RESULT_OK ) {
            return;
        }
        // Save trip to database
        if( requestCode == REQUEST_CODE_TRIP_DIALOG_SAVE) {
            Trip trip = (Trip) data.getSerializableExtra(ARGS_TRIP);
            if (trip != null) {
                updateTrip(trip);
            }
        }
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
        } else {
            Toast.makeText(getContext(), getString(R.string.toast_database_update_error), Toast.LENGTH_SHORT).show();
        }

        Log.i(TAG, getString(R.string.msg_trip_save) + ": " + ret);
        Log.i(TAG, getString(R.string.msg_trip_save) + ": " + trip.toString());
    }

    /**
     * Save trip to database and clear trip state.
     * Ripped from https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data
     */
    private void deleteTrip(Trip trip, int position) {
        // Delete from database
        int ret = TripRepo.delete(trip.getTripId());

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

        Log.i(TAG, getString(R.string.msg_trip_delete) + ": " + ret);
        Log.i(TAG, getString(R.string.msg_trip_delete) + ": " + trip.toString());
    }

    /**
     * FragmentDialog that allows the editing of the trip's details
     * @param requestCode int
     */
    private void showTripDetailDialog(Trip trip, int requestCode) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(TripDetailFragment.TAG);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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


    /*
     * Inner class:  TripHolder
     */
    private class TripHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String TAG = "TripHolder";

        private TextView mTextName;
        private TextView mTextDesc;
        private TextView mTextViewOption;

        public TripHolder (View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTextName = itemView.findViewById(R.id.textName);
            mTextDesc = itemView.findViewById(R.id.textDesc);
            mTextViewOption = itemView.findViewById(R.id.textViewOptions);
        }

        public void bind(Trip trip) {
            mTextName.setText(trip.getTripName());
            mTextDesc.setText(trip.getTripDesc());
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
    private class TripAdapter extends RecyclerView.Adapter<TripHolder> {

        private static final String TAG = "TripAdapter";

        public TripAdapter(List<Trip> trips) {
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
                    PopupMenu popup = new PopupMenu(getContext(), holder.mTextViewOption);
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
                                case R.id.tbd:
                                    //  TODO: Add action
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

        public void setTrips(List<Trip> trips) {
            mTrips = trips;
        }

    }

}
