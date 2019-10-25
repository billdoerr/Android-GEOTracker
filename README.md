# Android GEOTracker project

## GEOTracker project

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
    - Sync data.  Upload data to cloud.


### Known Issues 
- [ ] osmdroid thunderforest overlay isn't high resolution. Don't know how GAIA gps has such high resolution.   



An application in production, you must not have any log statements. To enable your logs statements calls only during development phase, 
Android offers you the BuildConfig.DEBUG property. This flag is set automatically to false when an application if deployed into an APK 
for production and then, it’s set to true during development.

To use it, you must write your log statements like that :

if (BuildConfig.DEBUG) {
    Log.d(TAG + "message");
}    