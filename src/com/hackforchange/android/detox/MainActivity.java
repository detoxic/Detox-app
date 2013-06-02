package com.hackforchange.android.detox;

import java.util.ArrayList;

import android.app.Dialog;
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
import com.google.android.gms.location.LocationClient;
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
import com.hackforchange.android.detox.service.SitesUpdateService;

public class MainActivity extends SherlockFragmentActivity implements ConnectionCallbacks,
		OnConnectionFailedListener, LoaderCallbacks<Cursor>, OnInfoWindowClickListener {
	private static final String TAG = "MainActivity";
	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 1;
	
	private static final String[] PROJECTION = { Sites.LATITUDE, Sites.LONGITUDE,
			Sites.PRIMARY_NAME, Sites.ADDRESS };

	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private ArrayList<Marker> mMarkers;

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

		mMarkers = new ArrayList<Marker>();

		getSupportLoaderManager().initLoader(0, null, this);
//		mPlusClient = new PlusClient.Builder(this, this, this).build();
//		mPlusClient.connect();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			if (dialog != null) dialog.show();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "[onConnected]");
		if (connectionHint != null) {
			Log.d(TAG, connectionHint.toString());
		} else {
			Log.d(TAG, "connectionHint = null");
		}

		Location location = mLocationClient.getLastLocation();
		LatLng target = new LatLng(location.getLatitude(), location.getLongitude());
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(target, 15);
		mMap.moveCamera(update);

		Intent intent = new Intent(this, SitesUpdateService.class);
		intent.putExtra(SitesUpdateService.EXTRA_LATLNG, target);
		startService(intent);
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "[onDisconnected]");
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, Sites.CONTENT_URI, PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(TAG, "[onLoadFinished]");
		updateMapMarkers(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {}

	private void updateMapMarkers(Cursor cursor) {
		Log.d(TAG, "[updateMapMarkers]");
		// clear markers
		for (Marker marker : mMarkers) {
			marker.remove();
		}
		mMarkers.clear();

		// make new markers
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Log.d(TAG, "Add marker " + cursor.getPosition());
			double lat = cursor.getDouble(cursor.getColumnIndex(Sites.LATITUDE));
			double lng = cursor.getDouble(cursor.getColumnIndex(Sites.LONGITUDE));
			String name = cursor.getString(cursor.getColumnIndex(Sites.PRIMARY_NAME));
			String address = cursor.getString(cursor.getColumnIndex(Sites.ADDRESS));
			MarkerOptions options = new MarkerOptions().position(new LatLng(lat, lng)).title(name)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
					.snippet(address).anchor(0.5f, 0.5f);
			Marker marker = mMap.addMarker(options);
			mMarkers.add(marker);
		}
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		Log.d(TAG, "[onInfoWindowClick]");
		
	}
}
