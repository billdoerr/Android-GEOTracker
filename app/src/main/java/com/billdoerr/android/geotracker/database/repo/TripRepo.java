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

    private Trip mTrip;

    public TripRepo() {
        mTrip = new Trip();
    }

    /**
     * Returns SQL string to create database.  Used by SQLiteDatabase.execSQL().
     * @return String:  Database creation SQL string.
     */
    public static String createTable() {
        return "CREATE TABLE " + Trip.TABLE + "("
                + Trip.KEY_TRIP_ID + " INTEGER PRIMARY KEY, "
                + Trip.KEY_TRIP_NAME + " TEXT NOT NULL UNIQUE, "
                + Trip.KEY_TRIP_DESC + " TEXT, "
                + Trip.KEY_TRIP_ACTIVE_FLAG + " INT, "
                + Trip.KEY_TRIP_ACTIVITY_TYPE_ID + " INT)";
    }

    /**
     * Insert record into database.
     * @param Trip  Trip:
     * @return Returns -1 if error else returns row id of inserted record.
     */
    public static int insert(Trip Trip) {
        int rowId = -1;

        ContentValues values = new ContentValues();
        values.put(Trip.KEY_TRIP_NAME, Trip.getTripName());
        values.put(Trip.KEY_TRIP_DESC, Trip.getTripDesc());
        values.put(Trip.KEY_TRIP_ACTIVE_FLAG, Trip.getTripActiveFlag());
        values.put(Trip.KEY_TRIP_ACTIVITY_TYPE_ID, Trip.getTripActivityTypeId());

        // Insert row
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            rowId = (int)db.insert(Trip.TABLE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }

        return rowId;
    }

    // TODO:  TripRepo.delete() not implemented
    /**
     * Delete record(s) from database specified by the index.
     * @param rowId  int:  Index for the Trip.
     * @return int:  Number of records deleted.
     */
    public static int delete(int rowId) {
        int recordsDeleted = 0;

        String whereClause = Trip.KEY_TRIP_ID + " = ?";
        String[] whereArgs = new String[]{Integer.toString(rowId) };

        // Delete record(s)
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            recordsDeleted = db.delete(Trip.TABLE, whereClause, whereArgs);
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
     * Update record in database specified by the Trip.id which is the index of the table.
     * @param trip  Trip:
     * @return  int:  Number of rows updated.
     */
    public static int update(Trip trip){
        int recordsUpdated = 0;

        ContentValues values = new ContentValues();
        values.put(Trip.KEY_TRIP_NAME, trip.getTripName());
        values.put(Trip.KEY_TRIP_DESC, trip.getTripDesc());
        values.put(Trip.KEY_TRIP_ACTIVE_FLAG, trip.getTripActiveFlag());
        values.put(Trip.KEY_TRIP_ACTIVITY_TYPE_ID, trip.getTripActivityTypeId());

        String whereClause = Trip.KEY_TRIP_ID + " = ?";
        String[] whereArgs = new String[]{ Integer.toString(trip.getTripId()) };

        // Update record
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            recordsUpdated = db.update(Trip.TABLE, values, whereClause, whereArgs);
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
     * Returns list of trips.
     * @return List<ActivityType>
     */
    public static List<Trip> getTrips() {
        List<Trip> trips = new ArrayList<>();
        Trip trip;

        String selectQuery = "SELECT "
                + Trip.KEY_TRIP_ID + ", "
                + Trip.KEY_TRIP_NAME + ", "
                + Trip.KEY_TRIP_DESC + ", "
                + Trip.KEY_TRIP_ACTIVE_FLAG + ", "
                + Trip.KEY_TRIP_ACTIVITY_TYPE_ID
                + " FROM " + Trip.TABLE
                + " ORDER BY " +  Trip.KEY_TRIP_NAME + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    trip = new Trip();
                    trip.setTripId(cursor.getInt(cursor.getColumnIndex(Trip.KEY_TRIP_ID)));
                    trip.setTripName(cursor.getString(cursor.getColumnIndex(Trip.KEY_TRIP_NAME)));
                    trip.setTripDesc(cursor.getString(cursor.getColumnIndex(Trip.KEY_TRIP_DESC)));
                    trip.setTripActiveFlag(cursor.getInt(cursor.getColumnIndex(Trip.KEY_TRIP_ACTIVE_FLAG)));
                    trip.setTripActivityTypeId(cursor.getInt(cursor.getColumnIndex(Trip.KEY_TRIP_ACTIVITY_TYPE_ID)));

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
