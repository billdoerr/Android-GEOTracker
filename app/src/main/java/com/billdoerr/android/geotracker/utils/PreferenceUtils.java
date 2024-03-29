package com.billdoerr.android.geotracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.billdoerr.android.geotracker.database.model.Trip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceUtils {

    private static final String PREFS_NAME = "com.billdoerr.android.geotracker_preferences";

    // Shared preferences
    private static final String PREF_KEY_UNITS_METRIC  = "com.billdoerr.android.geotracker.settings.PREF_KEY_UNITS_METRIC";
    private static final String PREF_KEY_UNITS_NAUTICAL  = "com.billdoerr.android.geotracker.settings.PREF_KEY_UNITS_NAUTICAL";
    private static final String PREF_KEY_UNITS_COORDINATE_TYPE  = "com.billdoerr.android.geotracker.settings.PREF_KEY_UNITS_COORDINATE_TYPE";
    // Feature supporting this has not been implemented
//    public static final String PREF_KEY_UNITS_COORDINATE_DATUM  = "com.billdoerr.android.geotracker.settings.PREF_KEY_UNITS_COORDINATE_DATUM";
    private static final String PREF_KEY_LOCATION_SERVICES_UPDATE_INTERVAL  = "com.billdoerr.android.geotracker.settings.PREF_KEY_LOCATION_SERVICES_UPDATE_INTERVAL";
    private static final String PREF_KEY_LOCATION_SERVICES_UPDATE_DISTANCE  = "com.billdoerr.android.geotracker.settings.PREF_KEY_LOCATION_SERVICES_UPDATE_DISTANCE";

    private static final String PREF_KEY_KEEP_DEVICE_AWAKE = "com.billdoerr.android.geotracker.settings.PREF_KEY_POWER_SAVINGS_KEEP_DEVICE_ON";

    private static final String PREF_KEY_ACTIVE_TRIP = "com.billdoerr.android.geotracker.settings.PREF_KEY_ACTIVE_TRIP";

    /*
     * ******************************************************************
     * Android Permissions Shared Preferences utility methods
     *
     * Credit goes to:  https://medium.com/@muthuraj57/handling-runtime-permissions-in-android-d9de2e18d18f
     *
     * ******************************************************************
     */

    @SuppressWarnings("SameParameterValue")
    static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime){
        SharedPreferences sharedPreference = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }

    static boolean isFirstTimeAskingPermission(Context context, String permission){
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(permission, true);
    }

    /*
     * ******************************************************************
     * Android Shared Preferences utility methods
     * ******************************************************************
     */

    /**
     * Get shared preferences.
     * @param context Context:  Application context.
     */
    public static SharedPreferencesUtils getSharedPreferences(Context context) {

        SharedPreferences appSharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferencesUtils prefs = new SharedPreferencesUtils();

        prefs.setMetric(appSharedPrefs.getBoolean(PREF_KEY_UNITS_METRIC, false));
        prefs.setNautical(appSharedPrefs.getBoolean(PREF_KEY_UNITS_NAUTICAL, false));

        // Convert to Integer
        prefs.setCoordinateType(Integer.valueOf(Objects.requireNonNull(appSharedPrefs.getString(PREF_KEY_UNITS_COORDINATE_TYPE, "0"))));
//
//        // Feature supporting this has not been implemented
////        prefs.setCoordinateDatum(Integer.valueOf(appSharedPrefs.getString(PREF_KEY_UNITS_COORDINATE_DATUM, "0")));
//
        // Convert to long and multiply my 1000 to convert from integer seconds to long milliseconds
        prefs.setLocationServicesUpdateInterval( Long.valueOf(Objects.requireNonNull(appSharedPrefs.getString(PREF_KEY_LOCATION_SERVICES_UPDATE_INTERVAL, "900"))) * 1000 );
//
//        // Convert value to float.  Preference is stored as string representative of meters.
        prefs.setLocationServicesUpdateDistance(Float.valueOf(Objects.requireNonNull(appSharedPrefs.getString(PREF_KEY_LOCATION_SERVICES_UPDATE_DISTANCE, "5"))));

        prefs.setKeepDeviceAwake(appSharedPrefs.getBoolean(PREF_KEY_KEEP_DEVICE_AWAKE, false));

        return prefs;

    }

    /**
     * Read active tracking to SharedPreferencesUtils as a JSON string.
     * @param context Context
     * @return Trip
     */
    public static Trip getActiveTripFromSharedPrefs(Context context) {
        SharedPreferences appSharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = appSharedPrefs.getString(PREF_KEY_ACTIVE_TRIP, "");
        if (Objects.requireNonNull(json).length() == 0) {
            return null;
        } else {
            return gson.fromJson(json, new TypeToken<Trip>(){}.getType());
        }
    }


    /**
     * Save active tracking to SharedPreferencesUtils as a JSON string.
     * @param context Context:  Application context.
     */
    public static void saveActiveTripToSharedPrefs(Context context, Trip trip) {
        SharedPreferences appSharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(trip); //tasks is an ArrayList instance variable
        prefsEditor.putString(PREF_KEY_ACTIVE_TRIP, json);
        prefsEditor.apply();
    }

//    public static void clearActiveTripFromSharedPrefs(Context context) {
//        SharedPreferences appSharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        appSharedPrefs.edit().remove(PREF_KEY_ACTIVE_TRIP).apply();
//    }

}
