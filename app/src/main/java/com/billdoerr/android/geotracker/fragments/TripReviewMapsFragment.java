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

    public static final String TAG = "TripReviewMapsFragment";

    private static final String ARGS_TRIP = "trip";

    //  Saved instance state data
    private static final String SAVED_MAP_TYPE = "map_type";
    private static final String SAVED_ZOOM = "zoom";

    private static final double mZoom = 15.0;  // Range:  2 - 21

    private View mView;
    private org.osmdroid.views.MapView mMap;
    private Trip mTrip;

    private List<TripDetails> mTripDetails;
    private List<GeoPoint> mGeoPoints;


    /**
     * Required empty public constructor
     */
    public TripReviewMapsFragment() {
        // Pass
    }

    public static TripReviewMapsFragment newInstance() {
        return new TripReviewMapsFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);

        Bundle args = getArguments();
        mTrip = (Trip) args.getSerializable(ARGS_TRIP);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_maps_review, container, false);

        Configuration.getInstance().setUserAgentValue(Objects.requireNonNull(getActivity()).getPackageName());

        createMapView(mView);

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

        //  TODO:  'initApp()'
        // Check location permissions, if granted 'initApp()' will be called
        // https://github.com/permissions-dispatcher/PermissionsDispatcher/issues/90
        checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

    }

    @Override
    public void onResume() {
        super.onResume();
        createMapView(mView);
//        if (mMap != null) {
////            mMap.onResume();
            plotTrip();
//        }
    }

    @Override
    public void onPause() {
        if (mMap != null) {
//            mMap.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDetach() {
        if (mMap != null)
            mMap.onDetach();
        mMap = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void createMapView(View view) {

        final String[] tileUrlOutdoor = {"https://tile.thunderforest.com/outdoors/"};

        final ITileSource tileSource =
                new XYTileSource("Outdoors",
                        0,
                        (int)mZoom,
                        256,
                        ".png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716",
                        tileUrlOutdoor,
                        "from thunderforest.com");

        mMap = view.findViewById(R.id.mapview);
        mMap.setTileSource(tileSource);

        // Add multi-touch capability
        mMap.setMultiTouchControls(true);

        final float density = getResources().getDisplayMetrics().density;
        TileSystem.setTileSize(Math.round(256*density));

        /* If true, tiles are scaled to the current DPI of the display. This effectively
         * makes it easier to read labels, how it may appear pixelated depending on the map
         * source.<br>
         * If false, tiles are rendered in their real size.
         */
        mMap.setTilesScaledToDpi(true);

        /*
         * Setting an additional scale factor both for ScaledToDpi and standard size
         * > 1.0 enlarges map display, < 1.0 shrinks map display
         */
        mMap.setTilesScaleFactor(2);

        // Add compass to map
        CompassOverlay compassOverlay = new CompassOverlay(getActivity(), new InternalCompassOrientationProvider(getActivity()), mMap);
        compassOverlay.enableCompass();
        mMap.getOverlays().add(compassOverlay);

        IMapController mapController = mMap.getController();
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

        // Plot trip
//        plotTrip();

    }

    /**
     * Draw trip markers
     */
    private void plotTrip() {
        mTripDetails = getTripDetails(mTrip.getId());
        mGeoPoints = MapUtils.getTripGeoPoints(mTripDetails);
        MapUtils.plotMarkers(getContext(), mMap, mTripDetails );
//        MapUtils.drawPolyLine(mMap, mTripDetails);
    }


    /**
     * Returns a List<TripDetails> with the give trip id.
     * @param tripId
     * @return
     */
    public List<TripDetails> getTripDetails(int tripId) {
        return TripDetailsRepo.getTripDetails(tripId);
    }

    /*
     * ******************************************************************
     * Android Permissions utility methods
     * ******************************************************************
     */

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
                        //  TODO:  initApp()
                    }
                });
    }

}
