package com.billdoerr.android.geotracker.fragments;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.services.GPSUtils;
import com.billdoerr.android.geotracker.services.LocationMessageEvent;
import com.billdoerr.android.geotracker.utils.PermissionUtils;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;


public class MapsFragment extends Fragment
{

    private static final String TAG = MapsFragment.class.getSimpleName();

    //  Saved instance state data
    private static final String SAVED_MAP_TYPE = "map_type";
    private static final String SAVED_ZOOM = "zoom";

    private static final float mZoom = 15.0f;  // 2 - 21

    private org.osmdroid.views.MapView mMapView;
    private MapController mMapController;

    private LatLng mCurrentLocation;
    private LatLng mDestination;


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
    public void onAttach(Context context) {
        super.onAttach(context);
        // Register event bus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setHasOptionsMenu(true);

        //  Restore state
//        restoreValuesFromBundle(savedInstanceState);

        //  Get args
        Bundle args = getArguments();
        if(args != null) {
            //  TODO:  Get args
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        Configuration.getInstance().setUserAgentValue(getActivity().getPackageName());

        mMapView = (MapView) view.findViewById(R.id.mapview);
        mMapView.setTileSource(TileSourceFactory.USGS_TOPO);

        // Add multi-touch capability
        mMapView.setMultiTouchControls(true);

        // Add compass to map
        CompassOverlay compassOverlay = new CompassOverlay(getActivity(), new InternalCompassOrientationProvider(getActivity()), mMapView);
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
        mapController.setZoom(16.0);

//        GeoPoint startPoint = new GeoPoint(47.3159d, -121.5040d);
//        mapController.setCenter(startPoint);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check location permissions, if granted 'initApp()' will be called
        // https://github.com/permissions-dispatcher/PermissionsDispatcher/issues/90
        checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        // Get current location
        GPSUtils.getCurrentLocation(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDetach() {
        // Unregister event bus, perform before calling super.onDetach()
        EventBus.getDefault().unregister(this);
        mMapView.onDetach();
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putInt(SAVED_MAP_TYPE, mMapType);
        outState.putFloat(SAVED_ZOOM, mZoom);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_maps, menu);
//        initMapType(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_map_type_hybrid:
//                item.setChecked(true);
//                mMapType = GoogleMap.MAP_TYPE_HYBRID;
//                updateMapType();
//                return true;
//            case R.id.action_map_type_normal:
//                item.setChecked(true);
//                mMapType = GoogleMap.MAP_TYPE_NORMAL;
//                updateMapType();
//                return true;
//            case R.id.action_map_type_satellite:
//                item.setChecked(true);
//                mMapType = GoogleMap.MAP_TYPE_SATELLITE;
//                updateMapType();
//                return true;
//            case R.id.action_map_type_terrain:
//                item.setChecked(true);
//                mMapType = GoogleMap.MAP_TYPE_TERRAIN;
//                updateMapType();
//                return true;
            default:
                return false;
        }
    }

    public static Marker addMarker(Context context, MapView map, double latitude, double longitude) {
        if (map == null || context == null) return null;
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(latitude, longitude));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        marker.setIcon(context.getResources().getDrawable(R.drawable.ic_map_marker_p));
        marker.setInfoWindow(null);
        map.getOverlays().add(marker);
        map.invalidate();

        GeoPoint newGeoPoint = new GeoPoint(latitude, longitude);
        map.getController().animateTo(newGeoPoint);

        return marker;
    }

    /*
     * Util methods
     */

    /**
     * This method will be called when a MessageEvent is posted
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationMessageEvent locationMessageEvent) {
        // Get location data
        Location location = locationMessageEvent.getLocation();
        // Update screen data
        Log.i(TAG, "onMessageEvent: " + location);

        // Add marker
        addMarker(getContext(), mMapView, location.getLatitude(), location.getLongitude());
    }

    /*
     * ******************************************************************
     * Android Permissions utility methods
     * ******************************************************************
     */

    private void checkPermissions(final String permission, final int resultCode) {
        PermissionUtils.checkPermission(getActivity(), permission,
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
                        //  TODO:  initApp()
                    }
                });
    }

    /*
     * ******************************************************************
     * Google Maps
     * ******************************************************************
     */
    /**
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
//        //  TODO
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
//        mMap.addMarker(new MarkerOptions()
//                .position(location)
//                .title(getString(R.string.maps_you_are_here))
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//        );
//    }
//
//    private void updateMapDestination(LatLng location) {
//        // Add a marker for the destination
//        mMap.addMarker(new MarkerOptions()
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
//        //  TODO
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
//                //  TODO : implement save zoom value
////                mZoom = savedInstanceState.getFloat(SAVED_ZOOM);
//            }
//        }
//    }



}
