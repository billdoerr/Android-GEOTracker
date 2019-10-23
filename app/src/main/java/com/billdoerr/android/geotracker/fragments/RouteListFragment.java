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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Route;
import com.billdoerr.android.geotracker.database.repo.ActivityTypeRepo;
import com.billdoerr.android.geotracker.database.repo.RouteRepo;

import java.util.ArrayList;
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


public class RouteListFragment extends Fragment {

    private static final String TAG = "RouteListFragment";

    private static final String ARGS_ROUTE = "route";

    private static final int REQUEST_CODE_ROUTE_DIALOG_ADD = 1;
    private static final int REQUEST_CODE_ROUTE_DIALOG_EDIT = 2;
    private static final int REQUEST_CODE_ROUTE_DIALOG_FILTER = 3;

    private static final String ARGS_FILTER_ACTIVE_FLAG = "args_filter_active_flag";
    private static final String ARGS_FILTER_ACTIVITY_TYPE_ID = "args_filter_activity_type_id";

    private static final int CLEAR_FILTER = -1;

    private RouteAdapter mRouteAdapter;
    private ViewFlipper mViewFlipper;
    private List<Route> mRoutes;

    private int mCurrentPosition;

    private static int mActiveFlagFilter = CLEAR_FILTER;
    private static int mActivityTypeIdFilter = CLEAR_FILTER;

    /**
     * Required empty public constructor
     */
    public RouteListFragment() {
        // Pass
    }

    public static RouteListFragment newInstance() {
        return new RouteListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_route_list, container, false);

        // Get routes. Only returns list where end time != 0.
        mRoutes = RouteRepo.getRoutes();

        mViewFlipper = view.findViewById(R.id.viewFlipper);
        if (mRoutes.isEmpty()) {
            mViewFlipper.setDisplayedChild(mViewFlipper.indexOfChild(view.findViewById(R.id.layout_no_route_data)));
        }

        RecyclerView routeRecyclerView = view.findViewById(R.id.list);
        routeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRouteAdapter = new RouteAdapter(mRoutes);
        routeRecyclerView.setAdapter(mRouteAdapter);

        mRouteAdapter.setRoutes(mRoutes);
        mRouteAdapter.notifyDataSetChanged();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Change the toolbar title text
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle(R.string.fragment_title_route_list);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode != Activity.RESULT_OK ) {
            return;
        }

        Route route = (Route) data.getSerializableExtra(ARGS_ROUTE);
        switch (requestCode) {
            case REQUEST_CODE_ROUTE_DIALOG_ADD:
                if (route != null) {
                    insertRoute(route);
                }
                break;
            case REQUEST_CODE_ROUTE_DIALOG_EDIT:
                if (route != null) {
                    updateRoute(route);
                }
                break;
            case REQUEST_CODE_ROUTE_DIALOG_FILTER:
                mActiveFlagFilter = data.getIntExtra(ARGS_FILTER_ACTIVE_FLAG, -1);
                mActivityTypeIdFilter = data.getIntExtra(ARGS_FILTER_ACTIVITY_TYPE_ID, -1);
                // Retrieve filter results
                filterResults();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_route_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_route_add:
                showRouteDetailDialog(new Route(), REQUEST_CODE_ROUTE_DIALOG_ADD);
                return true;
            case R.id.action_apply_filter:
                showRouteFilterDialog(REQUEST_CODE_ROUTE_DIALOG_FILTER);
                return true;
            case R.id.action_clear_filter:
                // Reset flags
                mActiveFlagFilter = CLEAR_FILTER;
                mActivityTypeIdFilter = CLEAR_FILTER;
                // Clear list, re-query and notify data changed
                mRoutes.clear();
                mRoutes = RouteRepo.getRoutes();
                mRouteAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Retrieve filter results
     */
    private void filterResults() {
        mRoutes.clear();
        mRoutes = RouteRepo.filterRoutes(mActiveFlagFilter, mActivityTypeIdFilter);
        mRouteAdapter.notifyDataSetChanged();
    }


    /**
     * Inserts route to database.
     * Ripped from https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data
     * @param route Route
     */
    private void insertRoute(Route route) {
        // Insert record
        int ret = RouteRepo.insert(route);

        // Returns -1 if error
        if (ret != -1) {
            // Update RecyclerView
            mRoutes.clear();
            mRoutes = RouteRepo.getRoutes();
            mRouteAdapter.notifyDataSetChanged();
            // If this is first item inserted then need to update ViewFlipper
            if (mRoutes.size() == 1) {
                mViewFlipper.setDisplayedChild(0);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.toast_database_update_error), Toast.LENGTH_SHORT).show();
        }

        Log.i(TAG, getString(R.string.msg_route_inserted) + ": " + ret);
        Log.i(TAG, getString(R.string.msg_route_inserted) + ": " + route.toString());
    }

    /**
     * Save route to database.
     * Ripped from https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data
     * @param route Route
     */
    private void updateRoute(Route route) {
        // Update record
        int ret = RouteRepo.update(route);

        // Returns -1 if error
        if (ret != -1) {
            // Update RecyclerView
            mRoutes.set(mCurrentPosition, route);
            mRouteAdapter.notifyItemChanged(mCurrentPosition);
        } else {
            Toast.makeText(getContext(), getString(R.string.toast_database_update_error), Toast.LENGTH_SHORT).show();
        }

        Log.i(TAG, getString(R.string.msg_route_update) + ": " + ret);
        Log.i(TAG, getString(R.string.msg_route_update) + ": " + route.toString());
    }

    /**
     * Deletes route from database.
     * Ripped from https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data
     * @param route Route
     * @param position int Table index.
     */
    private void deleteRoute(Route route, int position) {
        // Delete from database
        int ret = RouteRepo.delete(route.getId());

        // If 0, then no records updated
        if (ret > 0) {
            // Update RecyclerView
            mRoutes.remove(position);
            mRouteAdapter.notifyItemRemoved(position);
            if (mRoutes.isEmpty()) {
                mViewFlipper.setDisplayedChild(1);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.toast_database_delete_error), Toast.LENGTH_SHORT).show();
        }

        Log.i(TAG, getString(R.string.msg_route_delete) + ": " + ret);
        Log.i(TAG, getString(R.string.msg_route_delete) + ": " + route.toString());
    }

    /**
     * FragmentDialog that allows the filtering of the route list
     * @param requestCode int
     */
    private void showRouteDetailDialog(Route route, int requestCode) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = Objects.requireNonNull(getActivity().getSupportFragmentManager()).beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(RouteDetailFragment.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        Bundle args = new Bundle();
        args.putSerializable(ARGS_ROUTE, route);

        // Create and show the dialog.
        DialogFragment dialogFragment = RouteDetailFragment.newInstance();
        dialogFragment.setArguments(args);
        dialogFragment.setTargetFragment(this, requestCode);
        dialogFragment.show(ft, RouteDetailFragment.TAG);
    }

    /**
     * Display confirmation dialog
     * @param route Route
     */
    private void showRouteDeleteDialog(final Route route, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.dialog_title_route_delete);
        builder.setMessage(R.string.dialog_msg_route_delete);
        builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                deleteRoute(route, position);

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
    private void showRouteFilterDialog(int requestCode) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = Objects.requireNonNull(getActivity().getSupportFragmentManager()).beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(RouteDetailFragment.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        Bundle args = new Bundle();
        args.putSerializable(ARGS_ROUTE, new Route());
        args.putInt(ARGS_FILTER_ACTIVE_FLAG, mActiveFlagFilter);
        args.putInt(ARGS_FILTER_ACTIVITY_TYPE_ID, mActivityTypeIdFilter);

        // Create and show the dialog.
        DialogFragment dialogFragment = RouteListFilterFragment.newInstance();
        dialogFragment.setArguments(args);
        dialogFragment.setTargetFragment(this, requestCode);
        dialogFragment.show(ft, RouteListFilterFragment.TAG);
    }

    /*
     * Inner class:  RouteHolder
     */
    private class RouteHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String TAG = "RouteHolder";

        private final TextView mTextName;
        private final TextView mTextDesc;
        private final TextView mTextActivity;
        private final TextView mTextViewOption;

        private RouteHolder (View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTextName = itemView.findViewById(R.id.textName);
            mTextDesc = itemView.findViewById(R.id.textDesc);
            mTextActivity = itemView.findViewById(R.id.textActivity);
            mTextViewOption = itemView.findViewById(R.id.textViewOptions);
        }

        private void bind(Route route) {
            mTextName.setText(route.getName());
            mTextDesc.setText(route.getDesc());
            // Query the ActivityTypeName.
            mTextActivity.setText(ActivityTypeRepo.getActivityName(route.getActivityTypeId()));
        }

        @Override
        public void onClick(View view) {
            // Store current index
            mCurrentPosition = this.getAdapterPosition();
        }

    }


    /*
     * Inner Class:  RouteAdapter
     */
    private class RouteAdapter extends RecyclerView.Adapter<RouteHolder> implements Filterable {

        private static final String TAG = "RouteAdapter";

        private List<Route> routeListFiltered;

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
        public void onBindViewHolder(@NonNull final RouteHolder holder, int position) {
            final Route route = mRoutes.get(position);
            holder.bind(route);

            // Ripped from https://www.simplifiedcoding.net/create-options-menu-recyclerview-item-tutorial/
            holder.mTextViewOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Creating a popup menu
                    PopupMenu popup = new PopupMenu(Objects.requireNonNull(getContext()), holder.mTextViewOption);
                    // Inflating menu from xml resource
                    popup.inflate(R.menu.menu_route_list_item);
                    // Adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.editRoute:
                                    mCurrentPosition = holder.getAdapterPosition();
                                    showRouteDetailDialog(route, REQUEST_CODE_ROUTE_DIALOG_EDIT);
                                    return true;
                                case R.id.deleteRoute:
                                    showRouteDeleteDialog(route, holder.getAdapterPosition());
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
            return mRoutes.size();
        }

        public void setRoutes(List<Route> routes) {
            mRoutes = routes;
        }

        // Ripped from:  https://www.androidhive.info/2017/11/android-recyclerview-with-search-filter-functionality/
        @Override
        public Filter getFilter() {
            return new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        routeListFiltered = mRoutes;
                    } else {
                        List<Route> filteredList = new ArrayList<>();
                        for (Route row : mRoutes) {

                            // Name match condition. This might differ depending on your requirement
                            // here we are looking for name or phone number match
                            if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getName().contains(charSequence)) {
                                filteredList.add(row);
                            }
                        }

                        routeListFiltered = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = routeListFiltered;
                    return filterResults;
                }

                @SuppressWarnings (value="unchecked")
                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mRoutes = (ArrayList<Route>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

}
