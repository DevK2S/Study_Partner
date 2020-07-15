package com.studypartner.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;

import java.io.File;
import java.util.ArrayList;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotesFragment extends Fragment {
	private static final String TAG = "NotesFragment";
	
	private RecyclerView recyclerView;
	
	public NotesFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		View rootView = inflater.inflate(R.layout.fragment_notes, container, false);
		
		final MainActivity activity = (MainActivity) requireActivity();
		activity.mBottomAppBar.bringToFront();
		activity.fab.bringToFront();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				activity.mNavController.navigate(R.id.action_nav_notes_to_nav_home);
			}
		});
		
		recyclerView = rootView.findViewById(R.id.recyclerView);
		
		displayFolder();
		
		return rootView;
	}
	
	void displayFolder() {
		
		File file = new File(String.valueOf(getContext().getExternalFilesDir(null)), "Folders");
		File test = new File(String.valueOf(getContext().getExternalFilesDir(null)), "Folders/test");
		File test2 = new File(String.valueOf(getContext().getExternalFilesDir(null)), "Folders/test2");
		File test3 = new File(String.valueOf(getContext().getFilesDir()), "test3");
		File test4 = new File(String.valueOf(getContext().getExternalMediaDirs()[0]), "test4");
		
		test.mkdirs();
		
		test2.mkdirs();
		
		test3.mkdirs();
		
		test4.mkdirs();
		
		File[] f = file.listFiles();
		
		ArrayList<String> subjects = new ArrayList<>();
		
		if (f != null && f.length > 0) {
			
			for (File value : f) subjects.add(value.getName());
			subjects.add(test3.getName());
			subjects.add(test4.getName());
			
			NotesAdapter adapter = new NotesAdapter(getContext(), subjects);
			recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
			recyclerView.setAdapter(adapter);
		}
	}
}
