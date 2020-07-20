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
import java.util.Objects;
import java.util.UUID;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FileFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "BasicNotesFragment";
	private RecyclerView recyclerView;
	private FloatingActionButton fab;
	private File noteFolder;
	
	private Toolbar mToolbar;
	
	private NotesAdapter mNotesAdapter;
	
	private ActionMode actionMode;
	private boolean actionModeOn = false;
	
	private ArrayList<FileItem> notes = new ArrayList<>();
	
	public FileFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateView: starts");
		
		Connection.checkConnection(this);
		
		final View rootView = inflater.inflate(R.layout.fragment_file, container, false);
		
		FileItem fileDesc;
		
		if (getArguments() != null) {
			fileDesc = getArguments().getParcelable("FileDes");
			if (fileDesc != null) {
				noteFolder = new File(String.valueOf(fileDesc.getPath()));
			}
		}
		
		final MainActivity activity = (MainActivity) requireActivity();
		activity.fab.hide();
		activity.mBottomAppBar.performHide();
		
		mToolbar = activity.mToolbar;
		
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
				
				assert addFolder != null;
				addFolder.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						addFolder();
						bottomSheet.dismiss();
					}
				});
				assert addFile != null;
				addFile.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Files", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				assert addImage != null;
				addImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Images", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				assert addVideo != null;
				addVideo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Videos", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				assert addCamera != null;
				addCamera.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Camera", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				assert addNote != null;
				addNote.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Note", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				assert addLink != null;
				addLink.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Link", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				assert addAudio != null;
				addAudio.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getContext(), "Audio", Toast.LENGTH_SHORT).show();
						bottomSheet.dismiss();
					}
				});
				assert addVoice != null;
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
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		
		populateDataAndSetAdapter();
		
		return rootView;
	}
	
	@Override
	public void onClick(int position) {
		if (actionModeOn) {
			enableActionMode(position);
		} else if (notes.get(position).getType().equals(FileItem.FileType.FILE_TYPE_FOLDER)) {
			FileItem fileDesc = notes.get(position);
			Bundle bundle = new Bundle();
			bundle.putParcelable("FileDes", fileDesc);
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_basicNotesFragment_self, bundle);
		}
	}
	
	@Override
	public void onLongClick(int position) {
		enableActionMode(position);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		fab.show();
	}
	
	@Override
	public void onPause() {
		if (actionMode != null) {
			actionMode.finish();
		}
		Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
		super.onPause();
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
					fab.hide();
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
					Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
					fab.show();
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