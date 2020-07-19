package com.studypartner.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.models.FileItem;
import com.studypartner.utils.Connection;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;


public class BasicNotesFragment extends Fragment {
	private static final String TAG = "BasicNotesFragment";
	
	public BasicNotesFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		
		Log.d(TAG, "onCreateView: starts");
		
		Connection.checkConnection(this);
		
		View rootView = inflater.inflate(R.layout.fragment_basic_notes, container, false);
		final MainActivity activity = (MainActivity) requireActivity();
		activity.mBottomAppBar.bringToFront();
		activity.fab.bringToFront();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				activity.mNavController.navigate(R.id.action_basicNotesFragment_to_nav_notes);
			}
		});
		
		TextView text = rootView.findViewById(R.id.textView);
		FileItem FileDes = getArguments().getParcelable("FileDes");
		text.setText(FileDes.toString());
		return rootView;
	}
}