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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	private static final String TAG = "BaseActivity";
	
	protected final String SESSIONS = "SESSIONS";
	
	protected final String REMEMBER_ME_ENABLED = "rememberMeEnabled";
	protected final String REMEMBER_ME_EMAIL = "rememberMeEmail";
	protected final String REMEMBER_ME_PASSWORD = "rememberMePassword";
	
	private static final float END_SCALE = 0.7f;
	
	protected FrameLayout frameLayout;
	protected NavigationView navigationView;
	protected DrawerLayout drawerLayout;
	protected CoordinatorLayout coordinatorLayout;
	protected TextView toolbarTitle;
	protected AppBarLayout topAppBarLayout;
	protected MaterialToolbar topAppBar;
	protected BottomAppBar bottomAppBar;
	protected BottomNavigationView bottomNavigationView;
	protected FloatingActionButton fabMenu;
	
	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
			drawerLayout.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_activity);
		
		frameLayout= findViewById(R.id.baseScreenFrameLayout);
		drawerLayout = findViewById(R.id.baseScreenDrawerLayout);
		coordinatorLayout = findViewById(R.id.baseScreenCoordinatorLayout);
		topAppBarLayout=findViewById(R.id.topAppBarLayout);
		topAppBar=findViewById(R.id.topAppBar);
		navigationView = findViewById(R.id.baseScreenNavigationView);
		bottomAppBar=(findViewById(R.id.bottomAppBar));
		bottomNavigationView=findViewById(R.id.bottomNavigationView);
		toolbarTitle = findViewById(R.id.baseScreenToolbarTextView);
		fabMenu = findViewById(R.id.fabMenu);
		
//		drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
//
//			@Override
//			public void onDrawerSlide(View drawerView, float slideOffset) {
//				final float diffScaledOffset = slideOffset * (1 - END_SCALE);
//				final float offsetScale = 1 - diffScaledOffset;
//				coordinatorLayout.setScaleX(offsetScale);
//				coordinatorLayout.setScaleY(offsetScale);
//
//				final float xOffset = drawerView.getWidth() * slideOffset;
//				final float xOffsetDiff = coordinatorLayout.getWidth() * diffScaledOffset / 2;
//				final float xTranslation = xOffset - xOffsetDiff;
//				coordinatorLayout.setTranslationX(xTranslation);
//			}
//		});
		
		topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
					drawerLayout.closeDrawer(GravityCompat.START);
				} else {
					drawerLayout.openDrawer(GravityCompat.START);
				}
			}
		});
		
		navigationView.bringToFront();
		navigationView.setNavigationItemSelectedListener(this);
		
		ImageView profileImage = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileImage);
		TextView profileFullName = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileName);
		TextView profileVerifiedText = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerEmailVerified);
		
		if (null != FirebaseAuth.getInstance().getCurrentUser().getDisplayName()) {
			profileFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
		}
		
		if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
			Log.d(TAG, "onCreate: Downloading profile image");
			Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
					.error(Objects.requireNonNull(getDrawable(R.drawable.image_error_icon)))
					.placeholder(Objects.requireNonNull(getDrawable(R.drawable.image_loading_icon)))
					.into(profileImage);
		} else {
			Log.d(TAG, "onCreate: Image url does not exist for user");
			profileImage.setImageDrawable(getDrawable(R.drawable.image_error_icon));
		}
		
		if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
			profileVerifiedText.setText(R.string.navigation_header_verified);
			profileVerifiedText.setTextColor(getColor(R.color.navigationHeaderVerifiedText));
		} else {
			profileVerifiedText.setText(R.string.navigation_header_not_verified);
			profileVerifiedText.setTextColor(getColor(R.color.navigationHeaderNotVerifiedText));
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
	
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.navigationMenuHome:
				startActivity(new Intent(BaseActivity.this, MainActivity.class));
				finishAffinity();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				return true;
//			case R.id.navigationMenuNotes:
//				return true;
//			case R.id.navigationMenuAttendance:
//				return true;
			case R.id.navigationMenuReminder:
				return true;
			case R.id.navigationMenuProfile:
				startActivity(new Intent(BaseActivity.this, ProfileActivity.class));
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				return true;
			case R.id.navigationMenuLogout:
				FirebaseAuth.getInstance().signOut();
				startActivity(new Intent(BaseActivity.this, LoginActivity.class));
				finishAffinity();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				return true;
			case R.id.navigationMenuSettings:
				return true;
			case R.id.navigationMenuDarkMode:
				return true;
			default:
				return false;
		}
	}
}