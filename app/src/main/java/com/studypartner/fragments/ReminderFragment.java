package com.studypartner.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

public class ReminderFragment extends Fragment {
	private static final String TAG = "ReminderFragment";
	
	public ReminderFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				MainActivity activity = (MainActivity) requireActivity();
				activity.mBottomAppBar.setVisibility(View.VISIBLE);
				activity.mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
				activity.fab.setVisibility(View.VISIBLE);
				activity.mNavController.navigate(R.id.action_nav_reminder_to_nav_home);
			}
		});
		
		return inflater.inflate(R.layout.fragment_reminder, container, false);
	}
}