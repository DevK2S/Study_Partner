package com.studypartner;
import android.widget.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.*;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
				101);
		final DrawerLayout drawerLayout=findViewById(R.id.drawerLayout);
		findViewById(R.id.imageMenu).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);

            }
        });
		NavigationView navigationView=findViewById(R.id.navigationView);


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