package com.billdoerr.android.geotracker.database.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.billdoerr.android.geotracker.database.DatabaseManager;
import com.billdoerr.android.geotracker.database.model.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class which generates create, insert, delete,etc SQL statements.
 */
@SuppressWarnings("UnusedReturnValue")
public class RouteRepo {

    private static final String TAG = "RouteRepo";

    // Table name
    private static final String TABLE = "Route";

    // Columns
    private static final String KEY_ROUTE_ID = "route_id";
    private static final String KEY_ROUTE_NAME = "route_name";
    private static final String KEY_ROUTE_DESC = "route_desc";
    private static final String KEY_ROUTE_ACTIVE_FLAG = "route_active_flag";
    private static final String KEY_ROUTE_ACTIVITY_TYPE_ID = "activity_type_id";

//    private final Route mRoute;
//
//    public RouteRepo() {
//        mRoute = new Route();
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
                + KEY_ROUTE_ID + " INTEGER PRIMARY KEY, "
                + KEY_ROUTE_NAME + " TEXT NOT NULL UNIQUE, "
                + KEY_ROUTE_DESC + " TEXT, "
                + KEY_ROUTE_ACTIVE_FLAG + " INT, "
                + KEY_ROUTE_ACTIVITY_TYPE_ID + " INT)";
    }

    /**
     * Insert record into database.
     * @param route  Route:
     * @return Returns -1 if error else returns row id of inserted record.
     */
    public static int insert(Route route) {
        int rowId = -1;

        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE_NAME, route.getName());
        values.put(KEY_ROUTE_DESC, route.getDesc());
        values.put(KEY_ROUTE_ACTIVE_FLAG, route.isActive());
        values.put(KEY_ROUTE_ACTIVITY_TYPE_ID, route.getActivityTypeId());

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
     * @param id  int:  Index for the
     * @return int:  Number of records deleted.
     */
    public static int delete(int id) {
        int recordsDeleted = 0;

        String whereClause = KEY_ROUTE_ID + " = ?";
        String[] whereArgs = new String[]{Integer.toString(id) };

        // Delete record(s)
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
     * @param route  Route:
     * @return  int:  Number of rows updated.
     */
    public static int update(Route route){
        int recordsUpdated = 0;

        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE_NAME, route.getName());
        values.put(KEY_ROUTE_DESC, route.getDesc());
        values.put(KEY_ROUTE_ACTIVE_FLAG, route.isActive());
        values.put(KEY_ROUTE_ACTIVITY_TYPE_ID, route.getActivityTypeId());

        String whereClause = KEY_ROUTE_ID + " = ?";
        String[] whereArgs = new String[]{ Integer.toString(route.getId()) };

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
     * Returns list of routes.
     * @return List<Route>
     */
    public static List<Route> getRoutes() {
        List<Route> routes = new ArrayList<>();
        Route route;

        String selectQuery = "SELECT "
                + KEY_ROUTE_ID + ", "
                + KEY_ROUTE_NAME + ", "
                + KEY_ROUTE_DESC + ", "
                + KEY_ROUTE_ACTIVE_FLAG + ", "
                + KEY_ROUTE_ACTIVITY_TYPE_ID
                + " FROM " + TABLE
                + " ORDER BY " +  KEY_ROUTE_NAME + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    route = new Route();
                    route.setId(cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_ID)));
                    route.setName(cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NAME)));
                    route.setDesc(cursor.getString(cursor.getColumnIndex(KEY_ROUTE_DESC)));
                    route.setActive(cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_ACTIVE_FLAG)));
                    route.setActivityTypeId(cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_ACTIVITY_TYPE_ID)));
                    routes.add(route);
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

        return routes;
    }

    /**
     * Returns array of route names.
     * @return List<Route>
     */
    public static List<String> getRoutesNames() {
        List<String> array = new ArrayList<>();

        String selectQuery = "SELECT "
                + KEY_ROUTE_NAME
                + " FROM " + TABLE
                + " ORDER BY " +  KEY_ROUTE_NAME + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    array.add(cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NAME)));
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

        return array;
    }

    //  TODO:  This is crap, big pile of!
    /**
     * Returns list of routes.
     * @return List<ActivityType>
     */
    public static List<Route> filterRoutes(int activeFlag, int activityId) {
        List<Route> routes = new ArrayList<>();
        Route route;

        String s1 = "";
        String s2 = "";
        String where = "";
        if (activeFlag >= 0) {
            s1 = KEY_ROUTE_ACTIVE_FLAG + " = " + activeFlag;
        }
        if (activityId > 0) {
            s2 = KEY_ROUTE_ACTIVITY_TYPE_ID + " = " + activityId;
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
                + KEY_ROUTE_ID + ", "
                + KEY_ROUTE_NAME + ", "
                + KEY_ROUTE_DESC + ", "
                + KEY_ROUTE_ACTIVE_FLAG + ", "
                + KEY_ROUTE_ACTIVITY_TYPE_ID
                + " FROM " + TABLE
                + where
                + " ORDER BY " +  KEY_ROUTE_NAME + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    route = new Route();
                    route.setId(cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_ID)));
                    route.setName(cursor.getString(cursor.getColumnIndex(KEY_ROUTE_NAME)));
                    route.setDesc(cursor.getString(cursor.getColumnIndex(KEY_ROUTE_DESC)));
                    route.setActive(cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_ACTIVE_FLAG)));
                    route.setActivityTypeId(cursor.getInt(cursor.getColumnIndex(KEY_ROUTE_ACTIVITY_TYPE_ID)));
                    routes.add(route);
                } while (cursor.moveToNext());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

        return routes;
    }

}
