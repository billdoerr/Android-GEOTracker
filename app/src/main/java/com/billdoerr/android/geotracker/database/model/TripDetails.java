package com.billdoerr.android.geotracker.database.model;

import android.location.Location;

/**
 * Data model TripDetails table.
 */
public class TripDetails {

    private static final String TAG = "TripDetails";

    // Table name
    public static final String TABLE = "TripDetails";

    // Columns
    public static final String KEY_TRIP_DETAILS_TRIP_ID = "trip_id";
    public static final String KEY_TRIP_DETAILS_LATITUDE = "latitude";
    public static final String KEY_TRIP_DETAILS_LONGITUDE = "longitude";
    public static final String KEY_TRIP_DETAILS_ALTITUDE = "altitude";
    public static final String KEY_TRIP_DETAILS_TIME_STAMP = "time_stamp";
    public static final String KEY_TRIP_DETAILS_RAW_GPS_DATA = "raw_gps_data";

    private Location mLocation;
    private int tripId;
    private double latitude;
    private double longitude;
    private double altitude;
    private long timeStamp;
    private String rawGPSData;

    public void setLocation(Location location) {
        mLocation = location;
//        tripId = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        timeStamp = location.getTime();
//        rawGPSData = location.getR;
    }

    public Location getLocation() {
        return mLocation;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getRawGPSData() {
        return rawGPSData;
    }

    public void setRawGPSData(String rawGPSData) {
        this.rawGPSData = rawGPSData;
    }
}
