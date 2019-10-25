package com.billdoerr.android.geotracker.database.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.billdoerr.android.geotracker.database.DatabaseManager;
import com.billdoerr.android.geotracker.database.model.Trip;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class which generates create, insert, delete,etc SQL statements.
 */
public class TripRepo {

    private static final String TAG = "TripRepo";

    // Table mName
    private static final String TABLE = "Trip";

    // Columns
    private static final String KEY_TRIP_ID = "trip_id";
    private static final String KEY_TRIP_NAME = "trip_name";
    private static final String KEY_TRIP_DESC = "trip_desc";
    private static final String KEY_TRIP_STATE = "trip_state";
    private static final String KEY_TRIP_START_TIME = "start_time";
    private static final String KEY_TRIP_END_TIME = "end_time";
    private static final String KEY_TRIP_PAUSED_TIME = "paused_time";
    private static final String KEY_TRIP_TOTAL_TIME = "total_time";
    private static final String KEY_TRIP_ACTIVITY_TYPE_ID = "activity_id";
    private static final String KEY_TRIP_ACTIVE_FLAG = "active_flag";

//    private final Trip mTrip;
//
//    public TripRepo() {
//        mTrip = new Trip();
//    }

    /**
     * Returns table name.
     * @return String
     */
    public static String getTableName() {
        return TABLE;
    }

    /**
     * Returns SQL string to create database.  Used by SQLiteDatabase.execSQL().
     * @return String:  Database creation SQL string.
     */
    public static String createTable() {
        return "CREATE TABLE " + TABLE + "("
                + KEY_TRIP_ID + " INTEGER PRIMARY KEY, "
                + KEY_TRIP_NAME + " TEXT NOT NULL, "
                + KEY_TRIP_DESC + " TEXT, "
                + KEY_TRIP_STATE + " INT, "
                + KEY_TRIP_START_TIME + " INT, "
                + KEY_TRIP_END_TIME + " INT, "
                + KEY_TRIP_PAUSED_TIME + " INT, "
                + KEY_TRIP_TOTAL_TIME + " INT, "
                + KEY_TRIP_ACTIVE_FLAG + " INT, "
                + KEY_TRIP_ACTIVITY_TYPE_ID + " INT)";
    }

    /**
     * Insert record into database.
     * @param trip Trip:
     * @return sel else returns row id of inserted record.
     */
    public static int insert(Trip trip) {
        int rowId = -1;

        ContentValues values = new ContentValues();
        values.put(KEY_TRIP_NAME, trip.getName());
        values.put(KEY_TRIP_DESC, trip.getDesc());
        values.put(KEY_TRIP_STATE, trip.getState());
        values.put(KEY_TRIP_START_TIME, trip.getStartTime());
        values.put(KEY_TRIP_END_TIME, trip.getEndTime());
        values.put(KEY_TRIP_PAUSED_TIME, trip.getPausedTimeInMillis());
        values.put(KEY_TRIP_TOTAL_TIME, trip.getTotalTimeInMillis());
        values.put(KEY_TRIP_ACTIVE_FLAG, trip.isActive());
        values.put(KEY_TRIP_ACTIVITY_TYPE_ID, trip.getActivityTypeId());

        // Insert row
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            rowId = (int)db.insert(TABLE, null, values);
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
     * @param id int:  Index for the
     * @return int:  Number of records deleted.
     */
    public static int delete(int id) {
        int recordsDeleted = 0;

        String whereClause = KEY_TRIP_ID + " = ?";
        String[] whereArgs = new String[]{Integer.toString(id) };

        // Delete record(s) from table:  TripDetails
        TripDetailsRepo.delete(id);

        // Delete record(s) from table:  Trip
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            recordsDeleted = db.delete(TABLE, whereClause, whereArgs);
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
     * Update record in database specified by the id which is the index of the table.
     * @param trip  Trip:
     * @return  int:  Number of rows updated.
     */
    public static int update(Trip trip){
        int recordsUpdated = 0;

        ContentValues values = new ContentValues();
        values.put(KEY_TRIP_NAME, trip.getName());
        values.put(KEY_TRIP_DESC, trip.getDesc());
        values.put(KEY_TRIP_STATE, trip.getState());
        values.put(KEY_TRIP_START_TIME, trip.getStartTime());
        values.put(KEY_TRIP_END_TIME, trip.getEndTime());
        values.put(KEY_TRIP_PAUSED_TIME, trip.getPausedTimeInMillis());
        values.put(KEY_TRIP_TOTAL_TIME, trip.getTotalTimeInMillis());
        values.put(KEY_TRIP_ACTIVE_FLAG, trip.isActive());
        values.put(KEY_TRIP_ACTIVITY_TYPE_ID, trip.getActivityTypeId());

        String whereClause = KEY_TRIP_ID + " = ?";
        String[] whereArgs = new String[]{ Integer.toString(trip.getId()) };

        // Update record
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            recordsUpdated = db.update(TABLE, values, whereClause, whereArgs);
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
     * Returns list of trips that only has an end time set. End time of zero indicates running trip.
     * @return List<ActivityType>
     */
    public static List<Trip> getTrips() {
        List<Trip> trips = new ArrayList<>();
        Trip trip;

        String selectQuery = "SELECT "
                + KEY_TRIP_ID + ", "
                + KEY_TRIP_NAME + ", "
                + KEY_TRIP_DESC + ", "
                + KEY_TRIP_STATE + ", "
                + KEY_TRIP_START_TIME + ", "
                + KEY_TRIP_END_TIME + ", "
                + KEY_TRIP_PAUSED_TIME + ", "
                + KEY_TRIP_TOTAL_TIME + ", "
                + KEY_TRIP_ACTIVE_FLAG + ", "
                + KEY_TRIP_ACTIVITY_TYPE_ID
                + " FROM " + TABLE
                + " WHERE " + KEY_TRIP_END_TIME + "!= 0"
                + " ORDER BY " +  KEY_TRIP_START_TIME + " DESC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    trip = new Trip();
                    trip.setId(cursor.getInt(cursor.getColumnIndex(KEY_TRIP_ID)));
                    trip.setName(cursor.getString(cursor.getColumnIndex(KEY_TRIP_NAME)));
                    trip.setDesc(cursor.getString(cursor.getColumnIndex(KEY_TRIP_DESC)));
                    trip.setState(cursor.getInt(cursor.getColumnIndex(KEY_TRIP_STATE)));
                    trip.setStartTime(cursor.getLong(cursor.getColumnIndex(KEY_TRIP_START_TIME)));
                    trip.setEndTime(cursor.getLong(cursor.getColumnIndex(KEY_TRIP_END_TIME)));
                    trip.setPausedTimeInMillis(cursor.getLong(cursor.getColumnIndex(KEY_TRIP_PAUSED_TIME)));
                    trip.setTotalTimeInMillis(cursor.getLong(cursor.getColumnIndex(KEY_TRIP_TOTAL_TIME)));
                    trip.setActive(cursor.getInt(cursor.getColumnIndex(KEY_TRIP_ACTIVE_FLAG)));
                    trip.setActivityTypeId(cursor.getInt(cursor.getColumnIndex(KEY_TRIP_ACTIVITY_TYPE_ID)));
                    trips.add(trip);
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

        return trips;
    }

    /**
     * Returns list of trips.
     * @return List<ActivityType>
     */
    public static List<Trip> filterTrips(int activeFlag, int activityId) {
        List<Trip> trips = new ArrayList<>();
        Trip trip;

        String s1 = "";
        String s2 = "";
        String where = "";
        if (activeFlag >= 0) {
            s1 = KEY_TRIP_ACTIVE_FLAG + " = " + activeFlag;
        }
        if (activityId > 0) {
            s2 = KEY_TRIP_ACTIVITY_TYPE_ID + " = " + activityId;
        }
        if (s1.length() > 0) {
            where = " WHERE " + s1;
            if (s2.length() > 0) {
                where = where + " AND " + s2;
            }
        } else if (s2.length() > 0) {
            where = " WHERE " + s2;
        }

        String selectQuery = "SELECT "
                + KEY_TRIP_ID + ", "
                + KEY_TRIP_NAME + ", "
                + KEY_TRIP_DESC + ", "
                + KEY_TRIP_STATE + ", "
                + KEY_TRIP_START_TIME + ", "
                + KEY_TRIP_END_TIME + ", "
                + KEY_TRIP_PAUSED_TIME + ", "
                + KEY_TRIP_TOTAL_TIME + ", "
                + KEY_TRIP_ACTIVE_FLAG + ", "
                + KEY_TRIP_ACTIVITY_TYPE_ID
                + " FROM " + TABLE
                + where
                + " ORDER BY " +  KEY_TRIP_NAME + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    trip = new Trip();
                    trip.setId(cursor.getInt(cursor.getColumnIndex(KEY_TRIP_ID)));
                    trip.setName(cursor.getString(cursor.getColumnIndex(KEY_TRIP_NAME)));
                    trip.setDesc(cursor.getString(cursor.getColumnIndex(KEY_TRIP_DESC)));
                    trip.setState(cursor.getInt(cursor.getColumnIndex(KEY_TRIP_STATE)));
                    trip.setStartTime(cursor.getLong(cursor.getColumnIndex(KEY_TRIP_START_TIME)));
                    trip.setEndTime(cursor.getLong(cursor.getColumnIndex(KEY_TRIP_END_TIME)));
                    trip.setPausedTimeInMillis(cursor.getLong(cursor.getColumnIndex(KEY_TRIP_PAUSED_TIME)));
                    trip.setTotalTimeInMillis(cursor.getLong(cursor.getColumnIndex(KEY_TRIP_TOTAL_TIME)));
                    trip.setActive(cursor.getInt(cursor.getColumnIndex(KEY_TRIP_ACTIVE_FLAG)));
                    trip.setActivityTypeId(cursor.getInt(cursor.getColumnIndex(KEY_TRIP_ACTIVITY_TYPE_ID)));
                    trips.add(trip);
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

        return trips;
    }

}
