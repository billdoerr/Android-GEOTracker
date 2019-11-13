package com.billdoerr.android.geotracker.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.billdoerr.android.geotracker.utils.MyCompassOverlay;
import com.billdoerr.android.geotracker.utils.PermissionUtils;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.util.List;
import java.util.Objects;


public class MapsFragment extends Fragment {

    // Map constants
    private static final int TILE_SIZE_PIXELS = 256;
    private static final float SCALE_FACTOR = 1.0f;
    private static final int TILE_SIZE = 512;
    private static final double ZOOM_LEVEL = 18.0;  // Range:  2 - 21

    //  Saved instance state data
    private static final String SAVED_ZOOM = "zoom";

    private Trip mTrip;
    private boolean mIsActiveTrip = false;
    private boolean mIsAppInitialized = false;

    private org.osmdroid.views.MapView mMapView;
    private static Marker mEndMarker;
    private float mBearing = 0;
    private double mZoom;


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

        setHasOptionsMenu(true);

        mIsActiveTrip = false;
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

        mZoom = ZOOM_LEVEL;

        if (savedInstanceState != null) {
            // Get zoom level
            mZoom = savedInstanceState.getDouble(SAVED_ZOOM);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // Is there an active trip
        // This will at least initialized the mTrip object
        mIsActiveTrip = initTrip();

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

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

    /*
    * We are disabling the options menu in this fragment.
    * Must also set setHasOptionsMenu(true); in onCreate()
    *
     */
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.clear();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Pass
    }

    private void getCurrentLocation(Context context) {
        // Get current location
        GPSUtils.getCurrentLocation(Objects.requireNonNull(context));
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
        initMapView(Objects.requireNonNull(Objects.requireNonNull(getActivity()).getApplicationContext()), mMapView);

        // If active trip, plot points
        if (mIsActiveTrip) {
            plotTrip();
        }

        // Start GPSService if not running
        GPSUtils.startGPSService(getContext());

        // Get current location
        getCurrentLocation(getContext());

    }

    /**
     * Initializes the MapView
     */
    private void initMapView(Context context, MapView mapView) {

        // Set tile source
        final String[] tileUrlOutdoor = {"https://tile.thunderforest.com/outdoors/"};
        final ITileSource tileSource =
                new XYTileSource("Outdoors",
                        0,
                        (int) ZOOM_LEVEL,
                        TILE_SIZE_PIXELS,
                        ".png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716",
                        tileUrlOutdoor,
                        "from thunderforest.com");
        mapView.setTileSource(tileSource);

        // Add multi-touch capability
        mapView.setMultiTouchControls(true);

        final float density = context.getResources().getDisplayMetrics().density;
        TileSystem.setTileSize(Math.round(TILE_SIZE*density));

        /* If true, tiles are scaled to the current DPI of the display. This effectively
         * makes it easier to read labels, how it may appear pixelated depending on the map source.
         * If false, tiles are rendered in their real size.
         */
        mapView.setTilesScaledToDpi(false);

        /*
         * Setting an additional scale factor both for ScaledToDpi and standard size
         * > 1.0 enlarges map display, < 1.0 shrinks map display
         */
        mapView.setTilesScaleFactor(SCALE_FACTOR);

        // Add compass to map
//        CompassOverlay compassOverlay = new CompassOverlay(Objects.requireNonNull(context), new InternalCompassOrientationProvider(context), mapView);
        MyCompassOverlay compassOverlay = new MyCompassOverlay(Objects.requireNonNull(context), new InternalCompassOrientationProvider(context), mapView);
        compassOverlay.setMyCompassOverlayListener(new MyCompassOverlay.MyCompassOverlayListener() {
            @Override
            public void onBearingChange(float bearing) {
                if (mEndMarker != null) {
                    mBearing = bearing;
                    GeoPoint geoPoint = mEndMarker.getPosition();
                    drawEndMarker(geoPoint, bearing);
                }
            }
        });
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        /*
         * Maximum Zoom Level - we use Integers to store zoom levels so overflow happens at 2^32 - 1,
         * but we also have a tile size that is typically 2^8, so (32-1)-8-1 = 22
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
        IMapController mapController = mapView.getController();
        mapController.setZoom(mZoom);

    }

    /**
     * Plots list of GeoPoints.
     * This is called by onStart().
     */
    private void plotTrip() {
            List<GeoPoint>  geoPoints = MapUtils.getTripGeoPoints(getTripDetails(mTrip.getId()));
            plotData(geoPoints);
    }

    /**
     * This method will be called when a MessageEvent is posted with a new location
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationMessageEvent locationMessageEvent) {

        // Get location data
        Location location = locationMessageEvent.getLocation();

        // Format as GeoPoint
        GeoPoint geoPoint = MapUtils.LtoGeo(location);

        // Plot data of active trip
        if (mIsActiveTrip) {
            // We are recording trip, so plot it
            plotTrip();
        }
        // Currently not recording tracking data, just update location
        else {
            drawEndMarker(geoPoint, mBearing);
            MapUtils.animateTo(mMapView, geoPoint);
        }

    }

    /**
     * Plots lists of GeoPoints.
     * Adds start and end location markers.
     * @param geoPoints List<GeoPoint>
     */
    private void plotData(List<GeoPoint> geoPoints) {
        // Plot location data
        int size = geoPoints.size();
        if (size > 0) {
            // Draw trip
            MapUtils.drawPolyLine(mMapView, geoPoints);

            // Draw starting location marker
            MapUtils.drawMarker(mMapView, geoPoints.get(0), Objects.requireNonNull(getContext()).getDrawable(R.drawable.marker_default) );

            // Draw ending location marker
            mEndMarker = MapUtils.drawMarker(mMapView, geoPoints.get( size-1 ), mEndMarker, 0, Objects.requireNonNull(getContext()).getDrawable(R.drawable.person) );

            // Zoom in
            MapUtils.animateTo(mMapView, geoPoints.get( size-1 ));
        }

    }

    /**
     * Plot an end marker along with the bearing.
     * Note:  MapUtils.drawMarker will delete the Marker passed.
     * @param geoPoint GeoPoint
     * @param bearing float
     */
    private void drawEndMarker(GeoPoint geoPoint, float bearing) {
        // Draw ending location marker, but first check if fragment is attached
        if (isAdded()) {
            mEndMarker = MapUtils.drawMarker(mMapView, geoPoint, mEndMarker, bearing, Objects.requireNonNull(getContext()).getDrawable(R.drawable.person) );
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
     * @return boolean True if active trip
     */
    private boolean initTrip() {
        Trip trip = PreferenceUtils.getActiveTripFromSharedPrefs(Objects.requireNonNull(getContext()));

        if (trip != null) {
            // Assign to global variable
            mTrip = trip;

            // Is this an active trip
            //noinspection RedundantIfStatement
            if ( mTrip.getState() >= 0) {
                return true;
            } else {
                return false;
            }
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
