package com.billdoerr.android.geotracker.database.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.billdoerr.android.geotracker.database.DatabaseManager;
import com.billdoerr.android.geotracker.database.model.ActivityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class which generates create, insert, delete,etc SQL statements.
 */
public class ActivityTypeRepo {

    private static final String TAG = "ActivityTypeRepo";

    private ActivityType mActivityType;

    public ActivityTypeRepo() {
        mActivityType = new ActivityType();
    }

    /**
     * Returns SQL string to create database.  Used by SQLiteDatabase.execSQL().
     * @return String:  Database creation SQL string.
     */
    public static String createTable() {
        return "CREATE TABLE " + ActivityType.TABLE + "("
        + ActivityType.KEY_ACTIVITY_TYPE_ID + " INTEGER PRIMARY KEY, "
        + ActivityType.KEY_ACTIVITY_TYPE_NAME + " TEXT NOT NULL UNIQUE, "
        + ActivityType.KEY_ACTIVITY_TYPE_DESC + " TEXT, "
        + ActivityType.KEY_ACTIVITY_TYPE_ACTIVE_FLAG + " INT)";
    }

    /**
     * Insert record into database.
     * @param activityType  ActivityType:
     * @return Returns -1 if error else returns row id of inserted record.
     */
    public static int insert(ActivityType activityType) {
        int rowId = -1;

        ContentValues values = new ContentValues();
        values.put(ActivityType.KEY_ACTIVITY_TYPE_NAME, activityType.getName());
        values.put(ActivityType.KEY_ACTIVITY_TYPE_DESC, activityType.getDesc());
        values.put(ActivityType.KEY_ACTIVITY_TYPE_ACTIVE_FLAG, activityType.isActiveFlag());

        // Insert row
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            rowId = (int)db.insert(ActivityType.TABLE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }

        return rowId;
    }

    /**
     * Delete record(s) from database specified by the index.
     * @param id int:  Index for the ActivityType.
     * @return int:  Number of records deleted.
     */
    public static int delete(int id) {
        int recordsDeleted = 0;

        String whereClause = ActivityType.KEY_ACTIVITY_TYPE_ID + " = ?";
        String[] whereArgs = new String[]{Integer.toString(id) };

        // Delete record(s)
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            recordsDeleted = db.delete(ActivityType.TABLE, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }

        return recordsDeleted;
    }

    /**
     * Update record in database specified by the ActivityType.id which is the index of the table.
     * @param activityType  ActivityType:
     * @return  int:  Number of rows updated.
     */
    public static int update(ActivityType activityType){
        int recordsUpdated = 0;

        ContentValues values = new ContentValues();
        values.put(ActivityType.KEY_ACTIVITY_TYPE_NAME, activityType.getName());
        values.put(ActivityType.KEY_ACTIVITY_TYPE_DESC, activityType.getDesc());
        values.put(ActivityType.KEY_ACTIVITY_TYPE_ACTIVE_FLAG, activityType.isActiveFlag());

        String whereClause = ActivityType.KEY_ACTIVITY_TYPE_ID + " = ?";
        String[] whereArgs = new String[]{ Integer.toString(activityType.getId()) };

        // Update record
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            recordsUpdated = db.update(ActivityType.TABLE, values, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }

        return recordsUpdated;
    }

    /**
     * Returns list of activities.
     * @return List<ActivityType>
     */
    public static List<ActivityType> getActivities() {
        List<ActivityType> activities = new ArrayList<>();
        ActivityType activityType;

        String selectQuery = "SELECT "
                + ActivityType.KEY_ACTIVITY_TYPE_ID + ", "
                + ActivityType.KEY_ACTIVITY_TYPE_NAME + ", "
                + ActivityType.KEY_ACTIVITY_TYPE_DESC + ", "
                + ActivityType.KEY_ACTIVITY_TYPE_ACTIVE_FLAG
                + " FROM " + ActivityType.TABLE
                + " ORDER BY " +  ActivityType.KEY_ACTIVITY_TYPE_NAME + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    activityType = new ActivityType();
                    activityType.setId(cursor.getInt(cursor.getColumnIndex(ActivityType.KEY_ACTIVITY_TYPE_ID)));
                    activityType.setName(cursor.getString(cursor.getColumnIndex(ActivityType.KEY_ACTIVITY_TYPE_NAME)));
                    activityType.setDesc(cursor.getString(cursor.getColumnIndex(ActivityType.KEY_ACTIVITY_TYPE_DESC)));
                    activityType.setActive(cursor.getInt(cursor.getColumnIndex(ActivityType.KEY_ACTIVITY_TYPE_ACTIVE_FLAG)));

                    activities.add(activityType);
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

        return activities;
    }

    /**
     * Returns activity name give activity id.
     * @return String
     */
    public static String getActivityName(int id) {
        String name = "";

        String selectQuery = "SELECT "
                + ActivityType.KEY_ACTIVITY_TYPE_NAME
                + " FROM " + ActivityType.TABLE
                + " WHERE " +  ActivityType.KEY_ACTIVITY_TYPE_ID + " = " + id;

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    name = cursor.getString(cursor.getColumnIndex(ActivityType.KEY_ACTIVITY_TYPE_NAME));
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

        return name;
    }

}
