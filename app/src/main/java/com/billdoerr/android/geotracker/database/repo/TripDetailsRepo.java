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
public class TripDetailsRepo {

    private static final String TAG = "TripDetailsRepo";

    private TripDetails mTripDetails;

    public TripDetailsRepo() {
        mTripDetails = new TripDetails();
    }

    /**
     * Returns SQL string to create database.  Used by SQLiteDatabase.execSQL().
     * @return String:  Database creation SQL string.
     */
    public static String createTable() {
        return "CREATE TABLE " + TripDetails.TABLE + "("
                + TripDetails.KEY_TRIP_DETAILS_TRIP_ID + " INTEGER NOT NULL, "
                + TripDetails.KEY_TRIP_DETAILS_LATITUDE + " REAL, "
                + TripDetails.KEY_TRIP_DETAILS_LONGITUDE + " REAL, "
                + TripDetails.KEY_TRIP_DETAILS_ALTITUDE + " REAL, "
                + TripDetails.KEY_TRIP_DETAILS_TIME_STAMP + " TEXT, "
                + TripDetails.KEY_TRIP_DETAILS_RAW_GPS_DATA + " TEXT)";
    }

    /**
     * Insert record into database.
     * @param TripDetails  TripDetails:
     * @return Returns -1 if error else returns row id of inserted record.
     */
    public static int insert(TripDetails TripDetails) {
        int rowId = -1;

        ContentValues values = new ContentValues();
        values.put(TripDetails.KEY_TRIP_DETAILS_TRIP_ID, TripDetails.getTripId());
        values.put(TripDetails.KEY_TRIP_DETAILS_LATITUDE, TripDetails.getLatitude());
        values.put(TripDetails.KEY_TRIP_DETAILS_LONGITUDE, TripDetails.getLongitude());
        values.put(TripDetails.KEY_TRIP_DETAILS_ALTITUDE, TripDetails.getAltitude());
        values.put(TripDetails.KEY_TRIP_DETAILS_TIME_STAMP, TripDetails.getTimeStamp());
        values.put(TripDetails.KEY_TRIP_DETAILS_RAW_GPS_DATA, TripDetails.getRawGPSData());

        // Insert row
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            rowId = (int)db.insert(TripDetails.TABLE, null, values);
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
     * @param id int:  Index for the TripDetails.
     * @return int:  Number of records deleted.
     */
    public static int delete(int id) {
        int recordsDeleted = 0;

       String whereClause = TripDetails.KEY_TRIP_DETAILS_TRIP_ID + " = ?";
       String[] whereArgs = new String[]{Integer.toString(id) };

       // Delete record(s)
       SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
       try {
           db.beginTransaction();
           recordsDeleted = db.delete(TripDetails.TABLE, whereClause, whereArgs);
           db.setTransactionSuccessful();
       } catch (Exception e) {
           Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
       } finally {
           db.endTransaction();
           DatabaseManager.getInstance().closeDatabase();
       }

        return recordsDeleted;
    }

//    /**
//     * Update record in database specified by the TripDetails.id which is the index of the table.
//     * @param TripDetails  TripDetails:
//     * @return  int:  Number of rows updated.
//     */
//    public static int update(TripDetails TripDetails){
//        int recordsUpdated = 0;
//
//
//       ContentValues values = new ContentValues();
//       values.put(TripDetails.KEY_TRIP_DETAILS_TRIP_ID, TripDetails.getId());
//       values.put(TripDetails.KEY_TRIP_DETAILS_LATITUDE, TripDetails.getLatitude());
//       values.put(TripDetails.KEY_TRIP_DETAILS_LONGITUDE, TripDetails.getLongitude());
//       values.put(TripDetails.KEY_TRIP_DETAILS_ALTITUDE, TripDetails.getAltitude());
//       values.put(TripDetails.KEY_TRIP_DETAILS_TIME_STAMP, TripDetails.getTimeStamp());
//       values.put(TripDetails.KEY_TRIP_DETAILS_RAW_GPS_DATA, TripDetails.getRawGPSData());
//
//       String whereClause = TripDetails.KEY_ACTIVITY_TYPE_ID + " = ?";
//       String[] whereArgs = new String[]{ Integer.toString(TripDetails.getTripDetailsId()) };
//
//       // Update record
//       SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
//       try {
//           db.beginTransaction();
//           recordsUpdated = db.update(TripDetails.TABLE, values, whereClause, whereArgs);
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
                + TripDetails.KEY_TRIP_DETAILS_LATITUDE + ", "
                + TripDetails.KEY_TRIP_DETAILS_LONGITUDE + ", "
                + TripDetails.KEY_TRIP_DETAILS_ALTITUDE + ", "
                + TripDetails.KEY_TRIP_DETAILS_TIME_STAMP
                + " FROM " + TripDetails.TABLE
                + " WHERE " + TripDetails.KEY_TRIP_DETAILS_TRIP_ID + " = " + id
                + " ORDER BY " +  TripDetails.KEY_TRIP_DETAILS_TIME_STAMP + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    tripDetail = new TripDetails();
                    tripDetail.setLatitude(cursor.getDouble(cursor.getColumnIndex(TripDetails.KEY_TRIP_DETAILS_LATITUDE)));
                    tripDetail.setLongitude(cursor.getDouble(cursor.getColumnIndex(TripDetails.KEY_TRIP_DETAILS_LONGITUDE)));
                    tripDetail.setAltitude(cursor.getDouble(cursor.getColumnIndex(TripDetails.KEY_TRIP_DETAILS_ALTITUDE)));
                    tripDetail.setTimeStamp(cursor.getLong(cursor.getColumnIndex(TripDetails.KEY_TRIP_DETAILS_TIME_STAMP)));
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
