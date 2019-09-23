package com.billdoerr.android.geotracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.billdoerr.android.geotracker.database.model.ActivityType;
import com.billdoerr.android.geotracker.database.model.Route;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.billdoerr.android.geotracker.database.model.TripDetails;
import com.billdoerr.android.geotracker.database.repo.ActivityTypeRepo;
import com.billdoerr.android.geotracker.database.repo.RouteRepo;
import com.billdoerr.android.geotracker.database.repo.TripDetailsRepo;
import com.billdoerr.android.geotracker.database.repo.TripRepo;

/**
 * Helper class to manage database creation and version management.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME = "geo_tracker.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DatabaseHelper instantiated.");
    }

    //  Only called when the database needs to be created.
    //  Not called from your constructor; it’s called by the Android framework when you try to access your database.
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating database tables.");
        db.execSQL(ActivityTypeRepo.createTable());
        db.execSQL(RouteRepo.createTable());
        db.execSQL(TripRepo.createTable());
        db.execSQL(TripDetailsRepo.createTable());

        Log.d(TAG, "Inserting default values.");
        insertDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ActivityType.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Route.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Trip.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TripDetails.TABLE);
        onCreate(db);
    }

    /**
     * Inserts default data into the database.
     */
    private void insertDefaultData(SQLiteDatabase db) {

        ContentValues values = new ContentValues();

        // Default data:  ActivityType

        values.clear();
        values.put(ActivityType.KEY_ACTIVITY_TYPE_NAME, "Run");
        values.put(ActivityType.KEY_ACTIVITY_TYPE_DESC, "Going for a run.");
        values.put(ActivityType.KEY_ACTIVITY_TYPE_ACTIVE_FLAG, ActivityType.INACTIVE);
        insert(db, ActivityType.TABLE, values);

        values.put(ActivityType.KEY_ACTIVITY_TYPE_NAME, "Bike");
        values.put(ActivityType.KEY_ACTIVITY_TYPE_DESC, "Going for a bike ride.");
        values.put(ActivityType.KEY_ACTIVITY_TYPE_ACTIVE_FLAG, ActivityType.ACTIVE);
        insert(db, ActivityType.TABLE, values);

        values.put(ActivityType.KEY_ACTIVITY_TYPE_NAME, "Hike");
        values.put(ActivityType.KEY_ACTIVITY_TYPE_DESC, "Damn those steep climbs.");
        values.put(ActivityType.KEY_ACTIVITY_TYPE_ACTIVE_FLAG, ActivityType.ACTIVE);
        insert(db, ActivityType.TABLE, values);

        values.put(ActivityType.KEY_ACTIVITY_TYPE_NAME, "Car Trip");
        values.put(ActivityType.KEY_ACTIVITY_TYPE_DESC, "Could use a Porsche.");
        values.put(ActivityType.KEY_ACTIVITY_TYPE_ACTIVE_FLAG, ActivityType.INACTIVE);
        insert(db, ActivityType.TABLE, values);

    }

    /**
     * Helper routine to insert record into database.
     * @param db  SQLiteDatabase:
     * @param values ContentValues:  Data values to be inserted.
     */
    private int insert(SQLiteDatabase db, String table, ContentValues values) {
        int rowId = -1;
        try {
            db.beginTransaction();
            rowId = (int)db.insert(table, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return rowId;
    }

}
