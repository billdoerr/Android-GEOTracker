package com.billdoerr.android.geotracker.utils;

import java.io.Serializable;

public class SharedPreferencesUtils implements Serializable {

    private boolean mUnitsMetric;
    private boolean mUnitsNautical;
    private int mCoordinateType;
    private long mLocationServicesUpdateInterval;
    private float mLocationServicesUpdateDistance;
    // Feature supporting this has not been implemented
//    private static int mCoordinateDatum;
    private boolean mKeepDeviceAwake;


    public SharedPreferencesUtils() {
        // Pass
    }

    /**
     * Returns true if using metric system.
     * @return boolean
     */
    public boolean isMetric() {
        return mUnitsMetric;
    }

    /**
     * Sets units to use the metric system
     * @param unitsMetric boolean
     */
    public void setMetric(boolean unitsMetric) {
        mUnitsMetric = unitsMetric;
    }

    /**
     * Returns true if using nautical measurement.
     * @return boolean
     */
    public boolean isNautical() {
        return mUnitsNautical;
    }

    /**
     * Sets units to use the nautical system
     * @param unitsNautical boolean
     */
    public void setNautical(boolean unitsNautical) {
        mUnitsNautical = unitsNautical;
    }

    /**
     * Returns type of coordinates being used.  Maps to CoordinateType interface.
     * @return int
     */
    public int getCoordinateType() {
        return mCoordinateType;
    }

    /**
     * Sets type of coordinates being used.
     * @param coordinateType int Maps to CoordinateType interface.
     */
    public void setCoordinateType(int coordinateType) {
        mCoordinateType = coordinateType;
    }

    /**
     * Returns minimum time interval between location updates, in milliseconds.
     * @return long
     */
    public long getLocationServicesUpdateInterval() {
        return mLocationServicesUpdateInterval;
    }

    /**
     * Sets minimum time interval between location updates, in milliseconds.
     * @param locationServicesUpdateInterval int
     */
    public void setLocationServicesUpdateInterval(long locationServicesUpdateInterval) {
        mLocationServicesUpdateInterval = locationServicesUpdateInterval;
    }

    /**
     *  Returns minimum distance between location updates, in meters.
     * @return float
     */
    public float getLocationServicesUpdateDistance() {
        return mLocationServicesUpdateDistance;
    }

    /**
     * Sets minimum distance between location updates, in meters.
     * @param locationServicesUpdateDistance float
     */
    public void setLocationServicesUpdateDistance(float locationServicesUpdateDistance) {
        mLocationServicesUpdateDistance = locationServicesUpdateDistance;
    }

    // Feature supporting this has not been implemented
//    public int getCoordinateDatum() {
//        return mCoordinateDatum;
//    }

    // Feature supporting this has not been implemented
//    public void setCoordinateDatum(int coordinateDatum) {
//        mCoordinateDatum = coordinateDatum;
//    }


    /**
     * Check preferences whether to keep screen on
     * @return boolean
     */
    public boolean isKeepDeviceAwake() {
        return mKeepDeviceAwake;
    }

    /**
     * Check preferences whether to keep screen on
     * @param keepDeviceAwake boolean
     */
    public void setKeepDeviceAwake(boolean keepDeviceAwake) {
        mKeepDeviceAwake = keepDeviceAwake;
    }
}
