package com.ch_linghu.fanfoudroid.service;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Location Service
 * 
 * AndroidManifest.xml <code>
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * </code>
 * 
 * TODO: 使用DDMS对模拟器GPS位置进行更新时, 会造成死机现象
 * 
 */
public class LocationService implements IService {
	private static final String TAG = "LocationService";

	private LocationManager mLocationManager;
	private LocationListener mLocationListener = new MyLocationListener();
	private String mLocationProvider;
	private boolean running = false;

	public LocationService(Context context) {
		initLocationManager(context);
	}

	private void initLocationManager(Context context) {
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		mLocationProvider = mLocationManager.getBestProvider(new Criteria(),
				false);
	}

	public void startService() {
		if (!running) {
			Log.v(TAG, "START LOCATION SERVICE, PROVIDER:" + mLocationProvider);
			running = true;
			mLocationManager.requestLocationUpdates(mLocationProvider, 0, 0,
					mLocationListener);
		}
	}

	public void stopService() {
		if (running) {
			Log.v(TAG, "STOP LOCATION SERVICE");
			running = false;
			mLocationManager.removeUpdates(mLocationListener);
		}
	}

	/**
	 * @return the last known location for the provider, or null
	 */
	public Location getLastKnownLocation() {
		return mLocationManager.getLastKnownLocation(mLocationProvider);
	}

	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Log.v(TAG, "LOCATION CHANGED TO: " + location.toString());
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.v(TAG, "PROVIDER DISABLED " + provider);
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.v(TAG, "PROVIDER ENABLED " + provider);
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			Log.v(TAG, "STATUS CHANGED: " + provider + " " + status);
		}

	}

	// Only for debug
	public void logAllProviders() {
		// List all providers:
		List<String> providers = mLocationManager.getAllProviders();
		Log.v(TAG, "LIST ALL PROVIDERS:");
		for (String provider : providers) {
			boolean isEnabled = mLocationManager.isProviderEnabled(provider);
			Log.v(TAG, "Provider " + provider + ": " + isEnabled);
		}
	}

	// only for debug
	public static LocationService test(Context context) {
		LocationService ls = new LocationService(context);
		ls.startService();
		ls.logAllProviders();
		Location local = ls.getLastKnownLocation();
		if (local != null) {
			Log.v("LDS", ls.getLastKnownLocation().toString());
		}
		return ls;
	}

}
