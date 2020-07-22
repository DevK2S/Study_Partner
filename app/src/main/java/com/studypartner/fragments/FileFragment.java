package com.studypartner.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;
import com.studypartner.utils.Connection;
import com.studypartner.utils.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class FileFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "BasicNotesFragment";
	
	private final String SORT_BY_NAME = "By Name";
	private final String SORT_BY_SIZE = "By Size";
	private final String SORT_BY_CREATION_TIME = "By Creation Time";
	private final String SORT_BY_MODIFIED_TIME = "By Modification Time";
	
	private final String ASCENDING_ORDER = "Ascending Order";
	private final String DESCENDING_ORDER = "Descending Order";
	
	private String sortBy = SORT_BY_NAME;
	private String sortOrder = ASCENDING_ORDER;
	
	private RecyclerView recyclerView;
	private FloatingActionButton fab;
	private File noteFolder;
	private LinearLayout mLinearLayout;
	private TextView sortText;
	private ImageButton sortOrderButton, sortByButton;
	
	private NotesAdapter mNotesAdapter;
	
	private ActionMode actionMode;
	private boolean actionModeOn = false;
	
	private ArrayList<FileItem> notes = new ArrayList<>();
	
	public FileFragment() {
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requireActivity().getSharedPreferences("NOTES_SEARCH", MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("NOTES_SORTING", MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		if (sharedPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sharedPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sharedPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
		} else {
			editor.putBoolean("SORTING_ORDER_EXISTS", true);
			editor.putString("SORTING_BY", SORT_BY_NAME);
			editor.putString("SORTING_ORDER", ASCENDING_ORDER);
			editor.apply();
			sortBy = SORT_BY_NAME;
			sortOrder = ASCENDING_ORDER;
		}
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateView: starts");
		
		Connection.checkConnection(this);
		
		final View rootView = inflater.inflate(R.layout.fragment_file, container, false);
		
		if (getArguments() != null) {
			String filePath = getArguments().getString("FilePath");
			if (filePath != null) {
				noteFolder = new File(filePath);
			}
		}
		
		final MainActivity activity = (MainActivity) requireActivity();
		activity.fab.hide();
		activity.mBottomAppBar.performHide();
		activity.mBottomAppBar.setVisibility(View.GONE);
		
		Toolbar toolbar = activity.mToolbar;
		
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
		mLinearLayout = rootView.findViewById(R.id.fileLinearLayout);
		sortText = rootView.findViewById(R.id.fileSortText);
		sortOrderButton = rootView.findViewById(R.id.fileSortOrder);
		sortByButton = rootView.findViewById(R.id.fileSortButton);
		
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("NOTES_SORTING", MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		
		sortText.setText(sortBy);
		
		if (sortOrder.equals(ASCENDING_ORDER)) {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
		} else {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
		}
		
		sortOrderButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sortOrder.equals(ASCENDING_ORDER)) {
					sortOrder = DESCENDING_ORDER;
					sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
				} else {
					sortOrder = ASCENDING_ORDER;
					sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
				}
				sort(sortText.getText().toString(), sortOrder.equals(ASCENDING_ORDER));
				editor.putString("SORTING_ORDER", sortOrder).apply();
			}
		});
		
		sortByButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
				
				View dialogView = getLayoutInflater().inflate(R.layout.notes_sort_layout, null);
				
				Button okButton = dialogView.findViewById(R.id.sortByOkButton);
				Button cancelButton = dialogView.findViewById(R.id.sortByCancelButton);
				final RadioGroup radioGroup = dialogView.findViewById(R.id.sortByRadioGroup);
				radioGroup.clearCheck();
				
				switch (sortBy) {
					case SORT_BY_SIZE:
						radioGroup.check(R.id.sortBySizeRB);
						break;
					case SORT_BY_CREATION_TIME:
						radioGroup.check(R.id.sortByCreationTimeRB);
						break;
					case SORT_BY_MODIFIED_TIME:
						radioGroup.check(R.id.sortByModifiedTimeRB);
						break;
					default:
						radioGroup.check(R.id.sortByNameRB);
						break;
				}
				
				cancelButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG, "onClick: cancel pressed while changing subject");
						builder.dismiss();
					}
				});
				
				okButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (radioGroup.getCheckedRadioButtonId()) {
							case R.id.sortBySizeRB:
								sortBy = SORT_BY_SIZE;
								break;
							case R.id.sortByCreationTimeRB:
								sortBy = SORT_BY_CREATION_TIME;
								break;
							case R.id.sortByModifiedTimeRB:
								sortBy = SORT_BY_MODIFIED_TIME;
								break;
							default:
								sortBy = SORT_BY_NAME;
								break;
						}
						sortText.setText(sortBy);
						editor.putString("SORTING_BY", sortBy).apply();
						sort(sortText.getText().toString(), sortOrder.equals(ASCENDING_ORDER));
						builder.dismiss();
					}
				});
				
				builder.setView(dialogView);
				builder.show();
			}
		});
		
		toolbar.setTitle(getTitle());
		
		populateDataAndSetAdapter();
		
		return rootView;
	}
	
	@Override
	public void onClick(int position) {
		if (actionModeOn) {
			enableActionMode(position);
		} else if (notes.get(position).getType().equals(FileType.FILE_TYPE_FOLDER)) {
			FileItem fileDesc = notes.get(position);
			Bundle bundle = new Bundle();
			bundle.putString("FilePath", fileDesc.getPath());
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_self, bundle);
		}
	}
	
	@Override
	public void onLongClick(int position) {
		enableActionMode(position);
	}
	
	@Override
	public void onOptionsClick(View view, final int position) {
		PopupMenu popup = new PopupMenu(getContext(), view);
		popup.inflate(R.menu.notes_item_menu);
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					
					case R.id.notes_item_rename:
						
						AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
						alertDialog.setMessage("Enter a new name");
						
						final FileItem fileItem = notes.get(position);
						
						final EditText input = new EditText(getContext());
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.MATCH_PARENT);
						input.setLayoutParams(lp);
						input.setText(fileItem.getName());
						alertDialog.setView(input);
						
						alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								String newName = input.getText().toString().trim();
								File oldFile = new File(fileItem.getPath());
								File newFile = new File(noteFolder, newName);
								if (newName.equals(fileItem.getName()) || newName.equals("")) {
									Log.d(TAG, "onClick: filename not changed");
								} else if (newFile.exists()) {
									Toast.makeText(getContext(), "File with this name already exists", Toast.LENGTH_SHORT).show();
								} else {
									if (oldFile.renameTo(newFile)) {
										Toast.makeText(getContext(), "File renamed successfully", Toast.LENGTH_SHORT).show();
										notes.get(position).setName(newName);
										mNotesAdapter.notifyItemChanged(position);
										sort(sortText.getText().toString(), sortOrder.equals(ASCENDING_ORDER));
									} else {
										Toast.makeText(getContext(), "File could not be renamed", Toast.LENGTH_SHORT).show();
									}
								}
							}
						});
						
						alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
						
						alertDialog.show();
						
						return true;
					
					case R.id.notes_item_delete:
						
						final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setTitle("Delete File");
						builder.setMessage("Are you sure you want to delete the file?");
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								File file = new File(notes.get(position).getPath());
								deleteRecursive(file);
								mNotesAdapter.notifyItemRemoved(position);
								notes.remove(position);
							}
						});
						builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							
							}
						});
						builder.show();
						return true;
					
					case R.id.notes_item_share:
						
						if (notes.get(position).getType() != FileType.FILE_TYPE_FOLDER) {
							Intent intentShareFile = new Intent(Intent.ACTION_SEND);
							File shareFile = new File(notes.get(position).getPath());
							
							if(shareFile.exists()) {
								intentShareFile.setType("application/pdf");
								intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + notes.get(position).getPath()));
								intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File");
								intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing " + notes.get(position).getName());
								startActivity(Intent.createChooser(intentShareFile, "Share File"));
							}
						} else {
							Toast.makeText(getContext(), "Folder cannot be shared", Toast.LENGTH_SHORT).show();
						}
						return true;
					default:
						return false;
				}
			}
		});
		popup.show();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("NOTES_SEARCH", MODE_PRIVATE);
		
		if (sharedPreferences.getBoolean("NotesSearchExists", false)) {
			File searchedFile = new File(sharedPreferences.getString("NotesSearch", null));
			FileItem fileDesc = new FileItem(searchedFile.getPath());
			if (fileDesc.getType() == FileType.FILE_TYPE_FOLDER) {
				Bundle bundle = new Bundle();
				bundle.putString("FilePath", fileDesc.getPath());
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_self, bundle);
			}
		}
		setHasOptionsMenu(true);
		
		fab.show();
	}
	
	@Override
	public void onPause() {
		if (actionMode != null) {
			actionMode.finish();
		}
		Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
		
		setHasOptionsMenu(false);
		
		super.onPause();
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.notes_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected: starts");
		switch (item.getItemId()) {
			case R.id.notes_menu_refresh:
				populateDataAndSetAdapter();
				return true;
			case R.id.notes_menu_search:
				Bundle bundle = new Bundle();
				FileItem[] files = new FileItem[notes.size()];
				files = notes.toArray(files);
				bundle.putParcelableArray("NotesArray", files);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_to_notesSearchFragment, bundle);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void populateDataAndSetAdapter() {
		File[] files = noteFolder.listFiles();
		
		if (files != null && files.length > 0) {
			notes = new ArrayList<>();
			for (File f : files)
				notes.add(new FileItem(f.getPath()));
		}
		
		mNotesAdapter = new NotesAdapter(getContext(), notes, this, true);
		
		sort(sortText.getText().toString(), sortOrder.equals(ASCENDING_ORDER));
		
		recyclerView.setAdapter(mNotesAdapter);
	}
	
	void addFolder() {
		File file;
		
		do {
			String newFolder = UUID.randomUUID().toString().substring(0, 5);
			file = new File(noteFolder, newFolder);
		} while (file.exists());
		
		if (file.mkdirs())
			notes.add(new FileItem(file.getPath()));
		mNotesAdapter.notifyItemInserted(notes.size());
		
		sort(sortText.getText().toString(), sortOrder.equals(ASCENDING_ORDER));
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
					mNotesAdapter.notifyItemRemoved(selectedItemPositions.get(i));
					notes.remove(selectedItemPositions.get(i).intValue());
				}
				
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
					mLinearLayout.setVisibility(View.GONE);
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
					mLinearLayout.setVisibility(View.VISIBLE);
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
	
	private String getTitle() {
		
		String title = "Notes";
		
		FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
		
		if (firebaseUser != null && firebaseUser.getEmail() != null) {
			title = noteFolder.getPath().substring(noteFolder.getPath().indexOf(firebaseUser.getEmail()) + firebaseUser.getEmail().length() + 1);
		}
		
		return title.length() > 15 ? "..." + title.substring(title.length() - 12) : title;
	}
	
	private void sort (String text, boolean ascending) {
		switch (text) {
			case SORT_BY_SIZE:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return (int) ((o1.getSize() / 1048576) - (o2.getSize() / 1048576));
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return (int) ((o2.getSize() / 1048576) - (o1.getSize() / 1048576));
						}
					});
				}
				
				break;
			case SORT_BY_CREATION_TIME:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateCreated().compareTo(o2.getDateCreated());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateCreated().compareTo(o1.getDateCreated());
						}
					});
				}
				
				break;
			case SORT_BY_MODIFIED_TIME:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateModified().compareTo(o2.getDateModified());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateModified().compareTo(o1.getDateModified());
						}
					});
				}
				
				break;
			case SORT_BY_NAME:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getName().compareTo(o1.getName());
						}
					});
				}
				break;
		}
		
		mNotesAdapter.notifyDataSetChanged();
	}
	
}