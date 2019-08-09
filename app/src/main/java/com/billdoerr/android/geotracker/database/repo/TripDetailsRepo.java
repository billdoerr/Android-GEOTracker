package com.billdoerr.android.geotracker.database.repo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.billdoerr.android.geotracker.database.DatabaseManager;
import com.billdoerr.android.geotracker.database.model.TripDetails;

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
    public int insert(TripDetails TripDetails) {
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
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }

        return rowId;
    }

    /**
     * Delete record(s) from database specified by the index.
     * @param rowId  int:  Index for the TripDetails.
     * @return int:  Number of records deleted.
     */
    public int delete(int rowId) {
        int recordsDeleted = 0;

        // TODO:  Should we delete or even mark as inactive for this the TripDetails table?

//       String whereClause = TripDetails.KEY_TRIP_DETAILS_TRIP_ID + " = ?";
//       String[] whereArgs = new String[]{Integer.toString(rowId) };
//
//       // Delete record(s)
//       SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
//       try {
//           db.beginTransaction();
//           recordsDeleted = db.delete(TripDetails.TABLE, whereClause, whereArgs);
//           db.setTransactionSuccessful();
//       } catch (Exception e) {
//           Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
//       } finally {
//           db.endTransaction();
//           DatabaseManager.getInstance().closeDatabase();
//       }

        return recordsDeleted;
    }

    /**
     * Update record in database specified by the TripDetails.id which is the index of the table.
     * @param TripDetails  TripDetails:
     * @return  int:  Number of rows updated.
     */
    public int update(TripDetails TripDetails){
        int recordsUpdated = 0;

        // TODO:  Would we really have an update?

//       ContentValues values = new ContentValues();
//       values.put(TripDetails.KEY_TRIP_DETAILS_TRIP_ID, TripDetails.getTripId());
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

        return recordsUpdated;
    }

}
