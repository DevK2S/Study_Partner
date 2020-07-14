package com.studypartner.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.studypartner.R;
import com.studypartner.utils.Connection;

import java.io.File;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {
	private static final String TAG = "MainActivity";
	
	private AppBarConfiguration mAppBarConfiguration;
	public NavController mNavController;
	
	private BottomNavigationView mBottomNavigationView;
	public BottomAppBar mBottomAppBar;
	private NavigationView mNavigationView;
	private DrawerLayout mDrawerLayout;
	public FloatingActionButton fab;
	
	
	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed: back pressed");
		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
		} else if (mNavController.getCurrentDestination().getId() == R.id.nav_home){
			Log.d(TAG, "onBackPressed: closing app");
			finishAffinity();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		} else {
			Log.d(TAG, "onBackPressed: navigating to home");
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume: starts");
		super.onResume();
		
		ImageView profileImage = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileImage);
		TextView profileFullName = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileName);
		TextView profileEmail = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerEmail);
		
		if (null != FirebaseAuth.getInstance().getCurrentUser().getDisplayName()) {
			profileFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
		}
		
		if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
			Log.d(TAG, "onResume: Downloading profile image");
			Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
					.error(Objects.requireNonNull(getDrawable(R.drawable.image_error_icon)))
					.placeholder(Objects.requireNonNull(getDrawable(R.drawable.profile_photo_icon)))
					.into(profileImage);
		} else {
			Log.d(TAG, "onResume: Image url does not exist for user");
			profileImage.setImageDrawable(getDrawable(R.drawable.profile_photo_icon));
		}
		
		profileEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
		Log.d(TAG, "onResume: ends");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//checking Permissions
		WriteReadPermission();
		/// Access your app's Private documents directory
		File file = new File(getExternalFilesDir(null),"Folders");
		// Make the directory if it does not yet exist
		if(!file.mkdirs())
			file.mkdirs();

		Log.d(TAG, "onCreate: checking connection");
		Connection.checkConnection(this);
		
		//setting hooks
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		mDrawerLayout = findViewById(R.id.drawer_layout);
		mBottomNavigationView = findViewById(R.id.bottom_nav_view);
		mBottomAppBar = findViewById(R.id.bottom_app_bar);
		mNavigationView = findViewById(R.id.nav_view);
		fab = findViewById(R.id.fab);
		
		//set up navigation
		mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
		
		mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_attendance, R.id.nav_starred, R.id.nav_notes, R.id.nav_reminder, R.id.nav_profile, R.id.nav_settings, R.id.nav_logout)
				.setDrawerLayout(mDrawerLayout)
				.build();
		
		NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(mNavigationView, mNavController);
		NavigationUI.setupWithNavController(mBottomNavigationView, mNavController);
		
		mNavigationView.setCheckedItem(R.id.nav_home);
		mBottomNavigationView.setSelectedItemId(R.id.nav_home);
		
		mNavigationView.setNavigationItemSelectedListener(this);
		mBottomNavigationView.setOnNavigationItemSelectedListener(this);
		
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
				mDrawerLayout.closeDrawer(GravityCompat.START);
			} else {
				mDrawerLayout.openDrawer(GravityCompat.START);
			}
			}
		});
	}
	
	@Override
	public boolean onSupportNavigateUp() {
		Log.d(TAG, "onSupportNavigateUp: starts");
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}
	
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		Log.d(TAG, "onNavigationItemSelected: starts");
		int itemId = item.getItemId();
		
		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
		}
		
		Log.d(TAG, "onNavigationItemSelected: animations for opening fragment to right of current one");
		NavOptions.Builder leftToRightBuilder = new NavOptions.Builder();
		leftToRightBuilder.setEnterAnim(R.anim.slide_in_right);
		leftToRightBuilder.setExitAnim(R.anim.slide_out_left);
		leftToRightBuilder.setPopEnterAnim(R.anim.slide_in_left);
		leftToRightBuilder.setPopExitAnim(R.anim.slide_out_right);
		leftToRightBuilder.setLaunchSingleTop(true);
		
		Log.d(TAG, "onNavigationItemSelected: animations for opening fragment to left of current one");
		NavOptions.Builder rightToLeftBuilder = new NavOptions.Builder();
		rightToLeftBuilder.setEnterAnim(R.anim.slide_in_left);
		rightToLeftBuilder.setExitAnim(R.anim.slide_out_right);
		rightToLeftBuilder.setPopEnterAnim(R.anim.slide_in_right);
		rightToLeftBuilder.setPopExitAnim(R.anim.slide_out_left);
		rightToLeftBuilder.setLaunchSingleTop(true);
		
		switch (itemId) {
			case R.id.nav_home:
				Log.d(TAG, "onNavigationItemSelected: home selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_home) {
					Log.d(TAG, "onNavigationItemSelected: opening home fragment");
					fab.setVisibility(View.VISIBLE);
					mBottomAppBar.setVisibility(View.VISIBLE);
					mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
					mNavController.popBackStack(R.id.nav_home, false);
				}
				return true;
				
			case R.id.nav_attendance:
				Log.d(TAG, "onNavigationItemSelected: attendance selected");
				if (mNavController.getCurrentDestination().getId() == R.id.nav_home) {
					Log.d(TAG, "onNavigationItemSelected: opening attendance fragment");
					mNavController.navigate(R.id.nav_attendance, null, leftToRightBuilder.build());
				} else if (mNavController.getCurrentDestination().getId() != R.id.nav_attendance) {
					Log.d(TAG, "onNavigationItemSelected: opening attendance fragment");
					mNavController.navigate(R.id.nav_attendance, null, rightToLeftBuilder.build());
				}
				return true;
				
			case R.id.nav_starred:
				Log.d(TAG, "onNavigationItemSelected: starred selected");
				if (mNavController.getCurrentDestination().getId() == R.id.nav_notes) {
					Log.d(TAG, "onNavigationItemSelected: opening starred fragment");
					mNavController.navigate(R.id.nav_starred, null, rightToLeftBuilder.build());
				} else if (mNavController.getCurrentDestination().getId() != R.id.nav_starred) {
					Log.d(TAG, "onNavigationItemSelected: opening starred fragment");
					mNavController.navigate(R.id.nav_starred, null, leftToRightBuilder.build());
				}
				return true;
				
			case R.id.nav_notes:
				Log.d(TAG, "onNavigationItemSelected: notes selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_notes) {
					Log.d(TAG, "onNavigationItemSelected: opening notes fragment");
					mNavController.navigate(R.id.nav_notes, null, leftToRightBuilder.build());
				}
				return true;
				
			case R.id.nav_reminder:
				Log.d(TAG, "onNavigationItemSelected: reminder selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_reminder) {
					Log.d(TAG, "onNavigationItemSelected: opening reminder fragment");
					mBottomAppBar.setVisibility(View.GONE);
					mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
					mNavController.navigate(R.id.nav_reminder, null, leftToRightBuilder.build());
				}
				return true;
				
			case R.id.nav_settings:
				Log.d(TAG, "onNavigationItemSelected: settings selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_settings) {
					Log.d(TAG, "onNavigationItemSelected: opening settings fragment");
					mBottomAppBar.setVisibility(View.GONE);
					mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
					mNavController.navigate(R.id.nav_settings, null, leftToRightBuilder.build());
				}
				return true;
				
			case R.id.nav_profile:
				Log.d(TAG, "onNavigationItemSelected: profile selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_profile) {
					Log.d(TAG, "onNavigationItemSelected: opening profile fragment");
					mBottomAppBar.setVisibility(View.GONE);
					mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
					mNavController.navigate(R.id.nav_profile, null, leftToRightBuilder.build());
				}
				return true;
				
			case R.id.nav_logout:
				Log.d(TAG, "onNavigationItemSelected: logging out");
				FirebaseAuth.getInstance().signOut();
				mNavController.navigate(R.id.nav_logout);
				finishAffinity();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				return true;
				
			default:
				Toast.makeText(this, "Please don't select this", Toast.LENGTH_SHORT).show();
				return false;
		}
	}
	private  boolean isExternalStorageReadableWritable()
	{
		return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;
	}
	private void  WriteReadPermission()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		}
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
		}

	}
}