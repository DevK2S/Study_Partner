package com.studypartner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.studypartner.R;
import com.studypartner.adapters.OnBoardingViewPagerAdapter;
import com.studypartner.models.OnBoardingItem;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class OnBoardingActivity extends AppCompatActivity {
	private static final String TAG = "OnBoardingActivity";
	
	ViewPager screenPager;
	OnBoardingViewPagerAdapter onBoardingViewPagerAdapter;
	TabLayout tabIndicator;
	Button nextButton, getStartedButton, skipButton, backButton;
	Animation buttonAnimation;
	int currentPagePosition = 0;
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_on_boarding);

        nextButton = findViewById(R.id.onBoardingScreenNextButton);
        getStartedButton = findViewById(R.id.onBoardingScreenGetStartedButton);
        skipButton = findViewById(R.id.onBoardingScreenSkipButton);
        backButton = findViewById(R.id.onBoardingScreenBackButton);
        tabIndicator = findViewById(R.id.onBoardingScreenTabLayout);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.on_boarding_get_started_animation);

        Log.d(TAG, "onCreate: Initialising screens for onBoarding");

        final List<OnBoardingItem> screenList = new ArrayList<>();
        screenList.add(new OnBoardingItem("NOTES KEEPER", "One step to keep and arrange all your notes!", R.drawable.on_boarding_screen_notes_image));
        screenList.add(new OnBoardingItem("ALL TYPES OF NOTES", "Store Images, Documents, Videos and much more", R.drawable.on_boarding_screen_types_of_notes_image));
        screenList.add(new OnBoardingItem("ATTENDANCE MANAGER", "Manage your Attendance too", R.drawable.on_boarding_screen_attendance_image));

        screenPager = findViewById(R.id.onBoardingScreenViewPager);
        onBoardingViewPagerAdapter = new OnBoardingViewPagerAdapter(this, screenList);
        screenPager.setAdapter(onBoardingViewPagerAdapter);
        tabIndicator.setupWithViewPager(screenPager, true);

        if (screenList.size() == 1) {
            Log.d(TAG, "onCreate: onBoarding has only one screen");
            loadLastScreen();
        }
		
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: nextButton pressed");
				backButton.setVisibility(View.VISIBLE);
				
				currentPagePosition = screenPager.getCurrentItem();
				
				if (currentPagePosition < screenList.size()) {
					currentPagePosition++;
					screenPager.setCurrentItem(currentPagePosition);
				}
				
				if (currentPagePosition == screenList.size() - 1) {
					Log.d(TAG, "onClick: Loading last screen for next button");
					loadLastScreen();
				}
				
			}
		});
		
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: backButton pressed");
				currentPagePosition = screenPager.getCurrentItem();
				
				nextButton.setVisibility(View.VISIBLE);
				skipButton.setVisibility(View.VISIBLE);
				tabIndicator.setVisibility(View.VISIBLE);
				getStartedButton.setVisibility(View.INVISIBLE);
				
				if (currentPagePosition > 0) {
					currentPagePosition--;
					screenPager.setCurrentItem(currentPagePosition);
				}
				
				if (currentPagePosition == 0) {
					Log.d(TAG, "onClick: loading first page for back button");
					loadFirstScreen();
				}
			}
		});
		
		skipButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: loading LoginActivity for skip button");
				getSharedPreferences("OnBoarding", MODE_PRIVATE).edit().putBoolean("ON_BOARDING_SCREEN_VIEWED", true).apply();
				startActivity(new Intent(OnBoardingActivity.this, LoginActivity.class));
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
		});
		
		getStartedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: loading LoginActivity for get started button");
				getSharedPreferences("OnBoarding", MODE_PRIVATE).edit().putBoolean("ON_BOARDING_SCREEN_VIEWED", true).apply();
				startActivity(new Intent(OnBoardingActivity.this, LoginActivity.class));
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
		});
		
		tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				if (tab.getPosition() == screenList.size() - 1) {
					Log.d(TAG, "onTabSelected: loading last screen for tab indicator");
					loadLastScreen();
				} else if (tab.getPosition() == 0) {
					Log.d(TAG, "onTabSelected: loading first screen for tab indicator");
					loadFirstScreen();
				} else {
					nextButton.setVisibility(View.VISIBLE);
					skipButton.setVisibility(View.VISIBLE);
					tabIndicator.setVisibility(View.VISIBLE);
					getStartedButton.setVisibility(View.INVISIBLE);
					backButton.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			
			}
			
			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			
			}
		});
		
		Log.d(TAG, "onCreate: ends");
	}
	
	private void loadFirstScreen() {
		Log.d(TAG, "loadFirstScreen: starts");
		nextButton.setVisibility(View.VISIBLE);
		skipButton.setVisibility(View.VISIBLE);
		tabIndicator.setVisibility(View.VISIBLE);
		getStartedButton.setVisibility(View.INVISIBLE);
		backButton.setVisibility(View.INVISIBLE);
		Log.d(TAG, "loadFirstScreen: ends");
	}
	
	private void loadLastScreen() {
		Log.d(TAG, "loadLastScreen: starts");
		nextButton.setVisibility(View.INVISIBLE);
		skipButton.setVisibility(View.INVISIBLE);
		tabIndicator.setVisibility(View.INVISIBLE);
		getStartedButton.setVisibility(View.VISIBLE);
		backButton.setVisibility(View.VISIBLE);
		
		getStartedButton.setAnimation(buttonAnimation);
		Log.d(TAG, "loadLastScreen: ends");
	}
}