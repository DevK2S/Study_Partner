package com.studypartner.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class NotesFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "NotesFragment";
	
	private final String SORT_BY_NAME = "By Name";
	private final String SORT_BY_SIZE = "By Size";
	private final String SORT_BY_CREATION_TIME = "By Creation Time";
	private final String SORT_BY_MODIFIED_TIME = "By Modification Time";
	
	private final String ASCENDING_ORDER = "Ascending Order";
	private final String DESCENDING_ORDER = "Descending Order";
	
	private String sortBy;
	private String sortOrder;
	
	private LinearLayout mEmptyLayout;
	private FloatingActionButton fab;
	private RecyclerView recyclerView;
	private File noteFolder;
	private LinearLayout mLinearLayout;
	private TextView sortText;
	private ImageButton sortOrderButton, sortByButton;
	
	private NotesAdapter mNotesAdapter;
	
	private MainActivity activity;
	
	private ActionMode actionMode;
	private boolean actionModeOn = false;
	
	private ArrayList<FileItem> notes = new ArrayList<>();
	private ArrayList<FileItem> starred = new ArrayList<>();
	private ArrayList<FileItem> links = new ArrayList<>();
	
	public NotesFragment() {
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 1) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				populateDataAndSetAdapter();
				addFolder();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
				builder.setTitle("Read and Write Permissions");
				builder.setMessage("Read and write permissions are required to store notes in the app");
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "onClick: closing app");
					}
				});
				builder.show();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		SharedPreferences sortPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		SharedPreferences.Editor editor = sortPreferences.edit();
		
		if (sortPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sortPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sortPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		View rootView = inflater.inflate(R.layout.fragment_notes, container, false);
		
		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		activity = (MainActivity) requireActivity();
		
		if (firebaseUser != null) {
			File studyPartnerFolder = new File(String.valueOf(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(requireContext().getExternalFilesDir(null)).getParentFile()).getParentFile()).getParentFile()).getParentFile()), "StudyPartner");
			if (!studyPartnerFolder.exists()) {
				if (studyPartnerFolder.mkdirs()) {
					noteFolder = new File(studyPartnerFolder, firebaseUser.getUid());
				} else {
					noteFolder = new File(requireContext().getExternalFilesDir(null), firebaseUser.getUid());
				}
			} else {
				noteFolder = new File(studyPartnerFolder, firebaseUser.getUid());
			}
		} else {
			FirebaseAuth.getInstance().signOut();
			
			GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
					.requestIdToken(getString(R.string.default_web_client_id))
					.requestEmail()
					.build();
			GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
			googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					if (task.isSuccessful()) {
						activity.mNavController.navigate(R.id.nav_logout);
						activity.finishAffinity();
						activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
					} else {
						activity.finishAffinity();
					}
				}
			});
		}
		
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
		
		mEmptyLayout = rootView.findViewById(R.id.notesEmptyLayout);
		recyclerView = rootView.findViewById(R.id.notesRecyclerView);
		mLinearLayout = rootView.findViewById(R.id.notesLinearLayout);
		sortText = rootView.findViewById(R.id.notesSortText);
		sortOrderButton = rootView.findViewById(R.id.notesSortOrder);
		sortByButton = rootView.findViewById(R.id.notesSortButton);
		
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
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
				editor.putString("SORTING_ORDER", sortOrder).apply();
				sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
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
						sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
						builder.dismiss();
					}
				});
				
				builder.setView(dialogView);
				builder.show();
			}
		});
		
		populateDataAndSetAdapter();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE);
		
		if (sharedPreferences.getBoolean("NotesSearchExists", false)) {
			File searchedFile = new File(sharedPreferences.getString("NotesSearch", null));
			FileItem fileDesc = new FileItem(searchedFile.getPath());
			if (searchedFile.isDirectory()) {
				Bundle bundle = new Bundle();
				bundle.putString("FilePath", fileDesc.getPath());
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_notes_to_fileFragment, bundle);
			}
		}
		
		SharedPreferences sortPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		
		if (sortPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sortPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sortPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
		}
		
		sortText.setText(sortBy);
		
		if (sortOrder.equals(ASCENDING_ORDER)) {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
		} else {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
		}
		
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		
		if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = starredPreference.getString("STARRED_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			starred = gson.fromJson(json, type);
			
			if (starred == null) starred = new ArrayList<>();
		}
		
		ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
		for (FileItem starItem : starred) {
			File starFile = new File(starItem.getPath());
			if (starItem.getType() == FileType.FILE_TYPE_LINK) {
				if (starFile.getParentFile() == null || !starFile.getParentFile().exists()) {
					starredToBeRemoved.add(starItem);
				}
			} else {
				if (!starFile.exists()) {
					starredToBeRemoved.add(starItem);
				}
			}
		}
		starred.removeAll(starredToBeRemoved);
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			links = gson.fromJson(json, type);
			
			if (links == null) links = new ArrayList<>();
		}
		
		ArrayList<FileItem> linkToBeRemoved = new ArrayList<>();
		for (FileItem linkItem : links) {
			File linkParent = new File(linkItem.getPath()).getParentFile();
			assert linkParent != null;
			if (!linkParent.exists()) {
				linkToBeRemoved.add(linkItem);
			}
		}
		links.removeAll(linkToBeRemoved);
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
		setHasOptionsMenu(true);
		activity.mBottomAppBar.performShow();
		activity.mBottomAppBar.setVisibility(View.VISIBLE);
		activity.mBottomAppBar.bringToFront();
		activity.fab.show();
		activity.fab.bringToFront();
	}
	
	@Override
	public void onPause() {
		if (actionMode != null) {
			actionMode.finish();
		}
		fab.setEnabled(true);
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
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_notes_to_notesSearchFragment, bundle);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onClick(int position) {
		if (actionModeOn) {
			enableActionMode(position);
		} else if (notes.get(position).getType().equals(FileType.FILE_TYPE_FOLDER)) {
			FileItem fileDesc = notes.get(position);
			Bundle bundle = new Bundle();
			bundle.putString("FilePath", fileDesc.getPath());
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_notes_to_fileFragment, bundle);
		}
	}
	
	@Override
	public void onLongClick(int position) {
		enableActionMode(position);
	}
	
	@Override
	public void onOptionsClick(View view, final int position) {
		PopupMenu popup = new PopupMenu(getContext(), view);
		if (starredIndex(position) != -1) {
			popup.inflate(R.menu.notes_item_menu_unstar);
		} else {
			popup.inflate(R.menu.notes_item_menu_star);
		}
		
		popup.getMenu().removeItem(R.id.notes_item_share);
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.notes_item_rename:
						
						AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
						alertDialog.setMessage("New Name");
						
						final FileItem fileItem = notes.get(position);
						
						final EditText input = new EditText(getContext());
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.MATCH_PARENT);
						input.setLayoutParams(lp);
						
						if (fileItem.getType() == FileType.FILE_TYPE_FOLDER) {
							input.setText(fileItem.getName());
						}
						
						alertDialog.setView(input);
						
						alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								String newName = input.getText().toString().trim();
								File oldFile = new File(fileItem.getPath());
								File newFile = new File(noteFolder, newName);
								if (newName.equals(fileItem.getName()) || newName.equals("")) {
									Log.d(TAG, "onClick: filename not changed");
								} else if (newFile.exists()) {
									Toast.makeText(getContext(), "Folder with this name already exists", Toast.LENGTH_SHORT).show();
								} else if (newName.contains("/")) {
									Toast.makeText(getContext(), "Folder name is not valid", Toast.LENGTH_SHORT).show();
								} else {
									if (oldFile.renameTo(newFile)) {
										Toast.makeText(getContext(), "Folder renamed successfully", Toast.LENGTH_SHORT).show();
										notes.get(position).setName(newName);
										notes.get(position).setPath(newFile.getPath());
										
										int starredIndex = starredIndex(position);
										if (starredIndex != -1) {
											SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
											SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
											
											starred.get(starredIndex).setName(newName);
											
											Gson gson = new Gson();
											starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
											starredPreferenceEditor.apply();
										}
										
										for (FileItem starItem : starred) {
											if (starItem.getPath().contains(oldFile.getPath())) {
												starItem.setPath(starItem.getPath().replaceFirst(oldFile.getPath(), newFile.getPath()));
											}
										}
										for (FileItem linkItem : links) {
											if (linkItem.getPath().contains(oldFile.getPath())) {
												linkItem.setPath(linkItem.getPath().replaceFirst(oldFile.getPath(), newFile.getPath()));
											}
										}
										
										SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
										SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
										
										Gson gson = new Gson();
										linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
										linkPreferenceEditor.apply();
										
										SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
										SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
										
										starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
										starredPreferenceEditor.apply();
										
										mNotesAdapter.notifyItemChanged(position);
										sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
									} else {
										Toast.makeText(getContext(), "Folder could not be renamed", Toast.LENGTH_SHORT).show();
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
					
					case R.id.notes_item_star:
						
						int starredIndex = starredIndex(position);
						if (starredIndex == -1) {
							SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
							SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
							
							if (!starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
								starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", true);
								starred = new ArrayList<>();
							}
							notes.get(position).setStarred(true);
							starred.add(notes.get(position));
							Gson gson = new Gson();
							starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
							starredPreferenceEditor.apply();
							mNotesAdapter.notifyItemChanged(position);
						} else {
							Toast.makeText(activity, "You are some sort of wizard aren't you", Toast.LENGTH_SHORT).show();
						}
						
						return true;
					
					case R.id.notes_item_unstar:
						
						int unstarredIndex = starredIndex(position);
						if (unstarredIndex != -1) {
							SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
							SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
							
							starred.remove(unstarredIndex);
							
							notes.get(position).setStarred(false);
							if (starred.size() == 0) {
								starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
							}
							Gson gson = new Gson();
							starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
							starredPreferenceEditor.apply();
							mNotesAdapter.notifyItemChanged(position);
						} else {
							Toast.makeText(activity, "You are some sort of wizard aren't you", Toast.LENGTH_SHORT).show();
						}
						
						return true;
					
					case R.id.notes_item_delete:
						
						final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setTitle("Delete Folder");
						builder.setMessage("Are you sure you want to delete the folder?");
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								File file = new File(notes.get(position).getPath());
								deleteRecursive(file);
								
								if (notes.get(position).getType() == FileType.FILE_TYPE_FOLDER) {
									ArrayList<FileItem> linksToBeRemoved = new ArrayList<>();
									for (FileItem linkItem : links) {
										if (linkItem.getPath().contains(file.getPath())) {
											linksToBeRemoved.add(linkItem);
										}
									}
									links.removeAll(linksToBeRemoved);
									
									SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
									SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
									
									if (links.size() == 0) {
										linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
									}
									Gson gson = new Gson();
									linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
									linkPreferenceEditor.apply();
									
									ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
									for (FileItem starItem : starred) {
										if (starItem.getPath().contains(file.getPath()) && !starItem.getPath().equals(file.getPath())) {
											starredToBeRemoved.add(starItem);
										}
									}
									starred.removeAll(starredToBeRemoved);
									
									SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
									SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
									
									if (starred.size() == 0) {
										starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
									}
									starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
									starredPreferenceEditor.apply();
								}
								
								int starPosition = starredIndex(position);
								if (starPosition != -1) {
									
									starred.remove(starPosition);
									
									SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
									SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
									
									if (starred.size() == 0) {
										starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
									}
									Gson gson = new Gson();
									starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
									starredPreferenceEditor.apply();
								}
								activity.mBottomAppBar.performShow();
								mNotesAdapter.notifyItemRemoved(position);
								notes.remove(position);
								
								if (notes.isEmpty()) {
									mEmptyLayout.setVisibility(View.VISIBLE);
								}
							}
						});
						builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							
							}
						});
						builder.show();
						return true;
					
					default:
						return false;
				}
			}
		});
		popup.show();
	}
	
	private void populateDataAndSetAdapter() {
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		
		if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = starredPreference.getString("STARRED_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			starred = gson.fromJson(json, type);
			
			if (starred == null) starred = new ArrayList<>();
		}
		
		ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
		for (FileItem starItem : starred) {
			File starFile = new File(starItem.getPath());
			if (starItem.getType() == FileType.FILE_TYPE_LINK) {
				if (starFile.getParentFile() == null || !starFile.getParentFile().exists()) {
					starredToBeRemoved.add(starItem);
				}
			} else {
				if (!starFile.exists()) {
					starredToBeRemoved.add(starItem);
				}
			}
		}
		starred.removeAll(starredToBeRemoved);
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			links = gson.fromJson(json, type);
			
			if (links == null) links = new ArrayList<>();
		}
		
		ArrayList<FileItem> linkToBeRemoved = new ArrayList<>();
		for (FileItem linkItem : links) {
			File linkParent = new File(linkItem.getPath()).getParentFile();
			assert linkParent != null;
			if (!linkParent.exists()) {
				linkToBeRemoved.add(linkItem);
			}
		}
		links.removeAll(linkToBeRemoved);
		
		File[] files = noteFolder.listFiles();
		
		if (files != null && files.length > 0) {
			notes = new ArrayList<>();
			for (File f : files) {
				FileItem item = new FileItem(f.getPath());
				if (isStarred(item)) {
					item.setStarred(true);
				}
				notes.add(item);
			}
		}
		
		if (notes.isEmpty()) {
			mEmptyLayout.setVisibility(View.VISIBLE);
		} else {
			mEmptyLayout.setVisibility(View.INVISIBLE);
		}
		
		mNotesAdapter = new NotesAdapter(requireActivity(), notes, this, true);
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
		recyclerView.setAdapter(mNotesAdapter);
	}
	
	private int starredIndex(int position) {
		int index = -1;
		if (starred != null) {
			for (int i = 0; i < starred.size(); i++) {
				FileItem starredItem = starred.get(i);
				if (starredItem.getPath().equals(notes.get(position).getPath())) {
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	private boolean isStarred(FileItem item) {
		if (starred != null) {
			for (int i = 0; i < starred.size(); i++) {
				FileItem starredItem = starred.get(i);
				if (starredItem.getPath().equals(item.getPath())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isExternalStorageReadableWritable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	private boolean writeReadPermission() {
		if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
			return false;
		} else {
			return true;
		}
	}
	
	private void addFolder() {
		if (isExternalStorageReadableWritable()) {
			if (writeReadPermission()) {
				
				File file;
				
				int count = 0;
				do {
					String newFolder = "New Folder";
					if (count > 0) {
						newFolder += " " + count;
					}
					++count;
					file = new File(noteFolder, newFolder);
				} while (file.exists());
				
				
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
				alertDialog.setMessage("Name of the folder");
				final EditText input = new EditText(getContext());
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.MATCH_PARENT);
				input.setLayoutParams(lp);
				input.setText(file.getName());
				alertDialog.setView(input);
				
				alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String newName = input.getText().toString().trim();
						File newFolder = new File(noteFolder, newName);
						if (newName.isEmpty()) {
							Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
						} else if (newFolder.exists()) {
							Toast.makeText(getContext(), "Folder with this name already exists", Toast.LENGTH_SHORT).show();
						} else if (newName.contains("/")) {
							Toast.makeText(getContext(), "Folder name is not valid", Toast.LENGTH_SHORT).show();
						} else {
							if (newFolder.mkdirs()) {
								notes.add(new FileItem(newFolder.getPath()));
								
								if (mEmptyLayout.getVisibility() == View.VISIBLE) {
									mEmptyLayout.setVisibility(View.GONE);
								}
								
								mNotesAdapter.notifyItemInserted(notes.size());
								
								sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
							} else {
								Toast.makeText(activity, "Cannot create new folder", Toast.LENGTH_SHORT).show();
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
			}
		} else {
			Toast.makeText(activity, "Cannot create new folder", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			File[] files = fileOrDirectory.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					deleteRecursive(file);
				}
			}
		}
		
		Log.d(TAG, "deleteRecursive: " + fileOrDirectory.delete());
	}
	
	private void enableActionMode(int position) {
		if (actionMode == null) {
			
			actionMode = requireActivity().startActionMode(new android.view.ActionMode.Callback2() {
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					mode.getMenuInflater().inflate(R.menu.menu_notes_action_mode, menu);
					menu.removeItem(R.id.notes_action_unstar);
					menu.removeItem(R.id.notes_action_share);
					actionModeOn = true;
					fab.setEnabled(false);
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
					fab.setEnabled(true);
					activity.mBottomAppBar.performShow();
					mLinearLayout.setVisibility(View.VISIBLE);
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
	
	private void deleteRows() {
		final ArrayList<Integer> selectedItemPositions = mNotesAdapter.getSelectedItems();
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Delete Folders");
		builder.setMessage("Are you sure you want to delete " + selectedItemPositions.size() + " folders?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
					File file = new File(notes.get(selectedItemPositions.get(i)).getPath());
					deleteRecursive(file);
					
					if (notes.get(selectedItemPositions.get(i)).getType() == FileType.FILE_TYPE_FOLDER) {
						ArrayList<FileItem> linksToBeRemoved = new ArrayList<>();
						for (FileItem linkItem : links) {
							if (linkItem.getPath().contains(file.getPath())) {
								linksToBeRemoved.add(linkItem);
							}
						}
						links.removeAll(linksToBeRemoved);
						
						SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
						SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
						
						if (links.size() == 0) {
							linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
						}
						Gson gson = new Gson();
						linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
						linkPreferenceEditor.apply();
						
						ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
						for (FileItem starItem : starred) {
							if (starItem.getPath().contains(file.getPath()) && !starItem.getPath().equals(file.getPath())) {
								starredToBeRemoved.add(starItem);
							}
						}
						starred.removeAll(starredToBeRemoved);
						
						SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
						SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
						
						if (starred.size() == 0) {
							starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
						}
						starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
						starredPreferenceEditor.apply();
					}
					
					int starPosition = starredIndex(selectedItemPositions.get(i));
					if (starPosition != -1) {
						starred.remove(starPosition);
						SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
						SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
						
						if (starred.size() == 0) {
							starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
						}
						Gson gson = new Gson();
						starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
						starredPreferenceEditor.apply();
					}
					
					mNotesAdapter.notifyItemRemoved(selectedItemPositions.get(i));
					notes.remove(selectedItemPositions.get(i).intValue());
				}
				
				if (notes.isEmpty()) {
					mEmptyLayout.setVisibility(View.VISIBLE);
				}
				
				activity.mBottomAppBar.performShow();
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
	
	private void sort(String text, boolean ascending) {
		switch (text) {
			case SORT_BY_SIZE:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o1.getSize(), o2.getSize());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o2.getSize(), o1.getSize());
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