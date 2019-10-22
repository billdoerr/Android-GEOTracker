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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int isActiveFlag() {
        return activeFlag;
    }

    public void setActive(int activeFlag) {
        this.activeFlag = activeFlag;
    }

    // To display object as a string in spinner
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ActivityType){
            ActivityType c = (ActivityType )obj;
            return c.getName().equals(name) && c.getId() == id;
        }
        return false;
    }

}
