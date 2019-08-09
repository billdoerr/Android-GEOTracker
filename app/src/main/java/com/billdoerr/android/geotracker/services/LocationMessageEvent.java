package com.billdoerr.android.geotracker.services;

import android.location.Location;

public class LocationMessageEvent {

    private static final String TAG = "LocationMessageEvent";

    private Location mLocation;

    public LocationMessageEvent(Location location) {
        mLocation = location;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void sendMessage(Location location) {
        mLocation = location;
    }

}
