package com.studypartner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.studypartner.R;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
	private static final String TAG = "ProfileActivity";
	
	private Button updateProfile, updateEmail, updatePassword;
	private TextInputLayout fullNameTextInput, usernameTextInput, emailTextInput, passwordTextInput, oldPasswordTextInput,
			newPasswordTextInput, confirmPasswordTextInput;
	
	@Override
	public void onBackPressed() {
		startActivity(new Intent(ProfileActivity.this, MainActivity.class));
		finishAffinity();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		findViewById(R.id.profileScreenBackArrow).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		//Setting hooks
		
		updateProfile = findViewById(R.id.profileScreenUpdateProfileButton);
		updateEmail = findViewById(R.id.profileScreenUpdateEmailButton);
		updatePassword = findViewById(R.id.profileScreenUpdatePasswordButton);
		
		fullNameTextInput = findViewById(R.id.profileScreenFullNameTextInput);
		usernameTextInput = findViewById(R.id.profileScreenUsernameTextInput);
		emailTextInput = findViewById(R.id.profileScreenEmailTextInput);
		passwordTextInput = findViewById(R.id.profileScreenPasswordTextInput);
		oldPasswordTextInput = findViewById(R.id.profileScreenOldPasswordTextInput);
		newPasswordTextInput = findViewById(R.id.profileScreenNewPasswordTextInput);
		confirmPasswordTextInput = findViewById(R.id.profileScreenConfirmPasswordTextInput);
	}
}