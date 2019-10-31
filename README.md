# Android GEOTracker App

## GEOTracker

### Develop Android application to track GPS data for hikes, bikes, car trips, etc.

	- Simple Android app that tracks GPS location.
	- Use for bike/hike/car/etc.
	- Write to SQLite database.
		- Sync with external database.
    - Able to add routes.

### Features
    - Track GPS data for different activities.  
    - Users will have the ability to create different activities.  App will have default activities of bike, run, hike, walk.
    - Data saved to sqllite database.
    - User will have the ability to filter view by activity and routes.
    - Display GPS data on map.
### Planned Features    
    - Add photos.   
    - Analytics. 
    - Sync data. Upload data to cloud.

### Known Issues 
    - osmdroid thunderforest overlay isn't high resolution. Don't know how GAIA gps has such high resolution.   

___
### HowTo
___
**Grant Permissions**<br>
When GEOTracker is first launch it will request the user to grant Location and Storage permissions.  
The app is useless without these two permissions.<br>  
<img src="images/location_permissions.png" width="200"/>
<img src="images/storage_permissions.png" width="200"/>

**Map**<br>
The home screen defaults to the Map view which displays a marker off the device's current location.  
The marker position will update as the location changes but no data is stored at this point. It also contains a **Compass** overlay in the top left.<br>
<img src="images/home.png" width="200"/>

**Trips**<br>
Contains a list of trips that have been saved. You have the ability Edit, Delete and Review trips.<br>
<img src="images/trips.png" width="200"/>

**Filter** the list list by click the <img src="images/filter_icon.png" width="10"/> icon. Click **Apply Filter** to select your filter criteria. Clicking **Clear Filter** resets the view.<br>
**Search** the list list by click the <img src="images/search_icon.png" width="10"/> icon.<br>
<img src="images/filter_routes.png" width="200"/>
<img src="images/trip_search.png" width="200"/><br>

**Delete** a trip by selecting **Trip delete**.<br>

**Edit Trip** by selecting **Edit trip**. This allow the user to change trip name, activity, comments, etc. No ability is given to change the actual trip data.<br>
<img src="images/add_trip.png" width="200"/><br>

**Trip Review**<br>
Selecting **Review Trip** provides the user the ability to the tracking and mapped data.<br>
<img src="images/trip_review.png" width="200"/>
<img src="images/trip_review_detail.png" width="200"/><br>

**Track**<br>
This is the heart of the app. This tracks the device location.<br>
<img src="images/tracking.png" width="200"/>

The tracking is controlled by the **Start**, **Pause** and **Stop** buttons.<br>
<img src="images/tracking_action_bar.png" width="200"/>

**Start**<br>
Click the <img src="images/start_icon.png" width="10"/> button to begin tracking.<br>
This displays the **Edit Trip** dialog where the user enters the trip details. The trip name has autocomplete based on saved routes.<br>
<img src="images/add_trip.png" width="200"/>

<span style="color:red">**When tracking starts a notification will be display in the Notification Bar showing the Location Services has been started.**</span><br>
<span style="color:red">**This Location Service will remain running even when the app is closed as a Power Savings feature.**</span><br>
<span style="color:red">**If the trip is not stopped and saved then tracking will continue even if the app is dismissed.**</span><br>
<img src="images/notification_bar.png" width="200"/>

**Pause**<br>
Click the <img src="images/pause_icon.png" width="10"/> button to pause the tracking of location data.<br>
This stops the accumulation of moving time while keeping track of the paused time.<br>

**Stop**<br>
Click the <img src="images/stop_icon.png" width="10"/> button to stop tracking.<br>
This displays the **Edit Trip** dialog where the user can edit trip details and also save the trip to the routes list.<br>
<span style="color:red">**If the trip is not stopped and saved then tracking will continue even if the app is dismissed.**</span><br>
<img src="images/save_trip.png" width="200"/>
___
**Drawer Menu**<br>
The hamburger icon <img src="images/hamburger.png" width="10"/> displays the drawer menu which also slides out from the left.<br>
<img src="images/drawer_menu.png" width="200"/><br>

The drawer menu contains the following:

**Activities**<br>
Contains a list of default activities which include Hike, Bike, Car Trip, Run.  
Additional activities can be added, but they cannot be deleted. A flag is include to mark an activity as inactive.<br>
<img src="images/activities.png" width="200"/>

To manually add an activity click the <img src="images/fab_add_icon.png" width="10"/> icon which displays the **New Activity** dialog.<br>
<img src="images/add_activity.png" width="200"/>

**Routes**<br>
Routes are just favorite trips. These can be added manually or when a trip is completed an option is provided to save 
the trip name as a route. Routes can be deleted.<br>
<img src="images/routes.png" width="200"/>

To manually add a route click the <img src="images/add_icon.png" width="10"/> icon which displays the **Add Route** dialog.<br>
<img src="images/add_route.png" width="200"/>

**Filter** the routes list by click the <img src="images/filter_icon.png" width="10"/> icon. Click **Apply Filter** to select your filter criteria. Clicking **Clear Filter** resets the view.<br>
<img src="images/filter_routes.png" width="200"/>

**Settings**<br>
<img src="images/settings.png" width="200"/>

The settings screen allows to configure the following:

Units:  Default units use the the english system, select metric to use the metric system.<br>
Coordinate Type:<br>
<img src="images/settings_coordinate_type.png" width="100"/>

Update Interval: Change the location update interval.<br>
<img src="images/settings_update_interval.png" width="100"/>

Update Distance: Change the location update interval.<br>
<img src="images/settings_update_distance.png" width="100"/>

Power Savings: Enabled by default. As long as this window is visible to the user, keep the device's screen turned on and bright.<br>

**About**<br>
Simple screen that shows the app name, version and build number. The build number is a date/time stamp when app was complied.  
An example build number is 1910300646: 19=year 10=month 03=day of month 06=hour 46=minutes<br>
<img src="images/about.png" width="200"/>
