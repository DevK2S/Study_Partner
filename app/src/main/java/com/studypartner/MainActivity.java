package com.studypartner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
				101);
	}

	public void checkPermission(String permission, int requestCode) {
		if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
				== PackageManager.PERMISSION_DENIED) {

			// Requesting the permission
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{permission},
					requestCode);
		}
		/*else {
			Toast.makeText(MainActivity.this,
					"Permission already granted",
					Toast.LENGTH_SHORT)
					.show();
		}*/

	}
}