package com.hackforchange.android.detox.provider;

import com.hackforchange.android.detox.provider.DetoxContract.SitesColumns;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DetoxDatabase extends SQLiteOpenHelper {
	private static final String TAG = "DetoxDatabase";
	private static final String NAME = "detox.db";
	private static final int VERSION = 1;
	
	interface Tables {
		String SITES = "sites";
	}
	
	public DetoxDatabase(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "[onCreate]");
		db.execSQL("CREATE TABLE " + Tables.SITES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ SitesColumns.REGISTRY_ID + " TEXT NOT NULL,"
				+ SitesColumns.LATITUDE + " REAL,"
				+ SitesColumns.LONGITUDE + " REAL,"
				+ SitesColumns.PRIMARY_NAME + " TEXT,"
				+ SitesColumns.ADDRESS + " TEXT,"
				+ SitesColumns.CITY + " TEXT,"
				+ SitesColumns.STATE_NAME + " TEXT,"
				+ SitesColumns.STATE_CODE + " TEXT,"
				+ SitesColumns.POSTAL_CODE + " TEXT,"
				+ SitesColumns.INTEREST_TYPES + " TEXT,"
				+ SitesColumns.DETAIL_URL + " TEXT,"
				+ "UNIQUE (" + SitesColumns.REGISTRY_ID + ") ON CONFLICT REPLACE)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// dumb upgrade
		db.execSQL("DROP TABLE IF EXISTS " + Tables.SITES);	
		onCreate(db);
	}

}
