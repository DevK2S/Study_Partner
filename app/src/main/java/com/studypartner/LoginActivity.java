package com.studypartner;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
	private static final String TAG = "LoginActivity";
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		findViewById(R.id.loginScreenCreateAccountButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: creating shared animation on create account button press");
				Pair[] pairs = new Pair[6];
				pairs[0] = new Pair<>(findViewById(R.id.loginScreenWelcomeBack), "welcome_transition");
				pairs[1] = new Pair<>(findViewById(R.id.loginScreenCreateAccountButton), "create_account_transition");
				pairs[2] = new Pair<>(findViewById(R.id.loginScreenLoginButton), "login_transition");
				pairs[3] = new Pair<>(findViewById(R.id.loginScreenEmailTextInput), "email_transition");
				pairs[4] = new Pair<>(findViewById(R.id.loginScreenPasswordTextInput), "password_transition");
				pairs[5] = new Pair<>(findViewById(R.id.loginScreenBackgroundRectangle), "bgrect_transition");
				
				ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
				
				startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class), activityOptions.toBundle());
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		
		findViewById(R.id.loginScreenLoginButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TextInputLayout emailTextInput = findViewById(R.id.loginScreenEmailTextInput);
				TextInputEditText emailEditText = findViewById(R.id.loginScreenEmailEditText);
				emailTextInput.setError(validateEmail(emailEditText.getText().toString()));
				
				TextInputLayout passwordTextInput = findViewById(R.id.loginScreenPasswordTextInput);
				TextInputEditText passwordEditText = findViewById(R.id.loginScreenPasswordEditText);
				passwordTextInput.setError(validatePassword(passwordEditText.getText().toString()));
			}
		});
		
		Log.d(TAG, "onCreate: ends");
	}
	
	private String validateEmail(String email) {
		if (email.trim().length() == 0) {
			return "Email cannot be empty";
		} else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			return "Invalid Email";
		}
		return null;
	}
	
	private String validatePassword(String password) {
		if (password.trim().length() == 0) {
			return "Password cannot be empty";
		} else if (password.length() < 7) {
			return "Password too small. Minimum length is 7";
		} else if (password.length() > 15) {
			return "Password too long. Maximum length is 15";
		} else if (password.contains(" ")) {
			return "Password cannot contain spaces";
		} else if (!(password.contains("@") || password.contains("#")) || password.contains("$") || password.contains("%") || password.contains("*") || password.contains(".") || password.matches("(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))")) {
			return "Password should contain atleast one uppercase character or one number or any of the special characters from (@, #, $, %, *, .)";
		}
		return null;
	}
}