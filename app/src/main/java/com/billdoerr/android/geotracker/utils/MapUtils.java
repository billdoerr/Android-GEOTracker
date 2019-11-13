package com.billdoerr.android.geotracker.utils;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.billdoerr.android.geotracker.database.model.TripDetails;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MapUtils {

    /**
     * Draws PolyLine
     * @param map MapView
     * @param geoPoints List<GeoPoint>
     */
    public static void drawPolyLine(MapView map, final List<GeoPoint> geoPoints) {

        // Nothing to plot, let's get the heck out of here
        if (geoPoints.size() <= 0) return;

        // Create polyline
        final Polyline line = new Polyline(map);
        line.getOutlinePaint().setColor(Color.RED);
        line.getOutlinePaint().setStrokeWidth(5.0f);
        line.setVisible(true);
        line.setPoints(geoPoints);

        // Add overlay
        map.getOverlays().add(line);

        // Causes the drawing cache to be invalidated
        map.invalidate();
    }

    /**
     * Plot location to map. If animate true, then will zoom into the location specified
     * @param map MapView
     * @param geoPoint GeoPoint
     * @param icon Drawable
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Marker drawMarker(MapView map, GeoPoint geoPoint, Drawable icon) {
        if (map == null) return null;

        Marker marker = new Marker(map);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(icon);

        // Add overlay
        map.getOverlays().add(marker);

        // Causes the drawing cache to be invalidated
        map.invalidate();

        return marker;
    }

    /**
     * Plot location to map. If animate true, then will zoom into the location specified
     * @param map MapView
     * @param geoPoint GeoPoint
     * @param bearing float
     * @param icon Drawable
     */
    public static Marker drawMarker(MapView map, GeoPoint geoPoint, Marker endMarker, float bearing, Drawable icon) {
        if (map == null) return null;

        if (endMarker != null) {
            map.getOverlays().remove(endMarker);
        }

        Marker marker = new Marker(map);
        marker.setPosition(geoPoint);
        marker.setRotation(bearing);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(icon);

        // Add overlay
        map.getOverlays().add(marker);

        // Causes the drawing cache to be invalidated
        map.invalidate();

        return marker;
    }

    /**
     * Zoom to GeoPoint
     * @param mapView  MapView
     * @param geoPoint GeoPoint
     */
    public static void animateTo(MapView mapView, GeoPoint geoPoint) {
        mapView.getController().animateTo(geoPoint);
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
