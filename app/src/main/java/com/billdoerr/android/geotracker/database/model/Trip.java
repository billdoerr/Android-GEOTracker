package com.billdoerr.android.geotracker.database.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Data model for Trip table.
 */
public class Trip implements Serializable {

    private static final String TAG = "Trip";

    // Table mName
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

    public @interface TripState {
        int NO_PERMISSIONS = -99;
        int NOT_STARTED = -1;
        int RUNNING = 0;
        int PAUSED =  1;
        int STOPPED = 2;
    }

    private int mId;
    private String mName;
    private String mDesc;
    private Date mStartTime;
    private Date mEndTime;
    private int mActivityTypeId;
    private int mActiveFlag;

    //  TODO:  Should I add these here???
    private int mState;
    private Date mPausedTime;
    private long mTotalTimeInMillis;
    private long mPausedTimeInMillis;

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public Date getPausedTime() {
        return mPausedTime;
    }

    public void setPausedTime(Date pausedTime) {
        mPausedTime = pausedTime;
    }

    public long getTotalTimeInMillis() {
        return mTotalTimeInMillis;
    }

    public void setTotalTimeInMillis(long totalTimeInMillis) {
        mTotalTimeInMillis = totalTimeInMillis;
    }

    public long getPausedTimeInMillis() {
        return mPausedTimeInMillis;
    }

    public void setPausedTimeInMillis(long pausedTimeInMillis) {
        mPausedTimeInMillis = pausedTimeInMillis;
    }

    public int getTripId() {
        return mId;
    }

    public void setTripId(int id) {
        this.mId = id;
    }

    public String getTripName() {
        return mName;
    }

    public void setTripName(String name) {
        this.mName = name;
    }

    public String getTripDesc() {
        return mDesc;
    }

    public void setTripDesc(String desc) {
        this.mDesc = desc;
    }

    public Date getTripStartTime() {
        return mStartTime;
    }

    public void setTripStartTime(Date startTime) {
        this.mStartTime = startTime;
    }

    public Date getTripEndTime() {
        return mEndTime;
    }

    public void setTripEndTime(Date endTime) {
        this.mEndTime = endTime;
    }

    public int getTripActivityTypeId() {
        return mActivityTypeId;
    }

    public void setTripActivityTypeId(int activityTypeId) {
        this.mActivityTypeId = activityTypeId;
    }

    public int getTripActiveFlag() {
        return mActiveFlag;
    }

    public void setTripActiveFlag(int activeFlag) {
        this.mActiveFlag = activeFlag;
    }

}
