package com.studypartner.models;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import androidx.annotation.NonNull;

public class User {
	String fullName, username, email;
	Boolean isEmailVerified;
	
	public User(String fullName, String email, Boolean isEmailVerified) {
		this.fullName = fullName;
		this.email = email;
		this.isEmailVerified = isEmailVerified;
		generateUsername();
	}
	
	public User(String fullName, String username, String email, Boolean isEmailVerified) {
		this.fullName = fullName;
		this.username = username;
		this.email = email;
		this.isEmailVerified = isEmailVerified;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Boolean getEmailVerified() {
		return isEmailVerified;
	}
	
	public void setEmailVerified(Boolean emailVerified) {
		isEmailVerified = emailVerified;
	}
	
	public void generateUsername() {
		String newEmail = email.substring(0, email.indexOf("@"));
		final boolean[] uniqueUsername = new boolean[1];
		String username;
		do {
			uniqueUsername[0] = true;
			username = newEmail + (new Random().nextInt(1000));
			final String finalUsername = username;
			FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot snapshot) {
					for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
						for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
							if ("username".equals(dataSnapshot1.getKey()) && finalUsername.matches((String) dataSnapshot1.getValue())) {
								uniqueUsername[0] = false;
							}
						}
					}
				}
				
				@Override
				public void onCancelled(@NonNull DatabaseError error) {
				
				}
			});
		} while (!uniqueUsername[0]);
		
		this.username = username;
	}
}
