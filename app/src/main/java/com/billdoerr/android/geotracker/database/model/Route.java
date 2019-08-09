package com.billdoerr.android.geotracker.database.model;

/**
 * Data model for Route table.
 */
public class Route {

    private static final String TAG = "Route";

    // Table name
    public static final String TABLE = "Route";

    // Columns
    public static final String KEY_ROUTE_ID = "route_id";
    public static final String KEY_ROUTE_NAME = "route_name";
    public static final String KEY_ROUTE_DESC = "route_desc";
    public static final String KEY_ROUTE_ACTIVE_FLAG = "route_active_flag";
    public static final String KEY_ROUTE_ACTIVITY_TYPE_ID = "activity_type_id";

    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;

    private int id;
    private String name;
    private String desc;
    private int activityTypeId;
    private int activeFlag;

    public int getRouteId() {
        return id;
    }

    public void setRouteId(int id) {
        this.id = id;
    }

    public String getRouteName() {
        return name;
    }

    public void setRouteName(String name) {
        this.name = name;
    }

    public String getRouteDesc() {
        return desc;
    }

    public void setRouteDesc(String desc) {
        this.desc = desc;
    }

    public int getRouteActivityTypeId() {
        return activityTypeId;
    }

    public void setRouteActivityTypeId(int activityTypeId) {
        this.activityTypeId = activityTypeId;
    }

    public int getRouteActiveFlag() {
        return activeFlag;
    }

    public void setRouteActiveFlag(int activeFlag) {
        this.activeFlag = activeFlag;
    }
}
