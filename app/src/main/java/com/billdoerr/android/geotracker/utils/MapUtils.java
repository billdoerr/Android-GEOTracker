package com.billdoerr.android.geotracker.utils;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.TripDetails;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapUtils {

//    private static List<Marker> sMarkers;
//    private static Marker mStartMarker;
//    private static Marker mEndMarker;

    private static final float SCALE_FACTOR = 1.0f;
    private static final int TILE_SIZE = 512;
    private static final double ZOOM_LEVEL = 18.0;  // Range:  2 - 21

    /**
     * Initializes the MapView
     */
    public static void initMapView(Context context, MapView mapView) {

        final String[] tileUrlOutdoor = {"https://tile.thunderforest.com/outdoors/"};

        final ITileSource tileSource =
                new XYTileSource("Outdoors",
                        0,
                        (int) ZOOM_LEVEL,
                        256,
                        ".png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716",
                        tileUrlOutdoor,
                        "from thunderforest.com");

//        mapView = view.findViewById(R.id.mapview);
//        mapView.setTileSource(TileSourceFactory.USGS_TOPO);
        mapView.setTileSource(tileSource);

        // Add multi-touch capability
        mapView.setMultiTouchControls(true);

        final float density = context.getResources().getDisplayMetrics().density;
        TileSystem.setTileSize(Math.round(TILE_SIZE*density));

        /* If true, tiles are scaled to the current DPI of the display. This effectively
         * makes it easier to read labels, how it may appear pixelated depending on the map
         * source.<br>
         * If false, tiles are rendered in their real size.
         */
        mapView.setTilesScaledToDpi(false);

        /*
         * Setting an additional scale factor both for ScaledToDpi and standard size
         * > 1.0 enlarges map display, < 1.0 shrinks map display
         */
        mapView.setTilesScaleFactor(SCALE_FACTOR);

        // Add compass to map
        CompassOverlay compassOverlay = new CompassOverlay(Objects.requireNonNull(context), new InternalCompassOrientationProvider(context), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        IMapController mapController = mapView.getController();
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
        mapController.setZoom(ZOOM_LEVEL);

    }

    /**
     * Draws PolyLine
     */
    public static void drawPolyLine(Context context, MapView map, final List<GeoPoint> geoPoints) {

        //  Need to remove last added marker
        map.getOverlays().clear();

        // Nothing to plot, let's get the heck out of here
        if (geoPoints.size() <= 0) return;

        // Place marker at first GeoPoint
        drawMarker(context, map, geoPoints.get(0), false, true, false);

        // Create polyline
        final Polyline line = new Polyline(map);
        line.getOutlinePaint().setColor(Color.RED);
        line.getOutlinePaint().setStrokeWidth(5.0f);
        line.setVisible(true);
        line.setPoints(geoPoints);
        map.getOverlayManager().add(line);

        // Place marker and zoom to last GeoPoint
        drawMarker(context, map, geoPoints.get( geoPoints.size()-1 ), true, false, true);

        map.invalidate();
    }

    /**
     * Plot location to map. If animate true, then will zoom into the location specified
     * @param context Context
     * @param map MapView
     * @param geoPoint GeoPoint
     * @param animate boolean True if zoom animation
     * @param start boolean True if starting point
     * @param end boolean True if ending point
     */
    private static void drawMarker(Context context, MapView map, GeoPoint geoPoint, boolean animate, boolean start, boolean end) {
        if (map == null || context == null) return;

        Marker marker = new Marker(map);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setInfoWindow(null);

        // Set marker icon.
        if (start) {
            marker.setIcon(context.getDrawable(R.drawable.marker_default));
//            mStartMarker = marker;
        } else if (end) {
            marker.setIcon(context.getDrawable(R.drawable.person));
//            mEndMarker = marker;
        } else {
            marker.setIcon(context.getDrawable(R.drawable.marker_default));
        }

        // Add overlay
        map.getOverlays().add(marker);

        // Zoom to GeoPoint
        if (animate) {
            map.getController().animateTo(geoPoint);
        }

        map.invalidate();
    }

//    /**
//     * Draws a list of points on map
//     * @param context Context
//     * @param map Mapview
//     * @param geoPoints List<GeoPoint>
//     * @return int Number of markers drawn
//     */
//    public static int plotMarkers(Context context, MapView map, List<GeoPoint> geoPoints) {
//        // Loop through and add markers
//        final int size = geoPoints.size() - 1;
//        boolean start = false;
//        boolean end = false;
//        boolean animate = false;
//        int i = 0;
//        for (GeoPoint geoPoint : geoPoints) {
////        for (int i = 0; i <= size; i++) {
//            if (i ==0) {
//                start = true;
//            }
//            if (i == size) {
//                end = animate = true;
//            }
//            drawMarker(context, map, geoPoint, animate, start, end);
//            start = end = animate = false;
//            i++;
//        }
//        map.invalidate();
//        return i;
//    }

    /**
     * Converts Android Location to osmdroid GeoPoint
     * @param location Location
     * @return GeoPoint
     */
    public static GeoPoint LtoGeo(Location location) {
        return new GeoPoint(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    /**
     * Converts List<TripsDetails> to List<GeoPoint>
     * @param tripDetails List<TripsDetails>
     * @return List<GeoPoint>
     */
    public static ArrayList<GeoPoint> getTripGeoPoints(List<TripDetails> tripDetails) {
        ArrayList<GeoPoint> track = new ArrayList<>();
        for (TripDetails tripDetail : tripDetails) {
            GeoPoint geoPoint = new GeoPoint(tripDetail.getLatitude(), tripDetail.getLongitude(), tripDetail.getAltitude());
            track.add(geoPoint);
        }
        return track;
    }

//    public static Marker getStartMarker() {
//        return mStartMarker;
//    }

//    public static Marker getEndMarker() {
//        return mEndMarker;
//    }

}
