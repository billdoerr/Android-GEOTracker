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
public class RouteRepo {

    private static final String TAG = "RouteRepo";

    private Route mRoute;

    public RouteRepo() {
        mRoute = new Route();
    }

    /**
     * Returns SQL string to create database.  Used by SQLiteDatabase.execSQL().
     * @return String:  Database creation SQL string.
     */
    public static String createTable() {
        return "CREATE TABLE " + Route.TABLE + "("
                + Route.KEY_ROUTE_ID + " INTEGER PRIMARY KEY, "
                + Route.KEY_ROUTE_NAME + " TEXT NOT NULL UNIQUE, "
                + Route.KEY_ROUTE_DESC + " TEXT, "
                + Route.KEY_ROUTE_ACTIVE_FLAG + " INT, "
                + Route.KEY_ROUTE_ACTIVITY_TYPE_ID + " INT)";
    }

    /**
     * Insert record into database.
     * @param Route  Route:
     * @return Returns -1 if error else returns row id of inserted record.
     */
    public int insert(Route Route) {
        int rowId = -1;

        ContentValues values = new ContentValues();
        values.put(Route.KEY_ROUTE_NAME, Route.getRouteName());
        values.put(Route.KEY_ROUTE_DESC, Route.getRouteDesc());
        values.put(Route.KEY_ROUTE_ACTIVE_FLAG, Route.getRouteActiveFlag());
        values.put(Route.KEY_ROUTE_ACTIVITY_TYPE_ID, Route.getRouteActivityTypeId());

        // Insert row
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            rowId = (int)db.insert(Route.TABLE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }

        return rowId;
    }

    // TODO:  RouteRepo.delete() not implemented
    /**
     * Delete record(s) from database specified by the index.
     * @param rowId  int:  Index for the Route.
     * @return int:  Number of records deleted.
     */
    public int delete(int rowId) {
        int recordsDeleted = 0;

        String whereClause = Route.KEY_ROUTE_ID + " = ?";
        String[] whereArgs = new String[]{Integer.toString(rowId) };

        // Delete record(s)
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            recordsDeleted = db.delete(Route.TABLE, whereClause, whereArgs);
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
     * Update record in database specified by the Route.id which is the index of the table.
     * @param route  Route:
     * @return  int:  Number of rows updated.
     */
    public int update(Route route){
        int recordsUpdated = 0;

        ContentValues values = new ContentValues();
        values.put(Route.KEY_ROUTE_NAME, route.getRouteName());
        values.put(Route.KEY_ROUTE_DESC, route.getRouteDesc());
        values.put(Route.KEY_ROUTE_ACTIVE_FLAG, route.getRouteActiveFlag());
        values.put(Route.KEY_ROUTE_ACTIVITY_TYPE_ID, route.getRouteActivityTypeId());

        String whereClause = Route.KEY_ROUTE_ID + " = ?";
        String[] whereArgs = new String[]{ Integer.toString(route.getRouteId()) };

        // Update record
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            recordsUpdated = db.update(Route.TABLE, values, whereClause, whereArgs);
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
    public List<Route> getRoutes() {
        List<Route> routes = new ArrayList<>();
        Route route;

        String selectQuery = "SELECT "
                + Route.KEY_ROUTE_ID + ", "
                + Route.KEY_ROUTE_NAME + ", "
                + Route.KEY_ROUTE_DESC + ", "
                + Route.KEY_ROUTE_ACTIVE_FLAG + ", "
                + Route.KEY_ROUTE_ACTIVITY_TYPE_ID
                + " FROM " + Route.TABLE
                + " ORDER BY " +  Route.KEY_ROUTE_NAME + " ASC";

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try {
            Log.d(TAG, selectQuery);
            Cursor cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    route = new Route();
                    route.setRouteId(cursor.getInt(cursor.getColumnIndex(Route.KEY_ROUTE_ID)));
                    route.setRouteName(cursor.getString(cursor.getColumnIndex(Route.KEY_ROUTE_NAME)));
                    route.setRouteDesc(cursor.getString(cursor.getColumnIndex(Route.KEY_ROUTE_DESC)));
                    route.setRouteActiveFlag(cursor.getInt(cursor.getColumnIndex(Route.KEY_ROUTE_ACTIVE_FLAG)));
                    route.setRouteActivityTypeId(cursor.getInt(cursor.getColumnIndex(Route.KEY_ROUTE_ACTIVITY_TYPE_ID)));

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
