package com.billdoerr.android.geotracker.database.model;

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

    private int tripId;
    private float latitude;
    private float longitude;
    private float altitude;
    private String timeStamp;
    private String rawGPSData;

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getRawGPSData() {
        return rawGPSData;
    }

    public void setRawGPSData(String rawGPSData) {
        this.rawGPSData = rawGPSData;
    }
}
