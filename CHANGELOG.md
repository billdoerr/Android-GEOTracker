# Changelog for Android GEOTracker application
All notable changes to this Android application will be documented in this file.  


## [Unreleased]
### v1.4 (TBD)
#### Added
- [ ] Add map geo markers.   
    - [ ] Add photos
    - [ ] Add notes
    - [ ] Add video  
#### Changed
- [ ]
#### Removed
- [ ]


## [Unreleased]
### v1.3 (TBD)
#### Added
- [ ] Sync data.  Upload data to cloud or most likely a local database. Cloud would cost money. 
#### Changed
- [ ]
#### Removed
- [ ]


## [Unreleased]
### v1.2 (15Nov2019) (BETA)
#### Added
- [x] Have location marker change based on compass direction.
#### Changed
- [x] **RESOLVED:**  Try and resolve issue where user must press Back Button on TripReview screen for options menu to display on Routes screen. 
Undid this change in v1.1 (Disabled BottomNavigationView when navigating to Trip List->Trip Review). Issue solve by using 'getChildFragmentManager()' 
when initializing the PageViewAdapter instead of 'getSupportFragmentManager()'.
- [x] Enable auto backup which was set to false in previous releases.<br>
        android:allowBackup="true"<br>
        android:fullBackupContent="true"<br>
- [x] TrackDetailFragment
    - [x] Add data row:  Pace:  distance/time
#### Removed
- [x] Disabled BottomNavigationView when navigating to Trip List->Trip Review.


## [Released]
### v1.1 (6Nov2019)
#### Added
- [x] Get map overlay's to display in high resolution. Significant improvement.
- [x] Charts (MPAndroidCharts)
    - [x] Altitude LineChart
    - [x] Speed LineChart
    - [x] Route History implemented as BarChart
#### Changed
- [x] Removed FileStorageUtils class
- [x] Renamed GeoTrackerSharedPreferences to SharedPreferencesUtils
- [x] Migrate unit conversions to CoordinateConversionUtils
- [x] Disabled BottomNavigationView when navigating to Trip List->Trip Review
- [x] Updated build.gradle to use osmdroid v6.1.1. This corrects an exception generated when osmdroid calls 'mMapViewRepository.getDefaultMarkerIcon()'
- [x] Changed launch icon, not the best but it was free. Was using default Android icon.
#### Removed
- [ ]


## [Released]
### v1.0 (RELEASED) (31Oct2019)
- [x] Track GPS data for different activities.
- [x] Users will have the ability to create different activities.  App will have default activities of bike, run, hike, walk.
- [x] Data saved to sqllite database.
- [x] User will have the ability to filter view by activity and routes.
- [x] Display GPS data on map.



		

