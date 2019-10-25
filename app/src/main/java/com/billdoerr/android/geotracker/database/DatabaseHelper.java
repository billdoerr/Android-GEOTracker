package com.billdoerr.android.geotracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.billdoerr.android.geotracker.database.repo.ActivityTypeRepo;
import com.billdoerr.android.geotracker.database.repo.RouteRepo;
import com.billdoerr.android.geotracker.database.repo.TripDetailsRepo;
import com.billdoerr.android.geotracker.database.repo.TripRepo;

/**
 * Helper class to manage database creation and version management.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "geo_tracker.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DatabaseHelper instantiated.");
    }

    //  Only called when the database needs to be created.
    //  Not called from your constructor; it’s called by the Android framework when you try to access your database.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ActivityTypeRepo.createTable());
        db.execSQL(RouteRepo.createTable());
        db.execSQL(TripRepo.createTable());
        db.execSQL(TripDetailsRepo.createTable());

        ActivityTypeRepo.insertDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ActivityTypeRepo.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + RouteRepo.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + TripRepo.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + TripDetailsRepo.getTableName());
        onCreate(db);
    }


//    /**
//     * Helper routine to insert record into database.
//     * @param db  SQLiteDatabase:
//     * @param values ContentValues:  Data values to be inserted.
//     */
//    private int insert(SQLiteDatabase db, String table, ContentValues values) {
//        int rowId = -1;
//        try {
//            db.beginTransaction();
//            rowId = (int)db.insert(table, null, values);
//            db.setTransactionSuccessful();
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
//        } finally {
//            db.endTransaction();
//        }
//        return rowId;
//    }

}
