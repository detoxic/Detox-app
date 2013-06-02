package com.hackforchange.android.detox.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.hackforchange.android.detox.provider.DetoxContract.Sites;
import com.hackforchange.android.detox.provider.DetoxDatabase.Tables;
import com.hackforchange.android.detox.util.SelectionBuilder;

public class DetoxProvider extends ContentProvider {
	private static final String TAG = "DetoxProvider";

	private DetoxDatabase dbHelper;
	private static UriMatcher uriMatcher;

	private static final int SITES = 100;
	private static final int SITE_ID = 101;

	private static final int PETITIONS = 200;
	private static final int PETITION_ID = 201;

	static {
		final String authority = DetoxContract.CONTENT_AUTHORITY;
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(authority, "sites", SITES);
		uriMatcher.addURI(authority, "sites/*", SITE_ID);
	}

	@Override
	public boolean onCreate() {
		Log.d(TAG, "[onCreate]");
		return false;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case SITES:
			return Sites.CONTENT_TYPE;
		case SITE_ID:
			return Sites.CONTENT_ITEM_TYPE;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		final int match = uriMatcher.match(uri);
		SelectionBuilder builder = buildExpandedSelection(uri, match);
		return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "[insert]");
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "[delete]");
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		Log.d(TAG, "[update]");
		return 0;
	}

	private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
		final SelectionBuilder builder = new SelectionBuilder();
		
		switch (match) {
		case SITES:
			return builder.table(Tables.SITES);
		case SITE_ID:
			final String id = Sites.getSiteId(uri);
			return builder.table(Tables.SITES).where(Sites.REGISTRY_ID + "=?", id);
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);

		}
	}
}
