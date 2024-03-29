package com.hackforchange.android.detox.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class DetoxContract {

	interface SitesColumns {
		String REGISTRY_ID = "REGISTRY_ID";
		String LATITUDE = "LATITUDE";
		String LONGITUDE = "LONGITUDE";
		String PRIMARY_NAME = "PRIMARY_NAME";
		String ADDRESS = "LOCATION_ADDRESS";
		String CITY = "CITY_NAME";
		String STATE_NAME = "STATE_NAME";
		String STATE_CODE = "STATE_CODE";
		String POSTAL_CODE = "POSTAL_CODE";
		String INTEREST_TYPES = "INTEREST_TYPES";
		String DETAIL_URL = "FRS_FACILITY_DETAIL_REPORT_URL";
	}

	interface PetitionColumns {
		// nothing to see here yet
	}

	public static final String CONTENT_AUTHORITY = "com.hackforchange.android.detox";

	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	public static class Sites implements BaseColumns, SitesColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("sites")
				.build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.detox.site";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.detox.site";
		
		public static Uri buildSiteUri(String registryId) {
			return CONTENT_URI.buildUpon().appendPath(registryId).build();
		}
		
		public static String getSiteId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
}
