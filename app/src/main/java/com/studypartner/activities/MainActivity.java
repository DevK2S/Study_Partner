package com.studypartner.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {
	private static final String TAG = "MainActivity";
	
	protected final String SESSIONS = "SESSIONS";
	
	protected final String REMEMBER_ME_ENABLED = "rememberMeEnabled";
	protected final String REMEMBER_ME_EMAIL = "rememberMeEmail";
	protected final String REMEMBER_ME_PASSWORD = "rememberMePassword";
	
	private AppBarConfiguration mAppBarConfiguration;
	private NavController mNavController;
	
	private BottomNavigationView mBottomNavigationView;
	private BottomAppBar mBottomAppBar;
	private NavigationView mNavigationView;
	private DrawerLayout mDrawerLayout;
	private FloatingActionButton fab;
	
	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		checkConnection(this);
		
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
				Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
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
				Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
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
	
	protected void checkConnection (final Activity activity) {
		Log.d(TAG, "isConnected: internet check");
		
		ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
			Log.d(TAG, "onCreate: Internet not connected");
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
					.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "onClick: opening settings for internet");
							startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setNeutralButton("Reload", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							checkConnection(activity);
						}
					});
			alertDialog.show();
		} else {
			Log.d(TAG, "isConnected: internet connected");
		}
	}
}