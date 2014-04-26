package com.example.locationupdates;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity implements 
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener,
	LocationListener {
	
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	
	private TextView mLatLng;
	private TextView mAddress;
	private ProgressBar mActivityIndicator;
	private TextView mConnectionState;
	private TextView mConnectionStatus;
	
	SharedPreferences mPrefs;
	SharedPreferences.Editor mEditor;
	
	boolean mUpdatesRequested = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLatLng = (TextView) findViewById(R.id.lat_lng);
		mAddress = (TextView) findViewById(R.id.address);
		mActivityIndicator = (ProgressBar) findViewById(R.id.address_progress);
		mConnectionState = (TextView) findViewById(R.id.text_connection_state);
		mConnectionStatus = (TextView) findViewById(R.id.text_connection_status);
		
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		mUpdatesRequested = false;
		mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		mEditor = mPrefs.edit();
		mLocationClient = new LocationClient(this, this, this);
	}
	
	@Override
	public void onStop() {
		if (mLocationClient.isConnected()) {
			stopPeriodicUpdates();
		}
		
		mLocationClient.disconnect();
		super.onStop();
	}
	
	@Override
	public void onPause() {
		mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
		mEditor.commit();
		super.onPause();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mLocationClient.connect();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
			mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
		} else {
			mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
			mEditor.commit();
		}
	}

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

/*	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

/*	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}*/
	
	/**
	 * Handle results return to this activity by other activities started with startActivityForResult().
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		switch(requestCode) {
			case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:
				switch (resultCode) {
					case Activity.RESULT_OK:
						Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
						
						mConnectionState.setText(R.string.connected);
						mConnectionStatus.setText(R.string.resolved);
						break;

					default:
						Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
						
						mConnectionState.setText(R.string.disconnected);
						mConnectionStatus.setText(R.string.no_resolution);
						break;
				}
				break;
			
			default:
				Log.d(LocationUtils.APPTAG, getString(R.string.unknown_activity_request_code, requestCode));
				break;
		}
	}
	
	private boolean servicesConnected () {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		
		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));
			return true;
		} else {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);			
			}
			return false;
		}
	}
	
	/**
	 * 
	 * @param v
	 */
	public void getLocation(View v) {
		if (servicesConnected()) {
			Location currentLocation = mLocationClient.getLastLocation();
			mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
		}
	}
	
	/**
	 * geocoding service
	 * @param v
	 */
	public void getAddress(View v) {
		return;
	}
	
	public void startUpdates(View v) {
		mUpdatesRequested = true;
		if (servicesConnected()) {
			startPeriodicUpdates();
		}
	}
	
	public void stopUpdates(View v) {
		mUpdatesRequested = false;
		if (servicesConnected()) {
			stopPeriodicUpdates();
		}
	}
	
	/**
	 * connection callback
	 * @author baki
	 *
	 */
	@Override
	public void onConnected(Bundle bundle) {
		mConnectionStatus.setText(R.string.connected);
		if (mUpdatesRequested) {
			startPeriodicUpdates();
		}
	}
	
	/**
	 * 
	 * @author baki
	 *
	 */
	@Override
	public void onDisconnected() {
		mConnectionStatus.setText(R.string.disconnected);
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this, 
						LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
			
		} else {
			showErrorDialog(connectionResult.getErrorCode());
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {
		mConnectionStatus.setText(R.string.location_updated);
		mLatLng.setText(LocationUtils.getLatLng(this, location));
	}
	
	private void startPeriodicUpdates() {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		mConnectionState.setText(R.string.location_requested);
	}
	
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
		mConnectionState.setText(R.string.location_updates_stopped);
	}
	
	private void showErrorDialog(int errorCode) {
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, 
				LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
		if (errorDialog != null) {
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			errorFragment.setDialog(errorDialog);
			errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
		}
	}
	
	
	
	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog mDialog;
		
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}
		
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

}
