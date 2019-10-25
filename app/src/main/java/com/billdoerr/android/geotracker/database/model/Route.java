package com.billdoerr.android.geotracker.database.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Data model for Route table.
 */
public class Route implements Serializable {

    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;

    private int id;
    private String name;
    private String desc;
    private int activityTypeId;
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

    public int getActivityTypeId() {
        return activityTypeId;
    }

    public void setActivityTypeId(int activityTypeId) {
        this.activityTypeId = activityTypeId;
    }

    public int isActive() {
        return activeFlag;
    }

    public void setActive(int activeFlag) {
        this.activeFlag = activeFlag;
    }

    // To display object as a string in spinner
    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Route){
            Route c = (Route )obj;
            return c.getName().equals(name) && c.getId() == id;
        }
        return false;
    }
}
