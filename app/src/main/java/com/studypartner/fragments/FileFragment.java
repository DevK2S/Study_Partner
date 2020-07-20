package com.studypartner.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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


public class FileFragment extends Fragment {
	private static final String TAG = "BasicNotesFragment";
	private FloatingActionButton fab;
	private RecyclerView recyclerView;
	private File noteFolder;
	
	private NotesAdapter mNotesAdapter;
	
	private ArrayList<FileItem> notes = new ArrayList<>();
	
	public FileFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		
		Log.d(TAG, "onCreateView: starts");
		
		Connection.checkConnection(this);
		
		final View rootView = inflater.inflate(R.layout.fragment_file, container, false);
		
		FileItem fileDesc = getArguments().getParcelable("FileDes");
		
		noteFolder = new File(String.valueOf(fileDesc.getPath()));
		
		File[] files = noteFolder.listFiles();
		
		if (files != null && files.length > 0) {
			notes = new ArrayList<>();
			for (File f : files)
				notes.add(new FileItem(f.getPath(), f.getName(), FileItem.FileType.FILE_TYPE_FOLDER));
		}
		
		final MainActivity activity = (MainActivity) requireActivity();
		activity.fab.hide();
		activity.mBottomAppBar.performHide();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				activity.mNavController.navigateUp();
			}
		});
		
		fab = rootView.findViewById(R.id.fileFab);
		
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "onClick: fab onclick called");
				final BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
				bottomSheet.setDismissWithAnimation(true);
				bottomSheet.setContentView(R.layout.bottom_sheet_notes);
				LinearLayout addFolder = bottomSheet.findViewById(R.id.addFolder);
				LinearLayout addFile = bottomSheet.findViewById(R.id.addFile);
				LinearLayout addImage = bottomSheet.findViewById(R.id.addImage);
				LinearLayout addVideo = bottomSheet.findViewById(R.id.addVideo);
				LinearLayout addCamera = bottomSheet.findViewById(R.id.addCamera);
				LinearLayout addNote = bottomSheet.findViewById(R.id.addNote);
				LinearLayout addLink = bottomSheet.findViewById(R.id.addLink);
				LinearLayout addAudio = bottomSheet.findViewById(R.id.addAudio);
				LinearLayout addVoice = bottomSheet.findViewById(R.id.addVoice);
				
				addFolder.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						addFolder();
						bottomSheet.dismiss();
					}
				});
				addFile.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Files", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				addImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Images", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				addVideo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Videos", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				addCamera.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Camera", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				addNote.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Note", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				addLink.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Link", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				addAudio.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Audio", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				addVoice.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Voice", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				
				bottomSheet.show();
			}
		});
		
		recyclerView = rootView.findViewById(R.id.fileRecyclerView);
		
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
					((MainActivity) requireActivity()).mNavController.navigate(R.id.action_basicNotesFragment_self, bundle);
				}
			}
			
			@Override
			public void onLongClick(View view, int position) {
				File file = new File(notes.get(position).getPath());
				deleteRecursive(file);
				notes.remove(position);
				mNotesAdapter.notifyItemRemoved(position);
			}
		}));
		
		return rootView;
	}
	
	void addFolder() {
		String newFolder = UUID.randomUUID().toString().substring(0, 3);
		File file = new File(noteFolder, newFolder);
		if (file.mkdirs()) {
			notes.add(new FileItem(file.getPath(), file.getName(), FileItem.FileType.FILE_TYPE_FOLDER));
			Log.d(TAG, "addFolder: made folder");
		}
		mNotesAdapter.notifyItemInserted(notes.size());
	}
	
	public void deleteRecursive(File fileOrDirectory) {
		
		if (fileOrDirectory.isDirectory()) {
			for (File child : fileOrDirectory.listFiles()) {
				deleteRecursive(child);
			}
		}
		
		Log.d(TAG, "deleteRecursive: " + fileOrDirectory.delete());
	}
	
}