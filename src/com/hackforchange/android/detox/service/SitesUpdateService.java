package com.hackforchange.android.detox.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.hackforchange.android.detox.provider.DetoxContract.Sites;
import com.hackforchange.android.detox.util.Constants;
import com.hackforchange.android.detox.util.JSONRequestWithParams;

public class SitesUpdateService extends IntentService implements Listener<JSONObject>,
		ErrorListener {
	private static final String TAG = "SitesUpdateService";

	private static final String FUSION_TABLE_NAME = "1LN4iEN6XqiNGC39hS2FInp8F5O0bKFyLTwe0vjs";
	private static final double LAT_LNG_ADJUST = 0.005;

	private static final String[] COLUMNS = { Sites.REGISTRY_ID, Sites.LATITUDE, Sites.LONGITUDE };
	private static final int INDEX_REGISTRY_ID = 0;
	private static final int INDEX_LATITUDE = 1;
	private static final int INDEX_LONGITUDE = 2;

	private static final String COLUMN_PROJECTION = buildColumnProjection();
	private static final String BASE_SQL = "SELECT " + COLUMN_PROJECTION + " from "
			+ FUSION_TABLE_NAME + " where " + Sites.LATITUDE + ">=%f and " + Sites.LATITUDE
			+ "<=%f and " + Sites.LONGITUDE + ">=%f and " + Sites.LONGITUDE + "<=%f";

	public static final String EXTRA_LATLNG = "extra.LATLNG";

	private static String buildColumnProjection() {
		StringBuilder builder = new StringBuilder();
		final int count = COLUMNS.length;
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				builder.append(',');
			}
			builder.append(COLUMNS[i]);
		}
		return builder.toString();
	}

	public SitesUpdateService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "[onHandleIntent]");

		LatLng latLng = intent.getParcelableExtra(EXTRA_LATLNG);
		if (latLng == null) return;
		
		Log.d(TAG, String.format("latitude=%f, longitude=%f", latLng.latitude, latLng.longitude));
		LatLng minBounds = computeBounds(latLng, -LAT_LNG_ADJUST);
		LatLng maxBounds = computeBounds(latLng, LAT_LNG_ADJUST);

		// construct query
		String sql = String.format(BASE_SQL, minBounds.latitude, maxBounds.latitude,
				minBounds.longitude, maxBounds.longitude);
		Log.d(TAG, sql);

		// web call
		JSONRequestWithParams request = new JSONRequestWithParams(Request.Method.GET,
				Constants.FUSION_TABLE_QUERY_URL, null, this, this);
		request.setParameter(Constants.PARAM_SQL, sql);
		request.setParameter(Constants.PARAM_KEY, Constants.FUSION_TABLE_API_KEY);
		Volley.newRequestQueue(this).add(request);
	}

	private LatLng computeBounds(LatLng target, double adjust) {
		return new LatLng(target.latitude + adjust, target.longitude + adjust);
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		Log.d(TAG, "[onErrorResponse]");

	}

	@Override
	public void onResponse(JSONObject response) {
		Log.d(TAG, "[onResponse]");
		try {
			JSONArray rows = response.getJSONArray("rows");

			final int numRows = rows.length();
			for (int i = 0; i < numRows; i++) {
				JSONArray row = rows.getJSONArray(i);
				String registryId = row.getString(INDEX_REGISTRY_ID);
				double lat = row.getDouble(INDEX_LATITUDE);
				double lng = row.getDouble(INDEX_LONGITUDE);

//				double d = row.getDouble(0);
				Log.d(TAG, String.format("%d: %s, %f, %f", i, registryId, lat, lng));
//				Log.d(TAG, String.format("%d: %f", i, d));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
