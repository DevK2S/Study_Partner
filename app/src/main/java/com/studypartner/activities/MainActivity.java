package com.studypartner.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.studypartner.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
		
		final DrawerLayout drawerLayout = findViewById(R.id.mainScreenDrawerLayout);
		
		findViewById(R.id.mainScreenMenuImage).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				drawerLayout.openDrawer(GravityCompat.START);
			}
		});
		
		NavigationView navigationView = findViewById(R.id.mainScreenNavigationView);
		
	}
	
	public void checkPermission(String permission, int requestCode) {
		if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
			
			// Requesting the permission
			ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
		}
	}
}