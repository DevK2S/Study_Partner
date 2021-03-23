package com.studypartner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.muddzdev.styleabletoast.StyleableToast;
import com.squareup.picasso.Picasso;
import com.studypartner.R;
import com.studypartner.models.ReminderItem;
import com.studypartner.utils.Connection;
import com.studypartner.utils.NotificationHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener, NavController.OnDestinationChangedListener {
	private static final String TAG = "MainActivity";
	public NavController mNavController;
	public BottomAppBar mBottomAppBar;
	public DrawerLayout mDrawerLayout;
	public FloatingActionButton fab;
	public Toolbar mToolbar;
	public NavOptions.Builder leftToRightBuilder, rightToLeftBuilder;
	private AppBarConfiguration mAppBarConfiguration;
	private BottomNavigationView mBottomNavigationView;
	private NavigationView mNavigationView;
	
	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed: back pressed");
		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			Log.d(TAG, "onBackPressed: closing drawer");
			mDrawerLayout.closeDrawer(GravityCompat.START);
		} else if (mNavController.getCurrentDestination().getId() == R.id.nav_home) {
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
		super.onResume();
		
		ImageView profileImage = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileImage);
		ImageView verifiedImage = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerVerifiedImage);
		TextView profileFullName = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileName);
		TextView profileEmail = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerEmail);
		
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			
			if (FirebaseAuth.getInstance().getCurrentUser().getDisplayName() != null) {
				profileFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
			}
			
			if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
				Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
						.error(R.drawable.image_error_icon)
						.placeholder(R.drawable.user_icon)
						.into(profileImage);
			} else {
				profileImage.setImageResource(R.drawable.user_icon);
			}
			
			profileEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
			
			if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
				verifiedImage.setImageResource(R.drawable.verified_icon);
			} else {
				verifiedImage.setImageResource(R.drawable.not_verified_icon);
			}
			
			profileEmail.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
						FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
							@Override
							public void onSuccess(Void aVoid) {
								StyleableToast.makeText(MainActivity.this, "Verification email sent successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
							}
						});
					}
				}
			});
			
		} else {
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			finishAffinity();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//setting hooks
		
		mToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		
		mDrawerLayout = findViewById(R.id.drawer_layout);
		mBottomNavigationView = findViewById(R.id.bottom_nav_view);
		mBottomAppBar = findViewById(R.id.bottom_app_bar);
		mNavigationView = findViewById(R.id.nav_view);
		fab = findViewById(R.id.fab);
		
		//set up navigation
		mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
		mNavController.addOnDestinationChangedListener(this);
		
		mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_attendance, R.id.nav_starred, R.id.nav_notes, R.id.nav_reminder, R.id.nav_profile, R.id.nav_about_us, R.id.nav_logout)
				.setDrawerLayout(mDrawerLayout)
				.build();
		
		NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(mNavigationView, mNavController);
		NavigationUI.setupWithNavController(mBottomNavigationView, mNavController);
		
		mNavigationView.setCheckedItem(R.id.nav_home);
		mBottomNavigationView.setSelectedItemId(R.id.nav_home);
		
		mNavigationView.setNavigationItemSelectedListener(this);
		mBottomNavigationView.setOnNavigationItemSelectedListener(this);
		
		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
					Log.d(TAG, "onClick: closing drawer");
					mDrawerLayout.closeDrawer(GravityCompat.START);
				} else if (mNavController.getCurrentDestination().getId() == R.id.fileFragment) {
					mNavController.navigateUp();
				} else if (mNavController.getCurrentDestination().getId() == R.id.notesSearchFragment) {
					mNavController.navigateUp();
				} else if (mNavController.getCurrentDestination().getId() == R.id.reminderDialogFragment) {
					mNavController.navigateUp();
				} else {
					Log.d(TAG, "onClick: opening drawer");
					mDrawerLayout.openDrawer(GravityCompat.START);
				}
			}
		});
		
		Log.d(TAG, "onNavigationItemSelected: animations for opening fragment to right of current one");
		leftToRightBuilder = new NavOptions.Builder();
		leftToRightBuilder.setEnterAnim(R.anim.slide_in_right);
		leftToRightBuilder.setExitAnim(R.anim.slide_out_left);
		leftToRightBuilder.setPopEnterAnim(R.anim.slide_in_left);
		leftToRightBuilder.setPopExitAnim(R.anim.slide_out_right);
		leftToRightBuilder.setLaunchSingleTop(true);
		
		Log.d(TAG, "onNavigationItemSelected: animations for opening fragment to left of current one");
		rightToLeftBuilder = new NavOptions.Builder();
		rightToLeftBuilder.setEnterAnim(R.anim.slide_in_left);
		rightToLeftBuilder.setExitAnim(R.anim.slide_out_right);
		rightToLeftBuilder.setPopEnterAnim(R.anim.slide_in_right);
		rightToLeftBuilder.setPopExitAnim(R.anim.slide_out_left);
		rightToLeftBuilder.setLaunchSingleTop(true);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("EXTRA_REMINDER_ITEM");
		if (bundle != null) {
			Log.d(TAG, "onCreate: starting reminder");
			ReminderItem item = bundle.getParcelable("BUNDLE_REMINDER_ITEM");
			
			NotificationHelper notificationHelper = new NotificationHelper(this);
			if (item != null) {
				notificationHelper.getManager().cancel(item.getNotifyId());
			}
			if (FirebaseAuth.getInstance().getCurrentUser() != null) {
				mNavController.navigate(R.id.nav_reminder, null, leftToRightBuilder.build());
			} else {
				FirebaseAuth.getInstance().signOut();
				
				GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
						.requestIdToken(getString(R.string.default_web_client_id))
						.requestEmail()
						.build();
				GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
				googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							mNavController.navigate(R.id.nav_logout);
							finishAffinity();
							overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
						} else {
							StyleableToast.makeText(MainActivity.this, "Could not sign out. Please try again", Toast.LENGTH_SHORT, R.style.designedToast).show();
						}
					}
				});
			}
		}
	}
	
	@Override
	public boolean onSupportNavigateUp() {
		Log.d(TAG, "onSupportNavigateUp: starts");
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}
	
	@Override
	public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
		Log.d(TAG, "onDestinationChanged: starts");
		fab.setOnClickListener(null);
		switch (destination.getId()) {
			case R.id.nav_home:
				mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
				mBottomAppBar.setVisibility(View.VISIBLE);
				mBottomAppBar.performShow();
				mBottomAppBar.bringToFront();
				if (mBottomNavigationView.getMenu().size() != 5) {
					mBottomNavigationView.getMenu().add(Menu.NONE, R.id.nav_fab, 3, "");
				}
				fab.show();
				fab.setImageResource(R.drawable.plus_icon);
				fab.setVisibility(View.VISIBLE);
				break;
			case R.id.nav_attendance:
				fab.hide();
				mBottomAppBar.performShow();
				mBottomAppBar.setVisibility(View.VISIBLE);
				mBottomNavigationView.getMenu().removeItem(R.id.nav_fab);
				mBottomAppBar.bringToFront();
				break;
			case R.id.nav_starred:
				fab.show();
				fab.setVisibility(View.VISIBLE);
				fab.setImageResource(R.drawable.plus_icon);
				mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
				mBottomAppBar.setVisibility(View.VISIBLE);
				if (mBottomNavigationView.getMenu().size() != 5) {
					mBottomNavigationView.getMenu().add(Menu.NONE, R.id.nav_fab, 3, "");
				}
				mBottomAppBar.performShow();
				mBottomAppBar.bringToFront();
				break;
			case R.id.nav_notes:
				fab.show();
				fab.setVisibility(View.VISIBLE);
				fab.setImageResource(R.drawable.folder_add_icon);
				mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
				mBottomAppBar.performShow();
				if (mBottomNavigationView.getMenu().size() != 5) {
					mBottomNavigationView.getMenu().add(Menu.NONE, R.id.nav_fab, 3, "");
				}
				mBottomAppBar.setVisibility(View.VISIBLE);
				mBottomAppBar.bringToFront();
				break;
			case R.id.nav_reminder:
				mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
				mBottomAppBar.setVisibility(View.GONE);
				fab.show();
				fab.setVisibility(View.VISIBLE);
				fab.setImageResource(R.drawable.reminder_add_icon);
				break;
			case R.id.nav_profile:
			case R.id.nav_about_us:
				mBottomAppBar.setVisibility(View.GONE);
				fab.setVisibility(View.GONE);
				fab.hide();
				break;
			default:
				break;
		}
	}
	
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		Log.d(TAG, "onNavigationItemSelected: starts");
		int itemId = item.getItemId();
		
		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			Log.d(TAG, "onNavigationItemSelected: closing drawer");
			mDrawerLayout.closeDrawer(GravityCompat.START);
		}
		
		switch (itemId) {
			case R.id.nav_home:
				Log.d(TAG, "onNavigationItemSelected: home selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_home) {
					Log.d(TAG, "onNavigationItemSelected: opening home fragment");
					mNavController.navigate(R.id.nav_home, null, rightToLeftBuilder.build());
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
			
			case R.id.nav_fab:
				Log.d(TAG, "onNavigationItemSelected: fab selected");
				return true;
			
			case R.id.nav_reminder:
				Log.d(TAG, "onNavigationItemSelected: reminder selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_reminder) {
					Log.d(TAG, "onNavigationItemSelected: opening reminder fragment");
					mNavController.navigate(R.id.nav_reminder, null, leftToRightBuilder.build());
				}
				return true;
			
			case R.id.nav_profile:
				Log.d(TAG, "onNavigationItemSelected: profile selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_profile) {
					Log.d(TAG, "onNavigationItemSelected: opening profile fragment");
					mNavController.navigate(R.id.nav_profile, null, leftToRightBuilder.build());
				}
				return true;
			
			case R.id.nav_logout:
				Log.d(TAG, "onNavigationItemSelected: logging out");
				FirebaseAuth.getInstance().signOut();
				
				GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
						.requestIdToken(getString(R.string.default_web_client_id))
						.requestEmail()
						.build();
				GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
				googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							mNavController.navigate(R.id.nav_logout);
							finishAffinity();
							overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
						} else {
							StyleableToast.makeText(MainActivity.this, "Could not sign out. Please try again", Toast.LENGTH_SHORT, R.style.designedToast).show();
						}
					}
				});
				return true;
			
			case R.id.nav_feedback:
				
				Connection.feedback(this);
				return true;
			
			case R.id.nav_report_bug:
				Connection.reportBug(this);
				return true;
			
			case R.id.nav_about_us:
				Log.d(TAG, "onNavigationItemSelected: about us selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_about_us) {
					Log.d(TAG, "onNavigationItemSelected: opening about us fragment");
					mNavController.navigate(R.id.nav_about_us, null, leftToRightBuilder.build());
				}
				return true;
			
			default:
				StyleableToast.makeText(this, "This feature is not yet available", Toast.LENGTH_SHORT, R.style.designedToast).show();
				return false;
		}
	}
}