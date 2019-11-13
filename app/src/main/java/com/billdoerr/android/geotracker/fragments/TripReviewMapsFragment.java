package com.billdoerr.android.geotracker.fragments;

import android.Manifest;
import android.content.Context;
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
import com.billdoerr.android.geotracker.utils.MapUtils;
import com.billdoerr.android.geotracker.utils.PermissionUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;

import java.util.List;
import java.util.Objects;

public class TripReviewMapsFragment extends Fragment {

    // Map constants
    private static final int TILE_SIZE_PIXELS = 256;
    private static final float SCALE_FACTOR = 1.0f;
    private static final int TILE_SIZE = 512;
    private static final double ZOOM_LEVEL = 18.0;  // Range:  2 - 21

    private static final String ARGS_TRIP = "trip";

    private org.osmdroid.views.MapView mMapView;
    private Trip mTrip;

    /**
     * Required empty public constructor
     */
    public TripReviewMapsFragment() {
        // Pass
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        mTrip = (Trip) Objects.requireNonNull(args).getSerializable(ARGS_TRIP);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maps_review, container, false);

        mMapView = view.findViewById(R.id.mapview);

        /* Important! Set your user agent to prevent getting banned from the osm servers.
         * Background: This setting identifies your app uniquely to tile servers. It's not the end user's identity,
         * but the name of your app. If your users abuse the tile server or your app does in some way, this will
         * prevent everyone that uses osmdroid from getting banned rather than just the users of your app.
         */
        Configuration.getInstance().setUserAgentValue(Objects.requireNonNull(getActivity()).getPackageName());

        // Initialize the MapView
        initMapView(getActivity(), mMapView);

        return view;
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
        initMapView(Objects.requireNonNull(getActivity()), mMapView);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /*
    * We are disabling the options menu in this fragment.  Must also set setHasOptionsMenu(true); in onCreate()
     */
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.clear();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Pass
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
        mapController.setZoom(ZOOM_LEVEL);

    }

    /**
     * Draw trip markers, polyline, etc
     */
    private void plotTrip() {
        List<GeoPoint> geoPoints = MapUtils.getTripGeoPoints(getTripDetails(mTrip.getId()));
        int size = geoPoints.size();

        // Plot location data
        if (size > 0) {
            // Draw trip
            MapUtils.drawPolyLine(mMapView, geoPoints);

            // Draw starting location marker
            MapUtils.drawMarker(mMapView, geoPoints.get(0), Objects.requireNonNull(getContext()).getDrawable(R.drawable.marker_default) );

            // Draw ending location marker
            MapUtils.drawMarker(mMapView, geoPoints.get(size-1), getContext().getDrawable(R.drawable.person) );

            // Zoom in
            MapUtils.animateTo(mMapView, geoPoints.get(size-1));
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
