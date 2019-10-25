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
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

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

    private double mZoom = 18.0;  // Range:  2 - 21

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

        /* Important! Set your user agent to prevent getting banned from the osm servers.
         * Background: This setting identifies your app uniquely to tile servers. It's not the end user's identity,
         * but the name of your app. If your users abuse the tile server or your app does in some way, this will
         * prevent everyone that uses osmdroid from getting banned rather than just the users of your app.
         */
        Configuration.getInstance().setUserAgentValue(Objects.requireNonNull(getActivity()).getPackageName());

        // Initialize the MapView
        initMapView(view);

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

            // Get zoom level
            mZoom = savedInstanceState.getDouble(SAVED_ZOOM);
        }

        IMapController mapController = mMapView.getController();
        mapController.setZoom(mZoom);

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check location permissions, if granted 'initApp()' will be called
        // https://github.com/permissions-dispatcher/PermissionsDispatcher/issues/90
        checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        // Check for location permissions
        checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION, PermissionUtils.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        // Get current location
        getCurrentLocation(getContext());

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
        outState.putDouble(SAVED_ZOOM, mMapView.getZoomLevelDouble());

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
        }
    }

    /**
     * Module initialization steps
     */
    private void initApp() {
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
     * Initializes the mapping component
     * @param view View
     */
    private void initMapView(View view) {

        final String[] tileUrlOutdoor = {"https://tile.thunderforest.com/outdoors-v2/"};

        final ITileSource tileSourceThunderforest =
                new XYTileSource("Outdoors",
                        0,
                        (int)mZoom,
                        256,
                        ".png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716",
                        tileUrlOutdoor,
                        "from thunderforest.com");

        mMapView = view.findViewById(R.id.mapview);
//        mMapView.setTileSource(TileSourceFactory.USGS_TOPO);
        mMapView.setTileSource(tileSourceThunderforest);
//        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        // Add multi-touch capability
        mMapView.setMultiTouchControls(true);

        final float density = getResources().getDisplayMetrics().density;
        TileSystem.setTileSize(Math.round(256*density));

        /* If true, tiles are scaled to the current DPI of the display. This effectively
        * makes it easier to read labels, how it may appear pixelated depending on the map
        * source.<br>
        * If false, tiles are rendered in their real size.
        */
        mMapView.setTilesScaledToDpi(true);

        /*
         * Setting an additional scale factor both for ScaledToDpi and standard size
         * > 1.0 enlarges map display, < 1.0 shrinks map display
         */
        mMapView.setTilesScaleFactor(2);

        // Add compass to map
        CompassOverlay compassOverlay = new CompassOverlay(Objects.requireNonNull(getActivity()), new InternalCompassOrientationProvider(getActivity()), mMapView);
        compassOverlay.enableCompass();
        mMapView.getOverlays().add(compassOverlay);

        IMapController mapController = mMapView.getController();
        /*
        Approximate Map Scale 	OSM Zoom Level
        5M 	                        5
        2M 	                        8
        1M 	                        9
        500k 	                    10
        250k 	                    11-12
        50 	                        13-14
        25k 	                    15
        8k 	                        16
        */
        mapController.setZoom(mZoom);

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
                    }
                });
    }

    /*
     * ******************************************************************
     * Google Maps
     * ******************************************************************
     */
    /*
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        Log.i(TAG, "onMapReady");
//        mMap = googleMap;
//
//        //  Update the map type
//        updateMapType();
//
//        updateMapCurrentLocation(new LatLng(47.3159d, -121.5040d));
//
//        //  Add a marker for the destination
//        updateMapDestination(new LatLng(47.3159d, -121.5040d));
//
//        // Zoom in
//        zoom(new LatLng(47.3159d, -121.5040d), mZoom);
//
//    }

//    private void initMapType(Menu menu) {
//        //  Set default map type
//        if (mMapType == GoogleMap.MAP_TYPE_HYBRID) {
//            menu.findItem(R.id.action_map_type_hybrid).setChecked(true);
//        }
//        if (mMapType == GoogleMap.MAP_TYPE_NORMAL) {
//            menu.findItem(R.id.action_map_type_normal).setChecked(true);
//        }
//        if (mMapType == GoogleMap.MAP_TYPE_SATELLITE) {
//            menu.findItem(R.id.action_map_type_satellite).setChecked(true);
//        }
//        if (mMapType == GoogleMap.MAP_TYPE_TERRAIN) {
//            menu.findItem(R.id.action_map_type_terrain).setChecked(true);
//        }
//        updateMapType();
//    }

//    private void updateMapType() {
//        mMap.setMapType(mMapType);
//    }
//
//    private void updateMapCurrentLocation(LatLng location) {
//        // Add a marker to current location
//        mMap.drawMarker(new MarkerOptions()
//                .position(location)
//                .title(getString(R.string.maps_you_are_here))
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//        );
//    }
//
//    private void updateMapDestination(LatLng location) {
//        // Add a marker for the destination
//        mMap.drawMarker(new MarkerOptions()
//                .position(location)
//                .title(getString(R.string.maps_destination))
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
//        );
//    }
//
//    private void zoom(LatLng location, float zoom) {
//        // mMap.moveCamera(CameraUpdateFactory.newLatLng(mLocation));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
//    }
//
//    private void setDestination(LatLng location) {
//        //  47°31′59″N 121°50′40″W
//        mDestination = location;
//    }
//
//    private void setCurrentLocation(LatLng location) {
//        mCurrentLocation = location;
//    }
//
//    // **********************************************************************
//    //  SYSTEM RELATED
//    //  *********************************************************************
//    //  Restoring values from saved instance state
//    private void restoreValuesFromBundle(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            if (savedInstanceState.containsKey(SAVED_MAP_TYPE)) {
//                mMapType = savedInstanceState.getInt(SAVED_MAP_TYPE);
//            }
//
//            if (savedInstanceState.containsKey(SAVED_ZOOM)) {
////                mZoom = savedInstanceState.getFloat(SAVED_ZOOM);
//            }
//        }
//    }

}
