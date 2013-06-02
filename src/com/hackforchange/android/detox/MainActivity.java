package com.hackforchange.android.detox;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.PlusClient;
import com.hackforchange.android.detox.service.SitesUpdateService;

public class MainActivity extends SherlockFragmentActivity implements ConnectionCallbacks,
		OnConnectionFailedListener {
	private static final String TAG = "MainActivity";

	private GoogleMap mMap;
	private LocationClient mLocationClient;

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
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(target, 14);
		mMap.moveCamera(update);
		
		Intent intent = new Intent(this, SitesUpdateService.class);
		intent.putExtra(SitesUpdateService.EXTRA_LATLNG, target);
		startService(intent);
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "[onDisconnected]");
	}

}
