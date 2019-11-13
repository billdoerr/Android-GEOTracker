package com.billdoerr.android.geotracker.utils;

import android.content.Context;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.IOrientationProvider;

/**
 *
 *
 * https://guides.codepath.com/android/Creating-Custom-Listeners
 */
public class MyCompassOverlay extends CompassOverlay {

    public interface MyCompassOverlayListener {
        void onBearingChange(float bearing);
    }

    private MyCompassOverlayListener mListener;

    public MyCompassOverlay(Context context, IOrientationProvider orientationProvider, MapView mapView) {
        super(context, orientationProvider, mapView);
        mListener = null;
    }

    // Assign the listener implementing events interface that will receive the events
    public void setMyCompassOverlayListener(MyCompassOverlayListener listener) {
        mListener = listener;
    }

    @Override
    public void onOrientationChanged(float orientation, IOrientationProvider source) {
        super.onOrientationChanged(orientation, source);
        if (mListener != null) {
            mListener.onBearingChange(orientation);
        }
    }

}
