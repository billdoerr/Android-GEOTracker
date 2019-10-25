package com.billdoerr.android.geotracker.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.billdoerr.android.geotracker.R;

/*
 * Credit goes to:  https://medium.com/@muthuraj57/handling-runtime-permissions-in-android-d9de2e18d18f
 */
public class PermissionUtils {

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    /*
     * Check if version is marshmallow and above.
     * Used in deciding to ask runtime permission
     * */
    @SuppressLint("ObsoleteSdkInt")
    private static boolean shouldAskPermission() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    private static boolean shouldAskPermission(Context context, String permission){
        if (shouldAskPermission()) {
            int permissionResult = ActivityCompat.checkSelfPermission(context, permission);
            return (permissionResult != PackageManager.PERMISSION_GRANTED);
        }
        return false;
    }

    public static void checkPermission(Activity activity, String permission, PermissionAskListener listener){
        /*
         * If permission is not granted
         * */
        if (shouldAskPermission(activity.getApplicationContext(), permission)){
            /*
             * If permission denied previously
             * */
            if (activity.shouldShowRequestPermissionRationale(permission)) {
                listener.onPermissionPreviouslyDenied();
            } else {
                /*
                 * Permission denied or first time requested
                 * */
                if (PreferenceUtils.isFirstTimeAskingPermission(activity.getApplicationContext(), permission)) {
                    PreferenceUtils.firstTimeAskingPermission(activity.getApplicationContext(), permission, false);
                    listener.onNeedPermission();
                } else {
                    /*
                     * Handle the feature without permission or ask user to manually allow permission
                     * */
                    listener.onPermissionDisabled();
                }
            }
        } else {
            listener.onPermissionGranted();
        }
    }


    /*
     * Callback on various cases on checking permission
     *
     * 1.  Below M, runtime permission not needed. In that case onPermissionGranted() would be called.
     *     If permission is already granted, onPermissionGranted() would be called.
     *
     * 2.  Above M, if the permission is being asked first time onNeedPermission() would be called.
     *
     * 3.  Above M, if the permission is previously asked but not granted, onPermissionPreviouslyDenied()
     *     would be called.
     *
     * 4.  Above M, if the permission is disabled by device policy or the user checked "Never ask again"
     *     check box on previous request permission, onPermissionDisabled() would be called.
     * */
    public interface PermissionAskListener {
        /*
         * Callback to ask permission
         * */
        void onNeedPermission();
        /*
         * Callback on permission denied
         * */
        void onPermissionPreviouslyDenied();
        /*
         * Callback on permission "Never show again" checked and denied
         * */
        void onPermissionDisabled();
        /*
         * Callback on permission granted
         * */
        void onPermissionGranted();
    }

    /*
     * ******************************************************************
     * Alert Dialogs.  Used in the above callbacks
     * ******************************************************************
     */

    /**
     * Display dialog asking why we need permissions.  If Ok pressed, intent sent to open
     * app permissions settings to have user manually enable permissions.
     */
    public static void displayAppPermissionDialog(final Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.dialog_msg_grant_permissions));
        builder.setCancelable(false);
        builder.setPositiveButton(activity.getString(R.string.btn_permit_manually), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        });
        builder.setNegativeButton(activity.getString(R.string.btn_cancel), null);
        builder.show();
    }

    /**
     * Show an alert dialog here with request explanation
     */
    public static void displayPermissionsRequestDialog(final Activity activity,
                                                       final String permission,
                                                       final int resultCode,
                                                       final String msg
                                                        ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.dialog_title_please_grant_permissions));
        builder.setMessage(msg);
        builder.setPositiveButton(activity.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.requestPermissions(new String[]{permission}, resultCode);
            }
        });
        builder.setNeutralButton(activity.getString(R.string.btn_cancel),null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
