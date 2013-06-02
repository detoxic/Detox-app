package com.hackforchange.android.detox;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hackforchange.android.detox.provider.DetoxContract.Sites;

public class SiteDetailActivity extends SherlockFragmentActivity implements LoaderCallbacks<Cursor> {
	private static final String TAG = "SiteDetailActivity";

	private String mRegistryId;
	private Uri mUri;

	private TextView siteName;
	private TextView siteAddress1;
	private TextView siteAddress2;
	private MapView mapView;
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle saved) {
		super.onCreate(saved);
		setContentView(R.layout.activity_site_detail);

		final Intent intent = getIntent();
		mUri = intent.getData();
		if (mUri == null) {
			// see if it was packaged as an extra
			mRegistryId = intent.getStringExtra(mRegistryId);
			if (mRegistryId != null) {
				mUri = Sites.buildSiteUri(mRegistryId);
			} else {
				// frown
				throw new RuntimeException("No RegistryId!");
			}
		}

		siteName = (TextView) findViewById(R.id.site_name);
		siteAddress1 = (TextView) findViewById(R.id.site_address1);
		siteAddress2 = (TextView) findViewById(R.id.site_address2);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(saved);

		map = mapView.getMap();
		map.setMyLocationEnabled(false);
		UiSettings settings = map.getUiSettings();
		settings.setZoomControlsEnabled(false);
		settings.setAllGesturesEnabled(false);
		settings.setCompassEnabled(false);

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onDestroy() {
		mapView.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, mUri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		updateSiteDetails(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	private void updateSiteDetails(Cursor cursor) {
		if (cursor.moveToFirst()) {
			String name = cursor.getString(cursor.getColumnIndex(Sites.PRIMARY_NAME));
			String address = cursor.getString(cursor.getColumnIndex(Sites.ADDRESS));
			String city = cursor.getString(cursor.getColumnIndex(Sites.CITY));
			String stateCode = cursor.getString(cursor.getColumnIndex(Sites.STATE_CODE));
			String address2 = String.format("%s, %s", city, stateCode);

			siteName.setText(name);
			siteAddress1.setText(address);
			siteAddress2.setText(address2);

			double lat = cursor.getDouble(cursor.getColumnIndex(Sites.LATITUDE));
			double lng = cursor.getDouble(cursor.getColumnIndex(Sites.LONGITUDE));
			LatLng target = new LatLng(lat, lng);
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(target, 17);
			map.moveCamera(update);

			MarkerOptions options = new MarkerOptions().position(target).anchor(0.5f, 0.5f)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
			map.addMarker(options);
		}
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
}
