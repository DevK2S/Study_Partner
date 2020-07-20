package com.studypartner.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;
import com.studypartner.utils.Connection;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotesFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "NotesFragment";
	
	private FloatingActionButton fab;
	private RecyclerView recyclerView;
	private File noteFolder;
	
	private NotesAdapter mNotesAdapter;
	
	private MainActivity activity;
	
	private ActionMode actionMode;
	private boolean actionModeOn = false;
	
	private ArrayList<FileItem> notes = new ArrayList<>();
	
	public NotesFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		Connection.checkConnection(this);
		
		View rootView = inflater.inflate(R.layout.fragment_notes, container, false);
		
		noteFolder = new File(String.valueOf(requireContext().getExternalFilesDir(null)), "Folders");
		
		activity = (MainActivity) requireActivity();
		activity.mBottomAppBar.bringToFront();
		activity.fab.bringToFront();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				fab.setOnClickListener(null);
				activity.mNavController.navigate(R.id.action_nav_notes_to_nav_home);
			}
		});
		
		fab = activity.fab;
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "onClick: fab onclick called");
				addFolder();
			}
		});
		
		recyclerView = rootView.findViewById(R.id.notesRecyclerView);
		
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		
		populateDataAndSetAdapter();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		activity.mBottomAppBar.performShow();
		activity.mBottomAppBar.bringToFront();
		activity.fab.show();
		activity.fab.bringToFront();
	}
	
	@Override
	public void onPause() {
		fab.setOnClickListener(null);
		if (actionMode != null) {
			actionMode.finish();
		}
		fab.setEnabled(true);
		Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
		super.onPause();
	}
	
	@Override
	public void onClick(int position) {
		if (actionModeOn) {
			enableActionMode(position);
		} else if (notes.get(position).getType().equals(FileItem.FileType.FILE_TYPE_FOLDER)) {
			FileItem fileDesc = notes.get(position);
			Bundle bundle = new Bundle();
			bundle.putParcelable("FileDes", fileDesc);
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_notes_to_basicNotesFragment, bundle);
		}
	}
	
	@Override
	public void onLongClick(int position) {
		enableActionMode(position);
	}
	
	private void populateDataAndSetAdapter() {
		File[] files = noteFolder.listFiles();
		
		if (files != null && files.length > 0) {
			notes = new ArrayList<>();
			for (File f : files)
				notes.add(new FileItem(f.getPath(), f.getName(), FileItem.FileType.FILE_TYPE_FOLDER));
		}
		
		mNotesAdapter = new NotesAdapter(getContext(), notes, this);
		recyclerView.setAdapter(mNotesAdapter);
	}
	
	void addFolder() {
		String newFolder = UUID.randomUUID().toString().substring(0, 3);
		File file = new File(noteFolder, newFolder);
		if (file.mkdirs())
			notes.add(new FileItem(file.getPath(), file.getName(), FileItem.FileType.FILE_TYPE_FOLDER));
		mNotesAdapter.notifyItemInserted(notes.size());
	}
	
	public void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
				deleteRecursive(child);
			}
		}
		
		Log.d(TAG, "deleteRecursive: " + fileOrDirectory.delete());
	}
	
	private void deleteRows() {
		final ArrayList<Integer> selectedItemPositions = mNotesAdapter.getSelectedItems();
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Delete Files");
		builder.setMessage("Are you sure you want to delete " + selectedItemPositions.size() + " files?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
					File file = new File(notes.get(selectedItemPositions.get(i)).getPath());
					deleteRecursive(file);
					mNotesAdapter.removeData(selectedItemPositions.get(i));
				}
				
				mNotesAdapter.notifyDataSetChanged();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			
			}
		});
		builder.show();
		
		actionMode = null;
	}
	
	private void enableActionMode(int position) {
		if (actionMode == null) {
			
			actionMode = requireActivity().startActionMode(new android.view.ActionMode.Callback2() {
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					mode.getMenuInflater().inflate(R.menu.menu_notes_action_mode, menu);
					actionModeOn = true;
					fab.setEnabled(false);
					Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).hide();
					return true;
				}
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
						case R.id.notes_action_refresh:
							mNotesAdapter.notifyDataSetChanged();
							mode.finish();
							return true;
						case R.id.notes_action_delete:
							deleteRows();
							mode.finish();
							return true;
						case R.id.notes_action_select_all:
							selectAll();
							return true;
						default:
							return false;
					}
				}
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					mNotesAdapter.clearSelections();
					actionModeOn = false;
					actionMode = null;
					fab.setEnabled(true);
					Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
				}
			});
		}
		toggleSelection(position);
	}
	
	private void toggleSelection(int position) {
		mNotesAdapter.toggleSelection(position);
		int count = mNotesAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
			actionMode = null;
		} else {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
	}
	
	private void selectAll() {
		mNotesAdapter.selectAll();
		int count = mNotesAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
		} else if (actionMode != null) {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
		
		actionMode = null;
	}
}
