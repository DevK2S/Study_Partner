package com.studypartner.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.UUID;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotesFragment extends Fragment {
<<<<<<< HEAD
	private static final String TAG = "NotesFragment";
	private FloatingActionButton fab;
	private RecyclerView recyclerView;
	private File noteFolder;
	
	private NotesAdapter mNotesAdapter;
	
	private MainActivity activity;
	
	private ArrayList<FileItem> notes = new ArrayList<>();
	
	public NotesFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		Connection.checkConnection(this);
		
		View rootView = inflater.inflate(R.layout.fragment_notes, container, false);
		
		noteFolder = new File(String.valueOf(getContext().getExternalFilesDir(null)), "Folders");
		
		File[] files = noteFolder.listFiles();
		
		if (files != null && files.length > 0) {
			notes = new ArrayList<>();
			for (File f : files)
				notes.add(new FileItem(f.getPath(), f.getName(), FileItem.FileType.FILE_TYPE_FOLDER));
		}
		
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
		
		mNotesAdapter = new NotesAdapter(getContext(), notes);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(mNotesAdapter);
		recyclerView.addOnItemTouchListener(new NotesAdapter.NotesItemTouchListener(getContext(), recyclerView, new NotesAdapter.NotesClickListener() {
			@Override
			public void onClick(View view, final int position) {
				if (notes.get(position).getType().equals(FileItem.FileType.FILE_TYPE_FOLDER)) {
					FileItem fileDesc = notes.get(position);
					Bundle bundle = new Bundle();
					bundle.putParcelable("FileDes", fileDesc);
					((MainActivity)requireActivity()).mNavController.navigate(R.id.action_nav_notes_to_basicNotesFragment, bundle);
				}
			}
			
			@Override
			public void onLongClick(View view, int position) {
				File file = new File(notes.get(position).getPath());
				notes.remove(position);

				Log.d(TAG, "onClick: deletion: " + file.delete());
				mNotesAdapter.notifyItemRemoved(position);
			}
		}));
		
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
		super.onPause();
	}
	
	void addFolder() {
		String newFolder = UUID.randomUUID().toString().substring(0, 3);
		File file = new File(noteFolder, newFolder);
		if (file.mkdirs()) notes.add(new FileItem(file.getPath(), file.getName(), FileItem.FileType.FILE_TYPE_FOLDER));
		mNotesAdapter.notifyItemInserted(notes.size());
	}
=======
    private static final String TAG = "NotesFragment";
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private File noteFolder;

    private NotesAdapter mNotesAdapter;

    private MainActivity activity;

    private ArrayList<FileItem> notes = new ArrayList<>();

    public NotesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: starts");

        Connection.checkConnection(this);

        View rootView = inflater.inflate(R.layout.fragment_notes, container, false);

        noteFolder = new File(String.valueOf(getContext().getExternalFilesDir(null)), "Folders");

        File[] files = noteFolder.listFiles();

        if (files != null && files.length > 0) {
            notes = new ArrayList<>();
            for (File f : files)
                notes.add(new FileItem(f.getPath(), f.getName(), FileItem.FileType.FILE_TYPE_FOLDER));
        }

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

        mNotesAdapter = new NotesAdapter(getContext(), notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mNotesAdapter);
        recyclerView.addOnItemTouchListener(new NotesAdapter.NotesItemTouchListener(getContext(), recyclerView, new NotesAdapter.NotesClickListener() {
            @Override
            public void onClick(View view, final int position) {
                if (notes.get(position).getType().equals(FileItem.FileType.FILE_TYPE_FOLDER)) {
                    FileItem fileDesc = notes.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("FileDes", fileDesc);
                    ((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_notes_to_basicNotesFragment, bundle);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                File file = new File(notes.get(position).getPath());
                deleteRecursive(file);
                Log.d(TAG, "onClick: deletion: " + file.delete());
                mNotesAdapter.notifyItemRemoved(position);
            }
        }));

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
        super.onPause();
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
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

>>>>>>> parent of f1fc994... Revert "BasicNotesFragment,FileItem,PassArgs"
}
