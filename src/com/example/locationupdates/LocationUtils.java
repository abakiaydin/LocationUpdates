package com.example.locationupdates;

import android.content.Context;
import android.location.Location;

public final class LocationUtils {
	
//	Debugging tag for the application
	public static final String APPTAG = "LocationUpdates";
	
//	Name of the shared preferences repo
	public static final String SHARED_PREFERENCES = 
			"com.example.locationupdates.SHARED_PREFERENCES";
	
//	Key for the updates requested flag in shared preferences
	public static final String KEY_UPDATES_REQUESTED = 
			"com.example.locationupdates.KEY_UPDATES_REQUESTED";
	
//	Request code for google play services
	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
//	constanst for location update parameters
	
	public static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	public static final int FAST_CEILING_IN_SECONDS = 1;
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 
			MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = 
			MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
	
	public static final String EMPTY_STRING = new String();
	
	public static String getLatLng(Context context, Location currentLocation) {
		if (currentLocation != null) {
			return context.getString(R.string.latitude_longitude,
					currentLocation.getLatitude(),
					currentLocation.getLongitude());
		} else {
			return EMPTY_STRING;
		}
		
	}

}
