package com.studypartner.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.studypartner.R;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends BaseActivity {
	private static final String TAG = "MainActivity";
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "onRequestPermissionsResult: permission granted");
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.activity_main, frameLayout);
		
		toolbarTitle.setText(getString(R.string.app_name));
		
		Log.d(TAG, "onCreate: checking internet connection");
		checkConnection(this);
		
		checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
		
		Log.d(TAG, "onCreate: ends");
	}
	
	public void checkPermission(String permission, int requestCode) {
		Log.d(TAG, "checkPermission: requesting permission " + permission);
		if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
			// Requesting the permission
			ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
		}
	}

}
