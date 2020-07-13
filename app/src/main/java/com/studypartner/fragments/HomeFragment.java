package com.studypartner.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.studypartner.R;

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
	private static final String TAG = "HomeFragment";
	
	public HomeFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		return inflater.inflate(R.layout.fragment_home, container, false);
	}
}