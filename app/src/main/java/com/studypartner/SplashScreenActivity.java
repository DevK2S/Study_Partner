package com.studypartner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
	private static final String TAG = "SplashScreen";
	
	Animation mAnimation;
	ImageView splashScreenAppLogo;
	TextView splashScreenAppName;
	
	final String ON_BOARDING_SCREEN_VIEWED = "ON_BOARDING_SCREEN_VIEWED";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
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
//				if (sharedPreferences.getBoolean(ON_BOARDING_SCREEN_VIEWED,false)) {
//					Log.d(TAG, "run: starting Login Activity");
//					startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
//				} else {
//					Log.d(TAG, "run: starting On Boarding Activity");
//					startActivity(new Intent(SplashScreenActivity.this, OnBoardingActivity.class));
//				}
				finish();
			}
		},2500);
		Log.d(TAG, "onCreate: ends");
	}
}