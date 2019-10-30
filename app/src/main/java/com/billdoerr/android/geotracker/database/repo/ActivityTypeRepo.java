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
@SuppressWarnings("UnusedReturnValue")
public class ActivityTypeRepo {

    private static final String TAG = "ActivityTypeRepo";

    // Table name
    private static final String TABLE = "ActivityType";

    // Columns
    private static final String KEY_ACTIVITY_TYPE_ID = "activity_type_id";
    private static final String KEY_ACTIVITY_TYPE_NAME = "activity_type_name";
    private static final String KEY_ACTIVITY_TYPE_DESC = "activity_type_desc";
    private static final String KEY_ACTIVITY_TYPE_ACTIVE_FLAG = "activity_type_active_flag";

//    private final ActivityType mActivityType;
//
//    public ActivityTypeRepo() {
//        mActivityType = new ActivityType();
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
                + KEY_ACTIVITY_TYPE_ID + " INTEGER PRIMARY KEY, "
                + KEY_ACTIVITY_TYPE_NAME + " TEXT NOT NULL UNIQUE, "
                + KEY_ACTIVITY_TYPE_DESC + " TEXT, "
                + KEY_ACTIVITY_TYPE_ACTIVE_FLAG + " INT)";
    }

    /**
     * Insert record into database.
     * @param activityType  ActivityType:
     * @return Returns -1 if error else returns row id of inserted record.
     */
    public static int insert(ActivityType activityType) {
        int rowId = -1;

        ContentValues values = new ContentValues();
        values.put(KEY_ACTIVITY_TYPE_NAME, activityType.getName());
        values.put(KEY_ACTIVITY_TYPE_DESC, activityType.getDesc());
        values.put(KEY_ACTIVITY_TYPE_ACTIVE_FLAG, activityType.isActiveFlag());

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

//    /**
//     * Delete record(s) from database specified by the index.
//     * @param id int:  Index for the
//     * @return int:  Number of records deleted.
//     */
//    public static int delete(int id) {
//        int recordsDeleted = 0;
//
//        String whereClause = KEY_ACTIVITY_TYPE_ID + " = ?";
//        String[] whereArgs = new String[]{Integer.toString(id) };
//
//        // Delete record(s)
//        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
//        try {
//            db.beginTransaction();
//            recordsDeleted = db.delete(TABLE, whereClause, whereArgs);
//            db.setTransactionSuccessful();
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
//        } finally {
//            db.endTransaction();
//            DatabaseManager.getInstance().closeDatabase();
//        }
//
//        return recordsDeleted;
//    }

    /**
     * Update record in database specified by the id which is the index of the table.
     * @param activityType  ActivityType:
     * @return  int:  Number of rows updated.
     */
    public static int update(ActivityType activityType){
        int recordsUpdated = 0;

        ContentValues values = new ContentValues();
        values.put(KEY_ACTIVITY_TYPE_NAME, activityType.getName());
        values.put(KEY_ACTIVITY_TYPE_DESC, activityType.getDesc());
        values.put(KEY_ACTIVITY_TYPE_ACTIVE_FLAG, activityType.isActiveFlag());

        String whereClause = KEY_ACTIVITY_TYPE_ID + " = ?";
        String[] whereArgs = new String[]{ Integer.toString(activityType.getId()) };

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
     * Returns list of activities.
     * @return List<ActivityType>
     */
    public static List<ActivityType> getActivities() {
        List<ActivityType> activities = new ArrayList<>();
        ActivityType activityType;

        String selectQuery = "SELECT "
                + KEY_ACTIVITY_TYPE_ID + ", "
                + KEY_ACTIVITY_TYPE_NAME + ", "
                + KEY_ACTIVITY_TYPE_DESC + ", "
                + KEY_ACTIVITY_TYPE_ACTIVE_FLAG
                + " FROM " + TABLE
                + " ORDER BY " +  KEY_ACTIVITY_TYPE_NAME + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    activityType = new ActivityType();
                    activityType.setId(cursor.getInt(cursor.getColumnIndex(KEY_ACTIVITY_TYPE_ID)));
                    activityType.setName(cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY_TYPE_NAME)));
                    activityType.setDesc(cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY_TYPE_DESC)));
                    activityType.setActive(cursor.getInt(cursor.getColumnIndex(KEY_ACTIVITY_TYPE_ACTIVE_FLAG)));

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
     * Returns activity name given activity id.
     * @return String
     */
    public static String getActivityName(int id) {
        String name = "";

        String selectQuery = "SELECT "
                + KEY_ACTIVITY_TYPE_NAME
                + " FROM " + TABLE
                + " WHERE " +  KEY_ACTIVITY_TYPE_ID + " = " + id;

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    name = cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY_TYPE_NAME));
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

    /**
     * Inserts default data into the database.
     */
    public static void insertDefaultData(SQLiteDatabase db) {

        ContentValues values = new ContentValues();

        // Default data:  ActivityType

        values.clear();
        values.put(KEY_ACTIVITY_TYPE_NAME, "Run");
        values.put(KEY_ACTIVITY_TYPE_DESC, "Going for a run.");
        values.put(KEY_ACTIVITY_TYPE_ACTIVE_FLAG, ActivityType.INACTIVE);
        insert(db, TABLE, values);

        values.put(KEY_ACTIVITY_TYPE_NAME, "Bike");
        values.put(KEY_ACTIVITY_TYPE_DESC, "Going for a bike ride.");
        values.put(KEY_ACTIVITY_TYPE_ACTIVE_FLAG, ActivityType.ACTIVE);
        insert(db, TABLE, values);

        values.put(KEY_ACTIVITY_TYPE_NAME, "Hike");
        values.put(KEY_ACTIVITY_TYPE_DESC, "Damn those steep climbs.");
        values.put(KEY_ACTIVITY_TYPE_ACTIVE_FLAG, ActivityType.ACTIVE);
        insert(db, TABLE, values);

        values.put(KEY_ACTIVITY_TYPE_NAME, "Car Trip");
        values.put(KEY_ACTIVITY_TYPE_DESC, "Could use a Porsche.");
        values.put(KEY_ACTIVITY_TYPE_ACTIVE_FLAG, ActivityType.INACTIVE);
        insert(db, TABLE, values);

    }

    /**
     * Helper routine to insert record into database.
     * @param db  SQLiteDatabase:
     * @param values ContentValues:  Data values to be inserted.
     */
    @SuppressWarnings("SameParameterValue")
    private static int insert(SQLiteDatabase db, String table, ContentValues values) {
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
