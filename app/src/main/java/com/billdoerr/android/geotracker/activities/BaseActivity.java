package com.billdoerr.android.geotracker.activities;
// https://medium.com/@sandeeptengale/writing-good-baseactivity-class-for-android-activity-100636c81011
// https://www.simplifiedcoding.net/bottom-navigation-android-example/

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.DatabaseHelper;
import com.billdoerr.android.geotracker.database.DatabaseManager;
import com.billdoerr.android.geotracker.fragments.ActivityTypeListFragment;
import com.billdoerr.android.geotracker.fragments.AboutFragment;
import com.billdoerr.android.geotracker.fragments.MapsFragment;
import com.billdoerr.android.geotracker.fragments.RouteListFragment;
import com.billdoerr.android.geotracker.fragments.TrackingFragment;
import com.billdoerr.android.geotracker.fragments.TripListFragment;
import com.billdoerr.android.geotracker.settings.SettingsActivity;
import com.billdoerr.android.geotracker.utils.FileStorageUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.Objects;

/**
 * Main activity which other activities extend from.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    private static final String PREF_KEY_KEEP_DEVICE_AWAKE = "com.billdoerr.android.geotracker.settings.PREF_KEY_POWER_SAVINGS_KEEP_DEVICE_ON";

    // System log filename
    public static final String SYS_LOG = "geo_tracker.log";

    private ProgressDialog mProgressDialog;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;

    protected abstract Fragment createFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        // Implement Toolbar
        mToolbar = createToolbar();

        // Implement Drawer View
        createDrawerView();

        // Implement NavigationView
        createNavigationView();

        // Implement BottomNavigationView
        createBottomNavigationView();

        // Initialize database instance
        initDatabase();

        // Check preferences whether to keep screen on
        initPowerSavings(getApplicationContext());


    }

    //  TODO:  onCreateOptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_common, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Returns layout resource id.  Usage:  setContentView(getLayoutResId());
     * @return int:  Returns layout resource id.
     */
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.drawer_view;
    }

    /**
     * Implements Drawer View
     */
    protected void createDrawerView() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    /**
     * Creates the Bottom Navigation View
     */
    protected void createBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_trips:
//                        startActivity(new Intent(BaseActivity.this, TripsActivity.class));
                        fragment = new TripListFragment();
                        loadFragmentReplace(fragment);
                        return true;
                    case R.id.navigation_track:
//                        startActivity(new Intent(BaseActivity.this, TrackingActivity.class));
                        fragment = new TrackingFragment();
                        loadFragmentReplace(fragment);
                        return true;
                    case R.id.navigation_maps:
//                        startActivity(new Intent(BaseActivity.this, MapsActivity.class));
                        fragment = new MapsFragment();
                        loadFragmentReplace(fragment);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * Creates the NavigationView for use with DrawerLayout
     */
    protected void createNavigationView() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                        Fragment fragment;

                        // Set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // Close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Launch activity/fragment
                        switch (menuItem.getItemId()) {
                            // Fragment:  Activities
                            case R.id.drawer_activities:
                                fragment = new ActivityTypeListFragment();
                                return loadFragmentReplace(fragment);
                            // Fragment:  Routes
                            case R.id.drawer_routes:
                                fragment = new RouteListFragment();
//                               fr.setArguments(args);
                                return loadFragmentReplace(fragment);
                            // Activity:  Settings
                            case R.id.drawer_settings:
                                startActivity(new Intent(BaseActivity.this, SettingsActivity.class));
                                return true;
                            // Fragment:  About
                            case R.id.drawer_about:
//                               startActivity(new Intent(BaseActivity.this, MainActivity.class));
                                fragment = new AboutFragment();
                                return loadFragmentReplace(fragment);
                            default:
                                return true;
                        }
                    }
                });
    }

    /**
     * Creates fragment.  https://www.simplifiedcoding.net/bottom-navigation-android-example/
     * @param fragment Fragment
     * @return  boolean:  Returns true if fragment created
     */
    private boolean loadFragmentAdd(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Creates fragment.  https://www.simplifiedcoding.net/bottom-navigation-android-example/
     * @param fragment Fragment
     * @return  boolean:  Returns true if fragment created
     */
    private boolean loadFragmentReplace(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Initialize instance of DatabaseManager
     * @return  DatabaseHelper
     */
    protected DatabaseHelper initDatabase() {
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        DatabaseManager.initializeInstance(db);
        return db;
    }

    /**
     * Create Toolbar
     */
    protected Toolbar createToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        try {
//           actionbar.setDisplayHomeAsUpEnabled(true);
            showBackArrow();
            Objects.requireNonNull(actionbar).setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24px);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return toolbar;
    }

    /**
     * actionBar.setHomeButtonEnabled(true) will just make the icon clickable,
     * with the color at the background of the icon as a feedback of the click.
     *
     * actionBar.setDisplayHomeAsUpEnabled(true) will make the icon clickable
     * and add the < at the left of the icon.
     */
    protected void showBackArrow() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            // Set whether to include the application home affordance in the action bar.
            // (and put a back mark at icon in ActionBar for "up" navigation)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            // Enable or disable the "home" button in the corner of the action bar.
            // (clickable or not)
            supportActionBar.setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * Displays progress dialog.
     * @param msg  String:  Message to display.
     */
    protected void showProgress(String msg) {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            dismissProgress();

        mProgressDialog = ProgressDialog.show(this, getResources().getString(R.string.app_name), msg);
    }

    /**
     * Dismisses progress dialog.
     */
    protected void dismissProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * Hides the soft keyboard.
     */
    protected void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.exception_error_hiding_keyboard), e);
        }
    }


    /**
     * Wrapper for Android Toast.  Long duration.
     * @param msg  String:  Message to be displayed.
     */
    protected void showLongToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Wrapper for Android Toast.  Short duration.
     * @param msg  String:  Message to be displayed.
     */
    protected void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays alert dialog with Ok button.
     * @param msg String:  Message to be displayed.
     */
    protected void showAlert(String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.app_name))
                .setMessage(msg)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    /**
     * Writes message to app defined log file.
     * @param msg  String:  Message to be written to file.
     */
    private void writeSystemLog(String msg) {
        FileStorageUtils.writeSystemLog(this.getApplicationContext(), SYS_LOG,TAG + FileStorageUtils.TABS + msg + FileStorageUtils.LINE_SEPARATOR);
    }

    /**
     * Check preferences whether to keep screen on.
     * @param context Context:  Application context.
     */
    private void initPowerSavings(Context context) {

        SharedPreferences appSharedPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        boolean keepDeviceAwake = appSharedPrefs.getBoolean(PREF_KEY_KEEP_DEVICE_AWAKE, true);
        if (keepDeviceAwake) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }
}
