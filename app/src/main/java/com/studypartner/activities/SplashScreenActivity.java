package com.studypartner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.studypartner.R;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
	private static final String TAG = "SplashScreen";
	final String ON_BOARDING_SCREEN_VIEWED = "ON_BOARDING_SCREEN_VIEWED";
	Animation mAnimation;
	ImageView splashScreenAppLogo;
	TextView splashScreenAppName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(Color.WHITE);
		
		overridePendingTransition(0, R.anim.slide_out_left);
		
		mAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		
		splashScreenAppLogo = findViewById(R.id.splashScreenAppLogo);
		splashScreenAppName = findViewById(R.id.splashScreenAppName);
		
		splashScreenAppLogo.setAnimation(mAnimation);
		splashScreenAppName.setAnimation(mAnimation);
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "run: Splash Screen Finished");
				SharedPreferences sharedPreferences = getSharedPreferences("OnBoarding", MODE_PRIVATE);
				if (sharedPreferences.getBoolean(ON_BOARDING_SCREEN_VIEWED, false)) {
					Log.d(TAG, "run: starting Login Activity");
					startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
				} else {
					Log.d(TAG, "run: starting On Boarding Activity");
					startActivity(new Intent(SplashScreenActivity.this, OnBoardingActivity.class));
				}
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
		}, 1500);
		
		Log.d(TAG, "onCreate: ends");
	}
}