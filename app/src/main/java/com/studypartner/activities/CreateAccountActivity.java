package com.studypartner.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.studypartner.R;
import com.studypartner.models.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {
	private static final String TAG = "CreateAccountActivity";
	
	private TextInputLayout nameTextInput, usernameTextInput, emailTextInput, passwordTextInput, confirmPasswordTextInput;
	private TextInputEditText nameEditText, usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
	
	private String fullName, username, email;
	
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
		
		//Setting hooks
		
		nameTextInput = findViewById(R.id.createAccountScreenFullNameTextInput);
		nameEditText = findViewById(R.id.createAccountScreenFullNameEditText);
		
		nameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				nameTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		usernameTextInput = findViewById(R.id.createAccountScreenUsernameTextInput);
		usernameEditText = findViewById(R.id.createAccountScreenUsernameEditText);
		
		usernameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				usernameTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		emailTextInput = findViewById(R.id.createAccountScreenEmailTextInput);
		emailEditText = findViewById(R.id.createAccountScreenEmailEditText);
		
		emailEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				emailTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		passwordTextInput = findViewById(R.id.createAccountScreenPasswordTextInput);
		passwordEditText = findViewById(R.id.createAccountScreenPasswordEditText);
		
		passwordEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				passwordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		confirmPasswordTextInput = findViewById(R.id.createAccountScreenConfirmPasswordTextInput);
		confirmPasswordEditText = findViewById(R.id.createAccountScreenConfirmPasswordEditText);
		
		confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				confirmPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		Button createAccountButton = findViewById(R.id.createAccountScreenCreateAccountButton);
		Button termsAndConditionsButton = findViewById(R.id.createAccountScreenTCButton);
		
		//Setting on click listeners
		
		findViewById(R.id.createAccountBackArrow).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: back arrow is pressed");
				onBackPressed();
			}
		});
		
		findViewById(R.id.createAccountScreenLoginButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: login button pressed");
				onBackPressed();
			}
		});
		
		createAccountButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: create account button pressed");
				
				if (validateFields()) {
					checkUsernameValidity();
				}
			}
		});
		
		termsAndConditionsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: terms and conditions pressed");
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1MKWGuegWbqugvAPzyNpF0oVYQpDsIZON1DJr8Ap9CWc/edit?usp=sharing"));
				startActivity(Intent.createChooser(browserIntent,"Select the app to open the link"));
			}
		});
		
		Log.d(TAG, "onCreate: ends");
	}
	
	private void checkUsernameValidity() {
		
		findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.VISIBLE);
		
		FirebaseDatabase.getInstance().getReference().child("usernames").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (snapshot.exists() && snapshot.hasChildren()) {
					for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
						if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
							if (username.trim().matches((String) dataSnapshot.getValue())) {
								usernameTextInput.setError("Username is already taken by another user");
								findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.INVISIBLE);
								return;
							}
						}
					}
				}
				createAccount();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.INVISIBLE);
			}
		});
		
	}
	
	private void createAccount() {
		Log.d(TAG, "createAccount: starts");
		
		findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.VISIBLE);
		
		FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
				.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: account created");
							storeUserDetails();
						} else {
							findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.INVISIBLE);
							Toast.makeText(CreateAccountActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				});
	}
	
	private void storeUserDetails() {
		Log.d(TAG, "storeUserDetails: starts");
		final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		
		final User user = new User(fullName, username, email, FirebaseAuth.getInstance().getCurrentUser().isEmailVerified());
		
		UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
		profileUpdates.setDisplayName(fullName);
		
		FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates.build())
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
							
							//Make users database
							FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(user);
							
							//Make usernames database
							FirebaseDatabase.getInstance().getReference().child("usernames").child(uid).setValue(username);
							
							Log.d(TAG, "onComplete: starting main activity");
							startActivity(new Intent(CreateAccountActivity.this, MainActivity.class));
							finishAffinity();
							overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
							
						} else {
							Log.d(TAG, "onComplete: Could not update display name");
						}
					}
				});
		
		FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					Log.d(TAG, "onComplete: Verification mail sent successfully");
				} else {
					Log.d(TAG, "onComplete: Verification mail could not be sent");
				}
			}
		});
		
		Log.d(TAG, "storeUserDetails: ends");
	}
	
	private boolean validateFields() {
		fullName = nameEditText.getText().toString();
		String nameValidation = validateName(fullName);
		
		username = usernameEditText.getText().toString();
		String usernameValidation = validateUsername(username);
		
		email = emailEditText.getText().toString();
		String emailValidation = validateEmail(email);
		
		String passwordValidation = validatePassword(passwordEditText.getText().toString(), confirmPasswordEditText.getText().toString());
		
		String confirmPasswordValidation = validateConfirmPassword(confirmPasswordEditText.getText().toString(), passwordEditText.getText().toString());
		
		if (nameValidation == null && usernameValidation == null && emailValidation == null && passwordValidation == null && confirmPasswordValidation == null) {
			Log.d(TAG, "validateFields: all fields valid");
			return true;
		}
		
		if (nameValidation != null) {
			nameTextInput.setError(nameValidation);
		}
		
		if (usernameValidation != null) {
			usernameTextInput.setError(usernameValidation);
		}
		
		if (emailValidation != null) {
			emailTextInput.setError(emailValidation);
		}
		
		if (passwordValidation != null) {
			passwordTextInput.setError(passwordValidation);
		}
		
		if (confirmPasswordValidation != null) {
			confirmPasswordTextInput.setError(confirmPasswordValidation);
		}
		
		return false;
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
	
	private String validateUsername(final String username) {
		if (username.trim().length() == 0) {
			return "Username cannot be empty";
		} else if (username.trim().length() < 5) {
			return "Username too small. Minimum length is 5";
		} else if (username.trim().length() > 25) {
			return "Username too long. Maximum length is 25";
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