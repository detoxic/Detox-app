package com.hackforchange.android.detox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.PlusClient;
import com.hackforchange.android.detox.provider.DetoxContract.Sites;
import com.hackforchange.android.detox.service.GeofenceTransitionNotifyService;
import com.hackforchange.android.detox.service.SitesUpdateService;
import com.hackforchange.android.detox.util.Constants;

public class MainActivity extends SherlockFragmentActivity implements ConnectionCallbacks,
		OnConnectionFailedListener, LoaderCallbacks<Cursor>, OnInfoWindowClickListener,
		OnRemoveGeofencesResultListener, OnAddGeofencesResultListener {
	private static final String TAG = "MainActivity";
	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 1;

	private static final String[] PROJECTION = { Sites.REGISTRY_ID, Sites.LATITUDE,
			Sites.LONGITUDE, Sites.PRIMARY_NAME, Sites.ADDRESS };

	private static List<String> geofenceRequestIds;
	static {
		geofenceRequestIds = new ArrayList<String>();
		geofenceRequestIds.add(Constants.GEOFENCE_ID);
	}

	private LocationClient mLocationClient;
	private boolean mLocationClientConnected;

	private GoogleMap mMap;
	private Map<Marker, String> mMarkers;
	private LatLng mPendingGeofenceLatLng;
	private boolean mPendingGeofenceRequest;
	private boolean mFirstLocation;

	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLocationClient = new LocationClient(this, this, this);

		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map_fragment);
		mMap = fragment.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setOnInfoWindowClickListener(this);

		mMarkers = new HashMap<Marker, String>();
		mPendingGeofenceLatLng = new LatLng(0, 0);

		getSupportLoaderManager().initLoader(0, null, this);
		mFirstLocation = true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(mLocationListener);
		}
		mLocationClient.disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "[onLocationChanged]");
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			moveMapToLocation(latLng, 0);
			callUpdateSitesService(latLng);
		}
	};

	private void callUpdateSitesService(LatLng location) {
		Intent intent = new Intent(this, SitesUpdateService.class);
		intent.putExtra(SitesUpdateService.EXTRA_LATLNG, location);
		startService(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d(TAG, "[onConnectionFailed]");
		mConnectionResult = result;
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);
			if (dialog != null) dialog.show();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (connectionHint != null) {
			Log.d(TAG, connectionHint.toString());
		} else {
			Log.d(TAG, "connectionHint = null");
		}
		mLocationClientConnected = true;

		if (mFirstLocation) {
			mFirstLocation = false;
			Location location = mLocationClient.getLastLocation();
			LatLng target = new LatLng(location.getLatitude(), location.getLongitude());
			moveMapToLocation(target, 15);

			callUpdateSitesService(target);
			
			// TODO request location updates
		}

		if (mPendingGeofenceRequest) {
			removeGeofence();
		}
	}

	private void moveMapToLocation(LatLng latLng, float zoom) {
		CameraUpdate update;
		if (zoom > 0) {
			update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
		} else {
			update = CameraUpdateFactory.newLatLng(latLng);
		}
		mMap.moveCamera(update);
	}

	@Override
	public void onDisconnected() {
		mLocationClientConnected = false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, Sites.CONTENT_URI, PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(TAG, "[onLoadFinished]");
		updateMapMarkers(data);
		requestNewGeofence(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {}

	private void updateMapMarkers(Cursor cursor) {
		Log.d(TAG, "[updateMapMarkers]");
		// clear markers
		for (Marker marker : mMarkers.keySet()) {
			marker.remove();
		}
		mMarkers.clear();

		// make new markers
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			double lat = cursor.getDouble(cursor.getColumnIndex(Sites.LATITUDE));
			double lng = cursor.getDouble(cursor.getColumnIndex(Sites.LONGITUDE));
			String name = cursor.getString(cursor.getColumnIndex(Sites.PRIMARY_NAME));
			String address = cursor.getString(cursor.getColumnIndex(Sites.ADDRESS));
			String id = cursor.getString(cursor.getColumnIndex(Sites.REGISTRY_ID));
			MarkerOptions options = new MarkerOptions().position(new LatLng(lat, lng)).title(name)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
					.snippet(address).anchor(0.5f, 0.5f);
			Marker marker = mMap.addMarker(options);
			mMarkers.put(marker, id);
		}
	}

	private void requestNewGeofence(Cursor cursor) {
		if (cursor.moveToFirst()) {
			double lat = cursor.getDouble(cursor.getColumnIndex(Sites.LATITUDE));
			double lng = cursor.getDouble(cursor.getColumnIndex(Sites.LONGITUDE));
			mPendingGeofenceLatLng = new LatLng(lat, lng);
			mPendingGeofenceRequest = true;
		}

		if (mLocationClientConnected) {
			removeGeofence();
		} else {
			mLocationClient.connect();
		}
	}

	private void removeGeofence() {
		mLocationClient.removeGeofences(geofenceRequestIds, this);
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		Log.d(TAG, "[onInfoWindowClick]");
		String id = mMarkers.get(marker);
		if (id != null) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Sites.buildSiteUri(id));
			startActivity(intent);
		}
	}

	@Override
	public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {

	}

	@Override
	public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
		if (mPendingGeofenceRequest) {
			if (mLocationClientConnected) {
				Geofence geofence = new Geofence.Builder()
						.setRequestId(Constants.GEOFENCE_ID)
						.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
						.setExpirationDuration(AlarmManager.INTERVAL_DAY)
						.setCircularRegion(mPendingGeofenceLatLng.latitude,
								mPendingGeofenceLatLng.longitude, 50).build();

				List<Geofence> geofences = new ArrayList<Geofence>();
				geofences.add(geofence);
				Intent intent = new Intent(this, GeofenceTransitionNotifyService.class);
				PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
				mLocationClient.addGeofences(geofences, pendingIntent, this);

				mPendingGeofenceRequest = false;
			} else {
				mLocationClient.connect();
			}
		}
	}

	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {}
}
