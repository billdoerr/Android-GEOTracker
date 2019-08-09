package com.billdoerr.android.geotracker.database.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Data model for Trip table.
 */
public class Trip implements Serializable {

    private static final String TAG = "Trip";

    // Table name
    public static final String TABLE = "Trip";

    // Columns
    public static final String KEY_TRIP_ID = "trip_id";
    public static final String KEY_TRIP_NAME = "trip_name";
    public static final String KEY_TRIP_DESC = "trip_desc";
    public static final String KEY_TRIP_START_TIME = "start_time";
    public static final String KEY_TRIP_END_TIME = "end_time";
    public static final String KEY_TRIP_ACTIVITY_TYPE_ID = "activity_id";
    public static final String KEY_TRIP_ACTIVE_FLAG = "active_flag";

    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;

    private int id;
    private String name;
    private String desc;
    private Date startTime;
    private Date endTime;
    private int activityTypeId;
    private int activeFlag;

    public int getTripId() {
        return id;
    }

    public void setTripId(int id) {
        this.id = id;
    }

    public String getTripName() {
        return name;
    }

    public void setTripName(String name) {
        this.name = name;
    }

    public String getTripDesc() {
        return desc;
    }

    public void setTripDesc(String desc) {
        this.desc = desc;
    }

    public Date getTripStartTime() {
        return startTime;
    }

    public void setTripStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getTripEndTime() {
        return endTime;
    }

    public void setTripEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getTripActivityTypeId() {
        return activityTypeId;
    }

    public void setTripActivityTypeId(int activityTypeId) {
        this.activityTypeId = activityTypeId;
    }

    public int getTripActiveFlag() {
        return activeFlag;
    }

    public void setTripActiveFlag(int activeFlag) {
        this.activeFlag = activeFlag;
    }

}
