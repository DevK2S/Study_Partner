package com.studypartner.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.studypartner.R;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends BaseActivity {
	private static final String TAG = "MainActivity";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.activity_main, frameLayout);
		checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
		//navigationView.setCheckedItem(R.id.navigationMenuHome);
	}
	
	public void checkPermission(String permission, int requestCode) {
		if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
			// Requesting the permission
			ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
		}
	}

}
