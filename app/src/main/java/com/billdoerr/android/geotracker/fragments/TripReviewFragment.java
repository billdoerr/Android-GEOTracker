package com.billdoerr.android.geotracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.billdoerr.android.geotracker.R;
import com.billdoerr.android.geotracker.database.model.Trip;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class TripReviewFragment extends Fragment {

    public static final String TAG = "TripReviewFragment";

    private static final String ARGS_TRIP = "trip";

    private Trip mTrip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mTrip = (Trip) Objects.requireNonNull(args).getSerializable(ARGS_TRIP);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trip_review, container, false);

        //  Setup action bar
        setupActionBar();

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //  Add icons
//        addTabLayoutIcons();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Change the toolbar title text
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle(R.string.fragment_title_maps);
    }

    /**
     * Add fragments to tabs.
     * @param viewPager ViewPager: Layout manager adapter will be assigned.
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(Objects.requireNonNull(getActivity()).getSupportFragmentManager());

        // Maps fragment
        Bundle args = new Bundle();
        args.putSerializable(ARGS_TRIP, mTrip);

        TripReviewMapsFragment fragment = new TripReviewMapsFragment();
        fragment.setArguments(args);

        // Add fragment to adapter
        adapter.addFragment(fragment, getResources().getString(R.string.activity_title_maps));

        //  Set adapter to view pager
        viewPager.setAdapter(adapter);
    }

    /**
     * Setup action bar.
     */
    private void setupActionBar() {
        ActionBar actionbar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24px);
    }

//    /**
//     * Add icons to tabs.
//     */
//    private void addTabLayoutIcons() {
//        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
//            mTabLayout.getTabAt(i).setIcon(R.drawable.ic_baseline_photo_library_24px);
//        }
//    }

    /**
     * View Adapter Class.
     */
    class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager manager) {
            /*
             * Indicates that only the current fragment will be
             * in the Lifecycle.State#RESUMED state. All other Fragments
             * are capped at Lifecycle.State#STARTED.
             */
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        /**
         * Add fragment to tab.
         * @param fragment Fragment: Fragment to be added to tab layout.
         * @param title String:  Fragment title.
         */
        private void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//
//        final long itemId = getItemId(position);
//
//        // Do we already have this fragment?
////        String name = makeFragmentName(container.getId(), itemId);
//        Fragment fragment = mFragmentManager.findFragmentByTag(name);
//        if (fragment != null) {
//            mCurTransaction.attach(fragment);
//        } else {
//            fragment = getItem(position);
//            mCurTransaction.add(container.getId(), fragment,
//                    makeFragmentName(container.getId(), itemId));
//        }
//    }



}
