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
    public static final String KEY_TRIP_PAUSED_TIME = "paused_time";
    public static final String KEY_TRIP_TOTAL_TIME = "total_time";
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
    private long mStartTime;
    private long mEndTime;
    private int mState;
    private long mPausedTime;
    private long mTotalTimeInMillis;
    private long mPausedTimeInMillis;
    private int mActivityTypeId;
    private int mActiveFlag;

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public long getPausedTime() {
        return mPausedTime;
    }

    public void setPausedTime(long pausedTime) {
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

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String desc) {
        this.mDesc = desc;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        this.mStartTime = startTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long endTime) {
        this.mEndTime = endTime;
    }

    public int getActivityTypeId() {
        return mActivityTypeId;
    }

    public void setActivityTypeId(int activityTypeId) {
        this.mActivityTypeId = activityTypeId;
    }

    public int isActive() {
        return mActiveFlag;
    }

    public void setActive(int activeFlag) {
        this.mActiveFlag = activeFlag;
    }

}
