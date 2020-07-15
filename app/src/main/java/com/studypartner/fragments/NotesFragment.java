package com.studypartner.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.customRecyclerAdapter;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


public class NotesFragment extends Fragment {
	private static final String TAG = "NotesFragment";
	private  RecyclerView recyclerView;

	public NotesFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		View rootView = inflater.inflate(R.layout.fragment_notes,container, false);
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				final MainActivity activity = (MainActivity) requireActivity();
				activity.mNavController.navigate(R.id.action_nav_notes_to_nav_home);
			}
		});
		//TextView lg = rootView.findViewById(R.id.textView);
		recyclerView = rootView.findViewById(R.id.recyclerView);
		displayFolder();

		return rootView;
	}
	void displayFolder()
	{
		File file = new File(String.valueOf(getActivity().getExternalFilesDir(null)),"Folders");
		File test =new File(String.valueOf(getActivity().getExternalFilesDir(null)),"Folders/test");
		File test2 =new File(String.valueOf(getActivity().getExternalFilesDir(null)),"Folders/test2");
		test.mkdirs();
		test2.mkdirs();
		File f[]=file.listFiles();
		ArrayList<String> subjects=new ArrayList<>();
		if(f.length>0) {
		for(int i=0;i<f.length;i++)
			subjects.add(f[i].getName());
		//System.out.println(file);

			customRecyclerAdapter adapter = new customRecyclerAdapter(getActivity(), subjects);
			recyclerView.setAdapter(adapter);
			recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		}
	}
}
