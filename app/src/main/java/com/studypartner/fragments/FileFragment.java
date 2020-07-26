package com.studypartner.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static android.content.Context.MODE_PRIVATE;

public class FileFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "FileFragment";
	
	private final String SORT_BY_NAME = "By Name";
	private final String SORT_BY_SIZE = "By Size";
	private final String SORT_BY_CREATION_TIME = "By Creation Time";
	private final String SORT_BY_MODIFIED_TIME = "By Modification Time";
	
	private final String ASCENDING_ORDER = "Ascending Order";
	private final String DESCENDING_ORDER = "Descending Order";
	
	private final int RECORD_PERMISSION_REQUEST_CODE = 10;
	private final int CAMERA_PERMISSION_REQUEST_CODE = 30;
	
	private final int RECORD_REQUEST_CODE = 11;
	private final int IMAGE_REQUEST_CODE = 22;
	private final int CAMERA_IMAGE_REQUEST_CODE = 33;
	private final int DOC_REQUEST_CODE = 44;
	private final int VIDEO_REQUEST_CODE = 55;
	private final int AUDIO_REQUEST_CODE = 66;
	
	private File audioFile;
	
	private String sortBy;
	private String sortOrder;
	
	private FloatingActionButton fab;
	private RecyclerView recyclerView;
	private File noteFolder;
	private LinearLayout mLinearLayout;
	private TextView sortText;
	private ImageButton sortOrderButton, sortByButton;
	
	private NotesAdapter mNotesAdapter;
	
	private ActionMode actionMode;
	private boolean actionModeOn = false;
	
	private ArrayList<FileItem> notes = new ArrayList<>();
	private ArrayList<FileItem> starred = new ArrayList<>();
	private ArrayList<FileItem> links = new ArrayList<>();
	
	public FileFragment() {
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		
		if (requestCode == RECORD_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				recordAudio();
			}
		} else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				openCameraForImage();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
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
				final LinearLayout addLink = bottomSheet.findViewById(R.id.addLink);
				final LinearLayout addAudio = bottomSheet.findViewById(R.id.addAudio);
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
						getDocument();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addImage != null;
				addImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						getImage();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addVideo != null;
				addVideo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						getVideo();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addCamera != null;
				addCamera.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
						} else {
							
							openCameraForImage();
							
						}
						
						bottomSheet.dismiss();
					}
				});
				
				assert addNote != null;
				addNote.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						addNote();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addLink != null;
				addLink.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						addLink();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addAudio != null;
				addAudio.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						getAudio();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addVoice != null;
				addVoice.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_PERMISSION_REQUEST_CODE);
						} else {
							
							recordAudio();
							
						}
						
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
		recyclerView.setItemViewCacheSize(20);
		recyclerView.setDrawingCacheEnabled(true);
		
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
		
		toolbar.setTitle(getTitle());
		
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
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_self, bundle);
			} else if (fileDesc.getType() == FileType.FILE_TYPE_VIDEO || fileDesc.getType() == FileType.FILE_TYPE_IMAGE || fileDesc.getType() == FileType.FILE_TYPE_AUDIO) {
				Bundle bundle = new Bundle();
				bundle.putString("Media", fileDesc.getPath());
				bundle.putBoolean("InStarred", false);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_to_mediaActivity, bundle);
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
			Type type = new TypeToken<List<FileItem>>() {}.getType();
			starred = gson.fromJson(json, type);
			
			if (starred == null) starred = new ArrayList<>();
		}
		
		ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
		for (FileItem starItem : starred) {
			File starFile = new File(starItem.getPath());
			if (!starFile.exists()) {
				starredToBeRemoved.add(starItem);
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
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
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
	
	@Override
	public void onClick(int position) {
		if (actionModeOn) {
			enableActionMode(position);
		} else if (notes.get(position).getType().equals(FileType.FILE_TYPE_FOLDER)) {
			FileItem fileDesc = notes.get(position);
			Bundle bundle = new Bundle();
			bundle.putString("FilePath", fileDesc.getPath());
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_self, bundle);
		} else if (notes.get(position).getType().equals(FileType.FILE_TYPE_VIDEO) || notes.get(position).getType().equals(FileType.FILE_TYPE_AUDIO) || notes.get(position).getType() == FileType.FILE_TYPE_IMAGE) {
			Bundle bundle = new Bundle();
			bundle.putString("Media", notes.get(position).getPath());
			bundle.putBoolean("InStarred", false);
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_to_mediaActivity, bundle);
		} else if (notes.get(position).getType() == FileType.FILE_TYPE_LINK) {
			FileUtils.openLink(requireContext(),notes.get(position));
		} else {
			FileUtils.openFile(requireContext(), notes.get(position));
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
		
		if (notes.get(position).getType() == FileType.FILE_TYPE_FOLDER || notes.get(position).getType() == FileType.FILE_TYPE_LINK) {
			popup.getMenu().removeItem(R.id.notes_item_share);
		}
		
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
						lp.setMarginStart((int) requireActivity().getResources().getDimension(R.dimen.mediumMargin));
						lp.setMarginEnd((int) requireActivity().getResources().getDimension(R.dimen.mediumMargin));
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
						alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								String newName = input.getText().toString().trim();
								File oldFile = new File(fileItem.getPath());
								File newFile = new File(noteFolder, newName + finalExtension);
								if (fileItem.getType() == FileType.FILE_TYPE_LINK) {
									if (newName.equals(fileItem.getName()) || newName.equals("")) {
										Log.d(TAG, "onClick: link not changed");
									} else if (!FileUtils.isValidUrl(newName)) {
										Toast.makeText(getContext(), "Link is not valid", Toast.LENGTH_SHORT).show();
									} else {
										
										int linkIndex = linkIndex(position);
										if (linkIndex != -1) {
											links.get(linkIndex).setName(newName);
											notes.get(position).setName(newName);
											
											SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
											SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
											
											Gson gson = new Gson();
											linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
											linkPreferenceEditor.apply();
											
											int starredIndex = starredIndex(position);
											if (starredIndex != -1) {
												SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
												SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
												
												starred.get(starredIndex).setName(newName);
												starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
												starredPreferenceEditor.apply();
											}
											
											mNotesAdapter.notifyItemChanged(position);
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
											notes.get(position).setName(newName);
											notes.get(position).setPath(newFile.getPath());
											
											int starredIndex = starredIndex(position);
											if (starredIndex == -1) {
												SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
												SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
												
												starred.get(starredIndex).setName(newName);
												Gson gson = new Gson();
												starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
												starredPreferenceEditor.apply();
											}
											
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
												
												Gson gson = new Gson();
												linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
												linkPreferenceEditor.apply();
												
												SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
												SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
												
												starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
												starredPreferenceEditor.apply();
											}
											
											mNotesAdapter.notifyItemChanged(position);
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
					
					case R.id.notes_item_star:
						
						final int starredIndex = starredIndex(position);
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
							Toast.makeText(getContext(), "You are some sort of wizard aren't you", Toast.LENGTH_SHORT).show();
						}
						
						return true;
					
					case R.id.notes_item_unstar:
						
						int unstarredIndex = starredIndex(position);
						if (unstarredIndex != -1) {
							SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
							SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
							
							for (int i = 0; i < starred.size(); i++) {
								FileItem starItem = starred.get(i);
								if (starItem.isStarred() && starItem.getPath().equals(notes.get(position).getPath())) {
									starred.remove(i);
									break;
								}
							}
							notes.get(position).setStarred(false);
							if (starred.size() == 0) {
								starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
							}
							Gson gson = new Gson();
							starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
							starredPreferenceEditor.apply();
							mNotesAdapter.notifyItemChanged(position);
						} else {
							Toast.makeText(getContext(), "You are some sort of wizard aren't you", Toast.LENGTH_SHORT).show();
						}
						
						return true;
					
					case R.id.notes_item_delete:
						
						final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setTitle("Delete File");
						builder.setMessage("Are you sure you want to delete the file?");
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (notes.get(position).getType() != FileType.FILE_TYPE_LINK) {
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
							ArrayList<FileItem> fileItems = new ArrayList<>();
							fileItems.add(notes.get(position));
							if (shareFile.exists()) {
								intentShareFile.setType(FileUtils.getFileType(fileItems));
								intentShareFile.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", new File(notes.get(position).getPath())));
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
			if (!starFile.exists()) {
				starredToBeRemoved.add(starItem);
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
		
		for (FileItem link : links) {
			File linkFile = new File(link.getPath());
			if (linkFile.getParent().equals(noteFolder.getPath())) {
				if (isStarred(link)) {
					link.setStarred(true);
				}
				notes.add(link);
			}
		}
		
		mNotesAdapter = new NotesAdapter(requireActivity(), notes, this, true);
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
		recyclerView.setAdapter(mNotesAdapter);
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
	
	private int linkIndex(int position) {
		int index = -1;
		if (links != null) {
			for (int i = 0; i < links.size(); i++) {
				FileItem linkItem = links.get(i);
				if (linkItem.getPath().equals(notes.get(position).getPath())) {
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
	
	private String getTitle() {
		
		String title = "Notes";
		
		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		
		if (firebaseUser != null && firebaseUser.getEmail() != null) {
			title = noteFolder.getPath().substring(noteFolder.getPath().indexOf(firebaseUser.getEmail()) + firebaseUser.getEmail().length() + 1);
		}
		
		return title.length() > 15 ? "..." + title.substring(title.length() - 12) : title;
	}
	
	private String fileName(FileType fileType) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
		Date date = new Date();
		
		String file;
		if (fileType == FileType.FILE_TYPE_IMAGE) {
			file = "IMG";
		} else if (fileType == FileType.FILE_TYPE_VIDEO) {
			file = "VID";
		} else if (fileType == FileType.FILE_TYPE_AUDIO) {
			file = "AUD";
		} else {
			file = "DOC";
		}
		
		file += "_" + dateFormat.format(date) + UUID.randomUUID().toString().substring(0, 5);
		
		return file;
	}
	
	private void enableActionMode(int position) {
		if (actionMode == null) {
			
			actionMode = requireActivity().startActionMode(new android.view.ActionMode.Callback2() {
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					mode.getMenuInflater().inflate(R.menu.menu_notes_action_mode, menu);
					menu.removeItem(R.id.notes_action_unstar);
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
	
	private void deleteRows() {
		final ArrayList<Integer> selectedItemPositions = mNotesAdapter.getSelectedItems();
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Delete Files");
		builder.setMessage("Are you sure you want to delete " + selectedItemPositions.size() + " files?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
					if (notes.get(selectedItemPositions.get(i)).getType() != FileType.FILE_TYPE_LINK) {
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
					} else {
						int linkPosition = linkIndex(selectedItemPositions.get(i));
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
		ArrayList<Integer> selectedItemPositions = mNotesAdapter.getSelectedItems();
		ArrayList<Integer> positionsToBeRemoved = new ArrayList<>();
		for (int i = selectedItemPositions.size() - 1; i >= 0 ; i--) {
			if (FileUtils.getFileType(new File(notes.get(selectedItemPositions.get(i)).getPath())) == FileType.FILE_TYPE_FOLDER) {
				positionsToBeRemoved.add(i);
			} else if (notes.get(selectedItemPositions.get(i)).getType() == FileType.FILE_TYPE_LINK) {
				positionsToBeRemoved.add(i);
			}
		}
		
		selectedItemPositions.removeAll(positionsToBeRemoved);
		
		ArrayList<FileItem> fileItems = new ArrayList<>();
		ArrayList<Uri> fileItemsUri = new ArrayList<>();
		
		for (int i = selectedItemPositions.size() - 1; i >= 0 ; i--) {
			fileItems.add(notes.get(selectedItemPositions.get(i)));
			fileItemsUri.add(FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", new File(notes.get(selectedItemPositions.get(i)).getPath())));
		}
		
		Intent intentShareFile = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intentShareFile.setType(FileUtils.getFileType(fileItems));
		intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		intentShareFile.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileItemsUri);
		intentShareFile.putExtra(Intent.EXTRA_TEXT, "Shared using Study Partner application");
		startActivity(Intent.createChooser(intentShareFile, "Share File"));
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == RECORD_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				notes.add(new FileItem(audioFile.getPath()));
				mNotesAdapter.notifyItemInserted(notes.size() - 1);
			} else if (resultCode != Activity.RESULT_CANCELED) {
				Toast.makeText(getContext(), "Audio could not be saved", Toast.LENGTH_SHORT).show();
			}
			
		} else if (requestCode == IMAGE_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				assert data != null;
				ArrayList<Uri> imagePaths = new ArrayList<>(Objects.requireNonNull(data.<Uri>getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)));
				for (Uri uri : imagePaths) {
					String filePath = FileUtils.getFilePath(requireContext(), uri);
					notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
					mNotesAdapter.notifyItemInserted(notes.size() - 1);
				}
			} else if (resultCode != Activity.RESULT_CANCELED) {
				Toast.makeText(getContext(), "Image(s) could not be saved", Toast.LENGTH_SHORT).show();
			}
			
		} else if (requestCode == CAMERA_IMAGE_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				notes.add(new FileItem(ImagePicker.Companion.getFilePath(data)));
				mNotesAdapter.notifyItemInserted(notes.size() - 1);
			} else if (resultCode == ImagePicker.RESULT_ERROR) {
				Toast.makeText(getContext(), "Image could not be saved", Toast.LENGTH_SHORT).show();
			}
			
		} else if (requestCode == DOC_REQUEST_CODE) {
			
//			if (resultCode == Activity.RESULT_OK) {
//				assert data != null;
//				ArrayList<Uri> docPaths = new ArrayList<>(Objects.requireNonNull(data.<Uri>getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS)));
//				for (Uri uri : docPaths) {
//					String filePath = FileUtils.getFilePath(requireContext(), uri);
//					notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
//					mNotesAdapter.notifyItemInserted(notes.size() - 1);
//				}
//			} else if (resultCode != Activity.RESULT_CANCELED) {
//				Toast.makeText(getContext(), "Document(s) could not be saved", Toast.LENGTH_SHORT).show();
//			}
			if (resultCode == Activity.RESULT_OK) {
				assert data != null;
				if (null != data.getClipData()) {
					Log.d(TAG, "onActivityResult: document count = " + data.getClipData().getItemCount());
					for (int i = 0; i < data.getClipData().getItemCount(); i++) {
						Uri uri = data.getClipData().getItemAt(i).getUri();
						String filePath = FileUtils.getFilePath(requireContext(), uri);
						notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
						mNotesAdapter.notifyItemInserted(notes.size() - 1);
					}
				} else {
					Uri uri = data.getData();
					String filePath = FileUtils.getFilePath(requireContext(), uri);
					notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
					mNotesAdapter.notifyItemInserted(notes.size() - 1);
				}
			}
			
		} else if (requestCode == VIDEO_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				assert data != null;
				ArrayList<Uri> videoPaths = new ArrayList<>(Objects.requireNonNull(data.<Uri>getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)));
				for (Uri uri : videoPaths) {
					String filePath = FileUtils.getFilePath(requireContext(), uri);
					notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
					mNotesAdapter.notifyItemInserted(notes.size() - 1);
				}
			} else if (resultCode != Activity.RESULT_CANCELED) {
				Toast.makeText(getContext(), "Video(s) could not be saved", Toast.LENGTH_SHORT).show();
			}
			
		} else if (requestCode == AUDIO_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				assert data != null;
				if (null != data.getClipData()) {
					Log.d(TAG, "onActivityResult: audio count = " + data.getClipData().getItemCount());
					for (int i = 0; i < data.getClipData().getItemCount(); i++) {
						Uri uri = data.getClipData().getItemAt(i).getUri();
						String filePath = FileUtils.getFilePath(requireContext(), uri);
						notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
						mNotesAdapter.notifyItemInserted(notes.size() - 1);
					}
				} else {
					Uri uri = data.getData();
					String filePath = FileUtils.getFilePath(requireContext(), uri);
					notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
					mNotesAdapter.notifyItemInserted(notes.size() - 1);
				}
			} else if (resultCode != Activity.RESULT_CANCELED) {
				Toast.makeText(getContext(), "Audio(s) could not be saved", Toast.LENGTH_SHORT).show();
			}
			
		}
	}
	
	private void addFolder() {
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
		
		if (file.mkdirs()) {
			notes.add(new FileItem(file.getPath()));
			mNotesAdapter.notifyItemInserted(notes.size());
		}
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
	}
	
	private void getDocument() {
		String[] mimeTypes =
				{"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
						"application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
						"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
						"text/plain",
						"application/pdf",
						"application/zip"};
		
		Intent getDocument = new Intent(Intent.ACTION_GET_CONTENT);
		getDocument.addCategory(Intent.CATEGORY_OPENABLE);
		getDocument.setType("*/*");
		getDocument.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
		getDocument.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		startActivityForResult(Intent.createChooser(getDocument,"ChooseFile"), DOC_REQUEST_CODE);

//		FilePickerBuilder.getInstance()
//				.setActivityTitle("Select Documents")
//				.pickFile(this,DOC_REQUEST_CODE);
	}
	
	private void getImage() {
		FilePickerBuilder.getInstance()
				.setActivityTitle("Select images")
				.enableImagePicker(true)
				.enableCameraSupport(false)
				.enableVideoPicker(false)
				.pickPhoto(this, IMAGE_REQUEST_CODE);
	}
	
	private void openCameraForImage() {
		ImagePicker.Builder builder = new ImagePicker.Builder(this);
		builder.crop();
		builder.cameraOnly();
		builder.compress(5 * 1024);
		builder.saveDir(noteFolder);
		builder.start(CAMERA_IMAGE_REQUEST_CODE);
	}
	
	private void getVideo() {
		FilePickerBuilder.getInstance()
				.setActivityTitle("Select videos")
				.enableImagePicker(false)
				.enableCameraSupport(false)
				.enableVideoPicker(true)
				.pickPhoto(this, VIDEO_REQUEST_CODE);
	}
	
	private void addNote() {
		
		final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.notes_add_note_layout, null);
		
		alertDialog.setView(dialogView);
		
		Button okButton = dialogView.findViewById(R.id.notesAddNoteOkButton);
		Button cancelButton = dialogView.findViewById(R.id.notesAddNoteCancelButton);
		final TextInputLayout nameTextInput = dialogView.findViewById(R.id.notesAddNoteNameTextInputLayout);
		nameTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				nameTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		final TextInputLayout contentTextInput = dialogView.findViewById(R.id.notesAddNoteContentTextInputLayout);
		contentTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				contentTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = nameTextInput.getEditText().getText().toString().trim();
				String content = contentTextInput.getEditText().getText().toString().trim();
				
				if (name.isEmpty()) {
					nameTextInput.setError("Name cannot be empty");
				} else if (name.contains(".")) {
					nameTextInput.setError("Name cannot contain \".\"");
				} else if (name.contains("/")) {
					nameTextInput.setError("Name cannot contain \"/\"");
				} else {
					
					File file;
					
					int count = 0;
					String newNote;
					do {
						newNote = name;
						if (count > 0) {
							newNote += " " + count;
						}
						newNote += ".txt";
						++count;
						file = new File(noteFolder, newNote);
					} while (file.exists());
					
					newNote = newNote.substring(0, newNote.length() - 4);
					
					if (!newNote.equals(name)) {
						Toast.makeText(requireContext(), name + " is already used. Setting name to " + newNote, Toast.LENGTH_SHORT).show();
					}
					
					try {
						if (!file.exists()) {
							Log.d(TAG, "onClick: creating file " + file.createNewFile());
						}
						
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(content.getBytes());
						fos.close();
						notes.add(new FileItem(file.getPath()));
						mNotesAdapter.notifyItemInserted(notes.size() - 1);
					} catch (Exception e) {
						Toast.makeText(requireContext(), "Could not create the note " + e.getMessage(), Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
					
					alertDialog.dismiss();
				}
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});
		
		alertDialog.show();
		
	}
	
	private void addLink () {
		
		final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.notes_add_link_layout, null);
		
		alertDialog.setView(dialogView);
		
		Button okButton = dialogView.findViewById(R.id.notesAddLinkOkButton);
		Button cancelButton = dialogView.findViewById(R.id.notesAddLinkCancelButton);
		final TextInputLayout linkTextInput = dialogView.findViewById(R.id.notesAddLinkTextInputLayout);
		linkTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				linkTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String link = linkTextInput.getEditText().getText().toString().trim();
				
				if (link.isEmpty()) {
					linkTextInput.setError("Link cannot be empty");
				} else if (!FileUtils.isValidUrl(link)) {
					linkTextInput.setError("Link is not valid");
				} else {
					
					FileItem item = new FileItem(new File(noteFolder, "Link" + UUID.randomUUID().toString().substring(0, 5)).getPath());
					item.setName(link);
					item.setType(FileType.FILE_TYPE_LINK);
					
					Log.d(TAG, item.toString());
					
					notes.add(item);
					mNotesAdapter.notifyItemInserted(notes.size() - 1);
					
					SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
					SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
					
					if (!linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
						linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", true);
						links = new ArrayList<>();
					}
					
					links.add(item);
					
					Gson gson = new Gson();
					linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
					linkPreferenceEditor.apply();
					
					alertDialog.dismiss();
				}
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});
		
		alertDialog.show();
		
	}
	
	private void getAudio() {
		Intent getAudio = new Intent("android.intent.action.MULTIPLE_PICK");
		getAudio.setType("audio/*");
		getAudio.setAction(Intent.ACTION_GET_CONTENT);
		getAudio.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		startActivityForResult(getAudio, AUDIO_REQUEST_CODE);
	}
	
	private void recordAudio() {
		audioFile = new File(noteFolder, fileName(FileType.FILE_TYPE_AUDIO) + ".wav");
		AndroidAudioRecorder.with(FileFragment.this)
				.setColor(getResources().getColor(R.color.colorPrimary, requireActivity().getTheme()))
				.setFilePath(audioFile.getPath())
				.setRequestCode(RECORD_REQUEST_CODE)
				.setKeepDisplayOn(false)
				.recordFromFragment();
	}
}