package com.hackforchange.android.detox.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class SitesUpdateService extends IntentService {
	private static final String TAG = "SitesUpdateService";

	public SitesUpdateService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "[onHandleIntent]");
		
		// construct query
		
		// web call
		
		// parse response
	}

}
