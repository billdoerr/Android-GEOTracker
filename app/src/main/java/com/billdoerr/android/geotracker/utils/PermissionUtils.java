package com.billdoerr.android.geotracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * https://github.com/shikto1/RuntimePermissionInMarsmallow/blob/master/app/src/main/java/shishirstudio/runtimepermissioninandroid/MainActivity.java
 */
public class PermissionUtils {

    private static final String TAG = "PermissionUtils";

    public static void setFirstTimeAsking(final Context context, final String permission,
                                       final String prefKey, boolean isFirstTime) {
        SharedPreferences genPrefs = context.getSharedPreferences(prefKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = genPrefs.edit();
        editor.putBoolean(permission, isFirstTime);
        editor.commit();
    }

    public static boolean isFirstTimeAsking(final Context context, final String permission,
                                            final String prefKey) {
        SharedPreferences genPrefs = context.getSharedPreferences(prefKey, Context.MODE_PRIVATE);
        return genPrefs.getBoolean(permission, true);
    }

}
