package com.billdoerr.android.geotracker.activities;
// https://medium.com/@sandeeptengale/writing-good-baseactivity-class-for-android-activity-100636c81011
// https://www.simplifiedcoding.net/bottom-navigation-android-example/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.billdoerr.android.geotracker.services.GPSService;
import com.billdoerr.android.geotracker.settings.SettingsActivity;
import com.billdoerr.android.geotracker.utils.SharedPreferencesUtils;
import com.billdoerr.android.geotracker.utils.PreferenceUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
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
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

/**
 * Main activity which other activities extend from.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    // System log filename
    private static final String SYS_LOG = "geo_tracker.log";

    private ProgressDialog mProgressDialog;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigationView;

    private Intent mGPSServiceIntent;

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
                    .replace(R.id.fragment_container, fragment)
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

        // Start GPS Service
        startGPSServices();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_common, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
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
        // Stop GPSService when app is closed. If tracking in progress, TrackingService will restart the GPSService
        stopGPSService();
        super.onDestroy();
    }

    // Add onRequestPermissionsResult() in fragment
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    /**
     * Returns layout resource id.  Usage:  setContentView(getLayoutResId());
     * @return int:  Returns layout resource id.
     */
    @SuppressWarnings("SameReturnValue")
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.drawer_view;
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
     * Implements Drawer View
     */
    protected void createDrawerView() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
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
                                loadFragmentReplace(fragment);
                                return true;
                            // Fragment:  Routes
                            case R.id.drawer_routes:
                                fragment = new RouteListFragment();
//                               fr.setArguments(args);
                                loadFragmentReplace(fragment);
                                return true;
                            // Activity:  Settings
                            case R.id.drawer_settings:
                                startActivity(new Intent(BaseActivity.this, SettingsActivity.class));
                                return true;
                            // Fragment:  About
                            case R.id.drawer_about:
//                               startActivity(new Intent(BaseActivity.this, MainActivity.class));
                                fragment = new AboutFragment();
                                loadFragmentReplace(fragment);
                                return true;
                            default:
                                return true;
                        }
                    }
                });
    }

    /**
     * Creates the Bottom Navigation View
     */
    protected void createBottomNavigationView() {
        mBottomNavigationView = findViewById(R.id.bottom_navigation_view);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_trips:
                        fragment = new TripListFragment();
                        loadFragmentReplace(fragment);
                        return true;
                    case R.id.navigation_track:
                        fragment = new TrackingFragment();
                        loadFragmentReplace(fragment);
                        return true;
                    case R.id.navigation_maps:
                        fragment = new MapsFragment();
                        loadFragmentReplace(fragment);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * Enable/Disable the visibility of the BottomNavigationView
     * @param visible boolean
     */
    protected void setBottomNavigationViewVisibility(boolean visible) {
        if (visible) {
            mBottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            mBottomNavigationView.setVisibility(View.GONE);
        }
    }

    /**
     * Creates fragment.  https://www.simplifiedcoding.net/bottom-navigation-android-example/
     * @param fragment Fragment
     */
    private void loadFragmentAdd(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * Creates fragment.  https://www.simplifiedcoding.net/bottom-navigation-android-example/
     * @param fragment Fragment
     */
    private void loadFragmentReplace(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();

        // This prevents fragments from bleeding through
        clearBackStack(fm);

        // Create fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
//                    .remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                    .replace(R.id.fragment_container, fragment)
//                    .addToBackStack( fragment.getClass().getSimpleName() )
                    .commit();
        }
    }

    /**
     * This prevents fragments from bleeding through
     * @param fm FragmentManager
     */
    public void clearBackStack(FragmentManager fm) {
        //Here we are clearing back stack fragment entries
        int backStackEntry = fm.getBackStackEntryCount();
        if (backStackEntry > 0) {
            for (int i = 0; i < backStackEntry; i++) {
                fm.popBackStackImmediate();
            }
        }
    }

    /**
     * Initialize instance of DatabaseManager
     * @return  DatabaseHelper
     */
    @SuppressWarnings("UnusedReturnValue")
    protected DatabaseHelper initDatabase() {
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        DatabaseManager.initializeInstance(db);
        return db;
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
     * Check preferences whether to keep screen on.
     * @param context Context:  Application context.
     */
    private void initPowerSavings(Context context) {
        SharedPreferencesUtils sharedPrefs = PreferenceUtils.getSharedPreferences(this);
        boolean keepDeviceAwake = sharedPrefs.isKeepDeviceAwake();

        if (keepDeviceAwake) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    /**
     * Start GPS service
     */
    private void startGPSServices() {
        mGPSServiceIntent = new Intent(this, GPSService.class);
        Objects.requireNonNull(startService(mGPSServiceIntent));
    }

    /**
     * Stop GPS service
     */
    private void stopGPSService() {
        if (mGPSServiceIntent != null) {
            stopService(mGPSServiceIntent);
        }
    }

}
