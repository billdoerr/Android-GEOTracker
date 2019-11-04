package com.billdoerr.android.geotracker.database.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.billdoerr.android.geotracker.database.DatabaseManager;
import com.billdoerr.android.geotracker.database.model.TripDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class which generates create, insert, delete,etc SQL statements.
 */
@SuppressWarnings("UnusedReturnValue")
public class TripDetailsRepo {

    private static final String TAG = "TripDetailsRepo";

//    private final TripDetails mTripDetails;
//
//    public TripDetailsRepo() {
//        mTripDetails = new TripDetails();
//    }

    // Table name
    private static final String TABLE = "TripDetails";

    // Columns
    private static final String KEY_TRIP_DETAILS_TRIP_ID = "trip_id";
    private static final String KEY_TRIP_DETAILS_LATITUDE = "latitude";
    private static final String KEY_TRIP_DETAILS_LONGITUDE = "longitude";
    private static final String KEY_TRIP_DETAILS_ALTITUDE = "altitude";
    private static final String KEY_TRIP_DETAILS_TIME_STAMP = "time_stamp";
    private static final String KEY_TRIP_DETAILS_RAW_GPS_DATA = "raw_gps_data";

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
                + KEY_TRIP_DETAILS_TRIP_ID + " INTEGER NOT NULL, "
                + KEY_TRIP_DETAILS_LATITUDE + " REAL, "
                + KEY_TRIP_DETAILS_LONGITUDE + " REAL, "
                + KEY_TRIP_DETAILS_ALTITUDE + " REAL, "
                + KEY_TRIP_DETAILS_TIME_STAMP + " TEXT, "
                + KEY_TRIP_DETAILS_RAW_GPS_DATA + " TEXT)";
    }

    /**
     * Insert record into database.
     * @param tripDetails  TripDetails:
     * @return Returns -1 if error else returns row id of inserted record.
     */
    public static int insert(TripDetails tripDetails) {
        int rowId = -1;

        ContentValues values = new ContentValues();
        values.put(KEY_TRIP_DETAILS_TRIP_ID, tripDetails.getTripId());
        values.put(KEY_TRIP_DETAILS_LATITUDE, tripDetails.getLatitude());
        values.put(KEY_TRIP_DETAILS_LONGITUDE, tripDetails.getLongitude());
        values.put(KEY_TRIP_DETAILS_ALTITUDE, tripDetails.getAltitude());
        values.put(KEY_TRIP_DETAILS_TIME_STAMP, tripDetails.getTimeStamp());
//        values.put(KEY_TRIP_DETAILS_RAW_GPS_DATA, tripDetails.getRawGPSData());

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

        String whereClause = KEY_TRIP_DETAILS_TRIP_ID + " = ?";
        String[] whereArgs = new String[]{Integer.toString(id) };

        // Delete record(s)
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            recordsDeleted = db.delete(TABLE, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }

        return recordsDeleted;
    }

//    /**
//     * Update record in database specified by the id which is the index of the table.
//     * @param TripDetails  TripDetails:
//     * @return  int:  Number of rows updated.
//     */
//    public static int update(TripDetails TripDetails){
//        int recordsUpdated = 0;
//
//
//       ContentValues values = new ContentValues();
//       values.put(KEY_TRIP_DETAILS_TRIP_ID, getId());
//       values.put(KEY_TRIP_DETAILS_LATITUDE, getLatitude());
//       values.put(KEY_TRIP_DETAILS_LONGITUDE, getLongitude());
//       values.put(KEY_TRIP_DETAILS_ALTITUDE, getAltitude());
//       values.put(KEY_TRIP_DETAILS_TIME_STAMP, getTimeStamp());
//       values.put(KEY_TRIP_DETAILS_RAW_GPS_DATA, getRawGPSData());
//
//       String whereClause = KEY_ACTIVITY_TYPE_ID + " = ?";
//       String[] whereArgs = new String[]{ Integer.toString(getTripDetailsId()) };
//
//       // Update record
//       SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
//       try {
//           db.beginTransaction();
//           recordsUpdated = db.update(TABLE, values, whereClause, whereArgs);
//           db.setTransactionSuccessful();
//       } catch (Exception e) {
//           Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
//       } finally {
//           db.endTransaction();
//           DatabaseManager.getInstance().closeDatabase();
//       }
//
//        return recordsUpdated;
//    }

    /**
     * Returns a list of TripDetails with the give TripId.
     * @param id int
     * @return List<TripDetails>
     */
    public static List<TripDetails> getTripDetails(int id) {
        List<TripDetails> tripDetails = new ArrayList<>();
        TripDetails tripDetail;

        String selectQuery = "SELECT "
                + KEY_TRIP_DETAILS_LATITUDE + ", "
                + KEY_TRIP_DETAILS_LONGITUDE + ", "
                + KEY_TRIP_DETAILS_ALTITUDE + ", "
                + KEY_TRIP_DETAILS_TIME_STAMP
                + " FROM " + TABLE
                + " WHERE " + KEY_TRIP_DETAILS_TRIP_ID + " = " + id
                + " ORDER BY " +  KEY_TRIP_DETAILS_TIME_STAMP + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    tripDetail = new TripDetails();
                    tripDetail.setLatitude(cursor.getDouble(cursor.getColumnIndex(KEY_TRIP_DETAILS_LATITUDE)));
                    tripDetail.setLongitude(cursor.getDouble(cursor.getColumnIndex(KEY_TRIP_DETAILS_LONGITUDE)));
                    tripDetail.setAltitude(cursor.getDouble(cursor.getColumnIndex(KEY_TRIP_DETAILS_ALTITUDE)));
                    tripDetail.setTimeStamp(cursor.getLong(cursor.getColumnIndex(KEY_TRIP_DETAILS_TIME_STAMP)));
                    tripDetails.add(tripDetail);
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

        return tripDetails;
    }

}
