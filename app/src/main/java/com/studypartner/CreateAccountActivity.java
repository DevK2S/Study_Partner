package com.studypartner;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {
	private static final String TAG = "CreateAccountActivity";
	
	@Override
	public void onBackPressed() {
		Log.d(TAG, "onClick: creating shared animation on back pressed");
		
		finish();
		
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);
		
		findViewById(R.id.createAccountBackArrow).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		findViewById(R.id.createAccountScreenLoginButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		findViewById(R.id.createAccountScreenCreateAccountButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TextInputLayout nameTextInput = findViewById(R.id.createAccountScreenFullNameTextInput);
				TextInputEditText nameEditText = findViewById(R.id.createAccountScreenFullNameEditText);
				nameTextInput.setError(validateName(nameEditText.getText().toString()));
				
				TextInputLayout usernameTextInput = findViewById(R.id.createAccountScreenUsernameTextInput);
				TextInputEditText usernameEditText = findViewById(R.id.createAccountScreenUsernameEditText);
				usernameTextInput.setError(validateUsername(usernameEditText.getText().toString()));
				
				TextInputLayout emailTextInput = findViewById(R.id.createAccountScreenEmailTextInput);
				TextInputEditText emailEditText = findViewById(R.id.createAccountScreenEmailEditText);
				emailTextInput.setError(validateEmail(emailEditText.getText().toString()));
				
				TextInputLayout passwordTextInput = findViewById(R.id.createAccountScreenPasswordTextInput);
				TextInputEditText passwordEditText = findViewById(R.id.createAccountScreenPasswordEditText);
				
				TextInputLayout confirmPasswordTextInput = findViewById(R.id.createAccountScreenConfirmPasswordTextInput);
				TextInputEditText confirmPasswordEditText = findViewById(R.id.createAccountScreenConfirmPasswordEditText);
				
				passwordTextInput.setError(validatePassword(passwordEditText.getText().toString(), confirmPasswordEditText.getText().toString()));
				
				confirmPasswordTextInput.setError(validateConfirmPassword(confirmPasswordEditText.getText().toString(), passwordEditText.getText().toString()));
			}
		});
		
		Log.d(TAG, "onCreate: ends");
	}
	
	private String validateName(String name) {
		if (name.trim().length() == 0) {
			return "Name cannot be empty";
		} else if (name.trim().matches("^[0-9]+$")) {
			return "Name cannot have numbers in it";
		} else if (!name.trim().matches("^[a-zA-Z][a-zA-Z ]++$")) {
			return "Invalid Name";
		}
		return null;
	}
	
	private String validateUsername(String username) {
		if (username.trim().length() == 0) {
			return "Username cannot be empty";
		} else if (username.trim().length() < 5) {
			return "Username too small. Minimum length is 5";
		} else if (username.trim().length() > 15) {
			return "Username too long. Maximum length is 15";
		} else if (!username.trim().matches("^[a-zA-Z][a-zA-Z0-9]+$")) {
			return "Username can only contain letters and numbers";
		}
		return null;
	}
	
	private String validateEmail(String email) {
		if (email.trim().length() == 0) {
			return "Email cannot be empty";
		} else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
			return "Invalid Email";
		}
		return null;
	}
	
	private String validatePassword(String password, String confirmPassword) {
		if (password.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (confirmPassword.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (!confirmPassword.trim().matches(password.trim())) {
			return "Passwords do not match";
		} else if (password.trim().length() < 8) {
			return "Password too small. Minimum length is 8";
		} else if (password.trim().length() > 15) {
			return "Password too long. Maximum length is 15";
		} else if (password.trim().contains(" ")) {
			return "Password cannot contain spaces";
		} else if (!(password.trim().contains("@") || password.trim().contains("#")) || password.trim().contains("$") || password.trim().contains("%") || password.trim().contains("*") || password.trim().contains(".") || password.trim().matches("(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))")) {
			return "Password should contain atleast one uppercase character or one number or any of the special characters from (@, #, $, %, *, .)";
		}
		return null;
	}
	
	private String validateConfirmPassword(String confirmPassword, String password) {
		if (confirmPassword.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (password.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (!confirmPassword.trim().matches(password.trim())) {
			return "Passwords do not match";
		} else if (password.trim().length() < 8) {
			return "Password too small. Minimum length is 8";
		} else if (password.trim().length() > 15) {
			return "Password too long. Maximum length is 15";
		} else if (password.trim().contains(" ")) {
			return "Password cannot contain spaces";
		} else if (!(password.trim().contains("@") || password.trim().contains("#")) || password.trim().contains("$") || password.trim().contains("%") || password.trim().contains("*") || password.trim().contains(".") || password.trim().matches("(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))")) {
			return "Password should contain atleast one uppercase character or one number or any of the special characters from (@, #, $, %, *, .)";
		}
		return null;
	}
}