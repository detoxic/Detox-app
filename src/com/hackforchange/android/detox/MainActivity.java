package com.hackforchange.android.detox;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.hackforchange.detox_app.R;

public class MainActivity extends SherlockFragmentActivity {
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "[onCreateOptionsMenu]");
		return super.onCreateOptionsMenu(menu);
	}

}
