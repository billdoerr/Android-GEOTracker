# Android GEOTracker project

## GEOTracker project

- **Develop Android application to track GPS data for hikes, bikes, car trips, etc.**  <br />

	- Simple Android app that tracks GPS location.
	- Use for bike/hike/car/etc.
	- Write to SQLite database.
		- Sync with external database.
    - Able to add routes.

## Features
1. **PRIMARY GOAL**
    - Track GPS data for different activities.  
    - Users will have the ability to create different activites.  App will have default activities of bike, run, hike, walk.
    - Data saved to sqllite database.
    - User will have the ability to filter view by activity and routes.
2. **SECONDARY GOAL** 
    - Display GPS data on map.
3. **STRETCH GOAL** 
    - Add photos.   
4. **STRETCH GOAL** 
    - Analytics. 
5. **STRETCH GOAL** 
    - Sync data.  Upload data to cloud.
    
    <https://medium.com/@hinchman_amanda/the-singlefragmentactivity-pattern-in-android-kotlin-ce93385252e5>

   slf4j-android-1.5.8.jar 
   osmdroid-android-4.1.jar 
   
       <org.osmdroid.views.MapView
        android:id="@+id/mapz"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"  />