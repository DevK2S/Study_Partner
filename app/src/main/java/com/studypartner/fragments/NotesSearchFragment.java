package com.studypartner.fragments;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.SEARCH_SERVICE;

public class NotesSearchFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "NotesSearchFragment";
	
	public SearchView mSearchView;
	
	private ArrayList<FileItem> notes, notesCopy;
	
	private NotesAdapter mNotesAdapter;
	
	public NotesSearchFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_notes_search, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setHasOptionsMenu(true);
		
		if (getArguments() != null) {
			FileItem[] files = (FileItem[]) getArguments().getParcelableArray("NotesArray");
			assert files != null;
			notes = new ArrayList<>(Arrays.asList(files));
		}
		
		notesCopy = new ArrayList<>(notes);
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				NavHostFragment.findNavController(NotesSearchFragment.this).navigateUp();
			}
		});
		
		requireActivity().getSharedPreferences(requireActivity().getPackageName(), MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		((MainActivity) requireActivity()).mBottomAppBar.performHide();
		((MainActivity) requireActivity()).mBottomAppBar.setVisibility(View.GONE);
		((MainActivity) requireActivity()).fab.hide();
		
		RecyclerView recyclerView = view.findViewById(R.id.notesSearchRecyclerView);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		mNotesAdapter = new NotesAdapter(requireActivity(), notesCopy, this, false);
		recyclerView.setAdapter(mNotesAdapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onPause() {
		mSearchView.clearFocus();
		setHasOptionsMenu(false);
		super.onPause();
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.notes_menu_search, menu);
		
		SearchManager searchManager = (SearchManager) requireActivity().getSystemService(SEARCH_SERVICE);
		
		mSearchView = (SearchView) menu.findItem(R.id.notes_menu_search_frag).getActionView();
		SearchableInfo searchableInfo = searchManager.getSearchableInfo(requireActivity().getComponentName());
		mSearchView.setSearchableInfo(searchableInfo);
		mSearchView.setQueryHint("Enter the name of the file");
		mSearchView.setIconified(false);
		
		int searchPlateId = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		EditText searchPlate = mSearchView.findViewById(searchPlateId);
		searchPlate.setTextColor(getResources().getColor(R.color.colorAccent, requireActivity().getTheme()));
		searchPlate.setHintTextColor(getResources().getColor(R.color.colorAccent, requireActivity().getTheme()));
		
		mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				mNotesAdapter.filter(newText);
				return true;
			}
		});
		
		mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
			@Override
			public boolean onClose() {
				Log.d(TAG, "onClose: called");
				mSearchView.clearFocus();
				return false;
			}
		});
	}
	
	@Override
	public void onClick(int position) {
		
		FileItem fileDesc = notesCopy.get(position);
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("NOTES_SEARCH", MODE_PRIVATE);
		sharedPreferences.edit().putBoolean("NotesSearchExists", true).apply();
		sharedPreferences.edit().putString("NotesSearch", fileDesc.getPath()).apply();
		mSearchView.clearFocus();
		((MainActivity) requireActivity()).mNavController.navigateUp();
		
	}
	
	@Override
	public void onLongClick(int position) {
	
	}
	
	@Override
	public void onOptionsClick(View view, int position) {
	
	}
}