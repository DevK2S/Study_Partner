package com.studypartner.activities;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
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
	
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	
	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		ImageView profileImage = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileImage);
		TextView profileFullName = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileName);
		TextView profileEmail = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerEmail);
		
		if (null != FirebaseAuth.getInstance().getCurrentUser().getDisplayName()) {
			profileFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
		}
		
		if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
			Log.d(TAG, "onCreate: Downloading profile image");
			Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
					.error(Objects.requireNonNull(getDrawable(R.drawable.image_error_icon)))
					.placeholder(Objects.requireNonNull(getDrawable(R.drawable.profile_photo_icon)))
					.into(profileImage);
		} else {
			Log.d(TAG, "onCreate: Image url does not exist for user");
			profileImage.setImageDrawable(getDrawable(R.drawable.profile_photo_icon));
		}
		
		profileEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
		
		fragmentManager = getSupportFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	
	@Override
	public boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}
	
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		int itemId = item.getItemId();
		
		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
		}
		
		switch (itemId) {
			case R.id.nav_home:
				Log.d(TAG, "onNavigationItemSelected: home selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_home) {
					Log.d(TAG, "onNavigationItemSelected: opening home fragment");
					fab.setVisibility(View.VISIBLE);
					mBottomAppBar.setVisibility(View.VISIBLE);
					mNavController.navigate(R.id.nav_home);
				}
				return true;
				
			case R.id.nav_attendance:
				Toast.makeText(this, "Attendance", Toast.LENGTH_SHORT).show();
				return true;
				
			case R.id.nav_starred:
				Toast.makeText(this, "Starred", Toast.LENGTH_SHORT).show();
				return true;
				
			case R.id.nav_notes:
				Toast.makeText(this, "Notes", Toast.LENGTH_SHORT).show();
				return true;
				
			case R.id.nav_reminder:
				Toast.makeText(this, "Reminder", Toast.LENGTH_SHORT).show();
				return true;
				
			case R.id.nav_settings:
				Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
				return true;
				
			case R.id.nav_profile:
				Log.d(TAG, "onNavigationItemSelected: profile selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_profile) {
					Log.d(TAG, "onNavigationItemSelected: opening profile fragment");
					mBottomAppBar.setVisibility(View.GONE);
					fab.setVisibility(View.GONE);
					mNavController.navigate(R.id.nav_profile);
				}
				return true;
				
			case R.id.nav_logout:
				FirebaseAuth.getInstance().signOut();
				startActivity(new Intent(this, LoginActivity.class));
				finishAffinity();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				return true;
				
			default:
				Toast.makeText(this, "Default", Toast.LENGTH_SHORT).show();
				return false;
			
		}
	}
}