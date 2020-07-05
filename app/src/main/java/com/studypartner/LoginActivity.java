package com.studypartner;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
	private static final String TAG = "LoginActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		Log.d(TAG, "onCreate: ends");
	}
}