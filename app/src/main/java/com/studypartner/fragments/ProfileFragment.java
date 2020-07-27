package com.studypartner.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.studypartner.R;
import com.studypartner.activities.LoginActivity;
import com.studypartner.activities.MainActivity;
import com.studypartner.models.User;
import com.studypartner.utils.Connection;

import java.io.IOException;
import java.util.Objects;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
	private static final String TAG = "ProfileFragment";
	
	private final int PICK_IMAGE_REQUEST = 111;
	
	private final String SESSIONS = "SESSIONS";
	
	private final String REMEMBER_ME_ENABLED = "rememberMeEnabled";
	private final String REMEMBER_ME_EMAIL = "rememberMeEmail";
	private final String REMEMBER_ME_PASSWORD = "rememberMePassword";
	
	private FirebaseUser currentUser;
	private DatabaseReference mDatabaseReference;
	private User user;
	
	private Uri filePath;
	
	private Button updateProfile, updateEmail, updatePassword, deleteAccount;
	private TextInputLayout fullNameTextInput, usernameTextInput, emailTextInput, passwordTextInput, oldPasswordTextInput,
			newPasswordTextInput, confirmPasswordTextInput, deleteAccountPasswordTextInput;
	private ImageView profileImageView;
	private ProgressBar progressBar;
	private ImageButton cameraButton;
	
	private boolean signedInWithGoogle = false;
	
	private String fullName, username, email, password, oldPassword, newPassword, confirmPassword, deleteAccountPassword;
	
	public ProfileFragment() {}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Log.d(TAG, "onResume: checking internet connection");
		Connection.checkConnection(this);
		
		oldPasswordTextInput.setEnabled(true);
		emailTextInput.setEnabled(true);
		
		passwordTextInput.getEditText().setText("");
		oldPasswordTextInput.getEditText().setText("");
		newPasswordTextInput.getEditText().setText("");
		confirmPasswordTextInput.getEditText().setText("");
		deleteAccountPasswordTextInput.getEditText().setText("");
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		Log.d(TAG, "onActivityResult: starts");
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
			Log.d(TAG, "onActivityResult: image received");
			filePath = data.getData();
			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),filePath);
				Log.d(TAG, "onActivityResult: setting image");
				profileImageView.setImageBitmap(bitmap);
				uploadImage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		View rootView = inflater.inflate(R.layout.fragment_profile,container, false);
		
		Log.d(TAG, "onCreateView: checking connection");
		
		Connection.checkConnection(this);
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				MainActivity activity = (MainActivity) requireActivity();
				activity.mNavController.navigate(R.id.action_nav_profile_to_nav_home);
			}
		});
		
		//Setting hooks
		
		mDatabaseReference = FirebaseDatabase.getInstance().getReference();
		
		updateProfile = rootView.findViewById(R.id.profileScreenUpdateProfileButton);
		updateEmail = rootView.findViewById(R.id.profileScreenUpdateEmailButton);
		updatePassword = rootView.findViewById(R.id.profileScreenUpdatePasswordButton);
		deleteAccount = rootView.findViewById(R.id.profileScreenDeleteAccountButton);
		
		fullNameTextInput = rootView.findViewById(R.id.profileScreenFullNameTextInput);
		fullNameTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				fullNameTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		usernameTextInput = rootView.findViewById(R.id.profileScreenUsernameTextInput);
		usernameTextInput.getEditText().addTextChangedListener(new TextWatcher() {
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
		
		emailTextInput = rootView.findViewById(R.id.profileScreenEmailTextInput);
		emailTextInput.getEditText().addTextChangedListener(new TextWatcher() {
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
		
		passwordTextInput = rootView.findViewById(R.id.profileScreenPasswordTextInput);
		passwordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
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
		
		oldPasswordTextInput = rootView.findViewById(R.id.profileScreenOldPasswordTextInput);
		oldPasswordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				oldPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		newPasswordTextInput = rootView.findViewById(R.id.profileScreenNewPasswordTextInput);
		confirmPasswordTextInput = rootView.findViewById(R.id.profileScreenConfirmPasswordTextInput);
		
		newPasswordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				newPasswordTextInput.setError(null);
				confirmPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		confirmPasswordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				confirmPasswordTextInput.setError(null);
				newPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		deleteAccountPasswordTextInput = rootView.findViewById(R.id.profileScreenDeleteAccountPasswordTextInput);
		deleteAccountPasswordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				deleteAccountPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		profileImageView = rootView.findViewById(R.id.profileScreenImageView);
		cameraButton = rootView.findViewById(R.id.profileScreenImageButton);
		
		progressBar = rootView.findViewById(R.id.profileScreenProgressBar);
		
		currentUser = FirebaseAuth.getInstance().getCurrentUser();
		
		disableViews();
		
		mDatabaseReference.child("usernames").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				Log.d(TAG, "onDataChange: setting values of edit texts");
				
				user = new User(currentUser.getDisplayName(),
						snapshot.getValue(String.class),
						currentUser.getEmail(), currentUser.isEmailVerified());
				
				usernameTextInput.getEditText().setText(user.getUsername());
				fullNameTextInput.getEditText().setText(user.getFullName());
				emailTextInput.getEditText().setText(user.getEmail());
				
				enableViews();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError error) {
			
			}
		});
		
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: camera button clicked");
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
			}
		});
		
		for (UserInfo userInfo: currentUser.getProviderData()) {
			if (userInfo.getProviderId().equals("google.com")) {
				Log.d(TAG, "onCreate: logged in with google");
				signedInWithGoogle = true;
			}
		}
		
		Log.d(TAG, "onCreate: loading image in profile photo");
		if (currentUser.getPhotoUrl() != null) {
			Picasso.get().load(currentUser.getPhotoUrl())
					.error(R.drawable.image_error_icon)
					.placeholder(R.drawable.profile_photo_icon)
					.into(profileImageView);
		}
		
		updateProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onCreate: update profile button clicked");
				updateProfile();
			}
		});
		
		updateEmail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onCreate: update email button clicked");
				if (!signedInWithGoogle) {
					updateEmail();
				} else {
					Toast.makeText(getContext(), "Signed in with google, cannot update email", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		updatePassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onCreate: update password button clicked");
				if (!signedInWithGoogle) {
					updatePassword();
				} else {
					Toast.makeText(getContext(), "Signed in with google, cannot update password", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		deleteAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: delete account button pressed");
				
				deleteAccount();
			}
		});
		
		Log.d(TAG, "onCreate: ends");
		
		return rootView;
	}
	
	private void deleteAccount() {
		Log.d(TAG, "deleteAccount: checking internet connection");
		Connection.checkConnection(this);
		
		disableViews();
		
		deleteAccountPassword = deleteAccountPasswordTextInput.getEditText().getText().toString().trim();
		
		if (!signedInWithGoogle && deleteAccountPasswordTextInput.getVisibility() == View.GONE) {
			Log.d(TAG, "deleteAccount: showing delete account password edit text");
			deleteAccountPasswordTextInput.setVisibility(View.VISIBLE);
			Toast.makeText(getContext(), "Enter the current password to delete the account", Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG, "deleteAccount: re authenticating the user");
			
			AuthCredential authCredential = null;
			
			if (signedInWithGoogle) {
				GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
				if (googleSignInAccount != null) {
					authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
				}
			} else {
				authCredential = EmailAuthProvider.getCredential(user.getEmail(), deleteAccountPassword);
			}
			
			currentUser.reauthenticate(authCredential)
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Log.d(TAG, "onComplete: re authenticating successful");
								
								final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
								builder.setTitle("Deleting your account");
								builder.setMessage("Are you sure you want to delete the account?");
								builder.setCancelable(false);
								builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Log.d(TAG, "onClick: deleting account");
										
										mDatabaseReference.child("users").child(currentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
											@Override
											public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
												if (error == null) {
													mDatabaseReference.child("usernames").child(currentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
														@Override
														public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
															if (error == null) {
																
																if (!signedInWithGoogle && currentUser.getPhotoUrl() != null) {
																	FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(currentUser.getPhotoUrl())).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
																		@Override
																		public void onSuccess(Void aVoid) {
																			Log.d(TAG, "onSuccess: photo deleted successfully");
																		}
																	});
																}
																
																FirebaseAuth.getInstance().getCurrentUser().delete()
																		.addOnCompleteListener(new OnCompleteListener<Void>() {
																			@Override
																			public void onComplete(@NonNull Task<Void> task) {
																				if (task.isSuccessful()) {
																					Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
																					
																					SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SESSIONS, MODE_PRIVATE);
																					SharedPreferences.Editor editor = sharedPreferences.edit();
																					
																					editor.putBoolean(REMEMBER_ME_ENABLED, false);
																					editor.putString(REMEMBER_ME_EMAIL, "");
																					editor.putString(REMEMBER_ME_PASSWORD, "");
																					editor.apply();
																					
																					FirebaseAuth.getInstance().signOut();
																					
																					startActivity(new Intent(getContext(), LoginActivity.class));
																					getActivity().finishAffinity();
																					getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
																					
																				} else {
																					Log.d(TAG, "onComplete: could not delete account");
																				}
																			}
																		});
															} else {
																Log.d(TAG, "onComplete: could not delete data");
															}
														}
													});
												} else {
													Log.d(TAG, "onComplete: could not delete data");
												}
											}
										});
									}
								});
								builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										deleteAccountPasswordTextInput.getEditText().setText("");
										deleteAccountPasswordTextInput.setVisibility(View.GONE);
									}
								});
								
								builder.show();
							} else {
								Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
		}
		
		enableViews();
	}
	
	private void updateProfile() {
		Log.d(TAG, "updateProfile: checking internet connection");
		Connection.checkConnection(this);
		
		disableViews();
		
		fullName = fullNameTextInput.getEditText().getText().toString().trim();
		username = usernameTextInput.getEditText().getText().toString().trim();
		
		if (user.validateName(fullName) != null || user.validateUsername(username) != null) {
			Log.d(TAG, "updateProfile: username and full name invalid");
			fullNameTextInput.setError(user.validateName(fullName));
			usernameTextInput.setError(user.validateUsername(username));
			enableViews();
			return;
		}
		
		if (!fullName.matches(user.getFullName()) && !username.matches(user.getUsername())) {
			Log.d(TAG, "updateProfile: updating username and full name");
			
			user.setFullName(fullName);
			user.setUsername(username);
			
			UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
			profileUpdates.setDisplayName(fullName);
			
			FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates.build())
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Log.d(TAG, "onComplete: " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
							} else {
								Log.d(TAG, "onComplete: Could not update display name");
							}
						}
					});
			
			Log.d(TAG, "updateProfile: updating profile");
			UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
					.setDisplayName(fullName)
					.build();
			currentUser.updateProfile(profileChangeRequest)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Log.d(TAG, "onSuccess: display name changed successfully");
							
							mDatabaseReference.child("users").child(currentUser.getUid()).setValue(user)
									.addOnSuccessListener(new OnSuccessListener<Void>() {
										@Override
										public void onSuccess(Void aVoid) {
											Log.d(TAG, "onSuccess: users database updated successfully");
											
											mDatabaseReference.child("usernames").child(currentUser.getUid()).setValue(user.getUsername())
													.addOnSuccessListener(new OnSuccessListener<Void>() {
														@Override
														public void onSuccess(Void aVoid) {
															Log.d(TAG, "onSuccess: usernames database updated successfully");
															
															Toast.makeText(getContext(), "Display name and username updated successfully", Toast.LENGTH_SHORT).show();
															
															Log.d(TAG, "onSuccess: setting full name in nav header");
															NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
															TextView profileFullName = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileName);
															if (null != FirebaseAuth.getInstance().getCurrentUser().getDisplayName()) {
																profileFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
															}
														}
													})
													.addOnFailureListener(new OnFailureListener() {
														@Override
														public void onFailure(@NonNull Exception e) {
															Log.d(TAG, "onFailure: usernames database could not be updated");
															Toast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT).show();
														}
													});
										}
									})
									.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
											Log.d(TAG, "onFailure: users database could not be updated");
											Toast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT).show();
										}
									});
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Log.d(TAG, "onFailure: display name changing failed");
							Toast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
			
		} else if (!fullName.matches(user.getFullName())) {
			Log.d(TAG, "updateProfile: updating full name");
			
			user.setFullName(fullName);
			
			Log.d(TAG, "updateProfile: updating profile");
			UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
					.setDisplayName(fullName)
					.build();
			currentUser.updateProfile(profileChangeRequest)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Log.d(TAG, "onSuccess: display name changed successfully");
							
							mDatabaseReference.child("users").child(currentUser.getUid()).setValue(user)
									.addOnSuccessListener(new OnSuccessListener<Void>() {
										@Override
										public void onSuccess(Void aVoid) {
											Log.d(TAG, "onSuccess: users database updated successfully");
											
											Toast.makeText(getContext(), "Display name updated successfully", Toast.LENGTH_SHORT).show();
											
											Log.d(TAG, "onSuccess: setting full name in nav header");
											NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
											TextView profileFullName = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileName);
											
											if (null != FirebaseAuth.getInstance().getCurrentUser().getDisplayName()) {
												profileFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
											}
										}
									})
									.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
											Log.d(TAG, "onFailure: users database could not be updated");
											Toast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT).show();
										}
									});
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Log.d(TAG, "onFailure: display name changing failed");
							Toast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
			
		} else if (!username.matches(user.getUsername())) {
			Log.d(TAG, "updateProfile: updating username");
			
			user.setUsername(username);
			
			mDatabaseReference.child("users").child(currentUser.getUid()).setValue(user)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Log.d(TAG, "onSuccess: users database updated successfully");
							
							mDatabaseReference.child("usernames").child(currentUser.getUid()).setValue(user.getUsername())
									.addOnSuccessListener(new OnSuccessListener<Void>() {
										@Override
										public void onSuccess(Void aVoid) {
											Log.d(TAG, "onSuccess: usernames database updated successfully");
											
											Toast.makeText(getContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();
										}
									})
									.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
											Log.d(TAG, "onFailure: usernames database could not be updated");
											Toast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT).show();
										}
									});
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Log.d(TAG, "onFailure: users database could not be updated");
							Toast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
		} else {
			Toast.makeText(getContext(), "Full name and username are same as now", Toast.LENGTH_SHORT).show();
		}
		
		enableViews();
	}
	
	private void updateEmail() {
		Log.d(TAG, "updateEmail: checking internet connection");
		Connection.checkConnection(this);
		
		disableViews();
		
		email = emailTextInput.getEditText().getText().toString().trim();
		password = passwordTextInput.getEditText().getText().toString().trim();
		
		if (email.matches(user.getEmail())) {
			Toast.makeText(getContext(), "Entered email is same as current email", Toast.LENGTH_SHORT).show();
			enableViews();
			return;
		}
		
		if (user.validateEmail(email) != null) {
			Log.d(TAG, "updateEmail: email invalid");
			emailTextInput.setError(user.validateEmail(email));
			enableViews();
			return;
		}
		
		if (passwordTextInput.getVisibility() == View.GONE) {
			Log.d(TAG, "updateEmail: showing password edit text");
			passwordTextInput.setVisibility(View.VISIBLE);
			emailTextInput.setEnabled(false);
			Toast.makeText(getContext(), "Enter the current password to change the email", Toast.LENGTH_SHORT).show();
		} else {
			
			Log.d(TAG, "updateEmail: re authenticating the user");
			
			AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), password);
			
			currentUser.reauthenticate(authCredential)
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								
								currentUser.updateEmail(email)
										.addOnCompleteListener(new OnCompleteListener<Void>() {
											@Override
											public void onComplete(@NonNull Task<Void> task) {
												if (task.isSuccessful()) {
													Log.d(TAG, "updateEmail: email change successful: " + currentUser.getEmail());
													
													user.setEmail(email);
													user.setEmailVerified(false);
													
													mDatabaseReference.child("users").child(currentUser.getUid()).setValue(user);
													
													currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
														@Override
														public void onComplete(@NonNull Task<Void> task) {
															if (task.isSuccessful()) {
																Log.d(TAG, "onComplete: verification email sent successfully");
																Toast.makeText(getContext(), "Email changed successfully", Toast.LENGTH_SHORT).show();
																
																SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SESSIONS, MODE_PRIVATE);
																SharedPreferences.Editor editor = sharedPreferences.edit();
																
																if (sharedPreferences.getBoolean(REMEMBER_ME_ENABLED, false)) {
																	editor.putString(REMEMBER_ME_EMAIL, email);
																	editor.apply();
																}
																
																emailTextInput.setEnabled(true);
																
																passwordTextInput.getEditText().setText("");
																passwordTextInput.setVisibility(View.GONE);
															} else {
																Log.d(TAG, "onComplete: verification email could not be sent: " + task.getException().getMessage());
															}
														}
													});
												} else {
													Toast.makeText(getContext(), "Could not update email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
												}
											}
										});
								
							} else {
								Toast.makeText(getContext(), "Could not update email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
		}
		
		enableViews();
	}
	
	private void updatePassword() {
		Log.d(TAG, "updatePassword: checking internet connection");
		Connection.checkConnection(this);
		
		disableViews();
		
		oldPassword = oldPasswordTextInput.getEditText().getText().toString().trim();
		newPassword = newPasswordTextInput.getEditText().getText().toString().trim();
		confirmPassword = confirmPasswordTextInput.getEditText().getText().toString().trim();
		
		if (oldPasswordTextInput.getVisibility() == View.GONE) {
			Log.d(TAG, "updatePassword: showing old password edit text");
			oldPasswordTextInput.setVisibility(View.VISIBLE);
			Toast.makeText(getContext(), "Enter the current password to change it", Toast.LENGTH_SHORT).show();
		} else {
			
			Log.d(TAG, "updatePassword: re authenticating the user");
			
			AuthCredential authCredential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), oldPassword);
			
			currentUser.reauthenticate(authCredential)
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Log.d(TAG, "onComplete: re authentication successful");
								
								if (newPasswordTextInput.getVisibility() == View.GONE && confirmPasswordTextInput.getVisibility() == View.GONE) {
									Log.d(TAG, "updatePassword: showing password edit text");
									newPasswordTextInput.setVisibility(View.VISIBLE);
									confirmPasswordTextInput.setVisibility(View.VISIBLE);
									Toast.makeText(getContext(), "Enter the new password", Toast.LENGTH_SHORT).show();
									oldPasswordTextInput.setEnabled(false);
									enableViews();
									return;
								} else if (user.validatePassword(newPassword, confirmPassword) != null || user.validateConfirmPassword(confirmPassword, newPassword) != null) {
									
									newPasswordTextInput.setError(user.validatePassword(newPassword, confirmPassword));
									confirmPasswordTextInput.setError(user.validateConfirmPassword(confirmPassword, newPassword));
									enableViews();
									return;
								}
								
								Log.d(TAG, "onComplete: changing password");
								
								currentUser.updatePassword(newPassword)
										.addOnCompleteListener(new OnCompleteListener<Void>() {
											@Override
											public void onComplete(@NonNull Task<Void> task) {
												if (task.isSuccessful()) {
													Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
													
													SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SESSIONS, MODE_PRIVATE);
													SharedPreferences.Editor editor = sharedPreferences.edit();
													
													if (sharedPreferences.getBoolean(REMEMBER_ME_ENABLED, false)) {
														editor.putString(REMEMBER_ME_PASSWORD, newPassword);
														editor.apply();
													}
													
													oldPasswordTextInput.setEnabled(true);
													
													oldPasswordTextInput.getEditText().setText("");
													newPasswordTextInput.getEditText().setText("");
													confirmPasswordTextInput.getEditText().setText("");
													
													oldPasswordTextInput.setVisibility(View.GONE);
													newPasswordTextInput.setVisibility(View.GONE);
													confirmPasswordTextInput.setVisibility(View.GONE);
												} else {
													Toast.makeText(getContext(), "Could not update password " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
												}
											}
										});
								
							} else {
								Toast.makeText(getContext(), "Could not update password. Please reenter the correct password", Toast.LENGTH_SHORT).show();
							}
						}
					});
			
		}
		
		enableViews();
	}
	
	private void uploadImage() {
		Log.d(TAG, "uploadImage: checking internet connection");
		Connection.checkConnection(this);
		
		Log.d(TAG, "uploadImage: uploading image");
		
		if (filePath != null) {
			final ProgressDialog progressDialog = new ProgressDialog(getContext());
			progressDialog.setTitle("Uploading image...");
			progressDialog.show();
			
			final StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + currentUser.getUid() + "_pp.jpg");
			
			UploadTask uploadTask = ref.putFile(filePath);
			
			Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
				@Override
				public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
					if (!task.isSuccessful()) {
						throw task.getException();
					}
					
					return ref.getDownloadUrl();
				}
			}).addOnSuccessListener(getActivity(), new OnSuccessListener<Uri>() {
				@Override
				public void onSuccess(Uri uri) {
					Log.d(TAG, "onSuccess: photo upload successful");
					if (uri != null) {
						Log.d(TAG, "onSuccess: image download uri is " + uri);
						UserProfileChangeRequest userProfileUpdate = new UserProfileChangeRequest.Builder()
								.setPhotoUri(uri)
								.build();
						currentUser.updateProfile(userProfileUpdate)
								.addOnSuccessListener(new OnSuccessListener<Void>() {
									@Override
									public void onSuccess(Void aVoid) {
										Log.d(TAG, "onSuccess: image saved successfully");
										
										NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
										ImageView profileImage = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileImage);
										if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
											Log.d(TAG, "onCreate: Downloading profile image");
											Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
													.error(Objects.requireNonNull(requireActivity().getDrawable(R.drawable.image_error_icon)))
													.placeholder(Objects.requireNonNull(requireActivity().getDrawable(R.drawable.image_loading_icon)))
													.into(profileImage);
										} else {
											Log.d(TAG, "onCreate: Image url does not exist for user");
											profileImage.setImageDrawable(requireActivity().getDrawable(R.drawable.image_error_icon));
										}
									}
								})
								.addOnFailureListener(new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {
										Log.d(TAG, "onFailure: image could not be uploaded");
									}
								});
						progressDialog.dismiss();
					}
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Log.d(TAG, "onFailure: photo upload failed");
					progressDialog.dismiss();
					Toast.makeText(getContext(),"Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	private void disableViews() {
		progressBar.setVisibility(View.VISIBLE);
		
		updateProfile.setClickable(false);
		updateEmail.setClickable(false);
		updatePassword.setClickable(false);
		cameraButton.setClickable(false);
		deleteAccount.setClickable(false);
		
		fullNameTextInput.setEnabled(false);
		usernameTextInput.setEnabled(false);
		passwordTextInput.setEnabled(false);
		newPasswordTextInput.setEnabled(false);
		confirmPasswordTextInput.setEnabled(false);
		deleteAccountPasswordTextInput.setEnabled(false);
	}
	
	private void enableViews() {
		progressBar.setVisibility(View.INVISIBLE);
		
		updateProfile.setClickable(true);
		updateEmail.setClickable(true);
		updatePassword.setClickable(true);
		cameraButton.setClickable(true);
		deleteAccount.setClickable(true);
		
		fullNameTextInput.setEnabled(true);
		usernameTextInput.setEnabled(true);
		passwordTextInput.setEnabled(true);
		newPasswordTextInput.setEnabled(true);
		confirmPasswordTextInput.setEnabled(true);
		deleteAccountPasswordTextInput.setEnabled(true);
	}
}