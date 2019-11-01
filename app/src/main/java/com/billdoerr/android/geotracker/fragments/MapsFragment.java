package com.billdoerr.android.geotracker.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.model.TripDetails;
import com.billdoerr.android.geotracker.database.repo.TripDetailsRepo;
import com.billdoerr.android.geotracker.services.LocationMessageEvent;
import com.billdoerr.android.geotracker.utils.GPSUtils;
import com.billdoerr.android.geotracker.utils.MapUtils;
import com.billdoerr.android.geotracker.utils.PermissionUtils;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MapsFragment extends Fragment {

    //  Saved instance state data
    private static final String SAVED_GEO_P0INTS = "geopoints";
    private static final String SAVED_ZOOM = "zoom";

    private org.osmdroid.views.MapView mMapView;
    private Trip mTrip;
    private List<GeoPoint> mGeoPoints = new ArrayList<>();

    private boolean mIsAppInitialized = false;

    /**
     * Required empty public constructor
     */
    public MapsFragment() {
        // Pass
    }

    public static MapsFragment newInstance() {
        return new MapsFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Register event bus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        mMapView = view.findViewById(R.id.mapview);

        /* Important! Set your user agent to prevent getting banned from the osm servers.
         * Background: This setting identifies your app uniquely to tile servers. It's not the end user's identity,
         * but the name of your app. If your users abuse the tile server or your app does in some way, this will
         * prevent everyone that uses osmdroid from getting banned rather than just the users of your app.
         */
        Configuration.getInstance().setUserAgentValue(Objects.requireNonNull(getActivity()).getPackageName());

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Change the toolbar title text
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle(R.string.fragment_title_maps);

        // Get geopoints from bundle, if any
        if (savedInstanceState != null) {
            Gson gson = new Gson();
            String jsonString = savedInstanceState.getString(SAVED_GEO_P0INTS, "");
            if (!jsonString.isEmpty()) {
                mGeoPoints = gson.fromJson(jsonString, new TypeToken<List<GeoPoint>>(){}.getType());
            }

            //  TODO:  Fix this
            // Get zoom level
//            mZoom = savedInstanceState.getDouble(SAVED_ZOOM);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check location permissions, if granted 'initApp()' will be called
        // https://github.com/permissions-dispatcher/PermissionsDispatcher/issues/90
        // Check for location permissions
        checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION, PermissionUtils.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDetach() {
        // Unregister event bus, perform before calling super.onDetach()
        EventBus.getDefault().unregister(this);
        if (mMapView != null)
            mMapView.onDetach();
        mMapView = null;
        super.onDetach();
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Save geopoints to bundle
        if (mGeoPoints != null) {
            Gson gson = new Gson();
            String jsonString = gson.toJson(mGeoPoints);
            outState.putString(SAVED_GEO_P0INTS, jsonString);
        }

        // Save zoom level
        if (mMapView != null) {
            outState.putDouble(SAVED_ZOOM, mMapView.getZoomLevelDouble());
        }

        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case PermissionUtils.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initApp();
                }
                break;
//            case PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Initialize the MapView
////                    initMapView(mView);
//                }
//                break;
        }
    }

    /**
     * Module initialization steps
     */
    private void initApp() {

        // Flag to indicate app has been initialized
        mIsAppInitialized = true;

        // Maps need storage permissions
        checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        // Initialize the MapView
        MapUtils.initMapView(Objects.requireNonNull(getActivity()), mMapView);

        // If active trip, plot points
        plotActiveTrip();

        // Start GPSService if not running
        GPSUtils.startGPSService(getContext());

        // Get current location
        getCurrentLocation(getContext());

    }

    private void getCurrentLocation(Context context) {
        // Get current location
        GPSUtils.getCurrentLocation(Objects.requireNonNull(context));
    }

    /**
     * Plot geopoints if active trip
     */
    private void plotActiveTrip() {
        // Is there an active trip?
        if ( initTrip(getContext()) ) {
            // Plot markers
//            List<GeoPoint> geoPoints = MapUtils.getTripGeoPoints(getTripDetails(mTrip.getId()));
//            MapUtils.drawPolyLine(getContext(), mMapView, geoPoints);
            if ( (mTrip == null) || (mTrip.getState() < 0) ) {
                MapUtils.drawPolyLine(getContext(), mMapView, mGeoPoints);
            } else {
                List<GeoPoint> geoPoints = MapUtils.getTripGeoPoints(getTripDetails(mTrip.getId()));
                MapUtils.drawPolyLine(getContext(), mMapView, geoPoints);
            }
        }
    }

    /**
     * This method will be called when a MessageEvent is posted with a new location
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationMessageEvent locationMessageEvent) {
        // Get location data
        Location location = locationMessageEvent.getLocation();

        // Keep of list of geopoints
        mGeoPoints.add(MapUtils.LtoGeo(location));

        // Add marker
        if ( (mTrip == null) || (mTrip.getState() < 0) ) {
            MapUtils.drawPolyLine(getContext(), mMapView, mGeoPoints);
        } else {
            List<GeoPoint> geoPoints = MapUtils.getTripGeoPoints(getTripDetails(mTrip.getId()));
            MapUtils.drawPolyLine(getContext(), mMapView, geoPoints);
        }

    }

    /**
     * Returns a List<TripDetails> with the give trip id.
     * @param tripId int Table index
     * @return List<TripDetails>
     */
    private List<TripDetails> getTripDetails(int tripId) {
        return TripDetailsRepo.getTripDetails(tripId);
    }

    /**
     * Retrieve active Trip object from Shared Preferences
     * @param context Context Application context.
     * @return boolean True if active trip
     */
    private boolean initTrip(Context context) {
        Trip trip = PreferenceUtils.getActiveTripFromSharedPrefs(Objects.requireNonNull(context));
        if (trip != null) {
            // Assign to global variable
            mTrip = trip;
            return true;
        } else {
            return false;
        }
    }

    /*
     * ******************************************************************
     * Android Async Task
     * ******************************************************************
     */

//    /**
//     * AsyncTask to draw previous locations. Called when fragment is re-started (screen rotation, etc).
//     */
//    private class DrawMarkersPreviousLocations extends AsyncTask<List<TripDetails>, Void, Void> {
//
//        protected Void doInBackground(List<TripDetails>... listTripDetails) {
//            final int x;
//            final List<TripDetails> tripDetails = listTripDetails[0];
//
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    int i = 0;
//                    // Loop through and add markers
//                    final int size = tripDetails.size() - 1;
//                    boolean start = false;
//                    boolean end = false;
//                    boolean animate = false;
//
//                    for (TripDetails tripDetail : tripDetails) {
//                        if (i ==0) {
//                            start = true;
//                        }
//                        if (i == size) {
//                            end = animate = true;
//                        }
////                        GeoPoint geoPoint = new GeoPoint(tripDetails.get(i).getLatitude(), tripDetails.get(i).getLongitude(), tripDetails.get(i).getAltitude());
////                        MapUtils.drawMarker(getContext(), mMapView,geoPoint, animate, start, end);
//                        List<GeoPoint> geoPoints = MapUtils.getTripGeoPoints(getTripDetails(mTrip.getId()));
//                        MapUtils.drawPolyLine(getContext(), mMapView, geoPoints);
//                        start = end = animate = false;
//                        i++;
//                    }
//                }
//            });
//            return null;
//        }
//
//        protected void onProgressUpdate(Void... progress) {
//            // Pass
//        }
//
//        protected void onPostExecute(Void result) {
//            // Pass
//        }
//
//        protected void onPreExecute() {
//            // Pass
//        }
//    }


    /*
     * ******************************************************************
     * Android Permissions utility methods
     * ******************************************************************
     */

    /**
     * Verify required permissions have been accepted
     * @param permission String Permission being requested
     * @param resultCode int
     */
    @SuppressWarnings("SameParameterValue")
    private void checkPermissions(final String permission, final int resultCode) {
        PermissionUtils.checkPermission(Objects.requireNonNull(getActivity()), permission,
                new PermissionUtils.PermissionAskListener() {
                    @Override
                    public void onNeedPermission() {
                        requestPermissions(new String[]{permission},resultCode);
                    }
                    @Override
                    public void onPermissionPreviouslyDenied() {
                        // Show a dialog explaining permission and then request permission
                        PermissionUtils.displayPermissionsRequestDialog(getActivity(),
                                permission,
                                resultCode,
                                getString(R.string.dialog_msg_location_permissions_required));
                    }
                    @Override
                    public void onPermissionDisabled() {
                        PermissionUtils.displayAppPermissionDialog(getActivity());
                    }
                    @Override
                    public void onPermissionGranted() {
                        //  Init app
                        //  Handled by overriding the fragments onRequestPermissionsResult()

                        // Check if app has already been initialized
                        if (!mIsAppInitialized) {
                            initApp();
                        }
                    }
                });
    }

}
