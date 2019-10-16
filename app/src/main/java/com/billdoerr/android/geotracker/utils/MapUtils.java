package com.billdoerr.android.geotracker.utils;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.TripDetails;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MapUtils {

    private static final String TAG = "MapUtils";

    /**
     * Draws line
     */
    public static void drawPolyLine(MapView map, final List<TripDetails> tripDetails) {
        // Convert to GeoPoints
        final List<GeoPoint> geoPoints = getTripGeoPoints(tripDetails);

        // Create polyline
        final Polyline line = new Polyline(map);
        line.setColor(Color.RED);
        line.setWidth(5.0f);
        line.setVisible(true);
        line.setPoints(geoPoints);

        map.getOverlayManager().add(line);
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
    public static void drawMarker(Context context, MapView map, GeoPoint geoPoint, boolean animate, boolean start, boolean end) {
        if (map == null || context == null) return;
        Marker marker = new Marker(map);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(context.getDrawable(R.drawable.marker_default));
        marker.setInfoWindow(null);

        map.getOverlays().add(marker);

        if (animate) {
            map.getController().animateTo(geoPoint);
        }

    }

    /**
     * Draws a list of points on map
     * @param context Context
     * @param map Mapview
     * @param tripDetails List<TripDetails>
     * @return int Number of markers drawn
     */
    public static int plotMarkers(Context context, MapView map, List<TripDetails> tripDetails) {
        // Loop through and add markers
        final int size = tripDetails.size() - 1;
        boolean start = false;
        boolean end = false;
        boolean animate = false;
        int i = 0;
        for (TripDetails tripDetail : tripDetails) {
//        for (int i = 0; i <= size; i++) {
            if (i ==0) {
                end = animate = false;
                start = true;
            }
            if (i == size) {
                end = animate = true;
            }
            GeoPoint geoPoint = new GeoPoint(tripDetails.get(i).getLatitude(), tripDetails.get(i).getLongitude(), tripDetails.get(i).getAltitude());
            drawMarker(context, map, geoPoint, animate, start, end);
            start = end = animate = false;
            i++;
        }
        map.invalidate();
        return i;
    }

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
}
