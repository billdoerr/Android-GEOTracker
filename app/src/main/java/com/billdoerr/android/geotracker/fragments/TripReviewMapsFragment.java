package com.billdoerr.android.geotracker.fragments;

import android.Manifest;
import android.content.Context;
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
import com.billdoerr.android.geotracker.utils.MapUtils;
import com.billdoerr.android.geotracker.utils.PermissionUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.util.List;
import java.util.Objects;

public class TripReviewMapsFragment extends Fragment {

    private static final String ARGS_TRIP = "trip";

    //  Saved instance state data
//    private static final String SAVED_MAP_TYPE = "map_type";
//    private static final String SAVED_ZOOM = "zoom";

    private static final double mZoom = 10.0;  // Range:  2 - 21

    private View mView;
    private org.osmdroid.views.MapView mMapView;
    private Trip mTrip;

    /**
     * Required empty public constructor
     */
    public TripReviewMapsFragment() {
        // Pass
    }

// --Commented out by Inspection START (10/23/2019 2:14 PM):
//    public static TripReviewMapsFragment newInstance() {
//        return new TripReviewMapsFragment();
//    }
// --Commented out by Inspection STOP (10/23/2019 2:14 PM)

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);

        Bundle args = getArguments();
        mTrip = (Trip) Objects.requireNonNull(args).getSerializable(ARGS_TRIP);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_maps_review, container, false);

        /* Important! Set your user agent to prevent getting banned from the osm servers.
         * Background: This setting identifies your app uniquely to tile servers. It's not the end user's identity,
         * but the name of your app. If your users abuse the tile server or your app does in some way, this will
         * prevent everyone that uses osmdroid from getting banned rather than just the users of your app.
         */
        Configuration.getInstance().setUserAgentValue(Objects.requireNonNull(getActivity()).getPackageName());

        // Initialize the MapView
        initMapView(mView);

        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Change the toolbar title text
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle(R.string.fragment_title_maps);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check location permissions, if granted 'initApp()' will be called
        // https://github.com/permissions-dispatcher/PermissionsDispatcher/issues/90
        checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onResume() {
        super.onResume();
        initMapView(mView);
        if (mMapView != null) {
            mMapView.onResume();
            plotTrip();
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
        super.onSaveInstanceState(outState);
    }

    /**
     * Initializes the MapView
     * @param view View
     */
    private void initMapView(View view) {

        final String[] tileUrlOutdoor = {"https://tile.thunderforest.com/outdoors/"};

        final ITileSource tileSource =
                new XYTileSource("Outdoors",
                        0,
                        (int)mZoom,
                        256,
                        ".png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716",
                        tileUrlOutdoor,
                        "from thunderforest.com");

        mMapView = view.findViewById(R.id.mapview);
        mMapView.setTileSource(tileSource);

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
     * Draw trip markers, polyline, etc
     */
    private void plotTrip() {
        List<GeoPoint> geoPoints = MapUtils.getTripGeoPoints(getTripDetails(mTrip.getId()));
        MapUtils.drawPolyLine(getContext(), mMapView, geoPoints);
    }

    /**
     * Returns a List<TripDetails> with the give trip id.
     * @param tripId int Table index
     * @return List<TripDetails>
     */
    private List<TripDetails> getTripDetails(int tripId) {
        return TripDetailsRepo.getTripDetails(tripId);
    }

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

}
