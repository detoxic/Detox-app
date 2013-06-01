package com.hackforchange.android.detox;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.plus.PlusClient;

public class MainActivity extends SherlockFragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener {
	private static final String TAG = "MainActivity";

	private LocationClient mLocationClient;
	private PlusClient mPlusClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		
		mPlusClient = new PlusClient.Builder(this, this, this).build();
		mPlusClient.connect();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "[onCreateOptionsMenu]");
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d(TAG, "[onConnectionFailed]");
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "[onConnected]");
		if (connectionHint != null) {
			Log.d(TAG, connectionHint.toString());
		} else {
			Log.d(TAG, "connectionHint = null");
		}
		
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "[onDisconnected]");
		
	}

}
