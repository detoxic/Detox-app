package com.hackforchange.android.detox.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
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

	private static final int INDEX_REGISTRY_ID = 0;
	private static final int INDEX_LATITUDE = 1;
	private static final int INDEX_LONGITUDE = 2;
	private static final int INDEX_PRIMARY_NAME = 3;
	private static final int INDEX_ADDRESS = 4;
	private static final int INDEX_CITY = 5;
	private static final int INDEX_STATE_NAME = 6;
	private static final int INDEX_STATE_CODE = 7;
	private static final int INDEX_INTEREST_TYPES = 8;
	private static final int INDEX_DETAIL_URL = 9;
	private static final String[] COLUMNS = { Sites.REGISTRY_ID, Sites.LATITUDE, Sites.LONGITUDE,
			Sites.PRIMARY_NAME, Sites.ADDRESS, Sites.CITY, Sites.STATE_NAME, Sites.STATE_CODE,
			Sites.INTEREST_TYPES, Sites.DETAIL_URL };

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

		// make web call
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
			if (numRows < 1) return;
			
			ContentValues[] cv = new ContentValues[numRows];
			for (int i = 0; i < numRows; i++) {
				JSONArray row = rows.getJSONArray(i);
				cv[i] = convertRowToValues(row);
			}
			
			ContentResolver resolver = getContentResolver();
			resolver.delete(Sites.CONTENT_URI, null, null);
			resolver.bulkInsert(Sites.CONTENT_URI, cv);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private ContentValues convertRowToValues(JSONArray row) throws JSONException {
		ContentValues values = new ContentValues();
		values.put(Sites.REGISTRY_ID, row.getString(INDEX_REGISTRY_ID));
		values.put(Sites.LATITUDE, row.getDouble(INDEX_LATITUDE));
		values.put(Sites.LONGITUDE, row.getDouble(INDEX_LONGITUDE));
		values.put(Sites.PRIMARY_NAME, row.getString(INDEX_PRIMARY_NAME));
		values.put(Sites.ADDRESS, row.getString(INDEX_ADDRESS));
		values.put(Sites.CITY, row.getString(INDEX_CITY));
		values.put(Sites.STATE_NAME, row.getString(INDEX_STATE_NAME));
		values.put(Sites.STATE_CODE, row.getString(INDEX_STATE_CODE));
		values.put(Sites.INTEREST_TYPES, row.getString(INDEX_INTEREST_TYPES));
		values.put(Sites.DETAIL_URL, row.getString(INDEX_DETAIL_URL));
		return values;
	}
}
