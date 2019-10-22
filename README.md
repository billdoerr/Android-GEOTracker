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
    - Users will have the ability to create different activities.  App will have default activities of bike, run, hike, walk.
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

        
AutoCompleteTextView
  
 
OpenCycleMap
    https://tile.thunderforest.com/cycle/{z}/{x}/{y}.png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716 
Transport
    https://tile.thunderforest.com/transport/{z}/{x}/{y}.png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716 
Landscape
    https://tile.thunderforest.com/landscape/{z}/{x}/{y}.png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716 
Outdoors
    https://tile.thunderforest.com/outdoors/{z}/{x}/{y}.png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716 
Transport Dark
    https://tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716 
Spinal Map
    https://tile.thunderforest.com/spinal-map/{z}/{x}/{y}.png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716 
Pioneer
    https://tile.thunderforest.com/pioneer/{z}/{x}/{y}.png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716 
Mobile Atlas
    https://tile.thunderforest.com/mobile-atlas/{z}/{x}/{y}.png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716 
Neighbourhood
    https://tile.thunderforest.com/neighbourhood/{z}/{x}/{y}.png?apikey=0fd1dc369a2f49adb3bbb6892ebf3716    
    
    
    
     <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabAdd"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="80dp"
        android:layout_marginEnd="8dp"
        android:clickable="true"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:srcCompat="@drawable/ic_baseline_add_24px"
        android:focusable="true" />
        
         autocomplete = (AutoCompleteTextView)             
      findViewById(R.id.autoCompleteTextView1);

      ArrayAdapter<String> adapter = new ArrayAdapter<String>  
      (this,android.R.layout.select_dialog_item, arr);

      autocomplete.setThreshold(2);
      autocomplete.setAdapter(adapter);   