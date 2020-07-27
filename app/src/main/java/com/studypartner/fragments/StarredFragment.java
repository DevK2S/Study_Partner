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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.BuildConfig;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;
import com.studypartner.utils.FileUtils;

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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class StarredFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "StarredFragment";
	
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
	private LinearLayout mLinearLayout;
	private TextView sortText;
	private ImageButton sortOrderButton, sortByButton;
	
	private NotesAdapter mStarredAdapter;
	
	private MainActivity activity;
	
	private ActionMode actionMode;
	private boolean actionModeOn = false;
	
	private ArrayList<FileItem> starred = new ArrayList<>();
	private ArrayList<FileItem> links = new ArrayList<>();
	
	public StarredFragment() {}
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		View rootView = inflater.inflate(R.layout.fragment_starred, container, false);
		
		activity = (MainActivity) requireActivity();
		activity.mBottomAppBar.bringToFront();
		activity.fab.bringToFront();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				fab.setOnClickListener(null);
				activity.mNavController.navigate(R.id.action_nav_starred_to_nav_home);
			}
		});
		
		fab = activity.fab;
		fab.show();
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "onClick: fab onclick called");
				activity.mNavController.navigate(R.id.action_nav_starred_to_nav_notes);
			}
		});
		
		mEmptyLayout = rootView.findViewById(R.id.starredEmptyLayout);
		recyclerView = rootView.findViewById(R.id.starredRecyclerView);
		mLinearLayout = rootView.findViewById(R.id.starredLinearLayout);
		sortText = rootView.findViewById(R.id.starredSortText);
		sortOrderButton = rootView.findViewById(R.id.starredSortOrder);
		sortByButton = rootView.findViewById(R.id.starredSortButton);
		
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
			if (fileDesc.getType() == FileType.FILE_TYPE_FOLDER) {
				Bundle bundle = new Bundle();
				bundle.putString("FilePath", fileDesc.getPath());
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_fileFragment, bundle);
			} else if (fileDesc.getType() == FileType.FILE_TYPE_VIDEO || fileDesc.getType() == FileType.FILE_TYPE_IMAGE || fileDesc.getType() == FileType.FILE_TYPE_AUDIO) {
				Bundle bundle = new Bundle();
				bundle.putString("Media", fileDesc.getPath());
				bundle.putBoolean("InStarred", true);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_mediaActivity, bundle);
			} else if (fileDesc.getType() == FileType.FILE_TYPE_LINK) {
				FileUtils.openLink(requireContext(),fileDesc);
			} else {
				FileUtils.openFile(requireContext(), fileDesc);
			}
		}

		SharedPreferences sortPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);

		if (sortPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sortPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sortPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
		}
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {}.getType();
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
		
		sortText.setText(sortBy);

		if (sortOrder.equals(ASCENDING_ORDER)) {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
		} else {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
		}
		
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
				FileItem[] files = new FileItem[starred.size()];
				files = starred.toArray(files);
				bundle.putParcelableArray("NotesArray", files);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_notesSearchFragment, bundle);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(int position) {
		if (actionModeOn) {
			
			enableActionMode(position);
			
		} else if (starred.get(position).getType().equals(FileType.FILE_TYPE_FOLDER)) {
			
			FileItem fileDesc = starred.get(position);
			Bundle bundle = new Bundle();
			bundle.putString("FilePath", fileDesc.getPath());
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_fileFragment, bundle);
			
		} else if (starred.get(position).getType().equals(FileType.FILE_TYPE_VIDEO) || starred.get(position).getType().equals(FileType.FILE_TYPE_AUDIO) || starred.get(position).getType() == FileType.FILE_TYPE_IMAGE) {
			
			Bundle bundle = new Bundle();
			bundle.putString("Media", starred.get(position).getPath());
			bundle.putBoolean("InStarred", true);
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_mediaActivity, bundle);
			
		} else if (starred.get(position).getType() == FileType.FILE_TYPE_LINK) {
			
			FileUtils.openLink(requireContext(),starred.get(position));
			
		} else {
			
			FileUtils.openFile(requireContext(), starred.get(position));
			
		}
	}

	@Override
	public void onLongClick(int position) {
		enableActionMode(position);
	}

	@Override
	public void onOptionsClick(View view, final int position) {
		PopupMenu popup = new PopupMenu(getContext(), view);
		popup.inflate(R.menu.notes_item_menu_unstar);
		
		if (starred.get(position).getType() == FileType.FILE_TYPE_FOLDER || starred.get(position).getType() == FileType.FILE_TYPE_LINK) {
			popup.getMenu().removeItem(R.id.notes_item_share);
		}
		
		if (starred.get(position).getType() == FileType.FILE_TYPE_LINK) {
			popup.getMenu().getItem(0).setTitle("Edit Link");
		}
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.notes_item_rename:

						AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
						alertDialog.setMessage("Enter a new name");

						final FileItem fileItem = starred.get(position);

						final EditText input = new EditText(getContext());
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.MATCH_PARENT);
						input.setLayoutParams(lp);
						
						String extension = "";
						if (fileItem.getType() == FileType.FILE_TYPE_FOLDER || fileItem.getType() == FileType.FILE_TYPE_LINK) {
							input.setText(fileItem.getName());
						} else {
							String name = fileItem.getName();
							if (name.indexOf(".") > 0) {
								extension = name.substring(name.lastIndexOf("."));
								name = name.substring(0, name.lastIndexOf("."));
							}
							input.setText(name);
						}
						
						alertDialog.setView(input);
						
						final String finalExtension = extension;

						alertDialog.setView(input);

						alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								String newName = input.getText().toString().trim();
								File oldFile = new File(fileItem.getPath());
								File newFile = new File(oldFile.getParent(), newName + finalExtension);
								if (fileItem.getType() == FileType.FILE_TYPE_LINK) {
									if (newName.equals(fileItem.getName()) || newName.equals("")) {
										Log.d(TAG, "onClick: link not changed");
									} else if (!FileUtils.isValidUrl(newName)) {
										Toast.makeText(getContext(), "Link is not valid", Toast.LENGTH_SHORT).show();
									} else {
										
										int linkIndex = linkIndex(position);
										if (linkIndex != -1) {
											links.get(linkIndex).setName(newName);
											starred.get(position).setName(newName);
											
											SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
											SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
											
											Gson gson = new Gson();
											linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
											linkPreferenceEditor.apply();
											
											SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
											SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
											starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
											starredPreferenceEditor.apply();
											
											mStarredAdapter.notifyItemChanged(position);
											sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
										}
										
									}
								} else {
									if (newName.equals(fileItem.getName()) || newName.equals("")) {
										Log.d(TAG, "onClick: filename not changed");
									} else if (newFile.exists()) {
										Toast.makeText(getContext(), "File with this name already exists", Toast.LENGTH_SHORT).show();
									} else if (newName.contains(".") || newName.contains("/")) {
										Toast.makeText(getContext(), "File name is not valid", Toast.LENGTH_SHORT).show();
									} else {
										if (oldFile.renameTo(newFile)) {
											Toast.makeText(getContext(), "File renamed successfully", Toast.LENGTH_SHORT).show();
											starred.get(position).setName(newName);
											starred.get(position).setPath(newFile.getPath());
											Gson gson = new Gson();
											
											if (fileItem.getType() == FileType.FILE_TYPE_FOLDER) {
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
												
												linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
												linkPreferenceEditor.apply();
											}
											
											SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
											SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
											
											starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
											starredPreferenceEditor.apply();
											
											mStarredAdapter.notifyItemChanged(position);
											sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
										} else {
											Toast.makeText(getContext(), "File could not be renamed", Toast.LENGTH_SHORT).show();
										}
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

					case R.id.notes_item_unstar:

						SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
						SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
						
						mStarredAdapter.notifyItemRemoved(position);
						starred.remove(position);
						activity.mBottomAppBar.performShow();
						if (starred.size() == 0) {
							starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
							mEmptyLayout.setVisibility(View.VISIBLE);
						}
						Gson gson = new Gson();
						starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
						starredPreferenceEditor.apply();
						
						return true;

					case R.id.notes_item_delete:

//						final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//						builder.setTitle("Delete File");
//						builder.setMessage("Are you sure you want to delete the file?");
//						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								File file = new File(starred.get(position).getPath());
//								starred.remove(position);
//								deleteRecursive(file);
//								SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
//								SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
//
//								if (starred.size() == 0) {
//									starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
//								}
//								Gson gson = new Gson();
//								starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
//								starredPreferenceEditor.apply();
//								activity.mBottomAppBar.performShow();
//
//								mStarredAdapter.notifyDataSetChanged();
//							}
//						});
//						builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//
//							}
//						});
//						builder.show();
//						return true;
					
						final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setTitle("Delete File");
						builder.setMessage("Are you sure you want to delete the file?");
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (starred.get(position).getType() != FileType.FILE_TYPE_LINK) {
									File file = new File(starred.get(position).getPath());
									deleteRecursive(file);
									
									if (starred.get(position).getType() == FileType.FILE_TYPE_FOLDER) {
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
										starredToBeRemoved.add(starred.get(position));
										starred.removeAll(starredToBeRemoved);
										
										SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
										SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
										
										if (starred.size() == 0) {
											starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
											mEmptyLayout.setVisibility(View.VISIBLE);
										}
										starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
										starredPreferenceEditor.apply();
									}
									
								} else {
									int linkPosition = linkIndex(position);
									if (linkPosition != -1) {
										links.remove(linkPosition);
										SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
										SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
										
										if (links.size() == 0) {
											linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
										}
										Gson gson = new Gson();
										linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
										linkPreferenceEditor.apply();
									}
								}
								mStarredAdapter.notifyDataSetChanged();
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
						if (starred.get(position).getType() != FileType.FILE_TYPE_FOLDER) {
							Intent intentShareFile = new Intent(Intent.ACTION_SEND);
							File shareFile = new File(starred.get(position).getPath());
							ArrayList<FileItem> fileItems = new ArrayList<>();
							fileItems.add(starred.get(position));
							if (shareFile.exists()) {
								intentShareFile.setType(FileUtils.getFileType(fileItems));
								intentShareFile.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", new File(starred.get(position).getPath())));
								intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
								intentShareFile.putExtra(Intent.EXTRA_TEXT, "Shared using Study Partner application");
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

	private void populateDataAndSetAdapter() {
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		
		if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = starredPreference.getString("STARRED_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {}.getType();
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
			Type type = new TypeToken<List<FileItem>>() {}.getType();
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
		
		if (starred.isEmpty()) {
			mEmptyLayout.setVisibility(View.VISIBLE);
		}

		mStarredAdapter = new NotesAdapter(requireActivity(), starred, this, true);

		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));

		recyclerView.setAdapter(mStarredAdapter);
	}
	
	private int linkIndex(int position) {
		int index = -1;
		if (links != null) {
			for (int i = 0; i < links.size(); i++) {
				FileItem linkItem = links.get(i);
				if (linkItem.getPath().equals(starred.get(position).getPath())) {
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
				deleteRecursive(child);
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
					menu.removeItem(R.id.notes_action_delete);
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
						case R.id.notes_action_unstar:
							unstarRows();
							mode.finish();
							return true;
						case R.id.notes_action_share:
							shareRows();
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
					mStarredAdapter.clearSelections();
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
		mStarredAdapter.toggleSelection(position);
		int count = mStarredAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
			actionMode = null;
		} else {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
	}
	
	private void selectAll() {
		mStarredAdapter.selectAll();
		int count = mStarredAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
		} else if (actionMode != null) {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
		
		actionMode = null;
	}
	
	private void unstarRows() {
		final ArrayList<Integer> selectedItemPositions = mStarredAdapter.getSelectedItems();
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Unstar Files");
		builder.setMessage("Are you sure you want to unstar " + selectedItemPositions.size() + " files?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
					starred.remove(selectedItemPositions.get(i).intValue());
					mStarredAdapter.notifyItemRemoved(selectedItemPositions.get(i));
				}
				
				SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
				SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
				
				if (starred.size() == 0) {
					starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
					mEmptyLayout.setVisibility(View.VISIBLE);
				}
				Gson gson = new Gson();
				starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
				starredPreferenceEditor.apply();
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
	
	private void shareRows() {
		ArrayList<Integer> selectedItemPositions = mStarredAdapter.getSelectedItems();
		ArrayList<Integer> positionsToBeRemoved = new ArrayList<>();
		for (int i = selectedItemPositions.size() - 1; i >= 0 ; i--) {
			if (FileUtils.getFileType(new File(starred.get(selectedItemPositions.get(i)).getPath())) == FileType.FILE_TYPE_FOLDER) {
				positionsToBeRemoved.add(i);
			} else if (starred.get(selectedItemPositions.get(i)).getType() == FileType.FILE_TYPE_LINK) {
				positionsToBeRemoved.add(i);
			}
		}
		
		selectedItemPositions.removeAll(positionsToBeRemoved);
		
		ArrayList<FileItem> fileItems = new ArrayList<>();
		ArrayList<Uri> fileItemsUri = new ArrayList<>();
		
		for (int i = selectedItemPositions.size() - 1; i >= 0 ; i--) {
			fileItems.add(starred.get(selectedItemPositions.get(i)));
			fileItemsUri.add(FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", new File(starred.get(selectedItemPositions.get(i)).getPath())));
		}
		
		Intent intentShareFile = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intentShareFile.setType(FileUtils.getFileType(fileItems));
		intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		intentShareFile.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileItemsUri);
		intentShareFile.putExtra(Intent.EXTRA_TEXT, "Shared using Study Partner application");
		startActivity(Intent.createChooser(intentShareFile, "Share File"));
	}
	
	private void sort (String text, boolean ascending) {
		switch (text) {
			case SORT_BY_SIZE:
				
				if (ascending) {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o1.getSize(), o2.getSize());
						}
					});
				} else {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o2.getSize(), o1.getSize());
						}
					});
				}
				
				break;
			case SORT_BY_CREATION_TIME:
				
				if (ascending) {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateCreated().compareTo(o2.getDateCreated());
						}
					});
				} else {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateCreated().compareTo(o1.getDateCreated());
						}
					});
				}
				
				break;
			case SORT_BY_MODIFIED_TIME:
				
				if (ascending) {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateModified().compareTo(o2.getDateModified());
						}
					});
				} else {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateModified().compareTo(o1.getDateModified());
						}
					});
				}
				
				break;
			case SORT_BY_NAME:
				
				if (ascending) {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
				} else {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getName().compareTo(o1.getName());
						}
					});
				}
				break;
		}
		
		mStarredAdapter.notifyDataSetChanged();
	}
}