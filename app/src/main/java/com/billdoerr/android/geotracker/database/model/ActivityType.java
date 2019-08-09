package com.billdoerr.android.geotracker.database.model;

/**
 * Data model for ActivityType table.
 */
public class ActivityType {

    private static final String TAG = "ActivityType";

    // Table name
    public static final String TABLE = "ActivityType";

    // Columns
    public static final String KEY_ACTIVITY_TYPE_ID = "activity_type_id";
    public static final String KEY_ACTIVITY_TYPE_NAME = "activity_type_name";
    public static final String KEY_ACTIVITY_TYPE_DESC = "activity_type_desc";
    public static final String KEY_ACTIVITY_TYPE_ACTIVE_FLAG = "activity_type_active_flag";

    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;

    private int id;
    private String name;
    private String desc;
    private int activeFlag;

    public int getActivityTypeId() {
        return id;
    }

    public void setActivityTypeId(int id) {
        this.id = id;
    }

    public String getActivityTypeName() {
        return name;
    }

    public void setActivityTypeName(String name) {
        this.name = name;
    }

    public String getActivityTypeDesc() {
        return desc;
    }

    public void setActivityTypeDesc(String desc) {
        this.desc = desc;
    }

    public int getActivityTypeActiveFlag() {
        return activeFlag;
    }

    public void setActivityTypeActiveFlag(int activeFlag) {
        this.activeFlag = activeFlag;
    }

}
