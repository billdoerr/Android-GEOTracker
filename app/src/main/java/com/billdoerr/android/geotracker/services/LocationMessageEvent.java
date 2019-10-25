package com.billdoerr.android.geotracker.services;

import android.location.Location;

public class LocationMessageEvent {

    private final Location mLocation;

    public LocationMessageEvent(Location location) {
        mLocation = location;
    }

    public Location getLocation() {
        return mLocation;
    }

}
