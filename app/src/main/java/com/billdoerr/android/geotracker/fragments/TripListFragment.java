package com.billdoerr.android.geotracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.repo.TripRepo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class TripListFragment extends Fragment {

    private static final String TAG = "TripListFragment";

    private static final String ARGS_TRIP = "Trip";

    private List<Trip> mTrips;
    private RecyclerView mTripRecyclerView;
    private TripAdapter mTripAdapter;
    private FloatingActionButton mFab;


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

        mFab = (FloatingActionButton) view.findViewById(R.id.fabAdd);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // Pass selected Trip to fragment
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable(GlobalVariables.ARGS_TRIP, mTrips.get(mCurrentPosition));
            TrackingFragment trackingFragment = new TrackingFragment();
//                    trackingFragment.setArguments(bundle);

            // Display fragment
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, trackingFragment)
                    .addToBackStack(null)
                    .commit();
        }
        });

        // Get data
        mTrips = new TripRepo().getTrips();

        mTripRecyclerView = (RecyclerView) view.findViewById(R.id.tripList);
        mTripRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mTripAdapter = new TripAdapter(mTrips);
        mTripRecyclerView.setAdapter(mTripAdapter);

        mTripAdapter.setTrips(mTrips);
        mTripAdapter.notifyDataSetChanged();

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

    // TODO:  onCreateOptionsMenu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
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


    /**
     * Inner class:  TripHolder
     */
    private class TripHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String TAG = "TripHolder";

        private Trip mTrip;
        private CardView cv;
        private TextView mTextName;
        private TextView mTextDesc;
        private int mCurrentPosition;

        public TripHolder (View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            cv = (CardView) itemView.findViewById(R.id.cv);
            mTextName = (TextView) itemView.findViewById(R.id.textName);
            mTextDesc = (TextView) itemView.findViewById(R.id.textDesc);
        }

        public void bind(Trip trip) {
            mTrip = trip;
            mTextName.setText(mTrip.getTripName());
            mTextDesc.setText(mTrip.getTripDesc());
        }

        @Override
        public void onClick(View view) {
            // Store current index
            mCurrentPosition = this.getAdapterPosition();

            // Pass selected Trip to fragment
            Bundle bundle = new Bundle();
            bundle.putSerializable(ARGS_TRIP, mTrips.get(mCurrentPosition));
            TripDetailFragment tripDetailFragment = new TripDetailFragment();
            tripDetailFragment.setArguments(bundle);

            // Display fragment
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, tripDetailFragment )
                    .addToBackStack(null)
                    .commit();
        }

    }

    /**
     * Inner Class:  TripAdapter
     */
    private class TripAdapter extends RecyclerView.Adapter<TripHolder> {

        private static final String TAG = "TripAdapter";

        public TripAdapter(List<Trip> trips) {
            mTrips = trips;
        }

        @Override
        public TripHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.fragment_activity_type_list_item, parent, false);

            TripHolder viewHolder = new TripHolder(listItem);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(TripHolder holder, int position) {
            Trip trip = mTrips.get(position);
            holder.bind(trip);
        }

        @Override
        public int getItemCount() {
            return mTrips.size();
        }

        public void setTrips(List<Trip> trips) {
            mTrips = trips;
        }

        public void updateList() {
            mTrips.clear();
            mTrips = new TripRepo().getTrips();
        }

    }

}
