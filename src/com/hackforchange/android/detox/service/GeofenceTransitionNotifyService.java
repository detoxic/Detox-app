package com.hackforchange.android.detox.service;

import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.hackforchange.android.detox.R;

public class GeofenceTransitionNotifyService extends IntentService {
	private static final String TAG = "GeofenceTransitionNotifyService";
	private static final int mNotificationId = 42;

	public GeofenceTransitionNotifyService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (LocationClient.hasError(intent)) {
			// Log the error
			int errorCode = LocationClient.getErrorCode(intent);
			Log.e(TAG, "Location Services error: " + errorCode);
		} else {
			// Get the type of transition (entry or exit)
			int transitionType = LocationClient.getGeofenceTransition(intent);
			// Test that a valid transition was reported
			if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
				List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
				String[] triggerIds = new String[triggerList.size()];
				for (int i = 0; i < triggerIds.length; i++) {
					// Store the Id of each geofence
					triggerIds[i] = triggerList.get(i).getRequestId();
				}

				Intent contentIntent;
				PendingIntent pendingIntent;

				NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
				builder.setContentTitle(getString(R.string.notification_geofence_title))
						.setContentText(getString(R.string.notification_geofence_message))
						.setSmallIcon(R.drawable.ic_notification_geofence);
//						.setContentIntent(pendingIntent);

				NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				manager.notify(mNotificationId, builder.build());
			}
		}
	}

}
